/* 
 * (C) Copyright 2015-2016 by MSDK Development Team
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

package io.github.msdk.spectra.spectrumtypedetection;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import com.google.common.math.Quantiles;
import com.google.common.primitives.Doubles;

import io.github.msdk.datamodel.msspectra.MsSpectrum;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;

/**
 * Auto-detection of spectrum type from data points. Determines if the spectrum
 * represented by given array of data points is centroided or continuous
 * (profile or thresholded). Profile spectra are easy to detect, because they
 * contain zero-intensity data points. However, distinguishing centroided from
 * thresholded spectra is not trivial. We use multiple checks for that purpose,
 * as described in the code comments.
 */
public class SpectrumTypeDetectionAlgorithm {

    /**
     * <p>
     * detectSpectrumType.
     * </p>
     *
     * @param msSpectrum
     *            a {@link io.github.msdk.datamodel.msspectra.MsSpectrum}
     *            object.
     * @return a {@link io.github.msdk.datamodel.msspectra.MsSpectrumType}
     *         object.
     */
    public static MsSpectrumType detectSpectrumType(
            @Nonnull MsSpectrum msSpectrum) {
        double mzValues[] = msSpectrum.getMzValues();
        float intensityValues[] = msSpectrum.getIntensityValues();
        Integer size = msSpectrum.getNumberOfDataPoints();
        return detectSpectrumType(mzValues, intensityValues, size);
    }

    public static @Nonnull MsSpectrumType detectSpectrumType(
            @Nonnull double mzValues[], @Nonnull float intensityValues[],
            @Nonnull Integer size) {
        
        // If the spectrum has less than 5 data points, it should be
        // centroided.
        if (size < 5)
            return MsSpectrumType.CENTROIDED;
        
        // List<Float> intensityList = Floats.asList(intensityValues).subList(0, size - 1);
        // double percentile90 = Quantiles.percentiles().index(90).compute(intensityList);

        List<Double> diffs = new ArrayList<>();
        for (int i = 1; i < size; i++) {
            if (intensityValues[i] == 0f) continue;
            double diff = mzValues[i] - mzValues[i-1];
            diffs.add(diff);
        }
        DescriptiveStatistics ds = new DescriptiveStatistics();
        for (Double d : diffs) ds.addValue(d);

        double dev = ds.getStandardDeviation();
        double err = dev / ds.getMean();
        System.out.println("stderr " + err);
        // double diffPercentile25 = Quantiles.percentiles().index(25).compute(diffs);
        // System.out.println("diffperc25 " + diffPercentile25);
        if (err >= 0.025) return MsSpectrumType.CENTROIDED; else return MsSpectrumType.PROFILE;
        
    }
    /**
     * <p>
     * detectSpectrumType.
     * </p>
     *
     * @return a {@link io.github.msdk.datamodel.msspectra.MsSpectrumType}
     *         object.
     * @param mzValues
     *            an array of double.
     * @param intensityValues
     *            an array of float.
     * @param size
     *            a {@link java.lang.Integer} object.
     */
    public static @Nonnull MsSpectrumType detectSpectrumTypeOld(
            @Nonnull double mzValues[], @Nonnull float intensityValues[],
            @Nonnull Integer size) {

        // If the spectrum has less than 5 data points, it should be
        // centroided.
        if (size < 5)
            return MsSpectrumType.CENTROIDED;

        int basePeakIndex = 0;
        boolean hasZeroDataPoint = false;

        final double scanMzSpan = mzValues[size - 1] - mzValues[0];

        // Go through the data points and find the highest one
        for (int i = 0; i < size; i++) {

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
        while ((rightIndex < size - 1)
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
