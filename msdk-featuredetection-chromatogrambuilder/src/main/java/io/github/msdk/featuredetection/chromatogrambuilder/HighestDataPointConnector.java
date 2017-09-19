/*
 * (C) Copyright 2015-2017 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1 as published by the Free
 * Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation.
 */

package io.github.msdk.featuredetection.chromatogrambuilder;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

import io.github.msdk.datamodel.Chromatogram;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.datamodel.SimpleChromatogram;
import io.github.msdk.util.DataPointSorter;
import io.github.msdk.util.DataPointSorter.SortingDirection;
import io.github.msdk.util.DataPointSorter.SortingProperty;
import io.github.msdk.util.tolerances.MzTolerance;

class HighestDataPointConnector {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final @Nonnull Double noiseLevel;
  private final double minimumTimeSpan, minimumHeight;

  private final Set<BuildingChromatogram> buildingChromatograms, connectedChromatograms;

  // Data structures
  private float rtBuffer[] = new float[10000];
  private double mzBuffer[] = new double[10000];
  private float intensityBuffer[] = new float[10000];

  HighestDataPointConnector(@Nonnull Double noiseLevel, double minimumTimeSpan,
      double minimumHeight) {

    this.noiseLevel = noiseLevel;
    this.minimumHeight = minimumHeight;
    this.minimumTimeSpan = minimumTimeSpan;

    // We use LinkedHashSet to maintain a reproducible ordering. If we use
    // plain HashSet, the resulting peak list row IDs will have different
    // order every time the method is invoked.
    buildingChromatograms = new LinkedHashSet<BuildingChromatogram>();
    connectedChromatograms = new LinkedHashSet<BuildingChromatogram>();

  }

  void addScan(RawDataFile dataFile, MsScan scan, MzTolerance mzTolerance) {

    // Load data points
    mzBuffer = scan.getMzValues();
    intensityBuffer = scan.getIntensityValues();
    int numOfDataPoints = scan.getNumberOfDataPoints();

    // Sort m/z peaks by descending intensity
    DataPointSorter.sortDataPoints(mzBuffer, intensityBuffer, numOfDataPoints,
        SortingProperty.INTENSITY, SortingDirection.DESCENDING);

    // A set of already connected chromatograms in each iteration
    connectedChromatograms.clear();

    for (int i = 0; i < numOfDataPoints; i++) {

      if (intensityBuffer[i] < noiseLevel)
        continue;

      // Search for best chromatogram, which has the highest _last_ data
      // point
      BuildingChromatogram bestChromatogram = null;

      for (BuildingChromatogram testChrom : buildingChromatograms) {

        Range<Double> toleranceRange = mzTolerance.getToleranceRange(testChrom.getLastMz());

        if (toleranceRange.contains(mzBuffer[i])) {
          if ((bestChromatogram == null)
              || (testChrom.getLastIntensity() > bestChromatogram.getLastIntensity())) {
            bestChromatogram = testChrom;
          }
        }

      }

      // If we found best chromatogram, check if it is already connected.
      // In such case, we may discard this mass and continue. If we
      // haven't found a chromatogram, we can create a new one.
      if (bestChromatogram != null) {
        if (connectedChromatograms.contains(bestChromatogram)) {
          continue;
        }
      } else {
        bestChromatogram = new BuildingChromatogram();
      }

      // Add this mzPeak to the chromatogram
      Float rt = scan.getRetentionTime();
      Preconditions.checkNotNull(rt);
      bestChromatogram.addDataPoint(rt, mzBuffer[i], intensityBuffer[i]);

      // Move the chromatogram to the set of connected chromatograms
      connectedChromatograms.add(bestChromatogram);

    }

    // Process those chromatograms which were not connected to any m/z peak
    for (BuildingChromatogram testChrom : buildingChromatograms) {

      // Skip those which were connected
      if (connectedChromatograms.contains(testChrom)) {
        continue;
      }

      // Check if we just finished a long-enough segment
      if (testChrom.getBuildingSegmentLength() >= minimumTimeSpan) {
        testChrom.commitBuildingSegment();

        // Move the chromatogram to the set of connected chromatograms
        connectedChromatograms.add(testChrom);
        continue;
      }

      // Check if we have any committed segments in the chromatogram
      if (testChrom.getNumberOfCommittedSegments() > 0) {
        testChrom.removeBuildingSegment();

        // Move the chromatogram to the set of connected chromatograms
        connectedChromatograms.add(testChrom);
        continue;
      }

    }

    // All remaining chromatograms in buildingChromatograms are discarded
    // and buildingChromatograms is replaced with connectedChromatograms
    buildingChromatograms.clear();
    buildingChromatograms.addAll(connectedChromatograms);

  }

  void finishChromatograms(@Nonnull RawDataFile inputFile,
      List<Chromatogram> finalList) {

    logger.debug("Finishing " + buildingChromatograms.size() + " chromatograms");

    // Iterate through current chromatograms and remove those which do not
    // contain any committed segment or long-enough building segment
    Iterator<BuildingChromatogram> chromIterator = buildingChromatograms.iterator();
    while (chromIterator.hasNext()) {

      BuildingChromatogram chromatogram = chromIterator.next();

      if (chromatogram.getBuildingSegmentLength() >= minimumTimeSpan) {
        chromatogram.commitBuildingSegment();
      } else {
        if (chromatogram.getNumberOfCommittedSegments() == 0) {
          chromIterator.remove();
          continue;
        } else {
          chromatogram.removeBuildingSegment();
        }
      }

      // Remove chromatograms below minimum height
      if (chromatogram.getHeight() < minimumHeight) {
        chromIterator.remove();
      }

    }

    // All remaining chromatograms are good, so we can add them to the table
    int chromId = 1;
    for (BuildingChromatogram buildingChromatogram : buildingChromatograms) {

      // Make a new MSDK Chromatogram
      SimpleChromatogram newChromatogram = new SimpleChromatogram();

      // Copy the data points from the BuildingChromatogram
      rtBuffer = buildingChromatogram.getRtValues(rtBuffer);
      mzBuffer = buildingChromatogram.getMzValues(mzBuffer);
      intensityBuffer = buildingChromatogram.getIntensityValues(intensityBuffer);
      int size = buildingChromatogram.size();
      newChromatogram.setDataPoints(rtBuffer, mzBuffer, intensityBuffer, size);

      // Update the final m/z value of the Chromatogram
      Double mz = buildingChromatogram.calculateMz();
      newChromatogram.setMz(mz);

      // Add the Chromatogram to the result list
      finalList.add(newChromatogram);

      // Increase the ID
      chromId++;

    }

  }

}
