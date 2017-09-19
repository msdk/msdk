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
 * LocalMaximaCentroidingMethod class.
 * </p>
 */
public class LocalMaximaCentroidingAlgorithm implements MSDKCentroidingAlgorithm {

  private SimpleMsScan newScan;

  // Data structures
  private @Nonnull double mzBuffer[] = new double[10000];
  private @Nonnull float intensityBuffer[] = new float[10000];

  /** {@inheritDoc} */
  @Override
  public @Nonnull MsScan centroidScan(@Nonnull MsScan inputScan) {

    // Copy all scan properties
    this.newScan = MsScanUtil.clone(inputScan, false);

    // Load data points
    mzBuffer = inputScan.getMzValues();
    intensityBuffer = inputScan.getIntensityValues();
    final int numOfDataPoints = inputScan.getNumberOfDataPoints();
    int newNumOfDataPoints = 0;

    // If there are no data points, just return the scan
    if (numOfDataPoints == 0) {
      newScan.setDataPoints(mzBuffer, intensityBuffer, 0);
      return newScan;
    }

    int localMaximumIndex = 0;
    int rangeBeginning = 0, rangeEnd;
    boolean ascending = true;

    // Iterate through all data points
    for (int i = 0; i < numOfDataPoints - 1; i++) {

      final boolean nextIsBigger = intensityBuffer[i + 1] > intensityBuffer[i];
      final boolean nextIsZero = intensityBuffer[i + 1] == 0f;
      final boolean currentIsZero = intensityBuffer[i] == 0f;

      // Ignore zero intensity regions
      if (currentIsZero) {
        continue;
      }

      // Add current (non-zero) data point to the current m/z peak
      rangeEnd = i;

      // Check for local maximum
      if (ascending && (!nextIsBigger)) {
        localMaximumIndex = i;
        ascending = false;
        continue;
      }

      // Check for the end of the peak
      if ((!ascending) && (nextIsBigger || nextIsZero)) {

        final int numOfPeakDataPoints = rangeEnd - rangeBeginning;

        // Add the m/z peak if it has at least 4 data points
        if (numOfPeakDataPoints >= 4) {

          // Add the new data point
          mzBuffer[newNumOfDataPoints] = mzBuffer[localMaximumIndex];
          intensityBuffer[newNumOfDataPoints] = intensityBuffer[localMaximumIndex];
          newNumOfDataPoints++;

        }

        // Reset and start with new peak
        ascending = true;
        rangeBeginning = i;
      }

    }

    // Store the new data points
    newScan.setDataPoints(mzBuffer, intensityBuffer, newNumOfDataPoints);

    return newScan;

  }

}
