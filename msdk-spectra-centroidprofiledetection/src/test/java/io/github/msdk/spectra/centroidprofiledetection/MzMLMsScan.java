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

import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.ActivationInfo;
import io.github.msdk.datamodel.IsolationInfo;
import io.github.msdk.datamodel.MsScanType;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.PolarityType;
import io.github.msdk.util.tolerances.MzTolerance;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

class MzMLMsScan extends AbstractReadOnlyMsScan {

  private final @Nonnull MzMLRawDataFile dataFile;
  private final @Nonnull String spectrumId;

  MzMLMsScan(@Nonnull MzMLRawDataFile dataFile, @Nonnull String spectrumId,
      @Nonnull MsSpectrumType spectrumType, @Nonnull String msFunction,
      @Nullable Float rt, @Nonnull MsScanType scanType,
      @Nullable Range<Double> mzRange, @Nullable Range<Double> scanningRange,
      @Nonnull Integer scanNumber, @Nullable String scanDefinition, @Nonnull Float tic,
      @Nonnull PolarityType polarity, @Nullable ActivationInfo sourceFragmentation,
      @Nonnull List<IsolationInfo> isolations, @Nonnull Integer numOfDataPoints) {

    super(dataFile, spectrumType, msFunction, rt, scanType, mzRange, scanningRange,
        scanNumber, scanDefinition, tic, polarity, sourceFragmentation, isolations,
        numOfDataPoints);

    this.dataFile = dataFile;
    this.spectrumId = spectrumId;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public double[] getMzValues(double array[]) {
    try {
      MzMLUnmarshaller parser = dataFile.getParser();
      if (parser == null) {
        throw new MSDKRuntimeException("The raw data file object has been disposed");
      }
      Spectrum jmzSpectrum = parser.getSpectrumById(spectrumId);
      return MzMLConverter.extractMzValues(jmzSpectrum, null);
    } catch (MzMLUnmarshallerException e) {
      throw (new MSDKRuntimeException(e));
    }
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public float[] getIntensityValues(float array[]) {
    try {
      MzMLUnmarshaller parser = dataFile.getParser();
      if (parser == null) {
        throw new MSDKRuntimeException("The raw data file object has been disposed");
      }
      Spectrum jmzSpectrum = parser.getSpectrumById(spectrumId);
      return MzMLConverter.extractIntensityValues(jmzSpectrum, null);
    } catch (MzMLUnmarshallerException e) {
      throw (new MSDKRuntimeException(e));
    }
  }

  /** {@inheritDoc} */
  @Override
  public MzTolerance getMzTolerance() {
    return null;
  }

  @Override
  public Integer getMsLevel() {
    return 1;
  }

}
