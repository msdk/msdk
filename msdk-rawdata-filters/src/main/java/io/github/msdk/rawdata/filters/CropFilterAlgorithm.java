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
 * CropFilterAlgorithm class.
 * </p>
 */
public class CropFilterAlgorithm implements MSDKFilteringAlgorithm {

  private final @Nonnull Range<Double> mzRange;
  private final @Nonnull Range<Float> rtRange;

  // Data structures
  private @Nonnull double mzBuffer[] = new double[10000];
  private @Nonnull float intensityBuffer[] = new float[10000];
  private int numOfDataPoints, newNumOfDataPoints;

  /**
   * <p>
   * Constructor for CropFilterAlgorithm.
   * </p>
   *
   * @param mzRange a {@link com.google.common.collect.Range} object.
   * @param rtRange a {@link com.google.common.collect.Range} object.
   */
  public CropFilterAlgorithm(@Nonnull Range<Double> mzRange, @Nonnull Range<Float> rtRange) {
    this.mzRange = mzRange;
    this.rtRange = rtRange;
  }

  /** {@inheritDoc} */
  @Override
  public MsScan performFilter(@Nonnull MsScan scan) {

    // Do only if the scan's retention time is inside the user defined
    // retention time range
    final Float rt = scan.getRetentionTime();
    if ((rt == null) || (!rtRange.contains(rt)))
      return null;

    // Load data points
    mzBuffer = scan.getMzValues();
    intensityBuffer = scan.getIntensityValues();
    numOfDataPoints = scan.getNumberOfDataPoints();

    // Create a new scan
    SimpleMsScan newScan = MsScanUtil.clone(scan, false);

    if (numOfDataPoints == 0) {
      newScan.setDataPoints(mzBuffer, intensityBuffer, 0);
      return newScan;
    }

    int firstIndex = 0, lastIndex = numOfDataPoints - 1;
    while ((firstIndex < numOfDataPoints) && (!mzRange.contains(mzBuffer[firstIndex])))
      firstIndex++;
    if (firstIndex == numOfDataPoints) {
      newScan.setDataPoints(mzBuffer, intensityBuffer, 0);
      return newScan;
    }

    while (!mzRange.contains(mzBuffer[lastIndex]))
      lastIndex--;
    newNumOfDataPoints = lastIndex - firstIndex + 1;

    System.arraycopy(mzBuffer, firstIndex, mzBuffer, 0, newNumOfDataPoints);
    System.arraycopy(intensityBuffer, firstIndex, intensityBuffer, 0, newNumOfDataPoints);

    // Store the new data points
    newScan.setDataPoints(mzBuffer, intensityBuffer, newNumOfDataPoints);
    return newScan;
  }

}
