/*
 * (C) Copyright 2015-2016 by MSDK Development Team
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

package io.github.msdk.datamodel.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.msspectra.MsSpectrum;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.util.MsSpectrumUtil;
import io.github.msdk.util.tolerances.MzTolerance;

/**
 * Simple implementation of the MassSpectrum interface.
 */
class SimpleMsSpectrum implements MsSpectrum {

  private @Nonnull double mzValues[];
  private @Nonnull float intensityValues[];
  private @Nonnull Integer numOfDataPoints;
  private @Nullable Range<Double> mzRange;
  private @Nonnull Float totalIonCurrent;

  private @Nonnull MsSpectrumType spectrumType;
  private @Nullable MzTolerance mzTolerance;

  SimpleMsSpectrum(@Nonnull double mzValues[], @Nonnull float intensityValues[],
      @Nonnull Integer size, @Nonnull MsSpectrumType spectrumType) {
    Preconditions.checkNotNull(mzValues);
    Preconditions.checkNotNull(intensityValues);
    Preconditions.checkNotNull(size);
    Preconditions.checkNotNull(spectrumType);

    // Make sure the spectrum is sorted
    for (int i = 0; i < size - 1; i++) {
      if (mzValues[i] > mzValues[i + 1])
        throw new MSDKRuntimeException("m/z values must be sorted in ascending order");
    }

    this.mzValues = mzValues;
    this.intensityValues = intensityValues;
    this.numOfDataPoints = size;
    this.mzRange = MsSpectrumUtil.getMzRange(mzValues, size);
    this.totalIonCurrent = MsSpectrumUtil.getTIC(intensityValues, numOfDataPoints);
    this.spectrumType = spectrumType;
  }

  /** {@inheritDoc} */
  @Override
  public @Nonnull Integer getNumberOfDataPoints() {
    return numOfDataPoints;
  }

  /** {@inheritDoc} */
  @Override
  public @Nonnull double[] getMzValues() {
    return getMzValues(null);
  }

  /** {@inheritDoc} */
  @Override
  public @Nonnull double[] getMzValues(@Nullable double array[]) {
    if ((array == null) || (array.length < numOfDataPoints))
      array = new double[numOfDataPoints];
    System.arraycopy(mzValues, 0, array, 0, numOfDataPoints);
    return array;
  }

  /** {@inheritDoc} */
  @Override
  public @Nonnull float[] getIntensityValues() {
    return getIntensityValues(null);
  }

  /** {@inheritDoc} */
  @Override
  public @Nonnull float[] getIntensityValues(@Nullable float array[]) {
    if ((array == null) || (array.length < numOfDataPoints))
      array = new float[numOfDataPoints];
    System.arraycopy(intensityValues, 0, array, 0, numOfDataPoints);
    return array;
  }

  /** {@inheritDoc} */
  @Override
  public synchronized void setDataPoints(@Nonnull double mzValues[],
      @Nonnull float intensityValues[], @Nonnull Integer size) {

    Preconditions.checkNotNull(mzValues);
    Preconditions.checkNotNull(intensityValues);
    Preconditions.checkNotNull(size);

    // Make sure the spectrum is sorted
    for (int i = 0; i < size - 1; i++) {
      if (mzValues[i] > mzValues[i + 1])
        throw new MSDKRuntimeException("m/z values must be sorted in ascending order");
    }

    this.mzValues = mzValues;
    this.intensityValues = intensityValues;
    this.numOfDataPoints = size;
    this.mzRange = MsSpectrumUtil.getMzRange(mzValues, size);
    this.totalIonCurrent = MsSpectrumUtil.getTIC(intensityValues, size);
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public MsSpectrumType getSpectrumType() {
    return spectrumType;
  }

  /** {@inheritDoc} */
  @Override
  public void setSpectrumType(@Nonnull MsSpectrumType spectrumType) {
    this.spectrumType = spectrumType;
  }

  /** {@inheritDoc} */
  @Override
  public MzTolerance getMzTolerance() {
    return mzTolerance;
  }

  /**
   * <p>
   * Setter for the field <code>mzTolerance</code>.
   * </p>
   *
   * @param mzTolerance a {@link io.github.msdk.util.tolerances.MzTolerance} object.
   */
  public void setMzTolerance(MzTolerance mzTolerance) {
    this.mzTolerance = mzTolerance;
  }

  /**
   * <p>
   * getTIC.
   * </p>
   *
   * @return a {@link java.lang.Float} object.
   */
  @Nonnull
  public Float getTIC() {
    return totalIonCurrent;
  }

  /** {@inheritDoc} */
  @Override
  public Range<Double> getMzRange() {
    return mzRange;
  }

}
