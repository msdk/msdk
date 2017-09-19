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

import com.google.common.collect.Range;

import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.SimpleMsScan;
import io.github.msdk.util.MsScanUtil;

/**
 * <p>
 * RecursiveCentroidingAlgorithm class.
 * </p>
 */
public class RecursiveCentroidingAlgorithm implements MSDKCentroidingAlgorithm {

  private final @Nonnull Range<Double> mzPeakWidthRange;

  private SimpleMsScan newScan;

  // Data structures
  private @Nonnull double mzBuffer[];
  private @Nonnull float intensityBuffer[];
  private @Nonnull double newMzBuffer[];
  private @Nonnull float newIntensityBuffer[];
  private int numOfDataPoints, newNumOfDataPoints;

  /**
   * <p>
   * Constructor for RecursiveCentroidingMethod.
   * </p>
   *
   * @param mzPeakWidthRange a {@link com.google.common.collect.Range} object.
   */
  public RecursiveCentroidingAlgorithm(@Nonnull Range<Double> mzPeakWidthRange) {
    this.mzPeakWidthRange = mzPeakWidthRange;
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

    // Run the recursive search algorithm
    recursiveThreshold(0, numOfDataPoints - 1, 0);

    // Store the new data points
    newScan.setDataPoints(newMzBuffer, newIntensityBuffer, newNumOfDataPoints);

    return newScan;

  }

  /**
   * This function searches for maxima from given part of a spectrum
   */
  private int recursiveThreshold(int startInd, int stopInd, int recuLevel) {

    int peakStartInd, peakStopInd, peakMaxInd;
    double peakWidthMZ;

    for (int ind = startInd; ind < stopInd; ind++) {

      double localMinimum = Double.MAX_VALUE;

      // Add initial point of the peak
      peakStartInd = ind;
      peakMaxInd = peakStartInd;

      // While peak is on
      while (ind < stopInd) {

        final boolean isLocalMinimum = (intensityBuffer[ind - 1] > intensityBuffer[ind])
            && (intensityBuffer[ind] < intensityBuffer[ind + 1]);

        // Check if this is the minimum point of the peak
        if (isLocalMinimum && (intensityBuffer[ind] < localMinimum))
          localMinimum = intensityBuffer[ind];

        // Check if this is the maximum point of the peak
        if (intensityBuffer[ind] > intensityBuffer[peakMaxInd])
          peakMaxInd = ind;

        ind++;
      }

      // Add ending point of the peak
      peakStopInd = ind;

      peakWidthMZ = mzBuffer[peakStopInd] - mzBuffer[peakStartInd];

      // Verify width of the peak
      if (mzPeakWidthRange.contains(peakWidthMZ)) {

        // Declare a new MzPeak with intensity equal to max intensity
        // data point
        if (newMzBuffer.length < newNumOfDataPoints + 1) {
          double t1[] = new double[newNumOfDataPoints * 2];
          System.arraycopy(newMzBuffer, 0, t1, 0, newNumOfDataPoints);
          newMzBuffer = t1;
          float t2[] = new float[newNumOfDataPoints * 2];
          System.arraycopy(newIntensityBuffer, 0, t2, 0, newNumOfDataPoints);
          newIntensityBuffer = t2;
        }
        newMzBuffer[newNumOfDataPoints] = mzBuffer[peakMaxInd];
        newIntensityBuffer[newNumOfDataPoints] = intensityBuffer[peakMaxInd];
        newNumOfDataPoints++;

        if (recuLevel > 0) {
          // return stop index and beginning of the next peak
          return ind;
        }
      }

      // If the peak is still too big applies the same method until find a
      // peak of the right size
      if (peakWidthMZ > mzPeakWidthRange.upperEndpoint()) {
        if (localMinimum < Double.MAX_VALUE) {
          ind = recursiveThreshold(peakStartInd, peakStopInd, recuLevel + 1);
        }

      }

    }

    // return stop index
    return stopInd;

  }

}
