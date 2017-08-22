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

package io.github.msdk.io.netcdf;

import java.io.IOException;

import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.impl.SimpleMsScan;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.spectra.spectrumtypedetection.SpectrumTypeDetectionAlgorithm;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

public class NetCDFMsScan extends SimpleMsScan {

  private int[] scanStartPositions;
  private float[] scanRetentionTimes;
  private Variable massValueVariable;
  private Variable intensityValueVariable;
  private double massValueScaleFactor;
  private double intensityValueScaleFactor;
  private double[] preLoadedMzValues;
  private float[] preLoadedIntensityValues;
  private Integer numOfDataPoints;
  private MsSpectrumType spectrumType;

  public NetCDFMsScan(Integer scanNumber, int[] scanStartPositions, float[] scanRetentionTimes,
      Variable massValueVariable, Variable intensityValueVariable, double massValueScaleFactor,
      double intensityValueScaleFactor) {
    super(scanNumber);
    this.scanStartPositions = scanStartPositions;
    this.scanRetentionTimes = scanRetentionTimes;
    this.massValueVariable = massValueVariable;
    this.intensityValueVariable = intensityValueVariable;
    this.massValueScaleFactor = massValueScaleFactor;
    this.intensityValueScaleFactor = intensityValueScaleFactor;
    this.preLoadedMzValues = null;
    this.preLoadedIntensityValues = null;
    this.spectrumType = null;
  }

  @Override
  public float[] getIntensityValues(float[] intensityValues) {
    if (preLoadedIntensityValues == null) {
      final Integer scanIndex = getScanIndex();
      numOfDataPoints = getNumberOfDataPoints();
      try {
        final int scanStartPosition[] = {scanStartPositions[scanIndex]};
        final int scanLength[] =
            {scanStartPositions[scanIndex + 1] - scanStartPositions[scanIndex]};
        final Array intensityValueArray =
            intensityValueVariable.read(scanStartPosition, scanLength);
        final Index intensityValuesIndex = intensityValueArray.getIndex();

        if (intensityValues == null || intensityValues.length < numOfDataPoints)
          intensityValues = new float[numOfDataPoints];

        // Load the data points
        for (int i = 0; i < numOfDataPoints; i++) {
          final Index intensityIndex0 = intensityValuesIndex.set0(i);
          intensityValues[i] =
              (float) (intensityValueArray.getDouble(intensityIndex0) * intensityValueScaleFactor);
        }
      } catch (IOException | InvalidRangeException e) {
        throw new MSDKRuntimeException(e);
      }
    } else {
      if (intensityValues == null || intensityValues.length < numOfDataPoints)
        intensityValues = new float[numOfDataPoints];

      for (int i = 0; i < preLoadedIntensityValues.length; i++)
        intensityValues[i] = preLoadedIntensityValues[i];
    }

    return intensityValues;
  }

  @Override
  public double[] getMzValues(double[] mzValues) {
    if (preLoadedMzValues == null) {
      final Integer scanIndex = getScanIndex();
      numOfDataPoints = getNumberOfDataPoints();
      try {
        final int scanStartPosition[] = {scanStartPositions[scanIndex]};
        final int scanLength[] =
            {scanStartPositions[scanIndex + 1] - scanStartPositions[scanIndex]};
        final Array massValueArray = massValueVariable.read(scanStartPosition, scanLength);
        final Index massValuesIndex = massValueArray.getIndex();

        if (mzValues == null || mzValues.length < numOfDataPoints)
          mzValues = new double[numOfDataPoints];

        // Load the data points
        for (int i = 0; i < numOfDataPoints; i++) {
          final Index massIndex0 = massValuesIndex.set0(i);
          mzValues[i] = massValueArray.getDouble(massIndex0) * massValueScaleFactor;
        }

      } catch (IOException | InvalidRangeException e) {
        throw new MSDKRuntimeException(e);
      }
    } else {
      if (mzValues == null || mzValues.length < numOfDataPoints)
        mzValues = new double[numOfDataPoints];

      for (int i = 0; i < preLoadedMzValues.length; i++)
        mzValues[i] = preLoadedMzValues[i];
    }

    return mzValues;
  }

  @Override
  public Integer getNumberOfDataPoints() {
    if (numOfDataPoints == null) {
      final Integer scanIndex = getScanIndex();
      numOfDataPoints = scanStartPositions[scanIndex + 1] - scanStartPositions[scanIndex];
    }

    return numOfDataPoints;
  }

  @Override
  public Float getRetentionTime() {
    return scanRetentionTimes[getScanIndex()];
  }

  @Override
  public MsSpectrumType getSpectrumType() {
    if (spectrumType == null)
      spectrumType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(getMzValues(),
          getIntensityValues(), getNumberOfDataPoints());

    return spectrumType;
  }

  public Integer getScanIndex() {
    return getScanNumber() - 1;
  }

  public void parseScan() throws IOException, InvalidRangeException {
    // Load values to this scan instance itself, this method is called only when the scan passes the
    // predicate
    preLoadedMzValues = getMzValues();
    preLoadedIntensityValues = getIntensityValues();
    numOfDataPoints = getNumberOfDataPoints();

    setDataPoints(preLoadedMzValues, preLoadedIntensityValues, numOfDataPoints);
    spectrumType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(preLoadedMzValues,
        preLoadedIntensityValues, numOfDataPoints);
  }
}
