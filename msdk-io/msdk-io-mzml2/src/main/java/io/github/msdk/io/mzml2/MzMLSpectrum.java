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
import io.github.msdk.util.MsSpectrumUtil;
import io.github.msdk.util.tolerances.MzTolerance;
import it.unimi.dsi.io.ByteBufferInputStream;

/**
 * <p>MzMLSpectrum class.</p>
 *
 * @author plusik
 * @version $Id: $Id
 */
public class MzMLSpectrum implements MsScan {
  private ArrayList<MzMLCVParam> cvParams;
  private MzMLBinaryDataInfo mzBinaryDataInfo;
  private MzMLBinaryDataInfo intensityBinaryDataInfo;
  private ByteBufferInputStream mappedByteBufferInputStream;
  private String id;
  private Integer scanNumber;
  private MzMLRawDataFile dataFile;

  /**
   * <p>Constructor for MzMLSpectrum.</p>
   *
   * @param dataFile a {@link io.github.msdk.io.mzml2.MzMLRawDataFile} object.
   */
  public MzMLSpectrum(MzMLRawDataFile dataFile) {
    cvParams = new ArrayList<>();
    this.dataFile = dataFile;
  }

  /**
   * <p>getCVParams.</p>
   *
   * @return a {@link java.util.ArrayList} object.
   */
  public ArrayList<MzMLCVParam> getCVParams() {
    return cvParams;
  }

  /**
   * <p>Getter for the field <code>mzBinaryDataInfo</code>.</p>
   *
   * @return a {@link io.github.msdk.io.mzml2.MzMLBinaryDataInfo} object.
   */
  public MzMLBinaryDataInfo getMzBinaryDataInfo() {
    return mzBinaryDataInfo;
  }

  /**
   * <p>Setter for the field <code>mzBinaryDataInfo</code>.</p>
   *
   * @param mzBinaryDataInfo a {@link io.github.msdk.io.mzml2.MzMLBinaryDataInfo} object.
   */
  public void setMzBinaryDataInfo(MzMLBinaryDataInfo mzBinaryDataInfo) {
    this.mzBinaryDataInfo = mzBinaryDataInfo;
  }

  /**
   * <p>Getter for the field <code>intensityBinaryDataInfo</code>.</p>
   *
   * @return a {@link io.github.msdk.io.mzml2.MzMLBinaryDataInfo} object.
   */
  public MzMLBinaryDataInfo getIntensityBinaryDataInfo() {
    return intensityBinaryDataInfo;
  }

  /**
   * <p>Setter for the field <code>intensityBinaryDataInfo</code>.</p>
   *
   * @param intensityBinaryDataInfo a {@link io.github.msdk.io.mzml2.MzMLBinaryDataInfo} object.
   */
  public void setIntensityBinaryDataInfo(MzMLBinaryDataInfo intensityBinaryDataInfo) {
    this.intensityBinaryDataInfo = intensityBinaryDataInfo;
  }

  /**
   * <p>getByteBufferInputStream.</p>
   *
   * @return a {@link it.unimi.dsi.io.ByteBufferInputStream} object.
   */
  public ByteBufferInputStream getByteBufferInputStream() {
    return mappedByteBufferInputStream;
  }

  /**
   * <p>setByteBufferInputStream.</p>
   *
   * @param mappedByteBufferInputStream a {@link it.unimi.dsi.io.ByteBufferInputStream} object.
   */
  public void setByteBufferInputStream(ByteBufferInputStream mappedByteBufferInputStream) {
    this.mappedByteBufferInputStream = mappedByteBufferInputStream;
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
          getMzBinaryDataInfo().getArrayLength(), compressions).arr;
    } catch (Exception e) {
      throw (new MSDKRuntimeException(e));
    }

    return mzValues;
  }

  /** {@inheritDoc} */
  @Override
  public float[] getIntensityValues() {
    float[] intensityValues = null;
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
      System.out.println(new String(IOUtils.toByteArray(encodedIs)));

      intensityValues = MzMLIntensityPeaksDecoder.decode(decodedData, decodedData.length, precision,
          getIntensityBinaryDataInfo().getArrayLength(), compressions).arr;
    } catch (Exception e) {
      throw (new MSDKRuntimeException(e));
    }

    return intensityValues;
  }

  /** {@inheritDoc} */
  @Override
  public MsSpectrumType getSpectrumType() {
    if (getCVValue(MzMLCV.cvCentroidSpectrum) != null)
      return MsSpectrumType.CENTROIDED;

    if (getCVValue(MzMLCV.cvProfileSpectrum) != null)
      return MsSpectrumType.PROFILE;

    return null;
  }

  /** {@inheritDoc} */
  @Override
  public Float getTIC() {
    Float tic = null;
    try {
      tic = Float.valueOf(getCVValue(MzMLCV.cvTIC));
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (tic == null)
      tic = MsSpectrumUtil.getTIC(getIntensityValues(), getMzBinaryDataInfo().getArrayLength());
    return tic;
  }

  /** {@inheritDoc} */
  @Override
  public Range<Double> getMzRange() {
    Range<Double> mzRange = null;
    try {
      mzRange = Range.closed(Double.valueOf(getCVValue(MzMLCV.cvLowestMz)),
          Double.valueOf(getCVValue(MzMLCV.cvHighestMz)));
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (mzRange == null)
      mzRange = MsSpectrumUtil.getMzRange(getMzValues(), getMzBinaryDataInfo().getArrayLength());
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
    return getCVValue(MzMLCV.cvScanFilterString);
  }

  /** {@inheritDoc} */
  @Override
  public MsFunction getMsFunction() {
    Integer msLevel = 1;
    String value = getCVValue(MzMLCV.cvMSLevel);
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
    Range<Double> scanRange = null;
    try {
      scanRange = Range.closed(Double.valueOf(getCVValue(MzMLCV.cvScanWindowLowerLimit)),
          Double.valueOf(getCVValue(MzMLCV.cvScanWindowUpperLimit)));
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (scanRange == null)
      getMzRange();
    return scanRange;
  }

  /** {@inheritDoc} */
  @Override
  public PolarityType getPolarity() {
    if (getCVValue(MzMLCV.cvPolarityPositive) != null)
      return PolarityType.POSITIVE;

    if (getCVValue(MzMLCV.cvPolarityNegative) != null)
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
    // TODO Have to parse precursor lists
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public Float getRetentionTime() {
    float retentionTime = 0;
    if (retentionTime == 0) {
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

  /** {@inheritDoc} */
  @Override
  public MzTolerance getMzTolerance() {
    return null;
  }

  /**
   * <p>Getter for the field <code>id</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getId() {
    return id;
  }

  void setId(String id) {
    this.id = id;
  }

  void setScanNumber(Integer scanNumber) {
    this.scanNumber = scanNumber;
  }

  /**
   * <p>getCVValue.</p>
   *
   * @param accession a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public String getCVValue(String accession) {
    for (MzMLCVParam cvParam : cvParams) {
      if (cvParam.getAccession().equals(accession))
        return cvParam.getValue();
    }
    return null;
  }
}
