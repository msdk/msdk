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

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.util.MsScanUtil;

/**
 * <p>RecursiveCentroidingMethod class.</p>
 *
 */
public class RecursiveCentroidingMethod implements MSDKMethod<MsScan> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final @Nonnull MsScan inputScan;
    private final @Nonnull DataPointStore dataPointStore;
    private final @Nonnull Float noiseLevel;
    private final @Nonnull Range<Double> mzPeakWidthRange;

    private float methodProgress = 0f;
    private MsScan newScan;

    /**
     * <p>Constructor for RecursiveCentroidingMethod.</p>
     *
     * @param inputScan a {@link io.github.msdk.datamodel.rawdata.MsScan} object.
     * @param dataPointStore a {@link io.github.msdk.datamodel.datapointstore.DataPointStore} object.
     * @param noiseLevel a {@link java.lang.Float} object.
     * @param mzPeakWidthRange a {@link com.google.common.collect.Range} object.
     */
    public RecursiveCentroidingMethod(@Nonnull MsScan inputScan,
            @Nonnull DataPointStore dataPointStore, @Nonnull Float noiseLevel,
            @Nonnull Range<Double> mzPeakWidthRange) {
        this.inputScan = inputScan;
        this.dataPointStore = dataPointStore;
        this.noiseLevel = noiseLevel;
        this.mzPeakWidthRange = mzPeakWidthRange;
    }

    /** {@inheritDoc} */
    @Override
    public MsScan execute() throws MSDKException {

        logger.info("Started recursive centroider on scan #"
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

        // Run the recursive search algorithm
        recursiveThreshold(inputDataPoints, newDataPoints, 0,
                inputDataPoints.getSize() - 1, noiseLevel, 0);

        // Store the new data points
        newScan.setDataPoints(newDataPoints);

        // Finish
        methodProgress = 1f;

        logger.info("Finished recursive centroider on scan #"
                + inputScan.getScanNumber());

        return newScan;

    }

    /**
     * This function searches for maxima from given part of a spectrum
     */
    private int recursiveThreshold(MsSpectrumDataPointList inputDataPoints,
            MsSpectrumDataPointList newDataPoints, int startInd, int stopInd,
            double currentNoiseLevel, int recuLevel) {

        final double mzBuffer[] = inputDataPoints.getMzBuffer();
        final float intensityBuffer[] = inputDataPoints.getIntensityBuffer();

        int peakStartInd, peakStopInd, peakMaxInd;
        double peakWidthMZ;

        for (int ind = startInd; ind < stopInd; ind++) {

            double localMinimum = Double.MAX_VALUE;

            // Ignore intensities below curentNoiseLevel
            if (intensityBuffer[ind] <= currentNoiseLevel)
                continue;

            // Add initial point of the peak
            peakStartInd = ind;
            peakMaxInd = peakStartInd;

            // While peak is on
            while ((ind < stopInd)
                    && (intensityBuffer[ind] > currentNoiseLevel)) {

                final boolean isLocalMinimum = (intensityBuffer[ind
                        - 1] > intensityBuffer[ind])
                        && (intensityBuffer[ind] < intensityBuffer[ind + 1]);

                // Check if this is the minimum point of the peak
                if (isLocalMinimum && (intensityBuffer[ind] < localMinimum))
                    localMinimum = intensityBuffer[ind];

                // Check if this is the maximum point of the peak
                if (intensityBuffer[ind] > intensityBuffer[peakMaxInd])
                    peakMaxInd = ind;

                ind++;
            }

            // Add ending point of the peak
            peakStopInd = ind;

            peakWidthMZ = mzBuffer[peakStopInd] - mzBuffer[peakStartInd];

            // Verify width of the peak
            if (mzPeakWidthRange.contains(peakWidthMZ)) {

                // Declare a new MzPeak with intensity equal to max intensity
                // data point
                newDataPoints.add(mzBuffer[peakMaxInd],
                        intensityBuffer[peakMaxInd]);

                if (recuLevel > 0) {
                    // return stop index and beginning of the next peak
                    return ind;
                }
            }

            // If the peak is still too big applies the same method until find a
            // peak of the right size
            if (peakWidthMZ > mzPeakWidthRange.upperEndpoint()) {
                if (localMinimum < Double.MAX_VALUE) {
                    ind = recursiveThreshold(inputDataPoints, newDataPoints,
                            peakStartInd, peakStopInd, localMinimum,
                            recuLevel + 1);
                }

            }

        }

        // return stop index
        return stopInd;

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
