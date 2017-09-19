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

package io.github.msdk.spectra.centroiding;

import javax.annotation.Nonnull;

import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.SimpleMsScan;
import io.github.msdk.util.MsScanUtil;

/**
 * <p>
 * BinningCentroidingAlgorithm class.
 * </p>
 */
public class BinningCentroidingAlgorithm implements MSDKCentroidingAlgorithm {

  private final @Nonnull Double binSize;

  private SimpleMsScan newScan;

  // Data structures
  private @Nonnull double mzBuffer[] = new double[10000];
  private @Nonnull float intensityBuffer[] = new float[10000];
  private int numOfDataPoints, newNumOfDataPoints;

  /**
   * <p>
   * Constructor for BinningCentroidingMethod.
   * </p>
   *
   * @param binSize a {@link java.lang.Double} object.
   */
  public BinningCentroidingAlgorithm(@Nonnull Double binSize) {
    this.binSize = binSize;
  }

  /** {@inheritDoc} */
  @Override
  public @Nonnull MsScan centroidScan(@Nonnull MsScan inputScan) {

    // Copy all scan properties
    this.newScan = MsScanUtil.clone(inputScan, false);

    // Load data points
    mzBuffer = inputScan.getMzValues();
    intensityBuffer = inputScan.getIntensityValues();
    numOfDataPoints = inputScan.getNumberOfDataPoints();
    newNumOfDataPoints = 0;

    // If there are no data points, just return the scan
    if (numOfDataPoints == 0) {
      newScan.setDataPoints(mzBuffer, intensityBuffer, 0);
      return newScan;
    }

    double currentBinMzStart = mzBuffer[0];
    float currentBinIntensity = 0f;

    // Iterate through all data points
    for (int i = 0; i < numOfDataPoints; i++) {

      if (mzBuffer[i] < (currentBinMzStart + binSize)) {
        currentBinIntensity += intensityBuffer[i];
        continue;
      }

      // Add the new data point
      final double currentBinMzValue = currentBinMzStart + (binSize / 2);
      mzBuffer[newNumOfDataPoints] = currentBinMzValue;
      intensityBuffer[newNumOfDataPoints] = currentBinIntensity;
      newNumOfDataPoints++;
      currentBinMzStart += binSize;
      currentBinIntensity = 0f;

    }

    // Store the new data points
    newScan.setDataPoints(mzBuffer, intensityBuffer, newNumOfDataPoints);

    return newScan;

  }

}
