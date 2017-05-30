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

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.chromatograms.ChromatogramType;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;

/**
 * Simple implementation of the Chromatogram interface.
 */
class SimpleChromatogram implements Chromatogram {

  private @Nonnull DataPointStore dataPointStore;
  private @Nullable RawDataFile dataFile;
  private @Nonnull Integer chromatogramNumber, numOfDataPoints;
  private @Nonnull ChromatogramType chromatogramType;
  private @Nullable Double mz;
  private @Nonnull SeparationType separationType;
  private Object dataStoreRtId = null, dataStoreIntensityId = null, dataStoreMzId = null;
  private @Nullable IonAnnotation ionAnnotation;
  private Range<Float> rtRange;

  private final @Nonnull List<IsolationInfo> isolations = new LinkedList<>();

  /**
   * <p>
   * Constructor for SimpleChromatogram.
   * </p>
   *
   * @param dataPointStore a {@link io.github.msdk.datamodel.datastore.DataPointStore} object.
   * @param chromatogramNumber a {@link java.lang.Integer} object.
   * @param chromatogramType a {@link io.github.msdk.datamodel.chromatograms.ChromatogramType}
   *        object.
   * @param separationType a {@link io.github.msdk.datamodel.rawdata.SeparationType} object.
   */
  public SimpleChromatogram(@Nonnull DataPointStore dataPointStore,
      @Nonnull Integer chromatogramNumber, @Nonnull ChromatogramType chromatogramType,
      @Nonnull SeparationType separationType) {
    Preconditions.checkNotNull(chromatogramNumber);
    this.dataPointStore = dataPointStore;
    this.chromatogramNumber = chromatogramNumber;
    this.chromatogramType = chromatogramType;
    this.separationType = separationType;
    this.numOfDataPoints = 0;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public RawDataFile getRawDataFile() {
    return dataFile;
  }

  /** {@inheritDoc} */
  @Override
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
  @Override
  public void setChromatogramNumber(@Nonnull Integer chromatogramNumber) {
    this.chromatogramNumber = chromatogramNumber;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public ChromatogramType getChromatogramType() {
    return chromatogramType;
  }

  /** {@inheritDoc} */
  @Override
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

  /** {@inheritDoc} */
  public @Nonnull float[] getRetentionTimes(@Nullable float[] array) {
    if ((array == null) || (array.length < numOfDataPoints))
      array = new float[numOfDataPoints];
    dataPointStore.loadData(dataStoreRtId, array);
    return array;
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
    dataPointStore.loadData(dataStoreMzId, array);
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
    dataPointStore.loadData(dataStoreIntensityId, array);
    return array;
  }

  /** {@inheritDoc} */
  @Override
  public synchronized void setDataPoints(@Nonnull float rtValues[],
      @Nullable double mzValues[], @Nonnull float intensityValues[], @Nonnull Integer size) {

    if (dataStoreRtId != null)
      dataPointStore.removeData(dataStoreRtId);
    if (dataStoreMzId != null)
      dataPointStore.removeData(dataStoreMzId);
    if (dataStoreIntensityId != null)
      dataPointStore.removeData(dataStoreIntensityId);

    dataStoreRtId = dataPointStore.storeData(rtValues, size);
    if (mzValues != null)
      dataStoreMzId = dataPointStore.storeData(mzValues, size);
    else
      dataStoreMzId = null;
    dataStoreIntensityId = dataPointStore.storeData(intensityValues, size);
    this.numOfDataPoints = size;

    if (size > 0)
      this.rtRange = Range.closed(rtValues[0], rtValues[size - 1]);
    else
      this.rtRange = null;
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

  /** {@inheritDoc} */
  @Override
  public void setSeparationType(@Nonnull SeparationType separationType) {
    this.separationType = separationType;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Double getMz() {
    return mz;
  }

  /** {@inheritDoc} */
  @Override
  public void setMz(@Nullable Double newMz) {
    this.mz = newMz;
  }

  /** {@inheritDoc} */
  @Override
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

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format("Chromatogram #%d (%s)", chromatogramNumber, chromatogramType);
  }
}
