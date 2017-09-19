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

package io.github.msdk.spectra.centroiding;

import javax.annotation.Nonnull;

import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.SimpleMsScan;
import io.github.msdk.util.MsScanUtil;

/**
 * This class implements the Continuous Wavelet Transform (CWT), Mexican Hat, over raw data points
 * of a certain spectrum. After get the spectrum in the wavelet's time domain, we use the local
 * maxima to detect possible peaks in the original raw data points.
 */
public class WaveletCentroidingAlgorithm implements MSDKCentroidingAlgorithm {

  /**
   * Parameters of the wavelet, NPOINTS is the number of wavelet values to use The WAVELET_ESL &
   * WAVELET_ESL indicates the Effective Support boundaries
   */
  private static final double NPOINTS = 60000;
  private static final int WAVELET_ESL = -5;
  private static final int WAVELET_ESR = 5;

  private final @Nonnull Integer scaleLevel;
  private final @Nonnull Double waveletWindow;

  /**
   * <p>
   * Constructor for WaveletCentroidingMethod.
   * </p>
   *
   * @param scaleLevel a {@link java.lang.Integer} object.
   * @param waveletWindow a {@link java.lang.Double} object.
   */
  public WaveletCentroidingAlgorithm(@Nonnull Integer scaleLevel, @Nonnull Double waveletWindow) {
    this.scaleLevel = scaleLevel;
    this.waveletWindow = waveletWindow;
  }

  /** {@inheritDoc} */
  @Override
  public @Nonnull MsScan centroidScan(@Nonnull MsScan inputScan) {

    // Copy all scan properties
    SimpleMsScan newScan = MsScanUtil.clone(inputScan, false);

    // Load data points
    double[] mzBuffer = inputScan.getMzValues();
    float[] intensityBuffer = inputScan.getIntensityValues();
    int numOfDataPoints = inputScan.getNumberOfDataPoints();

    // If there are no data points, just return the scan
    if (numOfDataPoints == 0) {
      newScan.setDataPoints(mzBuffer, intensityBuffer, 0);
      return newScan;
    }

    float[] cwtDataPoints = performCWT(intensityBuffer, numOfDataPoints, scaleLevel, waveletWindow);
    int newNumOfDataPoints = extractMzPeaks(cwtDataPoints, mzBuffer, intensityBuffer);

    // Store the new data points
    newScan.setDataPoints(mzBuffer, intensityBuffer, newNumOfDataPoints);

    return newScan;

  }

  /**
   * Perform the CWT over raw intensities in the selected scale level
   */
  private static @Nonnull float[] performCWT(@Nonnull float[] intensityBuffer, int numOfDataPoints,
      int scaleLevel, double waveletWindow) {

    float[] cwtDataPoints = new float[numOfDataPoints * 2];

    double wstep = ((WAVELET_ESR - WAVELET_ESL) / NPOINTS);
    double[] W = new double[(int) NPOINTS];

    double waveletIndex = WAVELET_ESL;
    for (int j = 0; j < NPOINTS; j++) {
      // Pre calculate the values of the wavelet
      W[j] = cwtMEXHATreal(waveletIndex, waveletWindow, 0.0);
      waveletIndex += wstep;
    }

    /*
     * We only perform Translation of the wavelet in the selected scale
     */
    int d = (int) NPOINTS / (WAVELET_ESR - WAVELET_ESL);
    int a_esl = scaleLevel * WAVELET_ESL;
    int a_esr = scaleLevel * WAVELET_ESR;
    double sqrtScaleLevel = Math.sqrt(scaleLevel);
    for (int dx = 0; dx < numOfDataPoints; dx++) {

      /* Compute wavelet boundaries */
      int t1 = a_esl + dx;
      if (t1 < 0)
        t1 = 0;
      int t2 = a_esr + dx;
      if (t2 >= numOfDataPoints)
        t2 = (numOfDataPoints - 1);

      /* Perform convolution */
      float intensity = 0.0f;
      for (int i = t1; i <= t2; i++) {
        int ind = (int) (NPOINTS / 2) - (((int) d * (i - dx) / scaleLevel) * (-1));
        if (ind < 0)
          ind = 0;
        if (ind >= NPOINTS)
          ind = (int) NPOINTS - 1;
        intensity += intensityBuffer[i] * W[ind];
      }
      intensity /= sqrtScaleLevel;
      // Eliminate the negative part of the wavelet map
      if (intensity < 0)
        intensity = 0;
      cwtDataPoints[dx] = intensity;
    }

    return cwtDataPoints;
  }

  /**
   * This function calculates the wavelets's coefficients in Time domain
   * 
   * @param double x Step of the wavelet
   * @param double a Window Width of the wavelet
   * @param double b Offset from the center of the peak
   */
  private static double cwtMEXHATreal(double x, double a, double b) {
    /* c = 2 / ( sqrt(3) * pi^(1/4) ) */
    final double c = 0.8673250705840776;
    final double TINY = 1E-200;
    double x2;

    if (a == 0.0)
      a = TINY;
    x = (x - b) / a;
    x2 = x * x;
    return c * (1.0 - x2) * Math.exp(-x2 / 2);
  }

  /**
   * This function searches for maxima from wavelet data points
   * 
   * @return Number of peaks found
   */
  private static int extractMzPeaks(float[] cwtDataPoints, double[] mzBuffer, float[] intensityBuffer) {

    int peakMaxInd = 0;
    int stopInd = cwtDataPoints.length / 2 - 1;
    int newNumOfDataPoints = 0;

    for (int ind = 0; ind <= stopInd; ind++) {

      while ((ind <= stopInd) && (cwtDataPoints[ind] == 0)) {
        ind++;
      }
      peakMaxInd = ind;
      if (ind >= stopInd) {
        break;
      }

      // While peak is on
      while ((ind <= stopInd) && (cwtDataPoints[ind] > 0)) {
        // Check if this is the maximum point of the peak
        if (cwtDataPoints[ind] > cwtDataPoints[peakMaxInd]) {
          peakMaxInd = ind;
        }
        ind++;
      }

      if (ind >= stopInd) {
        break;
      }

      // Store the new data point
      mzBuffer[newNumOfDataPoints] = mzBuffer[peakMaxInd];
      intensityBuffer[newNumOfDataPoints] = intensityBuffer[peakMaxInd];
      newNumOfDataPoints++;
    }

    return newNumOfDataPoints;
  }

}
