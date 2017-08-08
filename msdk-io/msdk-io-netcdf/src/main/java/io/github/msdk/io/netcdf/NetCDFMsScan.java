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
  private double[] mzValues;
  private float[] intensityValues;
  private Integer numOfDataPoints;

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
    this.mzValues = null;
    this.intensityValues = null;
  }

  @Override
  public float[] getIntensityValues(float[] intensityValues) {
    if (this.intensityValues == null)
      try {
        parseScan(null, null);
      } catch (IOException | InvalidRangeException e) {
        throw new MSDKRuntimeException(e);
      }

    return this.intensityValues;
  }

  @Override
  public double[] getMzValues(double[] array) {
    if (this.mzValues == null)
      try {
        parseScan(null, null);
      } catch (IOException | InvalidRangeException e) {
        throw new MSDKRuntimeException(e);
      }

    return this.mzValues;
  }

  @Override
  public Integer getNumberOfDataPoints() {
    if (numOfDataPoints == null)
      try {
        parseScan(null, null);
      } catch (IOException | InvalidRangeException e) {
        throw new MSDKRuntimeException(e);
      }

    return numOfDataPoints;
  }

  @Override
  public Float getRetentionTime() {
    if (mzValues == null || intensityValues == null)
      getNumberOfDataPoints();
    return super.getRetentionTime();
  }

  @Override
  public MsSpectrumType getSpectrumType() {
    if (mzValues == null || intensityValues == null)
      getNumberOfDataPoints();

    return super.getSpectrumType();
  }

  void parseScan(double[] mzValues, float[] intensityValues)
      throws IOException, InvalidRangeException {

    final Integer scanIndex = getScanNumber() - 1;

    // Find the Index of mass and intensity values
    final int scanStartPosition[] = {scanStartPositions[scanIndex]};
    final int scanLength[] = {scanStartPositions[scanIndex + 1] - scanStartPositions[scanIndex]};
    final Array massValueArray = massValueVariable.read(scanStartPosition, scanLength);
    final Array intensityValueArray = intensityValueVariable.read(scanStartPosition, scanLength);
    final Index massValuesIndex = massValueArray.getIndex();
    final Index intensityValuesIndex = intensityValueArray.getIndex();

    // Get number of data points
    numOfDataPoints = massValueArray.getShape()[0];

    // Allocate space
    if (mzValues == null || mzValues.length < numOfDataPoints)
      mzValues = new double[numOfDataPoints];
    if (intensityValues == null || intensityValues.length < numOfDataPoints)
      intensityValues = new float[numOfDataPoints];

    this.mzValues = mzValues;
    this.intensityValues = intensityValues;

    // Load the data points
    for (int i = 0; i < numOfDataPoints; i++) {
      final Index massIndex0 = massValuesIndex.set0(i);
      final Index intensityIndex0 = intensityValuesIndex.set0(i);
      mzValues[i] = massValueArray.getDouble(massIndex0) * massValueScaleFactor;
      intensityValues[i] =
          (float) (intensityValueArray.getDouble(intensityIndex0) * intensityValueScaleFactor);
    }

    setDataPoints(mzValues, intensityValues, numOfDataPoints);
    MsSpectrumType spectrumType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(mzValues,
        intensityValues, numOfDataPoints);
    setSpectrumType(spectrumType);
    setRetentionTime(scanRetentionTimes[scanIndex]);

  }
}
