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

package io.github.msdk.io.mzml2;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Range;

import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.impl.SimpleIsolationInfo;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.ActivationInfo;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.MsScanType;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzml2.data.MzMLBinaryDataInfo;
import io.github.msdk.io.mzml2.data.MzMLCVGroup;
import io.github.msdk.io.mzml2.data.MzMLCVParam;
import io.github.msdk.io.mzml2.data.MzMLPrecursorElement;
import io.github.msdk.io.mzml2.data.MzMLIsolationWindow;
import io.github.msdk.io.mzml2.data.MzMLPrecursorList;
import io.github.msdk.io.mzml2.data.MzMLRawDataFile;
import io.github.msdk.io.mzml2.util.ByteBufferInputStreamAdapter;
import io.github.msdk.io.mzml2.util.MzMLIntensityPeaksDecoder;
import io.github.msdk.io.mzml2.util.MzMLMZPeaksDecoder;
import io.github.msdk.spectra.spectrumtypedetection.SpectrumTypeDetectionAlgorithm;
import io.github.msdk.util.MsSpectrumUtil;
import io.github.msdk.util.tolerances.MzTolerance;
import it.unimi.dsi.io.ByteBufferInputStream;

/**
 * <p>
 * MzMLSpectrum class.
 * </p>
 *
 * @author plusik
 * @version $Id: $Id
 */
public class MzMLSpectrum implements MsScan {
  private final @Nonnull MzMLRawDataFile dataFile;
  private final @Nonnull ByteBufferInputStream mappedByteBufferInputStream;
  private final @Nonnull String id;
  private final @Nonnull Integer scanNumber;
  private final @Nonnull Integer numOfDataPoints;

  private ArrayList<MzMLCVParam> cvParams;
  private MzMLPrecursorList precursorList;
  private MzMLBinaryDataInfo mzBinaryDataInfo;
  private MzMLBinaryDataInfo intensityBinaryDataInfo;
  private MsSpectrumType spectrumType;
  private Float tic;
  private Float retentionTime;
  private Range<Double> mzRange;
  private Range<Double> mzScanWindowRange;

  private Logger logger = LoggerFactory.getLogger(MzMLFileParser.class);

  /**
   * <p>
   * Constructor for MzMLSpectrum.
   * </p>
   *
   * @param dataFile a {@link io.github.msdk.io.mzml2.data.MzMLRawDataFile} object.
   */
  public MzMLSpectrum(MzMLRawDataFile dataFile, ByteBufferInputStream is, String id,
      Integer scanNumber, Integer numOfDataPoints) {
    this.cvParams = new ArrayList<>();
    this.precursorList = new MzMLPrecursorList();
    this.dataFile = dataFile;
    this.mappedByteBufferInputStream = is;
    this.id = id;
    this.scanNumber = scanNumber;
    this.numOfDataPoints = numOfDataPoints;
    this.mzBinaryDataInfo = null;
    this.intensityBinaryDataInfo = null;
    this.spectrumType = null;
    this.tic = null;
    this.retentionTime = null;
    this.mzRange = null;
    this.mzScanWindowRange = null;

  }

  /**
   * <p>
   * getCVParams.
   * </p>
   *
   * @return a {@link java.util.ArrayList} object.
   */
  public ArrayList<MzMLCVParam> getCVParams() {
    return cvParams;
  }

  /**
   * <p>
   * Getter for the field <code>mzBinaryDataInfo</code>.
   * </p>
   *
   * @return a {@link io.github.msdk.io.mzml2.data.MzMLBinaryDataInfo} object.
   */
  public MzMLBinaryDataInfo getMzBinaryDataInfo() {
    return mzBinaryDataInfo;
  }

  /**
   * <p>
   * Setter for the field <code>mzBinaryDataInfo</code>.
   * </p>
   *
   * @param mzBinaryDataInfo a {@link io.github.msdk.io.mzml2.data.MzMLBinaryDataInfo} object.
   */
  public void setMzBinaryDataInfo(MzMLBinaryDataInfo mzBinaryDataInfo) {
    this.mzBinaryDataInfo = mzBinaryDataInfo;
  }

  /**
   * <p>
   * Getter for the field <code>intensityBinaryDataInfo</code>.
   * </p>
   *
   * @return a {@link io.github.msdk.io.mzml2.data.MzMLBinaryDataInfo} object.
   */
  public MzMLBinaryDataInfo getIntensityBinaryDataInfo() {
    return intensityBinaryDataInfo;
  }

  /**
   * <p>
   * Setter for the field <code>intensityBinaryDataInfo</code>.
   * </p>
   *
   * @param intensityBinaryDataInfo a {@link io.github.msdk.io.mzml2.data.MzMLBinaryDataInfo}
   *        object.
   */
  public void setIntensityBinaryDataInfo(MzMLBinaryDataInfo intensityBinaryDataInfo) {
    this.intensityBinaryDataInfo = intensityBinaryDataInfo;
  }

  /**
   * <p>
   * getByteBufferInputStream.
   * </p>
   *
   * @return a {@link it.unimi.dsi.io.ByteBufferInputStream} object.
   */
  public ByteBufferInputStream getByteBufferInputStream() {
    return mappedByteBufferInputStream;
  }

  /**
   * <p>
   * getPrecursorList.
   * </p>
   *
   * @return a {@link io.github.msdk.io.mzml2.data.MzMLPrecursorList} object.
   */
  public MzMLPrecursorList getPrecursorList() {
    return precursorList;
  }

  /**
   * <p>
   * Getter for the field <code>id</code>.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getId() {
    return id;
  }

  /** {@inheritDoc} */
  @Override
  public Integer getNumberOfDataPoints() {
    return getMzBinaryDataInfo().getArrayLength();
  }

  /** {@inheritDoc} */
  @Override
  public double[] getMzValues() {
    double[] mzValues = null;
    Integer numOfDataPoints = this.numOfDataPoints;
    if (getMzBinaryDataInfo().getArrayLength() != numOfDataPoints) {
      logger.warn(
          "m/z binary data array contains a different array length from the default array length of the scan (#"
              + getScanNumber() + ")");
    }
    Integer precision;
    EnumSet<MzMLBinaryDataInfo.MzMLCompressionType> compressions =
        EnumSet.noneOf(MzMLBinaryDataInfo.MzMLCompressionType.class);
    try {
      switch (getMzBinaryDataInfo().getBitLength()) {
        case THIRTY_TWO_BIT_FLOAT:
        case THIRTY_TWO_BIT_INTEGER:
          precision = 32;
          break;
        case SIXTY_FOUR_BIT_FLOAT:
        case SIXTY_FOUR_BIT_INTEGER:
          precision = 64;
          break;
        default:
          precision = null;
      }

      compressions.add(getMzBinaryDataInfo().getCompressionType());

      InputStream encodedIs = new ByteBufferInputStreamAdapter(mappedByteBufferInputStream,
          getMzBinaryDataInfo().getPosition(), getMzBinaryDataInfo().getEncodedLength());
      InputStream decodedIs = Base64.getDecoder().wrap(encodedIs);
      byte[] decodedData = IOUtils.toByteArray(decodedIs);

      mzValues = MzMLMZPeaksDecoder
          .decode(decodedData, decodedData.length, precision, numOfDataPoints, compressions)
          .getDecodedArray();
    } catch (Exception e) {
      throw (new MSDKRuntimeException(e));
    }

    return mzValues;
  }

  /** {@inheritDoc} */
  @Override
  public float[] getIntensityValues() {
    float[] intensityValues = null;
    Integer numOfDataPoints = this.numOfDataPoints;
    if (getIntensityBinaryDataInfo().getArrayLength() != numOfDataPoints) {
      logger.warn(
          "Intensity binary data array contains a different array length from the default array length of the scan (#"
              + getScanNumber() + ")");
    }
    Integer precision;
    EnumSet<MzMLBinaryDataInfo.MzMLCompressionType> compressions =
        EnumSet.noneOf(MzMLBinaryDataInfo.MzMLCompressionType.class);
    try {
      switch (getIntensityBinaryDataInfo().getBitLength()) {
        case THIRTY_TWO_BIT_FLOAT:
        case THIRTY_TWO_BIT_INTEGER:
          precision = 32;
          break;
        case SIXTY_FOUR_BIT_FLOAT:
        case SIXTY_FOUR_BIT_INTEGER:
          precision = 64;
          break;
        default:
          precision = null;
      }

      compressions.add(getIntensityBinaryDataInfo().getCompressionType());

      InputStream encodedIs = new ByteBufferInputStreamAdapter(mappedByteBufferInputStream,
          getIntensityBinaryDataInfo().getPosition(),
          getIntensityBinaryDataInfo().getEncodedLength());
      InputStream decodedIs = Base64.getDecoder().wrap(encodedIs);
      byte[] decodedData = IOUtils.toByteArray(decodedIs);

      intensityValues = MzMLIntensityPeaksDecoder.decode(decodedData, decodedData.length, precision,
          numOfDataPoints, compressions).getArr();
    } catch (Exception e) {
      throw (new MSDKRuntimeException(e));
    }

    return intensityValues;
  }

  /** {@inheritDoc} */
  @Override
  public MsSpectrumType getSpectrumType() {
    if (spectrumType == null) {
      if (getCVValue(MzMLCV.cvCentroidSpectrum).isPresent())
        spectrumType = MsSpectrumType.CENTROIDED;

      if (getCVValue(MzMLCV.cvProfileSpectrum).isPresent())
        spectrumType = MsSpectrumType.PROFILE;

      if (spectrumType != null)
        return spectrumType;
    }
    spectrumType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(getMzValues(),
        getIntensityValues(), numOfDataPoints);
    return spectrumType;
  }

  /** {@inheritDoc} */
  @Override
  public Float getTIC() {
    if (tic == null) {
      Optional<String> cvv = getCVValue(MzMLCV.cvTIC);
      if (!cvv.isPresent()) {
        tic = MsSpectrumUtil.getTIC(getIntensityValues(), getMzBinaryDataInfo().getArrayLength());
        return tic;
      }
      try {
        tic = Float.valueOf(cvv.get());
      } catch (NumberFormatException e) {
        throw (new MSDKRuntimeException(
            "Could not convert TIC value in mzML file to a float\n" + e));
      }
    }
    return tic;
  }

  /** {@inheritDoc} */
  @Override
  public Range<Double> getMzRange() {
    if (mzRange == null) {
      Optional<String> cvv = getCVValue(MzMLCV.cvLowestMz);
      Optional<String> cvv1 = getCVValue(MzMLCV.cvHighestMz);
      if (!cvv.isPresent() || !cvv1.isPresent()) {
        mzRange = MsSpectrumUtil.getMzRange(getMzValues(), getMzBinaryDataInfo().getArrayLength());
        return mzRange;
      }
      try {
        mzRange = Range.closed(Double.valueOf(cvv.get()), Double.valueOf(cvv1.get()));
      } catch (NumberFormatException e) {
        throw (new MSDKRuntimeException(
            "Could not convert mz range value in mzML file to a double\n" + e));
      }
    }
    return mzRange;
  }

  /** {@inheritDoc} */
  @Override
  public RawDataFile getRawDataFile() {
    return dataFile;
  }

  /** {@inheritDoc} */
  @Override
  public Integer getScanNumber() {
    return scanNumber;
  }

  /** {@inheritDoc} */
  @Override
  public String getScanDefinition() {
    return getCVValue(MzMLCV.cvScanFilterString).get();
  }

  /** {@inheritDoc} */
  @Override
  public MsFunction getMsFunction() {
    Integer msLevel = 1;
    String value = getCVValue(MzMLCV.cvMSLevel).get();
    if (!Strings.isNullOrEmpty(value))
      msLevel = Integer.parseInt(value);
    return MSDKObjectBuilder.getMsFunction(msLevel);
  }

  /** {@inheritDoc} */
  @Override
  public MsScanType getMsScanType() {
    return MsScanType.UNKNOWN;
  }

  /** {@inheritDoc} */
  @Override
  public Range<Double> getScanningRange() {
    if (mzScanWindowRange == null) {
      Optional<String> cvv = getCVValue(MzMLCV.cvScanWindowLowerLimit);
      Optional<String> cvv1 = getCVValue(MzMLCV.cvScanWindowUpperLimit);
      if (!cvv.isPresent() || !cvv1.isPresent()) {
        mzScanWindowRange = getMzRange();
        return mzScanWindowRange;
      }
      try {
        mzScanWindowRange = Range.closed(Double.valueOf(cvv.get()), Double.valueOf(cvv1.get()));
      } catch (NumberFormatException e) {
        throw (new MSDKRuntimeException(
            "Could not convert scan window range value in mzML file to a double\n" + e));
      }
    }
    return mzScanWindowRange;
  }

  /** {@inheritDoc} */
  @Override
  public PolarityType getPolarity() {
    if (getCVValue(MzMLCV.cvPolarityPositive).isPresent())
      return PolarityType.POSITIVE;

    if (getCVValue(MzMLCV.cvPolarityNegative).isPresent())
      return PolarityType.NEGATIVE;

    return PolarityType.UNKNOWN;
  }

  /** {@inheritDoc} */
  @Override
  public ActivationInfo getSourceInducedFragmentation() {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public List<IsolationInfo> getIsolations() {
    if (precursorList.getPrecursorElements().size() == 0)
      return Collections.emptyList();

    List<IsolationInfo> isolations = new ArrayList<>();

    for (MzMLPrecursorElement precursor : precursorList.getPrecursorElements()) {
      Optional<String> precursorMz = Optional.ofNullable(null);
      Optional<String> precursorCharge = Optional.ofNullable(null);
      Optional<String> isolationWindowTarget = Optional.ofNullable(null);
      Optional<String> isolationWindowLower = Optional.ofNullable(null);
      Optional<String> isolationWindowUpper = Optional.ofNullable(null);

      if (!precursor.getSelectedIonList().isPresent())
        return Collections.emptyList();

      for (MzMLCVGroup cvGroup : precursor.getSelectedIonList().get().getSelectedIonList()) {
        precursorMz = getCVValue(cvGroup, MzMLCV.cvPrecursorMz);
        if (!precursorMz.isPresent())
          precursorMz = getCVValue(cvGroup, MzMLCV.cvMz);
        precursorCharge = getCVValue(cvGroup, MzMLCV.cvChargeState);
      }

      if (precursor.getIsolationWindow().isPresent()) {
        MzMLIsolationWindow isolationWindow = precursor.getIsolationWindow().get();
        isolationWindowTarget = getCVValue(isolationWindow, MzMLCV.cvIsolationWindowTarget);
        isolationWindowLower = getCVValue(isolationWindow, MzMLCV.cvIsolationWindowLowerOffset);
        isolationWindowUpper = getCVValue(isolationWindow, MzMLCV.cvIsolationWindowUpperOffset);
      }


      if (precursorMz.isPresent()) {
        if (!isolationWindowTarget.isPresent())
          isolationWindowTarget = precursorMz;
        if (!isolationWindowLower.isPresent())
          isolationWindowLower = Optional.ofNullable("0.5");
        if (!isolationWindowUpper.isPresent())
          isolationWindowUpper = Optional.ofNullable("0.5");
        Range<Double> isolationRange = Range.closed(
            Double.valueOf(isolationWindowTarget.get())
                - Double.valueOf(isolationWindowLower.get()),
            Double.valueOf(isolationWindowTarget.get())
                + Double.valueOf(isolationWindowLower.get()));
        IsolationInfo isolation = new SimpleIsolationInfo(isolationRange, null,
            Double.valueOf(precursorMz.get()), Integer.valueOf(precursorCharge.get()), null);
        isolations.add(isolation);

      }

    }

    return Collections.unmodifiableList(isolations);
  }

  /** {@inheritDoc} */
  @Override
  public Float getRetentionTime() {
    if (retentionTime != null)
      return retentionTime;

    for (MzMLCVParam param : cvParams) {
      String accession = param.getAccession();
      Optional<String> unitAccession = param.getUnitAccession();
      Optional<String> value = param.getValue();

      // check accession
      switch (accession) {
        case MzMLCV.MS_RT_SCAN_START:
        case MzMLCV.MS_RT_RETENTION_TIME:
        case MzMLCV.MS_RT_RETENTION_TIME_LOCAL:
        case MzMLCV.MS_RT_RETENTION_TIME_NORMALIZED:
          if (!value.isPresent()) {
            throw new IllegalStateException(
                "For retention time cvParam the `value` must have been specified");
          }
          if (unitAccession.isPresent()) {
            // there was a time unit defined
            switch (param.getUnitAccession().get()) {
              case MzMLCV.cvUnitsMin1:
              case MzMLCV.cvUnitsMin2:
                retentionTime = Float.parseFloat(value.get()) * 60f;
                break;
              case MzMLCV.cvUnitsSec:
                retentionTime = Float.parseFloat(value.get());
                break;

              default:
                throw new IllegalStateException(
                    "Unknown time unit encountered: [" + unitAccession + "]");
            }
          } else {
            // no time units defined, return the value as is
            retentionTime = Float.parseFloat(value.get());
          }
          break;

        default:
          continue; // not a retention time parameter
      }
    }
    return retentionTime;
  }

  /** {@inheritDoc} */
  @Override
  public MzTolerance getMzTolerance() {
    return null;
  }

  /**
   * <p>
   * getCVValue.
   * </p>
   *
   * @param accession a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public Optional<String> getCVValue(String accession) {
    for (MzMLCVParam cvParam : cvParams) {
      Optional<String> value;
      if (cvParam.getAccession().equals(accession)) {
        value = cvParam.getValue();
        if (!value.isPresent())
          value = Optional.ofNullable("");
        return value;
      }
    }
    return Optional.ofNullable(null);
  }

  /**
   * <p>
   * getCVValue.
   * </p>
   *
   * @param group a {@link io.github.msdk.io.mzml2.data.MzMLCVGroup} object.
   * @param accession a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public Optional<String> getCVValue(MzMLCVGroup group, String accession) {
    Optional<String> value;
    for (MzMLCVParam cvParam : group.getCVParams()) {
      if (cvParam.getAccession().equals(accession)) {
        value = cvParam.getValue();
        if (!value.isPresent())
          value = Optional.ofNullable("");
        return value;
      }
    }
    return Optional.ofNullable(null);
  }

}
