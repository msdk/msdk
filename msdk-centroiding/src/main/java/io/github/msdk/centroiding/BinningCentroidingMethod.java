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
 * <p>BinningCentroidingMethod class.</p>
 *
 */
public class BinningCentroidingMethod implements MSDKMethod<MsScan> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final @Nonnull MsScan inputScan;
    private final @Nonnull DataPointStore dataPointStore;
    private final @Nonnull Double binSize;

    private float methodProgress = 0f;
    private MsScan newScan;

    /**
     * <p>Constructor for BinningCentroidingMethod.</p>
     *
     * @param inputScan a {@link io.github.msdk.datamodel.rawdata.MsScan} object.
     * @param dataPointStore a {@link io.github.msdk.datamodel.datapointstore.DataPointStore} object.
     * @param binSize a {@link java.lang.Double} object.
     */
    public BinningCentroidingMethod(@Nonnull MsScan inputScan,
            @Nonnull DataPointStore dataPointStore, @Nonnull Double binSize) {
        this.inputScan = inputScan;
        this.dataPointStore = dataPointStore;
        this.binSize = binSize;
    }

    /** {@inheritDoc} */
    @Override
    public MsScan execute() throws MSDKException {

        logger.info("Started binning centroider on scan #"
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

        double currentBinMzStart = mzBuffer[0];
        float currentBinIntensity = 0f;

        // Iterate through all data points
        for (int i = 0; i < inputDataPoints.getSize(); i++) {

            if (mzBuffer[i] < (currentBinMzStart + binSize)) {
                currentBinIntensity += intensityBuffer[i];
                continue;
            } else {
                // Add the new data point
                double currentBinMzValue = currentBinMzStart + (binSize / 2);
                newDataPoints.add(currentBinMzValue, currentBinIntensity);
            }

        }

        // Store the new data points
        newScan.setDataPoints(newDataPoints);

        // Finish
        methodProgress = 1f;

        logger.info("Finished binning centroider on scan #"
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
