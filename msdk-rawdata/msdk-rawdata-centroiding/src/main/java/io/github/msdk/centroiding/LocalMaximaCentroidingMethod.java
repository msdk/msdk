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
 * <p>LocalMaximaCentroidingMethod class.</p>
 *
 */
public class LocalMaximaCentroidingMethod implements MSDKMethod<MsScan> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final @Nonnull MsScan inputScan;
    private final @Nonnull DataPointStore dataPointStore;
    private final @Nonnull Float noiseLevel;
    private float methodProgress = 0f;
    private MsScan newScan;

    /**
     * <p>Constructor for LocalMaximaCentroidingMethod.</p>
     *
     * @param inputScan a {@link io.github.msdk.datamodel.rawdata.MsScan} object.
     * @param dataPointStore a {@link io.github.msdk.datamodel.datapointstore.DataPointStore} object.
     * @param noiseLevel a {@link java.lang.Float} object.
     */
    public LocalMaximaCentroidingMethod(@Nonnull MsScan inputScan,
            @Nonnull DataPointStore dataPointStore, @Nonnull Float noiseLevel) {
        this.inputScan = inputScan;
        this.dataPointStore = dataPointStore;
        this.noiseLevel = noiseLevel;
    }

    /** {@inheritDoc} */
    @Override
    public MsScan execute() throws MSDKException {

        logger.info("Started local maxima centroider on scan #"
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

                    // Add the new data point
                    newDataPoints.add(mzBuffer[localMaximumIndex],
                            intensityBuffer[localMaximumIndex]);

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

        logger.info("Finished local maxima centroider on scan #"
                + inputScan.getScanNumber());

        return newScan;

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
