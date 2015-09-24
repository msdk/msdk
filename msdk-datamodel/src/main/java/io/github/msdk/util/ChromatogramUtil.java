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

import com.google.common.collect.Range;

import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.chromatograms.ChromatogramDataPointList;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;

public class ChromatogramUtil {

    private static ChromatogramDataPointList dataPointList = MSDKObjectBuilder
            .getChromatogramDataPointList();

    /**
     * Returns the range of ChromatographyInfo of all data points in this feature.
     */
    @SuppressWarnings("null")
    @Nonnull
    public static Range<ChromatographyInfo> getDataPointsChromatographyRange(
            @Nonnull Chromatogram chromatogram) {
        chromatogram.getDataPoints(dataPointList);
        final ChromatographyInfo[] chromatographyInfo = dataPointList
                .getRtBuffer();
        final Range<ChromatographyInfo> chromatographyRange = Range.closed(
                chromatographyInfo[0],
                chromatographyInfo[dataPointList.getSize()]);
        return chromatographyRange;
    }

    /**
     * Returns the range of intensity values of all data points in this feature.
     */
    @SuppressWarnings("null")
    @Nonnull
    public static Range<Float> getDataPointsIntensityRange(
            @Nonnull Chromatogram chromatogram) {
        chromatogram.getDataPoints(dataPointList);
        final float[] intensities = dataPointList.getIntensityBuffer();
        final float lower = intensities[0];
        final float upper = intensities[dataPointList.getSize()];
        final Range<Float> intensityRange = Range.closed(lower, upper);
        return intensityRange;
    }

    /**
     * Returns the number of data points for this feature.
     */
    @SuppressWarnings("null")
    public static int getNumberOfDataPoints(
            @Nonnull Chromatogram chromatogram) {
        chromatogram.getDataPoints(dataPointList);
        return dataPointList.getSize();
    }

}
