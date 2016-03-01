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

package io.github.msdk.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

import io.github.msdk.datamodel.rawdata.ChromatographyInfo;

/**
 * <p>
 * ChromatogramUtil class.
 * </p>
 */
public class ChromatogramUtil {

    /**
     * Returns the range of ChromatographyInfo of all data points in this
     * feature.
     *
     * @return a {@link com.google.common.collect.Range} object.
     * @param rtValues
     *            an array of
     *            {@link io.github.msdk.datamodel.rawdata.ChromatographyInfo}
     *            objects.
     * @param size
     *            a {@link java.lang.Integer} object.
     */
    @SuppressWarnings("null")
    @Nonnull
    public static Range<ChromatographyInfo> getDataPointsChromatographyRange(
            @Nonnull ChromatographyInfo rtValues[], @Nonnull Integer size) {

        // Parameter check
        Preconditions.checkNotNull(rtValues);
        Preconditions.checkNotNull(size);
        Preconditions.checkPositionIndex(size, rtValues.length);

        final Range<ChromatographyInfo> chromatographyRange = Range
                .closed(rtValues[0], rtValues[size - 1]);
        return chromatographyRange;
    }

    /**
     * Returns the range of intensity values of all data points in this
     * chromatogram.
     *
     * @return a {@link com.google.common.collect.Range} object.
     * @param intensityValues
     *            an array of {@link java.lang.Float} objects.
     * @param size
     *            a {@link java.lang.Integer} object.
     */
    public static @Nullable Range<Float> getDataPointsIntensityRange(
            @Nonnull Float intensityValues[], @Nonnull Integer size) {

        // Parameter check
        Preconditions.checkNotNull(intensityValues);
        Preconditions.checkNotNull(size);
        Preconditions.checkPositionIndex(size, intensityValues.length);

        if (size == 0)
            return null;

        float lower = intensityValues[0];
        float upper = intensityValues[0];
        for (int i = 0; i < size; i++) {
            if (intensityValues[i] < lower)
                lower = intensityValues[i];
            if (intensityValues[i] > upper)
                upper = intensityValues[i];
        }
        final Range<Float> intensityRange = Range.closed(lower, upper);
        return intensityRange;
    }

    /**
     * Calculates the m/z value of the chromatogram based on the list of m/z
     * values
     */
    public enum CalculationMethod {
        allAverage, allMedian, fwhmAverage, fwhmMedian
    }

    /**
     * <p>
     * calculateMz.
     * </p>
     *
     * @param intensityValues
     *            an array of float.
     * @param mzValues
     *            an array of double.
     * @param method
     *            a
     *            {@link io.github.msdk.util.ChromatogramUtil.CalculationMethod}
     *            object.
     * @return a {@link java.lang.Double} object.
     * @param size
     *            a {@link java.lang.Integer} object.
     */
    public static @Nullable Double calculateMz(@Nonnull double[] mzValues,
            @Nonnull float[] intensityValues, @Nonnull Integer size,
            @Nonnull CalculationMethod method) {

        // Parameter check
        Preconditions.checkNotNull(mzValues);
        Preconditions.checkNotNull(intensityValues);
        Preconditions.checkNotNull(size);
        Preconditions.checkPositionIndex(size, mzValues.length);
        Preconditions.checkPositionIndex(size, intensityValues.length);

        if (size == 0)
            return null;

        double newMz = 0;
        double sum = 0;
        int values = 0;

        switch (method) {
        case allAverage:
            for (int i = 0; i < size; i++) {
                if (mzValues[i] > 0) {
                    values++;
                    sum = sum + mzValues[i];
                }
            }
            newMz = sum / values;
            break;
        case allMedian:
            double index = Math.floor(size / 2);
            if (mzValues.length % 2 == 0) { // even
                sum = mzValues[(int) index] + mzValues[(int) index + 1];
                newMz = sum / 2;
            } else { // odd
                newMz = mzValues[(int) index];
            }
            break;
        case fwhmAverage:
            // Find the maximum intensity
            float max = intensityValues[0];
            for (int i = 1; i < size; i++) {
                if (intensityValues[i] > max) {
                    max = intensityValues[i];
                }
            }

            // Calculate m/z
            for (int i = 0; i < intensityValues.length; i++) {
                if (intensityValues[i] > max / 2) {
                    values++;
                    sum = sum + mzValues[i];
                }
            }
            newMz = sum / values;
            break;
        case fwhmMedian:
            // TODO
        }

        return newMz;
    }

    /**
     * Returns the retention time of this feature.
     *
     * @return a float.
     * @param rtValues
     *            an array of
     *            {@link io.github.msdk.datamodel.rawdata.ChromatographyInfo}
     *            objects.
     * @param intensityValues
     *            an array of float.
     * @param size
     *            a {@link java.lang.Integer} object.
     */
    public static @Nullable Float getRt(@Nonnull ChromatographyInfo rtValues[],
            @Nonnull float[] intensityValues, @Nonnull Integer size) {

        // Parameter check
        Preconditions.checkNotNull(rtValues);
        Preconditions.checkNotNull(intensityValues);
        Preconditions.checkNotNull(size);
        Preconditions.checkPositionIndex(size, rtValues.length);
        Preconditions.checkPositionIndex(size, intensityValues.length);

        if (size == 0)
            return null;

        // Find the maximum intensity index
        int max = 0;
        for (int i = 1; i < size; i++) {
            if (intensityValues[i] > intensityValues[max]) {
                max = i;
            }
        }

        return rtValues[max].getRetentionTime();
    }

    /**
     * Returns the start retention time of this feature.
     *
     * @return a Float.
     * @param rtValues
     *            an array of
     *            {@link io.github.msdk.datamodel.rawdata.ChromatographyInfo}
     *            objects.
     * @param size
     *            a {@link java.lang.Integer} object.
     */
    public static @Nullable Float getRtStart(
            @Nonnull ChromatographyInfo rtValues[], @Nonnull Integer size) {

        // Parameter check
        Preconditions.checkNotNull(rtValues);
        Preconditions.checkNotNull(size);
        Preconditions.checkPositionIndex(size, rtValues.length);

        if (size == 0)
            return null;

        return rtValues[0].getRetentionTime();
    }

    /**
     * Returns the end retention time of this feature.
     *
     * @return a Float.
     * @param rtValues
     *            an array of
     *            {@link io.github.msdk.datamodel.rawdata.ChromatographyInfo}
     *            objects.
     * @param size
     *            a {@link java.lang.Integer} object.
     */
    public static Float getRtEnd(@Nonnull ChromatographyInfo rtValues[],
            @Nonnull Integer size) {

        // Parameter check
        Preconditions.checkNotNull(rtValues);
        Preconditions.checkNotNull(size);
        Preconditions.checkPositionIndex(size, rtValues.length);

        if (size == 0)
            return null;

        return rtValues[size - 1].getRetentionTime();

    }

    /**
     * Returns the duration of this feature.
     *
     * @return a float.
     * @param rtValues
     *            an array of
     *            {@link io.github.msdk.datamodel.rawdata.ChromatographyInfo}
     *            objects.
     * @param size
     *            a {@link java.lang.Integer} object.
     */
    public static @Nullable Float getDuration(
            @Nonnull ChromatographyInfo rtValues[], @Nonnull Integer size) {

        // Parameter check
        Preconditions.checkNotNull(rtValues);
        Preconditions.checkNotNull(size);
        Preconditions.checkPositionIndex(size, rtValues.length);

        if (size == 0)
            return null;

        Float start = getRtStart(rtValues, size);
        Float end = getRtEnd(rtValues, size);

        if (start == null || end == null)
            return null;

        return end - start;
    }

    /**
     * Returns the maximum height of this chromatogram, or null if size == 0.
     *
     * @return a double.
     * @param intensityValues
     *            an array of float.
     * @param size
     *            a {@link java.lang.Integer} object.
     */
    public static @Nullable Float getMaxHeight(@Nonnull float[] intensityValues,
            @Nonnull Integer size) {

        // Parameter check
        Preconditions.checkNotNull(intensityValues);
        Preconditions.checkNotNull(size);
        Preconditions.checkPositionIndex(size, intensityValues.length);

        if (size == 0)
            return null;

        // Find the maximum intensity index
        int max = 0;
        for (int i = 1; i < size; i++) {
            if (intensityValues[i] > intensityValues[max]) {
                max = i;
            }
        }

        return intensityValues[max];

    }

    /**
     * Returns the area of this feature.
     *
     * @return a double.
     * @param rtValues
     *            an array of
     *            {@link io.github.msdk.datamodel.rawdata.ChromatographyInfo}
     *            objects.
     * @param intensityValues
     *            an array of float.
     * @param size
     *            a {@link java.lang.Integer} object.
     */
    public static @Nullable Double getArea(
            @Nonnull ChromatographyInfo rtValues[],
            @Nonnull float[] intensityValues, @Nonnull Integer size) {

        // Parameter check
        Preconditions.checkNotNull(rtValues);
        Preconditions.checkNotNull(intensityValues);
        Preconditions.checkNotNull(size);
        Preconditions.checkPositionIndex(size, rtValues.length);
        Preconditions.checkPositionIndex(size, intensityValues.length);

        if (size == 0)
            return null;

        double area = 0, rtDifference = 0, intensityStart = 0, intensityEnd = 0;
        for (int i = 0; i < size - 1; i++) {
            rtDifference = rtValues[i + 1].getRetentionTime()
                    - rtValues[i].getRetentionTime();
            intensityStart = intensityValues[i];
            intensityEnd = intensityValues[i + 1];
            area += (rtDifference * (intensityStart + intensityEnd) / 2);
        }
        return area;
    }

    /**
     * Returns the full width at half maximum (FWHM) of this feature.
     *
     * @return a {@link java.lang.Double} object.
     * @param rtValues
     *            an array of
     *            {@link io.github.msdk.datamodel.rawdata.ChromatographyInfo}
     *            objects.
     * @param intensityValues
     *            an array of float.
     * @param size
     *            a {@link java.lang.Integer} object.
     */
    public static @Nullable Double getFwhm(
            @Nonnull ChromatographyInfo rtValues[],
            @Nonnull float[] intensityValues, @Nonnull Integer size) {

        // Parameter check
        Preconditions.checkNotNull(rtValues);
        Preconditions.checkNotNull(intensityValues);
        Preconditions.checkNotNull(size);
        Preconditions.checkPositionIndex(size, rtValues.length);
        Preconditions.checkPositionIndex(size, intensityValues.length);

        if (size == 0)
            return null;

        Float height = getMaxHeight(intensityValues, size);
        Float rt = getRt(rtValues, intensityValues, size);

        if (height == null || rt == null)
            return null;

        double rtVals[] = findRTs(height / 2, rt, rtValues, intensityValues,
                size);
        Double fwhm = rtVals[1] - rtVals[0];
        if (fwhm < 0) {
            fwhm = null;
        }
        return fwhm;
    }

    /**
     * Returns the tailing factor of this feature.
     *
     * @return a {@link java.lang.Double} object.
     * @param rtValues
     *            an array of
     *            {@link io.github.msdk.datamodel.rawdata.ChromatographyInfo}
     *            objects.
     * @param intensityValues
     *            an array of float.
     * @param size
     *            a {@link java.lang.Integer} object.
     */
    public static @Nullable Double getTailingFactor(
            @Nonnull ChromatographyInfo rtValues[],
            @Nonnull float[] intensityValues, @Nonnull Integer size) {

        // Parameter check
        Preconditions.checkNotNull(rtValues);
        Preconditions.checkNotNull(intensityValues);
        Preconditions.checkNotNull(size);
        Preconditions.checkPositionIndex(size, rtValues.length);
        Preconditions.checkPositionIndex(size, intensityValues.length);

        if (size == 0)
            return null;

        Float height = getMaxHeight(intensityValues, size);
        Float rt = getRt(rtValues, intensityValues, size);

        if (height == null || rt == null)
            return null;

        double rtVals[] = findRTs(height * 0.05, rt, rtValues, intensityValues,
                size);

        if (rtVals[1] == 0)
            rtVals[1] = getRtEnd(rtValues, size);

        Double tf = (rtVals[1] - rtVals[0]) / (2 * (rt - rtVals[0]));
        if (tf < 0) {
            tf = null;
        }
        return tf;
    }

    /**
     * Returns the asymmetry factor of this feature.
     *
     * @return a {@link java.lang.Double} object.
     * @param rtValues
     *            an array of
     *            {@link io.github.msdk.datamodel.rawdata.ChromatographyInfo}
     *            objects.
     * @param intensityValues
     *            an array of float.
     * @param size
     *            a {@link java.lang.Integer} object.
     */
    public static @Nullable Double getAsymmetryFactor(
            @Nonnull ChromatographyInfo rtValues[],
            @Nonnull float[] intensityValues, @Nonnull Integer size) {

        // Parameter check
        Preconditions.checkNotNull(rtValues);
        Preconditions.checkNotNull(intensityValues);
        Preconditions.checkNotNull(size);
        Preconditions.checkPositionIndex(size, rtValues.length);
        Preconditions.checkPositionIndex(size, intensityValues.length);

        if (size == 0)
            return null;

        Float height = getMaxHeight(intensityValues, size);
        Float rt = getRt(rtValues, intensityValues, size);

        if (height == null || rt == null)
            return null;

        double rtValues3[] = findRTs(height * 0.1, rt, rtValues,
                intensityValues, size);
        Double af = (rtValues3[1] - rt) / (rt - rtValues3[0]);
        if (af < 0) {
            af = null;
        }
        return af;
    }

    private static double[] findRTs(double intensity, float rt,
            @Nonnull ChromatographyInfo rtValues[],
            @Nonnull float[] intensityValues, @Nonnull Integer size) {

        // Parameter check
        Preconditions.checkNotNull(rtValues);
        Preconditions.checkNotNull(intensityValues);
        Preconditions.checkNotNull(size);
        Preconditions.checkPositionIndex(size, rtValues.length);
        Preconditions.checkPositionIndex(size, intensityValues.length);

        double lastDiff1 = intensity, lastDiff2 = intensity, currentDiff;
        double x1 = 0, x2 = 0, x3 = 0, x4 = 0, y1 = 0, y2 = 0, y3 = 0, y4 = 0,
                currentRT;

        // Find the data points closet to input intensity on both side of the
        // apex
        for (int i = 1; i < size - 1; i++) {

            currentDiff = Math.abs(intensity - intensityValues[i]);
            currentRT = rtValues[i].getRetentionTime();

            if (currentDiff < lastDiff1 & currentDiff > 0 & currentRT <= rt) {
                x1 = rtValues[i].getRetentionTime();
                y1 = intensityValues[i];
                x2 = rtValues[i + 1].getRetentionTime();
                y2 = intensityValues[i + 1];
                lastDiff1 = currentDiff;
            } else if (currentDiff < lastDiff2 & currentDiff > 0
                    & currentRT >= rt) {
                x3 = rtValues[i - 1].getRetentionTime();
                y3 = intensityValues[i - 1];
                x4 = rtValues[i].getRetentionTime();
                y4 = intensityValues[i];
                lastDiff2 = currentDiff;
            }

        }

        // Calculate RT value for input intensity based on linear regression
        double slope, intercept, rt1, rt2;
        if (y1 > 0) {
            slope = (y2 - y1) / (x2 - x1);
            intercept = y1 - (slope * x1);
            rt1 = (intensity - intercept) / slope;
        } else { // Straight drop of peak to 0 intensity
            rt1 = x2;
        }
        if (y4 > 0) {
            slope = (y4 - y3) / (x4 - x3);
            intercept = y3 - (slope * x3);
            rt2 = (intensity - intercept) / slope;
        } else { // Straight drop of peak to 0 intensity
            rt2 = x3;
        }

        return new double[] { rt1, rt2 };
    }

}
