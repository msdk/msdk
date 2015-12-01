/* 
 * (C) Copyright 2015 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
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

import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.chromatograms.ChromatogramDataPointList;
import io.github.msdk.datamodel.chromatograms.ChromatogramType;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;
import io.github.msdk.util.DataPointSorter;
import io.github.msdk.util.MZTolerance;
import io.github.msdk.util.DataPointSorter.SortingDirection;
import io.github.msdk.util.DataPointSorter.SortingProperty;

class HighestDataPointConnector {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final @Nonnull MsSpectrumDataPointList dataPoints;
    private final MZTolerance mzTolerance;
    private final double minimumTimeSpan, minimumHeight;

    private final Set<BuildingChromatogram> buildingChromatograms,
            connectedChromatograms;

    HighestDataPointConnector(double minimumTimeSpan, double minimumHeight,
            MZTolerance mzTolerance) {

        this.mzTolerance = mzTolerance;
        this.minimumHeight = minimumHeight;
        this.minimumTimeSpan = minimumTimeSpan;

        // Create the data structure
        dataPoints = MSDKObjectBuilder.getMsSpectrumDataPointList();

        // We use LinkedHashSet to maintain a reproducible ordering. If we use
        // plain HashSet, the resulting peak list row IDs will have different
        // order every time the method is invoked.
        buildingChromatograms = new LinkedHashSet<BuildingChromatogram>();
        connectedChromatograms = new LinkedHashSet<BuildingChromatogram>();

    }

    void addScan(RawDataFile dataFile, MsScan scan) {

        // Load scan data points
        scan.getDataPoints(dataPoints);
        final double mzBuffer[] = dataPoints.getMzBuffer();
        final float intensityBuffer[] = dataPoints.getIntensityBuffer();

        // Sort m/z peaks by descending intensity
        DataPointSorter.sortDataPoints(mzBuffer, intensityBuffer,
                dataPoints.getSize(), SortingProperty.INTENSITY,
                SortingDirection.DESCENDING);

        // A set of already connected chromatograms in each iteration
        connectedChromatograms.clear();

        for (int i = 0; i < dataPoints.getSize(); i++) {

            // Search for best chromatogram, which has the highest _last_ data
            // point
            BuildingChromatogram bestChromatogram = null;

            for (BuildingChromatogram testChrom : buildingChromatograms) {

                Range<Double> toleranceRange = mzTolerance
                        .getToleranceRange(testChrom.getLastMz());

                if (toleranceRange.contains(mzBuffer[i])) {
                    if ((bestChromatogram == null)
                            || (testChrom.getLastIntensity() > bestChromatogram
                                    .getLastIntensity())) {
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
            ChromatographyInfo rt = scan.getChromatographyInfo();
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
            @Nonnull DataPointStore dataPointStore,
            List<Chromatogram> finalList) {

        logger.debug(
                "Finishing " + buildingChromatograms.size() + " chromatograms");

        // Iterate through current chromatograms and remove those which do not
        // contain any committed segment or long-enough building segment
        Iterator<BuildingChromatogram> chromIterator = buildingChromatograms
                .iterator();
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
            Chromatogram newChromatogram = MSDKObjectBuilder.getChromatogram(
                    dataPointStore, chromId, ChromatogramType.XIC,
                    SeparationType.UNKNOWN);

            // Copy the data points from the BuildingChromatogram
            @Nonnull
            ChromatogramDataPointList dataPoints = buildingChromatogram
                    .getDataPoints();
            newChromatogram.setDataPoints(dataPoints);

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
