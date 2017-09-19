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

package io.github.msdk.spectra.centroidprofiledetection;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

import io.github.msdk.datamodel.ActivationInfo;
import io.github.msdk.datamodel.IsolationInfo;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.MsScanType;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.PolarityType;
import io.github.msdk.datamodel.RawDataFile;

/**
 * <p>
 * Abstract AbstractReadOnlyMsScan class.
 * </p>
 *
 */
public abstract class AbstractReadOnlyMsScan implements MsScan {

  private final @Nonnull RawDataFile dataFile;
  private final @Nonnull Integer scanNumber;
  private final @Nullable String scanDefinition;

  private final @Nonnull MsSpectrumType spectrumType;
  private final @Nonnull String msFunction;
  private final @Nullable Float rt;
  private final @Nonnull MsScanType scanType;
  private final @Nullable Range<Double> mzRange, scanningRange;
  private final @Nonnull Float tic;
  private final @Nonnull PolarityType polarity;
  private final @Nullable ActivationInfo sourceFragmentation;
  private final @Nonnull List<IsolationInfo> isolations;
  private final @Nonnull Integer numOfDataPoints;

  /**
   * <p>
   * Constructor for AbstractReadOnlyMsScan.
   * </p>
   *
   * @param dataFile a {@link io.github.msdk.datamodel.RawDataFile} object.
   * @param spectrumType a {@link io.github.msdk.datamodel.MsSpectrumType} object.
   * @param msFunction a {@link io.github.msdk.datamodel.rawdata.MsFunction} object.
   * @param scanType a {@link io.github.msdk.datamodel.MsScanType} object.
   * @param mzRange a {@link com.google.common.collect.Range} object.
   * @param scanningRange a {@link com.google.common.collect.Range} object.
   * @param scanNumber a {@link java.lang.Integer} object.
   * @param scanDefinition a {@link java.lang.String} object.
   * @param tic a {@link java.lang.Float} object.
   * @param polarity a {@link io.github.msdk.datamodel.PolarityType} object.
   * @param sourceFragmentation a {@link io.github.msdk.datamodel.ActivationInfo} object.
   * @param isolations a {@link java.util.List} object.
   * @param numOfDataPoints a {@link java.lang.Integer} object.
   * @param rt a {@link java.lang.Float} object.
   */
  public AbstractReadOnlyMsScan(@Nonnull RawDataFile dataFile, @Nonnull MsSpectrumType spectrumType,
      @Nonnull String msFunction, @Nullable Float rt,
      @Nonnull MsScanType scanType, @Nullable Range<Double> mzRange,
      @Nullable Range<Double> scanningRange, @Nonnull Integer scanNumber,
      @Nullable String scanDefinition, @Nonnull Float tic, @Nonnull PolarityType polarity,
      @Nullable ActivationInfo sourceFragmentation, @Nonnull List<IsolationInfo> isolations,
      @Nonnull Integer numOfDataPoints) {
    this.dataFile = dataFile;
    this.spectrumType = spectrumType;
    this.msFunction = msFunction;
    this.rt = rt;
    this.scanType = scanType;
    this.mzRange = mzRange;
    this.scanningRange = scanningRange;
    this.scanNumber = scanNumber;
    this.scanDefinition = scanDefinition;
    this.tic = tic;
    this.polarity = polarity;
    this.sourceFragmentation = sourceFragmentation;
    this.isolations = isolations;
    this.numOfDataPoints = numOfDataPoints;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public MsSpectrumType getSpectrumType() {
    return spectrumType;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Range<Double> getMzRange() {
    return mzRange;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public Float getTIC() {
    return tic;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public RawDataFile getRawDataFile() {
    return dataFile;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public Integer getScanNumber() {
    return scanNumber;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public String getScanDefinition() {
    return scanDefinition;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public String getMsFunction() {
    return msFunction;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public MsScanType getMsScanType() {
    return scanType;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Float getRetentionTime() {
    return rt;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Range<Double> getScanningRange() {
    return scanningRange;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public PolarityType getPolarity() {
    return polarity;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public ActivationInfo getSourceInducedFragmentation() {
    return sourceFragmentation;
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
  public Integer getNumberOfDataPoints() {
    return numOfDataPoints;
  }


}
