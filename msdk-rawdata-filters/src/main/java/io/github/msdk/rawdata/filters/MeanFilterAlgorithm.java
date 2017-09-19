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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.SimpleMsScan;
import io.github.msdk.util.MsScanUtil;

/**
 * <p>
 * MeanFilterAlgorithm class.
 * </p>
 */
public class MeanFilterAlgorithm implements MSDKFilteringAlgorithm {

  private final double windowLength;

  // Data structures
  private @Nonnull double mzBuffer[] = new double[10000];
  private @Nonnull float intensityBuffer[] = new float[10000];
  private int numOfDataPoints, newNumOfDataPoints;

  /**
   * <p>
   * Constructor for MeanFilterAlgorithm.
   * </p>
   *
   * @param windowLength a double.
   */
  public MeanFilterAlgorithm(double windowLength) {
    this.windowLength = windowLength;
  }

  /** {@inheritDoc} */
  @Override
  public MsScan performFilter(@Nonnull MsScan scan) {

    List<Double> massWindow = new ArrayList<>();
    List<Float> intensityWindow = new ArrayList<>();

    double currentMass;
    double lowLimit;
    double hiLimit;
    double mzVal;
    float elSum;
    int addi = 0;

    // Load data points
    mzBuffer = scan.getMzValues();
    intensityBuffer = scan.getIntensityValues();
    numOfDataPoints = scan.getNumberOfDataPoints();
    newNumOfDataPoints = 0;

    // For each data point
    for (int i = 0; i < numOfDataPoints; i++) {
      currentMass = mzBuffer[i];
      lowLimit = currentMass - windowLength;
      hiLimit = currentMass + windowLength;

      // Remove all elements from window whose m/z value is less than the
      // low limit
      if (massWindow.size() > 0) {
        mzVal = massWindow.get(0);
        while ((massWindow.size() > 0) && (mzVal < lowLimit)) {
          massWindow.remove(0);
          intensityWindow.remove(0);
          if (massWindow.size() > 0)
            mzVal = massWindow.get(0);
        }
      }

      // Add new elements as long as their m/z values are less than the hi
      // limit
      while ((addi < numOfDataPoints) && (mzBuffer[addi] <= hiLimit)) {
        massWindow.add(mzBuffer[addi]);
        intensityWindow.add(intensityBuffer[addi]);
        addi++;
      }

      elSum = 0;
      for (Float intensity : intensityWindow) {
        elSum += intensity;
      }

      mzBuffer[newNumOfDataPoints] = currentMass;
      intensityBuffer[newNumOfDataPoints] = elSum / (float) intensityWindow.size();
      newNumOfDataPoints++;
    }

    // Return a new scan with the new data points
    SimpleMsScan result = MsScanUtil.clone(scan, false);
    result.setDataPoints(mzBuffer, intensityBuffer, newNumOfDataPoints);

    return result;
  }

}
