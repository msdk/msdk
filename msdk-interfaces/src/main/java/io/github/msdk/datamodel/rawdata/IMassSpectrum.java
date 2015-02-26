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

package io.github.msdk.datamodel.rawdata;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

/**
 * A mass spectrum. This is a base interface typically extended by other,
 * specialized interfaces. It may represent a single scan in raw MS data, a
 * calculated isotope pattern, a predicted fragmentation spectrum of a molecule,
 * etc.
 */
public interface IMassSpectrum {

    /**
     * Returns the m/z range of this mass spectrum (minimum and maximum m/z
     * values, inclusive).
     * 
     * @return m/z range of this mass spectrum
     */
    @Nonnull
    Range<Double> getMzRange();

    /**
     * Returns the top intensity data point, also called "base peak". May return
     * null if there are no data points in this spectrum.
     * 
     * @return Highest data point
     */
    @Nullable
    IDataPoint getHighestDataPoint();

    /**
     * Returns the sum of intensities of all data points (total ion current or
     * TIC).
     * 
     * @return Total ion current.
     */
    @Nonnull
    Double getTIC();

    /**
     * Returns the type of this mass spectrum. For spectra that are loaded from
     * raw data files, the type is automatically detected. For calculated
     * spectra, the type depends on the method of calculation.
     * 
     * @return Spectrum type (profile, centroided, thresholded)
     */
    @Nonnull
    IMassSpectrumType getSpectrumType();

    /**
     * Updates the type of this mass spectrum.
     * 
     * @param spectrumType
     *            New spectrum type.
     */
    void setSpectrumType(@Nonnull IMassSpectrumType spectrumType);

    /**
     * @return Number of m/z and intensity data points.
     */
    int getNumberOfDataPoints();

    /**
     * Returns data points of this spectrum, sorted in m/z order.
     * 
     * Note: this method may need to read data from disk, therefore it may be
     * quite slow.
     * 
     * @return Data points (m/z and intensity pairs) of this spectrum
     */
    @Nonnull
    List<IDataPoint> getDataPoints();

    /**
     * Updates the data points of this mass spectrum. The method will sort the
     * 
     * @param newDataPoints
     *            New data points
     */
    void setDataPoints(@Nonnull List<IDataPoint> newDataPoints);

    /**
     * Returns data points in given m/z range, sorted in m/z order.
     * 
     * Note: this method may need to read data from disk, therefore it may be
     * quite slow.
     * 
     * @return Data points (m/z and intensity pairs) of this spectrum
     */
    @Nonnull
    List<IDataPoint> getDataPointsByMass(@Nonnull Range<Double> mzRange);

    /**
     * Returns data points over given intensity, sorted in m/z order.
     * 
     * Note: this method may need to read data from disk, therefore it may be
     * quite slow.
     * 
     * @return Data points (m/z and intensity pairs) of this spectrum
     */
    @Nonnull
    List<IDataPoint> getDataPointsByIntensity(
	    @Nonnull Range<Double> intensityRange);

}
