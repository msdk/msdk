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

package io.github.msdk.datamodel.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.chromatograms.ChromatogramType;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;
import io.github.msdk.util.ChromatogramUtil;

/**
 * Simple implementation of the Chromatogram interface.
 */
public class SimpleChromatogram implements Chromatogram {

  private @Nullable RawDataFile dataFile;
  private @Nonnull Integer chromatogramNumber, numOfDataPoints = 0;
  private @Nonnull ChromatogramType chromatogramType;
  private @Nullable Double mz;
  private @Nonnull SeparationType separationType;
  private @Nonnull float rtValues[];
  private @Nullable double mzValues[];
  private @Nonnull float intensityValues[];
  private @Nonnull Range<Float> rtRange;
  private @Nullable IonAnnotation ionAnnotation;

  private final @Nonnull List<IsolationInfo> isolations = new ArrayList<>();

  /** {@inheritDoc} */
  @Override
  @Nullable
  public RawDataFile getRawDataFile() {
    return dataFile;
  }

  /**
   * {@inheritDoc}
   *
   * @param newRawDataFile a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
   */
  public void setRawDataFile(@Nonnull RawDataFile newRawDataFile) {
    this.dataFile = newRawDataFile;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public Integer getChromatogramNumber() {
    return chromatogramNumber;
  }

  /** {@inheritDoc} */
  public void setChromatogramNumber(@Nonnull Integer chromatogramNumber) {
    Preconditions.checkNotNull(chromatogramNumber);
    this.chromatogramNumber = chromatogramNumber;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public ChromatogramType getChromatogramType() {
    return chromatogramType;
  }

  /**
   * {@inheritDoc}
   *
   * @param newChromatogramType a {@link io.github.msdk.datamodel.chromatograms.ChromatogramType}
   *        object.
   */
  public void setChromatogramType(@Nonnull ChromatogramType newChromatogramType) {
    this.chromatogramType = newChromatogramType;
  }

  /** {@inheritDoc} */
  @Override
  public @Nonnull Integer getNumberOfDataPoints() {
    return numOfDataPoints;
  }

  /** {@inheritDoc} */
  @Override
  public @Nonnull float[] getRetentionTimes() {
    return getRetentionTimes(null);
  }

  /**
   * {@inheritDoc}
   *
   * @param array an array of float.
   * @return an array of float.
   */
  public @Nonnull float[] getRetentionTimes(@Nullable float[] array) {
    if ((array == null) || (array.length < numOfDataPoints))
      array = new float[numOfDataPoints];
    if (rtValues != null)
      System.arraycopy(rtValues, 0, array, 0, numOfDataPoints);
    return array;
  }

  /** {@inheritDoc} */
  @Override
  public @Nonnull double[] getMzValues() {
    return getMzValues(null);
  }

  /**
   * {@inheritDoc}
   *
   * @param array an array of float.
   * @return an array of float.
   */
  public @Nonnull double[] getMzValues(@Nullable double[] array) {
    if ((array == null) || (array.length < numOfDataPoints))
      array = new double[numOfDataPoints];
    if (mzValues != null)
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
    if (intensityValues != null)
      System.arraycopy(intensityValues, 0, array, 0, numOfDataPoints);
    return array;
  }

  /**
   * {@inheritDoc}
   *
   * @param rtValues an array of float.
   * @param mzValues an array of double.
   * @param intensityValues an array of float.
   * @param size a {@link java.lang.Integer} object.
   */
  public synchronized void setDataPoints(@Nonnull float rtValues[], @Nullable double mzValues[],
      @Nonnull float intensityValues[], @Nonnull Integer size) {

    Preconditions.checkNotNull(rtValues);
    Preconditions.checkNotNull(intensityValues);
    Preconditions.checkArgument(rtValues.length >= size);
    Preconditions.checkArgument(intensityValues.length >= size);
    if (mzValues != null)
      Preconditions.checkArgument(mzValues.length >= size);

    // Make a copy of the data, instead of saving a reference to the provided array
    if ((this.rtValues == null) || (this.rtValues.length < size))
      this.rtValues = new float[size];
    System.arraycopy(rtValues, 0, this.rtValues, 0, size);

    if ((this.intensityValues == null) || (this.intensityValues.length < size))
      this.intensityValues = new float[size];
    System.arraycopy(intensityValues, 0, this.intensityValues, 0, size);

    if (mzValues != null) {
      if ((this.mzValues == null) || (this.mzValues.length < size))
        this.mzValues = new double[size];
      System.arraycopy(mzValues, 0, this.mzValues, 0, size);
    } else {
      this.mzValues = null;
    }

    // Save the size of the arrays
    this.numOfDataPoints = size;

    // Update the RT range
    this.rtRange = ChromatogramUtil.getRtRange(rtValues, size);

  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public List<IsolationInfo> getIsolations() {
    return isolations;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public SeparationType getSeparationType() {
    return separationType;
  }

  /**
   * {@inheritDoc}
   *
   * @param separationType a {@link io.github.msdk.datamodel.rawdata.SeparationType} object.
   */
  public void setSeparationType(@Nonnull SeparationType separationType) {
    this.separationType = separationType;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Double getMz() {
    return mz;
  }

  /**
   * {@inheritDoc}
   *
   * @param newMz a {@link java.lang.Double} object.
   */
  public void setMz(@Nullable Double newMz) {
    this.mz = newMz;
  }

  /**
   * {@inheritDoc}
   *
   * @param ionAnnotation a {@link io.github.msdk.datamodel.ionannotations.IonAnnotation} object.
   */
  public void setIonAnnotation(@Nonnull IonAnnotation ionAnnotation) {
    this.ionAnnotation = ionAnnotation;
  }

  /** {@inheritDoc} */
  @Override
  public IonAnnotation getIonAnnotation() {
    return ionAnnotation;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Range<Float> getRtRange() {
    return rtRange;
  }

}
