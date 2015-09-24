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
    private boolean canceled = false;

    public SpectrumTypeDetectionMethod(@Nonnull MsSpectrum msSpectrum) {
        this.dataPoints = MSDKObjectBuilder.getMsSpectrumDataPointList();
        msSpectrum.getDataPoints(dataPoints);
    }

    public SpectrumTypeDetectionMethod(
            @Nonnull MsSpectrumDataPointList dataPoints) {
        this.dataPoints = dataPoints;
    }

    @Override
    public Float getFinishedPercentage() {
        return finishedPercentage;
    }

    @Override
    public MsSpectrumType execute() throws MSDKException {
        result = detectSpectrumType(dataPoints);
        finishedPercentage = 1f;
        return result;
    }

    @Override
    public MsSpectrumType getResult() {
        return result;
    }

    @Override
    public void cancel() {
        this.canceled = true;
    }

    private MsSpectrumType detectSpectrumType(
            @Nonnull MsSpectrumDataPointList dataPoints) {

        // If the spectrum has less than 100 data points, it should be
        // centroided.
        if (dataPoints.getSize() < 100)
            return MsSpectrumType.CENTROIDED;

        // Go through the data points and find the highest one
        float maxIntensity = 0f;
        int topDataPointIndex = 0;

        final double mzValues[] = dataPoints.getMzBuffer();
        final float intensityValues[] = dataPoints.getIntensityBuffer();

        for (int i = 0; i < dataPoints.getSize(); i++) {

            // If the spectrum contains data points of zero intensity, it should
            // be in profile mode
            if (intensityValues[i] == 0.0) {
                return MsSpectrumType.PROFILE;
            }

            // Let's ignore the first and the last data point, because
            // that would complicate our following checks
            if ((i == 0) || (i == dataPoints.getSize() - 1))
                continue;

            // Update the maxDataPointIndex accordingly
            if (intensityValues[i] > maxIntensity) {
                maxIntensity = intensityValues[i];
                topDataPointIndex = i;
            }
        }

        // Check if canceled
        if (canceled)
            return null;

        finishedPercentage = 0.3f;

        // Now we have the index of the top data point (except the first and
        // the last). We also know the spectrum has at least 5 data points.
        assert topDataPointIndex > 0;
        assert topDataPointIndex < dataPoints.getSize() - 1;
        assert dataPoints.getSize() >= 5;

        // Calculate the m/z difference between the top data point and the
        // previous one
        final double topMzDifference = Math.abs(
                mzValues[topDataPointIndex] - mzValues[topDataPointIndex - 1]);

        // For 5 data points around the top one (with the top one in the
        // center), we check the distribution of the m/z values. If the spectrum
        // is continuous (thresholded), the distances between data points should
        // be more or less constant. On the other hand, centroided spectra
        // usually have unevenly distributed data points.
        for (int i = topDataPointIndex - 2; i < topDataPointIndex + 2; i++) {

            // Check if the index is within acceptable range
            if ((i < 1) || (i > dataPoints.getSize() - 1))
                continue;

            final double currentMzDifference = Math
                    .abs(mzValues[i] - mzValues[i - 1]);

            // Check if the m/z distance of the pair of consecutive data points
            // falls within 25% tolerance of the distance of the top data point
            // and its neighbor. If not, the spectrum should be centroided.
            if ((currentMzDifference < (0.8 * topMzDifference))
                    || (currentMzDifference > (1.25 * topMzDifference))) {
                return MsSpectrumType.CENTROIDED;
            }

        }

        // Check if canceled
        if (canceled)
            return null;

        finishedPercentage = 0.7f;

        // Finally, we check the data points on the left and on the right of the
        // top one. If the spectrum is continuous (thresholded), their intensity
        // should decrease gradually from the top data point. Let's check if
        // their intensity is above 1/3 of the top data point. If not, the
        // spectrum should be centroided.
        final double thirdMaxIntensity = maxIntensity / 3;
        final double leftDataPointIntensity = intensityValues[topDataPointIndex
                - 1];
        final double rightDataPointIntensity = intensityValues[topDataPointIndex
                + 1];
        if ((leftDataPointIntensity < thirdMaxIntensity)
                || (rightDataPointIntensity < thirdMaxIntensity))
            return MsSpectrumType.CENTROIDED;

        // Check if canceled
        if (canceled)
            return null;

        // If we could not find any sign that the spectrum is centroided, we
        // conclude it should be thresholded.
        return MsSpectrumType.THRESHOLDED;
    }

}
