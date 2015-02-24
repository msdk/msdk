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

package io.github.msdk.datamodel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

/**
 * Represents any kind of mass spectrum. For example, a single scan in raw MS
 * data, a predicted isotope pattern etc.
 */
public interface MassSpectrum {

    /**
     * Returns the m/z range of this Scan. Never returns null.
     * 
     * @return m/z range of this Scan
     */
    @Nonnull
    Range<Double> getMzRange();

    /**
     * Returns the top intensity data point. May return null if there are no
     * data points in this spectrum.
     * 
     * @return Base peak
     */
    @Nullable
    DataPoint getHighestDataPoint();

    /**
     * 
     * @return True if the spectrum is centroided
     */
    @Nonnull
    MassSpectrumType getSpectrumType();

    /**
     * 
     * @return True if the spectrum is centroided
     */
    void setSpectrumType(@Nonnull MassSpectrumType spectrumType);

    /**
     * @return Number of m/z and intensity data points.
     */
    int getNumberOfDataPoints();

    /**
     * Returns data points of this spectrum, always sorted in m/z order.
     * 
     * This method may need to read data from disk, therefore it may be quite
     * slow. Modules should be aware of that and cache the data points if
     * necessary.
     * 
     * @return Data points (m/z and intensity pairs) of this scan
     */
    @Nonnull
    DataPoint[] getDataPoints();

    void setDataPoints(@Nonnull DataPoint newDataPoints[]);

    /**
     * Returns data points in given m/z range, sorted in m/z order.
     * 
     * This method may need to read data from disk, therefore it may be quite
     * slow. Modules should be aware of that and cache the data points if
     * necessary.
     * 
     * @return Data points (m/z and intensity pairs) of this spectrum
     */
    @Nonnull
    DataPoint[] getDataPointsByMass(@Nonnull Range<Double> mzRange);

    /**
     * Returns data points over given intensity, sorted in m/z order.
     * 
     * This method may need to read data from disk, therefore it may be quite
     * slow. Modules should be aware of that and cache the data points if
     * necessary.
     * 
     * @return Data points (m/z and intensity pairs) of this Spectrum
     */
    @Nonnull
    DataPoint[] getDataPointsOverIntensity(double intensity);

}
