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

package io.github.msdk.featuredetection.srm;

import javax.annotation.Nonnull;

class BuildingChromatogram {

  // Initial variables
  private int size = 0;
  private float[] rtValues = new float[100];
  private double[] mzValues = new double[100];
  private float[] intensityValues = new float[100];

  void addDataPoint(@Nonnull Float rt, @Nonnull Double mz, @Nonnull Float intensity) {

    // Make sure we have enough space to add a new data point
    if (size == mzValues.length) {
      allocate(size * 2);
    }

    // Add data point
    rtValues[size] = rt;
    mzValues[size] = mz;
    intensityValues[size] = intensity;
    size++;
  }

  float[] getRtValues() {
    return rtValues;
  }

  double[] getMzValues() {
    return mzValues;
  }

  float[] getIntensityValues() {
    return intensityValues;
  }

  /**
   * <p>
   * allocate.
   * </p>
   *
   * @param newSize a {@link java.lang.Integer} object.
   */
  public void allocate(int newSize) {

    if (mzValues.length >= newSize)
      return;

    float[] rtValuesNew = new float[newSize];
    double[] mzValuesNew = new double[newSize];
    float[] intensityValuesNew = new float[newSize];

    if (size > 0) {
      System.arraycopy(rtValues, 0, rtValuesNew, 0, size);
      System.arraycopy(mzValues, 0, mzValuesNew, 0, size);
      System.arraycopy(intensityValues, 0, intensityValuesNew, 0, size);
    }

    rtValues = rtValuesNew;
    mzValues = mzValuesNew;
    intensityValues = intensityValuesNew;
  }

  /**
   * <p>
   * Getter for the field <code>size</code>.
   * </p>
   *
   * @return a {@link java.lang.Integer} object.
   */
  public int getSize() {
    return size;
  }
}
