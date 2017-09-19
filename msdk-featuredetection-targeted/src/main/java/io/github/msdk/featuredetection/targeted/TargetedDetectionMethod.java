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

package io.github.msdk.featuredetection.targeted;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.Chromatogram;
import io.github.msdk.datamodel.IonAnnotation;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.datamodel.SimpleChromatogram;
import io.github.msdk.util.ChromatogramUtil;
import io.github.msdk.util.ChromatogramUtil.CalculationMethod;
import io.github.msdk.util.MsSpectrumUtil;
import io.github.msdk.util.RawDataFileUtil;
import io.github.msdk.util.tolerances.MzTolerance;
import io.github.msdk.util.tolerances.RTTolerance;

/**
 * This class creates a list of Chromatograms from a RawDataFile based on a list of IonAnnotations.
 */
public class TargetedDetectionMethod implements MSDKMethod<List<Chromatogram>> {

  private final @Nonnull List<IonAnnotation> ionAnnotations;
  private final @Nonnull RawDataFile rawDataFile;
  private final @Nonnull MzTolerance mzTolerance;
  private final @Nonnull RTTolerance rtTolerance;
  private final @Nonnull Double intensityTolerance;
  private final @Nonnull Double noiseLevel;

  private List<Chromatogram> result;
  private boolean canceled = false;
  private int processedScans = 0, totalScans = 0;

  // Data structures
  private @Nonnull double mzBuffer[];
  private @Nonnull float intensityBuffer[];
  private int numOfDataPoints;

  /**
   * <p>
   * Constructor for TargetedDetectionMethod.
   * </p>
   *
   * @param ionAnnotations a {@link java.util.List} object.
   * @param rawDataFile a {@link io.github.msdk.datamodel.RawDataFile} object.
   * @param mzTolerance an object
   * @param rtTolerance a {@link io.github.msdk.util.tolerances.RTTolerance} object.
   * @param intensityTolerance a {@link java.lang.Double} object.
   * @param noiseLevel a {@link java.lang.Double} object.
   */
  public TargetedDetectionMethod(@Nonnull List<IonAnnotation> ionAnnotations,
      @Nonnull RawDataFile rawDataFile, @Nonnull MzTolerance mzTolerance,
      @Nonnull RTTolerance rtTolerance, @Nonnull Double intensityTolerance,
      @Nonnull Double noiseLevel) {
    this.ionAnnotations = ionAnnotations;
    this.rawDataFile = rawDataFile;
    this.mzTolerance = mzTolerance;
    this.rtTolerance = rtTolerance;
    this.intensityTolerance = intensityTolerance;
    this.noiseLevel = noiseLevel;
  }

  /** {@inheritDoc} */
  @Override
  public List<Chromatogram> execute() throws MSDKException {

    result = new ArrayList<Chromatogram>();
    List<BuildingChromatogram> tempChromatogramList = new ArrayList<BuildingChromatogram>();
    int chromatogramNumber = RawDataFileUtil.getNextChromatogramNumber(rawDataFile);

    // Variables
    SimpleChromatogram chromatogram;
    BuildingChromatogram buildingChromatogram;
    int ionNr;

    // Create a new BuildingChromatogram for all ions
    for (int i = 0; i < ionAnnotations.size(); i++) {
      BuildingChromatogram newChromatogram = new BuildingChromatogram();
      tempChromatogramList.add(newChromatogram);
    }

    // Get MS1 scans from the raw data file
    List<MsScan> allScans = rawDataFile.getScans();
    List<MsScan> msScans = new ArrayList<MsScan>();
    for (MsScan scan : allScans) {
      Integer msLevel = scan.getMsLevel();
      if (msLevel.equals(1))
        msScans.add(scan);
    }

    // Loop through all scans
    totalScans = msScans.size();
    for (MsScan msScan : msScans) {

      // Load data points
      mzBuffer = msScan.getMzValues();
      intensityBuffer = msScan.getIntensityValues();
      numOfDataPoints = msScan.getNumberOfDataPoints();

      Float chromatographyInfo = msScan.getRetentionTime();

      // Loop through all the ions in the ion annotation list
      ionNr = 0;
      for (IonAnnotation ionAnnotation : ionAnnotations) {
        Double ionMz = ionAnnotation.getExpectedMz();
        if (ionMz != null) {
          Range<Double> mzRange = mzTolerance.getToleranceRange(ionMz);

          // Get highest data point which has a m/z within the mzRange
          Double mz = 0d;
          Float intensity = 0f;
          Integer index =
              MsSpectrumUtil.getBasePeakIndex(mzBuffer, intensityBuffer, numOfDataPoints, mzRange);
          if (index != null) {
            mz = mzBuffer[index];
            intensity = intensityBuffer[index];
          }

          // Add this mzPeak or zero values to the chromatogram
          buildingChromatogram = tempChromatogramList.get(ionNr);
          buildingChromatogram.addDataPoint(chromatographyInfo, mz, intensity);

        }
        ionNr++;
      }

      processedScans++;

      if (canceled)
        return null;

    }

    // Loop through all the ions in the ion annotation list
    ionNr = 0;
    for (IonAnnotation ionAnnotation : ionAnnotations) {

      // Temporary chromatogram
      buildingChromatogram = tempChromatogramList.get(ionNr);

      // Find the most intense data point and crop the chromatogram based
      // on the input parameters
      Float rt = ionAnnotation.getExpectedRetentionTime();
      if (rt != null) {
        Range<Float> rtRange = rtTolerance.getToleranceRange(rt);
        buildingChromatogram.cropChromatogram(rtRange, intensityTolerance, noiseLevel);
      }

      // Final chromatogram
      chromatogram = new SimpleChromatogram();
      chromatogram.setChromatogramNumber(chromatogramNumber);

      // Add the data points to the final chromatogram
      float[] rtValues = buildingChromatogram.getRtValues();
      double[] mzValues = buildingChromatogram.getMzValues();
      float[] intensityValues = buildingChromatogram.getIntensityValues();
      int size = buildingChromatogram.getSize();
      chromatogram.setDataPoints(rtValues, mzValues, intensityValues, size);

      Double newMz;
      if (mzValues != null) {
        newMz = ChromatogramUtil.calculateMz(mzValues, intensityValues, size,
            CalculationMethod.allAverage);
        chromatogram.setMz(newMz);

        // Add the ion annotation to the chromatogram
        chromatogram.setIonAnnotation(ionAnnotation);

        // Add the chromatogram to the chromatogram list
        result.add(chromatogram);
      }

      chromatogramNumber++;
      ionNr++;
    }

    return result;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Float getFinishedPercentage() {
    return totalScans == 0 ? null : (float) processedScans / totalScans;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public List<Chromatogram> getResult() {
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    canceled = true;
  }

}
