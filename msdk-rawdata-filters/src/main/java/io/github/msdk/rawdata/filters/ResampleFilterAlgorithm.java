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

package io.github.msdk.rawdata.filters;

import javax.annotation.Nonnull;

import com.google.common.collect.Range;

import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.SimpleMsScan;
import io.github.msdk.util.MsScanUtil;

/**
 * <p>
 * ResampleFilterAlgorithm class.
 * </p>
 */
public class ResampleFilterAlgorithm implements MSDKFilteringAlgorithm {

  private double binSize;

  // Data structures
  private @Nonnull double mzBuffer[];
  private @Nonnull float intensityBuffer[];
  private int numOfDataPoints, newNumOfDataPoints;

  /**
   * <p>
   * Constructor for ResampleFilterAlgorithm.
   * </p>
   *
   * @param binSize a double.
   */
  public ResampleFilterAlgorithm(double binSize) {
    if (binSize <= 0.0)
      throw new IllegalArgumentException("Bin size must be >0");
    this.binSize = binSize;
  }

  /** {@inheritDoc} */
  @Override
  public MsScan performFilter(@Nonnull MsScan scan) {

    // Load data points
    mzBuffer = scan.getMzValues();
    intensityBuffer = scan.getIntensityValues();
    numOfDataPoints = scan.getNumberOfDataPoints();
    newNumOfDataPoints = 0;

    Range<Double> mzRange = scan.getMzRange();

    if (mzRange == null) {
      MsScan result = MsScanUtil.clone(scan, true);
      return result;
    }

    if (binSize > mzRange.upperEndpoint()) {
      this.binSize = (int) Math.round(mzRange.upperEndpoint());
    }

    int numberOfBins =
        (int) Math.round((mzRange.upperEndpoint() - mzRange.lowerEndpoint()) / binSize);
    if (numberOfBins <= 0) {
      numberOfBins++;
    }

    // Create the array with the intensity values for each bin
    Float[] newY = new Float[numberOfBins];
    int intVal = 0;
    for (int i = 0; i < numberOfBins; i++) {
      newY[i] = 0.0f;
      int pointsInTheBin = 0;
      for (int j = 0; j < binSize; j++) {
        if (intVal < numOfDataPoints) {
          newY[i] += intensityBuffer[intVal++];
          pointsInTheBin++;
        }
      }
      newY[i] /= pointsInTheBin;
    }

    // Set the new m/z value in the middle of the bin
    double newX = mzRange.lowerEndpoint() + binSize / 2.0;

    // Creates new DataPoints

    for (Float newIntensity : newY) {
      mzBuffer[newNumOfDataPoints] = newX;
      intensityBuffer[newNumOfDataPoints] = newIntensity;
      newNumOfDataPoints++;

      newX += binSize;
    }

    // Return a new scan with the new data points
    SimpleMsScan result = MsScanUtil.clone(scan, false);
    result.setDataPoints(mzBuffer, intensityBuffer, newNumOfDataPoints);

    return result;
  }

}
