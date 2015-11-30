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

package io.github.msdk.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;

/**
 * <p>
 * MsSpectrumUtil class.
 * </p>
 *
 */
public class MsSpectrumUtil {

    /**
     * Returns the m/z range of given data points. Can return null if the data
     * point list is empty.
     *
     * @param dataPoints
     *            a
     *            {@link io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList}
     *            object.
     * @return a {@link com.google.common.collect.Range} object.
     */
    @Nullable
    public static Range<Double> getMzRange(
            @Nonnull MsSpectrumDataPointList dataPoints) {
        Preconditions.checkNotNull(dataPoints);
        if (dataPoints.getSize() == 0)
            return null;

        double mzValues[] = dataPoints.getMzBuffer();
        double min = mzValues[0];
        double max = mzValues[dataPoints.getSize() - 1];
        return Range.closed(min, max);
    }

    /**
     * Calculates the total ion current (=sum of all intensity values)
     *
     * @param dataPoints
     *            a
     *            {@link io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList}
     *            object.
     * @return a {@link java.lang.Float} object.
     */
    public static @Nonnull Float getTIC(
            @Nonnull MsSpectrumDataPointList dataPoints) {
        Preconditions.checkNotNull(dataPoints);
        float tic = 0f;
        float intValues[] = dataPoints.getIntensityBuffer();
        for (int i = 0; i < dataPoints.getSize(); i++) {
            tic += intValues[i];
        }
        return tic;
    }

    /**
     * Returns the highest intensity value. Returns 0 if the list has no data
     * points.
     *
     * @param dataPoints
     *            a
     *            {@link io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList}
     *            object.
     * @return a {@link java.lang.Float} object.
     */
    public static @Nonnull Float getMaxIntensity(
            @Nonnull MsSpectrumDataPointList dataPoints) {
        Preconditions.checkNotNull(dataPoints);
        Integer topIndex = getBasePeakIndex(dataPoints);
        if (topIndex == null)
            return 0f;
        float intValues[] = dataPoints.getIntensityBuffer();
        return intValues[topIndex];
    }

    /**
     * Returns the index of the highest intensity value. Returns null if the
     * list has no data points.
     *
     * @param dataPoints
     *            a
     *            {@link io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList}
     *            object.
     * @return a {@link java.lang.Integer} object.
     */
    public static @Nullable Integer getBasePeakIndex(
            @Nonnull MsSpectrumDataPointList dataPoints) {
        Preconditions.checkNotNull(dataPoints);
        if (dataPoints.getSize() == 0)
            return null;
        final float intValues[] = dataPoints.getIntensityBuffer();
        int topIndex = 0;
        for (int i = 1; i < dataPoints.getSize(); i++) {
            if (intValues[i] > intValues[topIndex])
                topIndex = i;
        }
        return topIndex;
    }

    /**
     * Returns the index of the highest intensity value. Returns null if the
     * list has no data points or if no data point was found within the mz
     * range.
     *
     * @param dataPoints
     *            a
     *            {@link io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList}
     *            object.
     * @param mzRange
     *            a {@link com.google.common.collect.Range} object.
     * @return a {@link java.lang.Integer} object.
     */
    public static @Nullable Integer getBasePeakIndex(
            @Nonnull MsSpectrumDataPointList dataPoints,
            @Nonnull Range<Double> mzRange) {
        Preconditions.checkNotNull(dataPoints);
        Preconditions.checkNotNull(mzRange);
        if (dataPoints.getSize() == 0)
            return null;
        final float intValues[] = dataPoints.getIntensityBuffer();
        final double mzValues[] = dataPoints.getMzBuffer();
        Integer topIndex = null;
        for (int i = 0; i < dataPoints.getSize(); i++) {
            if ((topIndex == null || intValues[i] > intValues[topIndex])
                    && mzRange.contains(mzValues[i]))
                topIndex = i;
        }
        return topIndex;
    }

    public static void convertToRelative(MsSpectrumDataPointList dataPoints,
            int scale) {

        final float max = getMaxIntensity(dataPoints);
        float intensityValues[] = dataPoints.getIntensityBuffer();

        for (int i = 0; i < intensityValues.length; i++) {
            intensityValues[i] = intensityValues[i] / max * scale;
        }

    }

}
