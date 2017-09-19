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

package io.github.msdk.spectra.peakinvestigator;

import java.util.TreeMap;

import com.google.common.collect.Range;

import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.util.tolerances.MzTolerance;

/**
 * This class is used provide m/z-dependent mass tolerances from PeakInvestigator by decorating an
 * existing instance of MsSpectrum.
 */
public class PeakInvestigatorMsSpectrum implements MsSpectrum {

  private final MsSpectrum spectrum;
  private final TreeMap<Double, Error> errors;
  private double multiplier = 1.0;

  /**
   * <p>
   * Constructor for PeakInvestigatorMsSpectrum.
   * </p>
   *
   * @param spectrum a {@link io.github.msdk.datamodel.MsSpectrum} object.
   * @param errors a {@link java.util.TreeMap} object.
   */
  public PeakInvestigatorMsSpectrum(MsSpectrum spectrum, TreeMap<Double, Error> errors) {
    this.spectrum = spectrum;
    this.errors = errors;
  }

  /**
   * <p>
   * Getter for the field <code>multiplier</code>.
   * </p>
   *
   * @return a double.
   */
  public double getMultiplier() {
    return this.multiplier;
  }

  /**
   * <p>
   * Setter for the field <code>multiplier</code>.
   * </p>
   *
   * @param multiplier a double.
   */
  public void setMultiplier(double multiplier) {
    this.multiplier = multiplier;
  }

  /** {@inheritDoc} */
  @Override
  public MsSpectrumType getSpectrumType() {
    return MsSpectrumType.CENTROIDED;
  }

  /** {@inheritDoc} */
  @Override
  public Integer getNumberOfDataPoints() {
    return spectrum.getNumberOfDataPoints();
  }

  /** {@inheritDoc} */
  @Override
  public double[] getMzValues(double array[]) {
    return spectrum.getMzValues(array);
  }

  /** {@inheritDoc} */
  @Override
  public float[] getIntensityValues(float array[]) {
    return spectrum.getIntensityValues(array);
  }

  /** {@inheritDoc} */
  @Override
  public Float getTIC() {
    return spectrum.getTIC();
  }

  /** {@inheritDoc} */
  @Override
  public Range<Double> getMzRange() {
    return spectrum.getMzRange();
  }

  /**
   * <p>
   * getToleranceRange.
   * </p>
   *
   * @param mzValue a {@link java.lang.Double} object.
   * @return a {@link com.google.common.collect.Range} object.
   */
  public Range<Double> getToleranceRange(Double mzValue) {
    Error error = errors.get(mzValue);
    double mzError = multiplier * error.MZ_ERROR;
    if (error.MIN_ERROR > mzError) {
      mzError = error.MIN_ERROR;
    }

    return Range.closed(mzValue - mzError, mzValue + mzError);
  }

  /**
   * <p>
   * getError.
   * </p>
   *
   * @param mzValue a {@link java.lang.Double} object.
   * @return a {@link io.github.msdk.spectra.peakinvestigator.PeakInvestigatorMsSpectrum.Error}
   *         object.
   */
  public Error getError(Double mzValue) {
    return errors.get(mzValue);
  }

  public static class Error {
    public final double MZ_ERROR;
    public final float INTENSITY_ERROR;
    public final double MIN_ERROR;

    public Error(double mzError, float intensityError, double minError) {
      MZ_ERROR = mzError;
      INTENSITY_ERROR = intensityError;
      MIN_ERROR = minError;
    }
  }

  /** {@inheritDoc} */
  @Override
  public MzTolerance getMzTolerance() {
    // TODO Auto-generated method stub
    return null;
  }
}
