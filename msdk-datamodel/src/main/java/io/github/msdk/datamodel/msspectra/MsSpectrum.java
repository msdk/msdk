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

package io.github.msdk.datamodel.msspectra;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

/**
 * A mass spectrum. This is a base interface typically extended by other, more
 * specialized interfaces. It may represent a single scan in raw MS data, a
 * calculated isotope pattern, a predicted fragmentation spectrum of a molecule,
 * etc.
 */
public interface MsSpectrum {

    /**
     * Returns the type of this mass spectrum. For spectra that are loaded from
     * raw data files, the type is detected automatically. For calculated
     * spectra, the type depends on the method of calculation.
     *
     * @return spectrum type (profile, centroided, thresholded)
     */
    @Nonnull
    MsSpectrumType getSpectrumType();

    /**
     * Updates the type of this mass spectrum.
     *
     * @param spectrumType
     *            new spectrum type
     */
    void setSpectrumType(@Nonnull MsSpectrumType spectrumType);

    /**
     * Loads the data points of this spectrum into the given DataPointList. If
     * the DataPointList is not empty, it is cleared first. This method allows
     * the internal arrays of the DataPointList to be reused for loading
     * multiple spectra.
     *
     * Note: this method may need to read data from disk, therefore it may be
     * quite slow.
     *
     * @param dataPointList
     *            DataPointList into which the data points should be loaded
     */
    void getDataPoints(@Nonnull MsSpectrumDataPointList dataPointList);

    /**
     * Returns data points in given m/z and intensity ranges. Importantly, a new
     * instance of DataPointList is created by each call to this method.
     *
     * Note: this method may need to read data from disk, therefore it may be
     * quite slow.
     *
     * @param mzRange
     *            range of m/z values to select
     * @param intensityRange
     *            range of intensity values to select
     * @param dataPointList a {@link io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList} object.
     */
    void getDataPointsByMzAndIntensity(
            @Nonnull MsSpectrumDataPointList dataPointList,
            @Nonnull Range<Double> mzRange,
            @Nonnull Range<Float> intensityRange);

    /**
     * Updates the data points of this mass spectrum. If this MassSpectrum has
     * been added to a raw data file or a feature table, the data points will be
     * immediately stored in a temporary file. Therefore, the DataPointList in
     * the parameter can be reused for other purposes.
     *
     * Note: this method may need to write data to disk, therefore it may be
     * quite slow.
     *
     * @param newDataPoints
     *            new data points
     */
    void setDataPoints(@Nonnull MsSpectrumDataPointList newDataPoints);

    /**
     * Returns the sum of intensities of all data points (total ion current or
     * TIC).
     *
     * @return total ion current
     */
    @Nonnull
    Float getTIC();

    /**
     * Returns the range of m/z values for the current spectrum. This can return
     * null if the spectrum has no data points.
     *
     * @return m/z range
     */
    @Nullable
    Range<Double> getMzRange();
}
