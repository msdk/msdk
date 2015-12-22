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

import com.google.common.collect.Range;

import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.util.MsScanUtil;

/**
 * <p>
 * RecursiveCentroidingAlgorithm class.
 * </p>
 */
public class RecursiveCentroidingAlgorithm implements MSDKCentroidingAlgorithm {

    private final @Nonnull DataPointStore dataPointStore;
    private final @Nonnull Range<Double> mzPeakWidthRange;

    private MsScan newScan;

    /**
     * <p>
     * Constructor for RecursiveCentroidingMethod.
     * </p>
     *
     * @param dataPointStore
     *            a
     *            {@link io.github.msdk.datamodel.datapointstore.DataPointStore}
     *            object.
     * @param mzPeakWidthRange
     *            a {@link com.google.common.collect.Range} object.
     */
    public RecursiveCentroidingAlgorithm(@Nonnull DataPointStore dataPointStore,
            @Nonnull Range<Double> mzPeakWidthRange) {
        this.dataPointStore = dataPointStore;
        this.mzPeakWidthRange = mzPeakWidthRange;
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

        // If there are no data points, just return the scan
        if (inputDataPoints.getSize() == 0) {
            newScan.setDataPoints(inputDataPoints);
            return newScan;
        }

        // Run the recursive search algorithm
        recursiveThreshold(inputDataPoints, newDataPoints, 0,
                inputDataPoints.getSize() - 1, 0);

        // Store the new data points
        newScan.setDataPoints(newDataPoints);

        return newScan;

    }

    /**
     * This function searches for maxima from given part of a spectrum
     */
    private int recursiveThreshold(MsSpectrumDataPointList inputDataPoints,
            MsSpectrumDataPointList newDataPoints, int startInd, int stopInd,
            int recuLevel) {

        final double mzBuffer[] = inputDataPoints.getMzBuffer();
        final float intensityBuffer[] = inputDataPoints.getIntensityBuffer();

        int peakStartInd, peakStopInd, peakMaxInd;
        double peakWidthMZ;

        for (int ind = startInd; ind < stopInd; ind++) {

            double localMinimum = Double.MAX_VALUE;

            // Add initial point of the peak
            peakStartInd = ind;
            peakMaxInd = peakStartInd;

            // While peak is on
            while (ind < stopInd) {

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
                            peakStartInd, peakStopInd, recuLevel + 1);
                }

            }

        }

        // return stop index
        return stopInd;

    }

}
