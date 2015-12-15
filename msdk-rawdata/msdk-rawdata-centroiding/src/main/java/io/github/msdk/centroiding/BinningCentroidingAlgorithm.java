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

import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.util.MsScanUtil;

/**
 * <p>
 * BinningCentroidingAlgorithm class.
 * </p>
 */
public class BinningCentroidingAlgorithm implements MSDKCentroidingAlgorithm {

    private final @Nonnull DataPointStore dataPointStore;
    private final @Nonnull Double binSize;

    private MsScan newScan;

    /**
     * <p>
     * Constructor for BinningCentroidingMethod.
     * </p>
     *
     * @param dataPointStore
     *            a
     *            {@link io.github.msdk.datamodel.datapointstore.DataPointStore}
     *            object.
     * @param binSize
     *            a {@link java.lang.Double} object.
     */
    public BinningCentroidingAlgorithm(@Nonnull DataPointStore dataPointStore,
            @Nonnull Double binSize) {
        this.dataPointStore = dataPointStore;
        this.binSize = binSize;
    }

    /** {@inheritDoc} */
    @Override
    public @Nonnull MsScan centroidScan(@Nonnull MsScan inputScan) {

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
            return newScan;
        }

        double currentBinMzStart = mzBuffer[0];
        float currentBinIntensity = 0f;

        // Iterate through all data points
        for (int i = 0; i < inputDataPoints.getSize(); i++) {

            if (mzBuffer[i] < (currentBinMzStart + binSize)) {
                currentBinIntensity += intensityBuffer[i];
                continue;
            }

            // Add the new data point
            final double currentBinMzValue = currentBinMzStart + (binSize / 2);
            newDataPoints.add(currentBinMzValue, currentBinIntensity);
            currentBinMzStart += binSize;
            currentBinIntensity = 0f;

        }

        // Store the new data points
        newScan.setDataPoints(newDataPoints);

        return newScan;

    }

}
