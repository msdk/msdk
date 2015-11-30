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
 * <p>ExactMassCentroidingMethod class.</p>
 *
 */
public class ExactMassCentroidingMethod implements MSDKMethod<MsScan> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final @Nonnull MsScan inputScan;
    private final @Nonnull DataPointStore dataPointStore;
    private final @Nonnull Float noiseLevel;
    private float methodProgress = 0f;
    private MsScan newScan;

    /**
     * <p>Constructor for ExactMassCentroidingMethod.</p>
     *
     * @param inputScan a {@link io.github.msdk.datamodel.rawdata.MsScan} object.
     * @param dataPointStore a {@link io.github.msdk.datamodel.datapointstore.DataPointStore} object.
     * @param noiseLevel a {@link java.lang.Float} object.
     */
    public ExactMassCentroidingMethod(@Nonnull MsScan inputScan,
            @Nonnull DataPointStore dataPointStore, @Nonnull Float noiseLevel) {
        this.inputScan = inputScan;
        this.dataPointStore = dataPointStore;
        this.noiseLevel = noiseLevel;
    }

    /** {@inheritDoc} */
    @Override
    public MsScan execute() throws MSDKException {

        logger.info("Started exact mass centroider on scan #"
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
        final double mzBuffer[] = inputDataPoints.getMzBuffer();
        final float intensityBuffer[] = inputDataPoints.getIntensityBuffer();

        // If there are no data points, just return the scan
        if (inputDataPoints.getSize() == 0) {
            newScan.setDataPoints(inputDataPoints);
            methodProgress = 1f;
            return newScan;
        }

        int localMaximumIndex = 0;
        int rangeBeginning = 0, rangeEnd;
        boolean ascending = true;

        // Iterate through all data points
        for (int i = 0; i < inputDataPoints.getSize() - 1; i++) {

            final boolean nextIsBigger = intensityBuffer[i
                    + 1] > intensityBuffer[i];
            final boolean nextIsZero = intensityBuffer[i + 1] == 0f;
            final boolean currentIsZero = intensityBuffer[i] == 0f;

            // Ignore zero intensity regions
            if (currentIsZero) {
                continue;
            }

            // Add current (non-zero) data point to the current m/z peak
            rangeEnd = i;

            // Check for local maximum
            if (ascending && (!nextIsBigger)) {
                localMaximumIndex = i;
                ascending = false;
                continue;
            }

            // Check for the end of the peak
            if ((!ascending) && (nextIsBigger || nextIsZero)) {

                final int numOfDataPoints = rangeEnd - rangeBeginning;

                // Add the m/z peak if it is above the noise level and has at
                // least 4 data points
                if ((intensityBuffer[localMaximumIndex] > noiseLevel)
                        && (numOfDataPoints >= 4)) {

                    // Calculate the "center" m/z value
                    double calculatedMz = calculateExactMass(mzBuffer,
                            intensityBuffer, rangeBeginning, localMaximumIndex,
                            rangeEnd);
                    float intensity = intensityBuffer[localMaximumIndex];

                    // Add the new data point
                    newDataPoints.add(calculatedMz, intensity);

                }

                // Reset and start with new peak
                ascending = true;
                rangeBeginning = i;
            }

        }

        // Store the new data points
        newScan.setDataPoints(newDataPoints);

        // Finish
        methodProgress = 1f;

        logger.info("Finished exact mass centroider on scan #"
                + inputScan.getScanNumber());

        return newScan;

    }

    /**
     * This method calculates the exact mass of a peak using the FWHM concept
     * and linear regression (y = mx + b).
     * 
     * @param ExactMassDataPoint
     * @return double
     */
    private double calculateExactMass(double mzBuffer[],
            float intensityBuffer[], int rangeBeginning, int localMaximumIndex,
            int rangeEnd) {

        /*
         * According with the FWHM concept, the exact mass of this peak is the
         * half point of FWHM. In order to get the points in the curve that
         * define the FWHM, we use the linear equation.
         * 
         * First we look for, in left side of the peak, 2 data points together
         * that have an intensity less (first data point) and bigger (second
         * data point) than half of total intensity. Then we calculate the slope
         * of the line defined by this two data points. At least, we calculate
         * the point in this line that has an intensity equal to the half of
         * total intensity
         * 
         * We repeat the same process in the right side.
         */

        double xRight = -1, xLeft = -1;
        float halfIntensity = intensityBuffer[localMaximumIndex] / 2f;

        for (int i = rangeBeginning; i < rangeEnd - 1; i++) {

            // Left side of the curve
            if ((intensityBuffer[i] <= halfIntensity) && (i < localMaximumIndex)
                    && (intensityBuffer[i + 1] >= halfIntensity)) {

                // First point with intensity just less than half of total
                // intensity
                double leftY1 = intensityBuffer[i];
                double leftX1 = mzBuffer[i];

                // Second point with intensity just bigger than half of total
                // intensity
                double leftY2 = intensityBuffer[i + 1];
                double leftX2 = mzBuffer[i + 1];

                // We calculate the slope with formula m = Y1 - Y2 / X1 - X2
                double mLeft = (leftY1 - leftY2) / (leftX1 - leftX2);

                // We calculate the desired point (at half intensity) with the
                // linear equation
                // X = X1 + [(Y - Y1) / m ], where Y = half of total intensity
                xLeft = leftX1 + (((halfIntensity) - leftY1) / mLeft);
                continue;
            }

            // Right side of the curve
            if ((intensityBuffer[i] >= halfIntensity) && (i > localMaximumIndex)
                    && (intensityBuffer[i + 1] <= halfIntensity)) {

                // First point with intensity just bigger than half of total
                // intensity
                double rightY1 = intensityBuffer[i];
                double rightX1 = mzBuffer[i];

                // Second point with intensity just less than half of total
                // intensity
                double rightY2 = intensityBuffer[i + 1];
                double rightX2 = mzBuffer[i + 1];

                // We calculate the slope with formula m = Y1 - Y2 / X1 - X2
                double mRight = (rightY1 - rightY2) / (rightX1 - rightX2);

                // We calculate the desired point (at half intensity) with the
                // linear equation
                // X = X1 + [(Y - Y1) / m ], where Y = half of total intensity
                xRight = rightX1 + (((halfIntensity) - rightY1) / mRight);
                break;
            }
        }

        // We verify the values to confirm we find the desired points. If not we
        // return the same mass value.
        if ((xRight == -1) || (xLeft == -1))
            return mzBuffer[localMaximumIndex];

        // The center of left and right points is the exact mass of our peak.
        double exactMass = (xLeft + xRight) / 2;

        return exactMass;
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
