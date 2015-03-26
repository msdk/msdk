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
 * A mass spectrum. This is a base interface typically extended by other, more
 * specialized interfaces. It may represent a single scan in raw MS data, a
 * calculated isotope pattern, a predicted fragmentation spectrum of a molecule,
 * etc.
 */
public interface MassSpectrum {

    /**
     * Returns the type of this mass spectrum. For spectra that are loaded from
     * raw data files, the type is detected automatically. For calculated
     * spectra, the type depends on the method of calculation.
     * 
     * @return Spectrum type (profile, centroided, thresholded)
     */
    @Nonnull
    MassSpectrumType getSpectrumType();

    /**
     * Updates the type of this mass spectrum.
     * 
     * @param spectrumType
     *            New spectrum type.
     */
    void setSpectrumType(@Nonnull MassSpectrumType spectrumType);

    /**
     * Returns data points of this spectrum.
     * 
     * Note: this method may need to read data from disk, therefore it may be
     * quite slow.
     * 
     * @return Data points (m/z and intensity pairs) of this spectrum
     */
    @Nonnull
    DataPointList getDataPoints();

    /**
     * Loads the data points of this spectrum into the given DataPointList. If
     * the DataPointList is not empty, it is cleared first. This method allows
     * the internal arrays of the DataPointList to be reused for loading
     * multiple spectra.
     * 
     * Note: this method may need to read data from disk, therefore it may be
     * quite slow.
     * 
     * @param list
     *            DataPointList into which the data points should be loaded
     */
    void getDataPoints(DataPointList list);

    /**
     * Returns data points in given m/z range, sorted in m/z order.
     * 
     * Note: this method may need to read data from disk, therefore it may be
     * quite slow.
     * 
     * @return Data points (m/z and intensity pairs) of this spectrum
     */
    @Nonnull
    DataPointList getDataPointsByMass(@Nonnull Range<Double> mzRange);

    /**
     * Returns data points over given intensity, sorted in m/z order.
     * 
     * Note: this method may need to read data from disk, therefore it may be
     * quite slow.
     * 
     * @return Data points (m/z and intensity pairs) of this spectrum
     */
    @Nonnull
    List<DataPoint> getDataPointsByIntensity(
            @Nonnull Range<Double> intensityRange);

    /**
     * Updates the data points of this mass spectrum. If this MassSpectrum has
     * been added to a raw data file or a peak list, the data points will be
     * immediately stored in a temporary file. Therefore, the DataPointList in
     * the parameter can be reused for other purposes.
     * 
     * Note: this method may need to write data to disk, therefore it may be
     * quite slow.
     * 
     * @param newDataPoints
     *            New data points
     */
    void setDataPoints(@Nonnull DataPointList newDataPoints);

    /**
     * Returns the m/z range of this mass spectrum (minimum and maximum m/z
     * values of all data points, inclusive).
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
    DataPoint getHighestDataPoint();

    /**
     * Returns the sum of intensities of all data points (total ion current or
     * TIC).
     * 
     * @return Total ion current.
     */
    @Nonnull
    Float getTIC();

}
