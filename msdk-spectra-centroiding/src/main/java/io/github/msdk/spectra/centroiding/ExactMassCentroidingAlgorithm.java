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
 * ExactMassCentroidingAlgorithm class.
 * </p>
 */
public class ExactMassCentroidingAlgorithm implements MSDKCentroidingAlgorithm {

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

    // If there are no data points, just return the scan
    if (numOfDataPoints == 0) {
      newScan.setDataPoints(mzBuffer, intensityBuffer, 0);
      return newScan;
    }

    int localMaximumIndex = 0;
    int rangeBeginning = 0, rangeEnd;
    boolean ascending = true;
    int newNumOfDataPoints = 0;

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

          // Calculate the "center" m/z value
          double calculatedMz = calculateExactMass(mzBuffer, intensityBuffer, rangeBeginning,
              localMaximumIndex, rangeEnd);
          float intensity = intensityBuffer[localMaximumIndex];

          // Add the new data point
          mzBuffer[newNumOfDataPoints] = calculatedMz;
          intensityBuffer[newNumOfDataPoints] = intensity;
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

  /**
   * This method calculates the exact mass of a peak using the FWHM concept and linear regression (y
   * = mx + b).
   * 
   * @param ExactMassDataPoint
   * @return double
   */
  private double calculateExactMass(double mzBuffer[], float intensityBuffer[], int rangeBeginning,
      int localMaximumIndex, int rangeEnd) {

    /*
     * According with the FWHM concept, the exact mass of this peak is the half point of FWHM. In
     * order to get the points in the curve that define the FWHM, we use the linear equation.
     * 
     * First we look for, in left side of the peak, 2 data points together that have an intensity
     * less (first data point) and bigger (second data point) than half of total intensity. Then we
     * calculate the slope of the line defined by this two data points. At least, we calculate the
     * point in this line that has an intensity equal to the half of total intensity
     * 
     * We repeat the same process in the right side.
     */

    double xRight = -1, xLeft = -1;
    float halfIntensity = intensityBuffer[localMaximumIndex] / 2f;

    for (int i = rangeBeginning; i < rangeEnd - 1; i++) {

      // Left side of the curve
      if ((intensityBuffer[i] <= halfIntensity) && (i < localMaximumIndex)
          && (intensityBuffer[i + 1] >= halfIntensity)) {

        // First point with intensity just less than half of total
        // intensity
        double leftY1 = intensityBuffer[i];
        double leftX1 = mzBuffer[i];

        // Second point with intensity just bigger than half of total
        // intensity
        double leftY2 = intensityBuffer[i + 1];
        double leftX2 = mzBuffer[i + 1];

        // We calculate the slope with formula m = Y1 - Y2 / X1 - X2
        double mLeft = (leftY1 - leftY2) / (leftX1 - leftX2);

        // We calculate the desired point (at half intensity) with the
        // linear equation
        // X = X1 + [(Y - Y1) / m ], where Y = half of total intensity
        xLeft = leftX1 + (((halfIntensity) - leftY1) / mLeft);
        continue;
      }

      // Right side of the curve
      if ((intensityBuffer[i] >= halfIntensity) && (i > localMaximumIndex)
          && (intensityBuffer[i + 1] <= halfIntensity)) {

        // First point with intensity just bigger than half of total
        // intensity
        double rightY1 = intensityBuffer[i];
        double rightX1 = mzBuffer[i];

        // Second point with intensity just less than half of total
        // intensity
        double rightY2 = intensityBuffer[i + 1];
        double rightX2 = mzBuffer[i + 1];

        // We calculate the slope with formula m = Y1 - Y2 / X1 - X2
        double mRight = (rightY1 - rightY2) / (rightX1 - rightX2);

        // We calculate the desired point (at half intensity) with the
        // linear equation
        // X = X1 + [(Y - Y1) / m ], where Y = half of total intensity
        xRight = rightX1 + (((halfIntensity) - rightY1) / mRight);
        break;
      }
    }

    // We verify the values to confirm we find the desired points. If not we
    // return the same mass value.
    if ((xRight == -1) || (xLeft == -1))
      return mzBuffer[localMaximumIndex];

    // The center of left and right points is the exact mass of our peak.
    double exactMass = (xLeft + xRight) / 2;

    return exactMass;
  }

}
