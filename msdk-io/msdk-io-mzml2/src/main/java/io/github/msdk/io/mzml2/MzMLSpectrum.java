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
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Range;

import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.ActivationInfo;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.MsScanType;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.spectra.spectrumtypedetection.SpectrumTypeDetectionAlgorithm;
import io.github.msdk.util.MsSpectrumUtil;
import io.github.msdk.util.tolerances.MzTolerance;
import it.unimi.dsi.io.ByteBufferInputStream;

public class MzMLSpectrum implements MsScan {
  private ArrayList<MzMLCVParam> cvParams;
  private MzMLBinaryDataInfo mzBinaryDataInfo;
  private MzMLBinaryDataInfo intensityBinaryDataInfo;
  private ByteBufferInputStream mappedByteBufferInputStream;
  private String id;
  private Integer scanNumber;
  private Integer mzArrayLength;
  private MzMLRawDataFile dataFile;
  private MsSpectrumType spectrumType;
  private Float tic;
  private Float retentionTime;
  private Range<Double> mzRange;
  private Range<Double> mzScanWindowRange;

  public MzMLSpectrum(MzMLRawDataFile dataFile) {
    this.cvParams = new ArrayList<>();
    this.dataFile = dataFile;
    this.spectrumType = null;
    this.tic = null;
    this.retentionTime = null;
    this.mzRange = null;
    this.mzScanWindowRange = null;
    this.mzArrayLength = null;
  }

  public ArrayList<MzMLCVParam> getCVParams() {
    return cvParams;
  }

  public MzMLBinaryDataInfo getMzBinaryDataInfo() {
    return mzBinaryDataInfo;
  }

  public void setMzBinaryDataInfo(MzMLBinaryDataInfo mzBinaryDataInfo) {
    this.mzBinaryDataInfo = mzBinaryDataInfo;
  }

  public MzMLBinaryDataInfo getIntensityBinaryDataInfo() {
    return intensityBinaryDataInfo;
  }

  public void setIntensityBinaryDataInfo(MzMLBinaryDataInfo intensityBinaryDataInfo) {
    this.intensityBinaryDataInfo = intensityBinaryDataInfo;
  }

  public ByteBufferInputStream getByteBufferInputStream() {
    return mappedByteBufferInputStream;
  }

  public void setByteBufferInputStream(ByteBufferInputStream mappedByteBufferInputStream) {
    this.mappedByteBufferInputStream = mappedByteBufferInputStream;
  }

  @Override
  public Integer getNumberOfDataPoints() {
    return getMzBinaryDataInfo().getArrayLength();
  }

  @Override
  public double[] getMzValues() {
    double[] mzValues = null;
    this.mzArrayLength = getMzBinaryDataInfo().getArrayLength();
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

      mzValues = MzMLMZPeaksDecoder.decode(decodedData, decodedData.length, precision,
          mzArrayLength, compressions).arr;
    } catch (Exception e) {
      throw (new MSDKRuntimeException(e));
    }

    return mzValues;
  }

  @Override
  public float[] getIntensityValues() {
    float[] intensityValues = null;
    this.mzArrayLength = getIntensityBinaryDataInfo().getArrayLength();
    Integer precision;
    EnumSet<MzMLBinaryDataInfo.MzMLCompressionType> compressions =
        EnumSet.noneOf(MzMLBinaryDataInfo.MzMLCompressionType.class);
    try {
      mappedByteBufferInputStream.position(getIntensityBinaryDataInfo().getPosition());

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
          mzArrayLength, compressions).arr;
    } catch (Exception e) {
      throw (new MSDKRuntimeException(e));
    }

    return intensityValues;
  }

  @Override
  public MsSpectrumType getSpectrumType() {
    if (spectrumType == null) {
      if (getCVValue(MzMLCV.cvCentroidSpectrum) != null)
        spectrumType = MsSpectrumType.CENTROIDED;

      if (getCVValue(MzMLCV.cvProfileSpectrum) != null)
        spectrumType = MsSpectrumType.PROFILE;

      if (spectrumType != null)
        return spectrumType;
    }
    spectrumType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(getMzValues(),
        getIntensityValues(), mzArrayLength);
    return spectrumType;
  }

  @Override
  public Float getTIC() {
    if (tic == null) {
      String cvv = getCVValue(MzMLCV.cvTIC);
      if (cvv == null) {
        tic = MsSpectrumUtil.getTIC(getIntensityValues(), getMzBinaryDataInfo().getArrayLength());
        return tic;
      }
      try {
        tic = Float.valueOf(getCVValue(MzMLCV.cvTIC));
      } catch (NumberFormatException e) {
        throw (new MSDKRuntimeException(
            "Could not convert TIC value in mzML file to a float\n" + e));
      }
    }
    return tic;
  }

  @Override
  public Range<Double> getMzRange() {
    if (mzRange == null) {
      String cvv = getCVValue(MzMLCV.cvLowestMz);
      String cvv1 = getCVValue(MzMLCV.cvHighestMz);
      if (cvv == null || cvv1 == null) {
        mzRange = MsSpectrumUtil.getMzRange(getMzValues(), getMzBinaryDataInfo().getArrayLength());
        return mzRange;
      }
      try {
        mzRange = Range.closed(Double.valueOf(cvv), Double.valueOf(cvv1));
      } catch (NumberFormatException e) {
        throw (new MSDKRuntimeException(
            "Could not convert mz range value in mzML file to a double\n" + e));
      }
    }
    return mzRange;
  }

  @Override
  public RawDataFile getRawDataFile() {
    return dataFile;
  }

  @Override
  public Integer getScanNumber() {
    return scanNumber;
  }

  @Override
  public String getScanDefinition() {
    return getCVValue(MzMLCV.cvScanFilterString);
  }

  @Override
  public MsFunction getMsFunction() {
    Integer msLevel = 1;
    String value = getCVValue(MzMLCV.cvMSLevel);
    if (!Strings.isNullOrEmpty(value))
      msLevel = Integer.parseInt(value);
    return MSDKObjectBuilder.getMsFunction(msLevel);
  }

  @Override
  public MsScanType getMsScanType() {
    return MsScanType.UNKNOWN;
  }

  @Override
  public Range<Double> getScanningRange() {
    if (mzScanWindowRange == null) {
      String cvv = getCVValue(MzMLCV.cvScanWindowLowerLimit);
      String cvv1 = getCVValue(MzMLCV.cvScanWindowUpperLimit);
      if (cvv == null || cvv1 == null) {
        mzScanWindowRange = getMzRange();
        return mzScanWindowRange;
      }
      try {
        mzScanWindowRange = Range.closed(Double.valueOf(cvv), Double.valueOf(cvv1));
      } catch (NumberFormatException e) {
        throw (new MSDKRuntimeException(
            "Could not convert scan window range value in mzML file to a double\n" + e));
      }
    }
    return mzScanWindowRange;
  }

  @Override
  public PolarityType getPolarity() {
    if (getCVValue(MzMLCV.cvPolarityPositive) != null)
      return PolarityType.POSITIVE;

    if (getCVValue(MzMLCV.cvPolarityNegative) != null)
      return PolarityType.NEGATIVE;

    return PolarityType.UNKNOWN;
  }

  @Override
  public ActivationInfo getSourceInducedFragmentation() {
    return null;
  }

  @Override
  public List<IsolationInfo> getIsolations() {
    // TODO Have to parse precursor lists
    return null;
  }

  @Override
  public Float getRetentionTime() {
    if (retentionTime == null) {
      for (MzMLCVParam param : cvParams) {
        String accession = param.getAccession();
        String unitAccession = param.getUnitAccession();
        String value = param.getValue();
        if ((accession == null) || (value == null))
          continue;

        // Retention time (actually "Scan start time") MS:1000016
        if (accession.equals(MzMLCV.cvScanStartTime)) {
          try {
            if ((unitAccession == null) || (unitAccession.equals(MzMLCV.cvUnitsMin1))
                || unitAccession.equals(MzMLCV.cvUnitsMin2)) {
              // Minutes
              retentionTime = Float.parseFloat(value) * 60f;
            } else {
              // Seconds
              retentionTime = Float.parseFloat(value);
            }
            return retentionTime;
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      return null;
    }
    return retentionTime;
  }

  @Override
  public MzTolerance getMzTolerance() {
    return null;
  }

  public String getId() {
    return id;
  }

  void setId(String id) {
    this.id = id;
  }

  void setScanNumber(Integer scanNumber) {
    this.scanNumber = scanNumber;
  }

  public String getCVValue(String accession) {
    for (MzMLCVParam cvParam : cvParams) {
      if (cvParam.getAccession().equals(accession))
        return cvParam.getValue();
    }
    return null;
  }
}
