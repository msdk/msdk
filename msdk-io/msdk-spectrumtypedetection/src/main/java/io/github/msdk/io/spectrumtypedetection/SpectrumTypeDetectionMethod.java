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

package io.github.msdk.io.spectrumtypedetection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrum;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;

/**
 * Auto-detection of spectrum type from data points. Determines if the spectrum
 * represented by given array of data points is centroided or continuous
 * (profile or thresholded). Profile spectra are easy to detect, because they
 * contain zero-intensity data points. However, distinguishing centroided from
 * thresholded spectra is not trivial. We use multiple checks for that purpose,
 * as described in the code comments.
 */
public class SpectrumTypeDetectionMethod implements MSDKMethod<MsSpectrumType> {

    private @Nonnull MsSpectrumDataPointList dataPoints;
    private @Nullable MsSpectrumType result = null;
    private Float finishedPercentage = null;

    /**
     * <p>Constructor for SpectrumTypeDetectionMethod.</p>
     *
     * @param msSpectrum a {@link io.github.msdk.datamodel.msspectra.MsSpectrum} object.
     */
    public SpectrumTypeDetectionMethod(@Nonnull MsSpectrum msSpectrum) {
        this.dataPoints = MSDKObjectBuilder.getMsSpectrumDataPointList();
        msSpectrum.getDataPoints(dataPoints);
    }

    /**
     * <p>Constructor for SpectrumTypeDetectionMethod.</p>
     *
     * @param dataPoints a {@link io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList} object.
     */
    public SpectrumTypeDetectionMethod(
            @Nonnull MsSpectrumDataPointList dataPoints) {
        this.dataPoints = dataPoints;
    }

    /** {@inheritDoc} */
    @Override
    public Float getFinishedPercentage() {
        return finishedPercentage;
    }

    /** {@inheritDoc} */
    @Override
    public MsSpectrumType execute() throws MSDKException {
        result = detectSpectrumType(dataPoints);
        finishedPercentage = 1f;
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public MsSpectrumType getResult() {
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() {
        // this method is too fast to be canceled
    }

    /**
     * <p>detectSpectrumType.</p>
     *
     * @param dataPoints a {@link io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList} object.
     * @return a {@link io.github.msdk.datamodel.msspectra.MsSpectrumType} object.
     */
    public static MsSpectrumType detectSpectrumType(
            @Nonnull MsSpectrumDataPointList dataPoints) {

        // If the spectrum has less than 5 data points, it should be
        // centroided.
        if (dataPoints.getSize() < 5)
            return MsSpectrumType.CENTROIDED;

        int basePeakIndex = 0;
        boolean hasZeroDataPoint = false;

        final double mzValues[] = dataPoints.getMzBuffer();
        final float intensityValues[] = dataPoints.getIntensityBuffer();

        final double scanMzSpan = mzValues[mzValues.length - 1] - mzValues[0];

        // Go through the data points and find the highest one
        for (int i = 0; i < dataPoints.getSize(); i++) {

            // Update the topDataPointIndex accordingly
            if (intensityValues[i] > intensityValues[basePeakIndex])
                basePeakIndex = i;

            if (intensityValues[i] == 0.0)
                hasZeroDataPoint = true;
        }

        // Find the all data points around the base peak that have intensity
        // above half maximum
        final double halfIntensity = intensityValues[basePeakIndex] / 2.0;
        int leftIndex = basePeakIndex;
        while ((leftIndex > 0)
                && intensityValues[leftIndex - 1] > halfIntensity) {
            leftIndex--;
        }
        int rightIndex = basePeakIndex;
        while ((rightIndex < intensityValues.length - 1)
                && intensityValues[rightIndex + 1] > halfIntensity) {
            rightIndex++;
        }
        final double mainPeakMzSpan = mzValues[rightIndex]
                - mzValues[leftIndex];
        final int mainPeakDataPointCount = rightIndex - leftIndex + 1;

        // If the main peak has less than 3 data points above half intensity, it
        // indicates a centroid spectrum. Further, if the m/z span of the main
        // peak is more than 0.1% of the scan m/z range, it also indicates a
        // centroid spectrum. These criteria are empirical and probably not
        // bulletproof. However, it works for all the test cases we have.
        if ((mainPeakDataPointCount < 3)
                || (mainPeakMzSpan > (scanMzSpan / 1000.0)))
            return MsSpectrumType.CENTROIDED;
        else {
            if (hasZeroDataPoint)
                return MsSpectrumType.PROFILE;
            else
                return MsSpectrumType.THRESHOLDED;
        }

    }

}
