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

package io.github.msdk.datamodel.peaklists;

import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

/* 
 * WARNING: the interfaces in this package are still under construction
 */

/**
 * A class representing shape (data points) of a detected feature.
 */
public interface FeatureShape {

    /**
     * @return Raw data file where this feature is present, or null if this peak
     *         is not connected to any raw data.
     */
    @Nullable
    RawDataFile getDataFile();

    /**
     * Assigns a raw data file to this feature.
     */
    void setDataFile(@Nullable RawDataFile dataFile);

    /**
     * @return The most representative scan of this feature (with highest signal
     *         intensity), or null if this peak is not connected to any raw
     *         data.
     */
    @Nonnull
    MsScan getRepresentativeScan();

    /**
     * Returns the number of scan that represents the fragmentation of this peak
     * in MS2 level.
     */
    @Nullable
    MsScan getMostIntenseFragmentScan();

    /**
     * This method returns m/z and intensity of this peak in a given scan. This
     * m/z and intensity does not need to match any actual raw data point (e.g.,
     * in profile mode spectra, the m/z value may be calculated from the data
     * points forming the m/z signal).
     */
    @Nonnull
    List<FeatureDataPoint> getDataPoints();

    /**
     * Returns the retention time range of all raw data points used to detect
     * this peak
     */
    @Nonnull
    Range<Double> getRawDataPointsRTRange();

    /**
     * Returns the range of m/z values of all raw data points used to detect
     * this peak
     */
    @Nonnull
    Range<Double> getRawDataPointsMzRange();

    /**
     * Returns the range of intensity values of all raw data points used to
     * detect this peak
     */
    @Nonnull
    Range<Double> getRawDataPointsIntensityRange();

}
