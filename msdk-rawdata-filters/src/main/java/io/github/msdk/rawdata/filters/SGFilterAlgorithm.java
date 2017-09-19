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

import java.util.HashMap;

import javax.annotation.Nonnull;

import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.SimpleMsScan;
import io.github.msdk.util.MsScanUtil;

/**
 * <p>
 * SGFilterAlgorithm class.
 * </p>
 */
public class SGFilterAlgorithm implements MSDKFilteringAlgorithm {

  private static final HashMap<Integer, Integer> Hvalues = new HashMap<>();
  private static final HashMap<Integer, int[]> Avalues = new HashMap<>();

  static {
    int[] a5Ints = {17, 12, -3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    Avalues.put(5, a5Ints);
    int[] a7Ints = {7, 6, 3, -2, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    Avalues.put(7, a7Ints);
    int[] a9Ints = {59, 54, 39, 14, -21, 0, 0, 0, 0, 0, 0, 0, 0};
    Avalues.put(9, a9Ints);
    int[] a11Ints = {89, 84, 69, 44, 9, -36, 0, 0, 0, 0, 0, 0, 0};
    Avalues.put(11, a11Ints);
    int[] a13Ints = {25, 24, 21, 16, 9, 0, -11, 0, 0, 0, 0, 0, 0};
    Avalues.put(13, a13Ints);
    int[] a15Ints = {167, 162, 147, 122, 87, 42, -13, -78, 0, 0, 0, 0, 0};
    Avalues.put(15, a15Ints);
    int[] a17Ints = {43, 42, 39, 34, 27, 18, 7, -6, -21, 0, 0, 0, 0};
    Avalues.put(17, a17Ints);
    int[] a19Ints = {269, 264, 249, 224, 189, 144, 89, 24, -51, -136, 0, 0, 0};
    Avalues.put(19, a19Ints);
    int[] a21Ints = {329, 324, 309, 284, 249, 204, 149, 84, 9, -76, -171, 0, 0};
    Avalues.put(21, a21Ints);
    int[] a23Ints = {79, 78, 75, 70, 63, 54, 43, 30, 15, -2, -21, -42, 0};
    Avalues.put(23, a23Ints);
    int[] a25Ints = {467, 462, 447, 422, 387, 343, 287, 222, 147, 62, -33, -138, -253};
    Avalues.put(25, a25Ints);

    Hvalues.put(5, 35);
    Hvalues.put(7, 21);
    Hvalues.put(9, 231);
    Hvalues.put(11, 429);
    Hvalues.put(13, 143);
    Hvalues.put(15, 1105);
    Hvalues.put(17, 323);
    Hvalues.put(19, 2261);
    Hvalues.put(21, 3059);
    Hvalues.put(23, 805);
    Hvalues.put(25, 5175);

  }

  private final int sgDataPoints;

  // Data structures
  private @Nonnull double mzBuffer[] = new double[10000];
  private @Nonnull float intensityBuffer[] = new float[10000];
  private int numOfDataPoints, newNumOfDataPoints;

  /**
   * <p>
   * Constructor for SGFilterAlgorithm.
   * </p>
   *
   * @param sgDataPoints a int.
   */
  public SGFilterAlgorithm(int sgDataPoints) {
    this.sgDataPoints = sgDataPoints;
  }

  /** {@inheritDoc} */
  @Override
  public MsScan performFilter(@Nonnull MsScan scan) {

    if (!Avalues.containsKey(sgDataPoints) || !Hvalues.containsKey(sgDataPoints)) {
      return MsScanUtil.clone(scan, true);
    }

    int[] aVals = Avalues.get(sgDataPoints);
    int h = Hvalues.get(sgDataPoints);

    int marginSize = (sgDataPoints + 1) / 2 - 1;
    float sumOfInts;

    // Load data points
    mzBuffer = scan.getMzValues();
    intensityBuffer = scan.getIntensityValues();
    numOfDataPoints = scan.getNumberOfDataPoints();
    newNumOfDataPoints = 0;

    for (int spectrumInd =
        marginSize; spectrumInd < (numOfDataPoints - marginSize); spectrumInd++) {

      // zero intensity data points must be left unchanged
      if (intensityBuffer[spectrumInd] == 0) {
        intensityBuffer[spectrumInd - marginSize] = intensityBuffer[spectrumInd];
        continue;
      }

      sumOfInts = aVals[0] * intensityBuffer[spectrumInd];

      for (int windowInd = 1; windowInd <= marginSize; windowInd++) {
        sumOfInts += aVals[windowInd]
            * (intensityBuffer[spectrumInd + windowInd] + intensityBuffer[spectrumInd - windowInd]);
      }

      sumOfInts = sumOfInts / h;

      if (sumOfInts < 0) {
        sumOfInts = 0;
      }

      mzBuffer[newNumOfDataPoints] = mzBuffer[spectrumInd];
      intensityBuffer[newNumOfDataPoints] = sumOfInts;
      newNumOfDataPoints++;

    }

    // Return a new scan with the new data points
    SimpleMsScan result = MsScanUtil.clone(scan, false);
    result.setDataPoints(mzBuffer, intensityBuffer, newNumOfDataPoints);

    return result;
  }

}
