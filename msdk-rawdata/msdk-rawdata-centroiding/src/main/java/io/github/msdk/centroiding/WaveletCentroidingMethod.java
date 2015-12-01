/* 
 * (C) Copyright 2015 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */

package io.github.msdk.centroiding;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.util.MsScanUtil;

/**
 * This class implements the Continuous Wavelet Transform (CWT), Mexican Hat,
 * over raw datapoints of a certain spectrum. After get the spectrum in the
 * wavelet's time domain, we use the local maxima to detect possible peaks in
 * the original raw datapoints.
 */
public class WaveletCentroidingMethod implements MSDKMethod<MsScan> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Parameters of the wavelet, NPOINTS is the number of wavelet values to use
     * The WAVELET_ESL & WAVELET_ESL indicates the Effective Support boundaries
     */
    private static final double NPOINTS = 60000;
    private static final int WAVELET_ESL = -5;
    private static final int WAVELET_ESR = 5;

    private final @Nonnull MsScan inputScan;
    private final @Nonnull DataPointStore dataPointStore;
    private final @Nonnull Float noiseLevel;
    private final @Nonnull Integer scaleLevel;
    private final @Nonnull Double waveletWindow;

    private float methodProgress = 0f;
    private MsScan newScan;

    /**
     * <p>Constructor for WaveletCentroidingMethod.</p>
     *
     * @param inputScan a {@link io.github.msdk.datamodel.rawdata.MsScan} object.
     * @param dataPointStore a {@link io.github.msdk.datamodel.datapointstore.DataPointStore} object.
     * @param noiseLevel a {@link java.lang.Float} object.
     * @param scaleLevel a {@link java.lang.Integer} object.
     * @param waveletWindow a {@link java.lang.Double} object.
     */
    public WaveletCentroidingMethod(@Nonnull MsScan inputScan,
            @Nonnull DataPointStore dataPointStore, @Nonnull Float noiseLevel,
            @Nonnull Integer scaleLevel, @Nonnull Double waveletWindow) {
        this.inputScan = inputScan;
        this.dataPointStore = dataPointStore;
        this.noiseLevel = noiseLevel;
        this.scaleLevel = scaleLevel;
        this.waveletWindow = waveletWindow;
    }

    /** {@inheritDoc} */
    @Override
    public MsScan execute() throws MSDKException {

        logger.info("Started wavelet centroider on scan #"
                + inputScan.getScanNumber());

        // Copy all scan properties
        this.newScan = MsScanUtil.clone(dataPointStore, inputScan, false);

        // Create data structures
        final MsSpectrumDataPointList inputDataPoints = MSDKObjectBuilder
                .getMsSpectrumDataPointList();
        final MsSpectrumDataPointList newDataPoints = MSDKObjectBuilder
                .getMsSpectrumDataPointList();

        // Load data points
        inputScan.getDataPoints(inputDataPoints);

        // If there are no data points, just return the scan
        if (inputDataPoints.getSize() == 0) {
            newScan.setDataPoints(inputDataPoints);
            methodProgress = 1f;
            return newScan;
        }

        float waveletIntensities[] = performCWT(inputDataPoints);
        getMzPeaks(inputDataPoints, waveletIntensities, newDataPoints);

        // Store the new data points
        newScan.setDataPoints(newDataPoints);

        // Finish
        methodProgress = 1f;

        logger.info("Finished wavelet centroider on scan #"
                + inputScan.getScanNumber());

        return newScan;

    }

    /**
     * Perform the CWT over raw data points in the selected scale level
     * 
     * @param dataPoints
     */
    private float[] performCWT(MsSpectrumDataPointList inputDataPoints) {

        final double mzBuffer[] = inputDataPoints.getMzBuffer();
        final float intensityBuffer[] = inputDataPoints.getIntensityBuffer();

        final int length = mzBuffer.length;
        final float cwtDataPoints[] = new float[length];
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
        for (int dx = 0; dx < length; dx++) {

            /* Compute wavelet boundaries */
            int t1 = a_esl + dx;
            if (t1 < 0)
                t1 = 0;
            int t2 = a_esr + dx;
            if (t2 >= length)
                t2 = (length - 1);

            /* Perform convolution */
            float intensity = 0.0f;
            for (int i = t1; i <= t2; i++) {
                int ind = (int) (NPOINTS / 2)
                        - (((int) d * (i - dx) / scaleLevel) * (-1));
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
     * @param double
     *            x Step of the wavelet
     * @param double
     *            a Window Width of the wavelet
     * @param double
     *            b Offset from the center of the peak
     */
    private double cwtMEXHATreal(double x, double a, double b) {
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
     */
    private void getMzPeaks(MsSpectrumDataPointList inputDataPoints,
            float waveletIntensities[], MsSpectrumDataPointList newDataPoints) {

        final double mzBuffer[] = inputDataPoints.getMzBuffer();
        final float intensityBuffer[] = inputDataPoints.getIntensityBuffer();

        int peakMaxInd = 0;
        int stopInd = waveletIntensities.length - 1;

        for (int ind = 0; ind <= stopInd; ind++) {

            while ((ind <= stopInd) && (waveletIntensities[ind] == 0)) {
                ind++;
            }
            peakMaxInd = ind;
            if (ind >= stopInd) {
                break;
            }

            // While peak is on
            while ((ind <= stopInd) && (waveletIntensities[ind] > 0)) {
                // Check if this is the maximum point of the peak
                if (waveletIntensities[ind] > waveletIntensities[peakMaxInd]) {
                    peakMaxInd = ind;
                }
                ind++;
            }

            if (ind >= stopInd) {
                break;
            }

            if (intensityBuffer[peakMaxInd] > noiseLevel) {
                newDataPoints.add(mzBuffer[peakMaxInd],
                        intensityBuffer[peakMaxInd]);
            }
        }

    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Float getFinishedPercentage() {
        return methodProgress;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public MsScan getResult() {
        return newScan;
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() {
        // This method is too fast to be canceled
    }

}
