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
     * <p>
     * getNumberOfDataPoints.
     * </p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Nonnull
    Integer getNumberOfDataPoints();

    /**
     * Loads the data points of this spectrum into the given DataPointList. If
     * the DataPointList is not empty, it is cleared first. This method allows
     * the internal arrays of the DataPointList to be reused for loading
     * multiple spectra.
     *
     * Note: this method may need to read data from disk, therefore it may be
     * quite slow.
     *
     * @return an array of double.
     */
    @Nonnull
    double[] getMzValues();

    /**
     * <p>
     * getMzValues.
     * </p>
     *
     * @param array
     *            an array of double.
     * @return an array of double.
     */
    @Nonnull
    double[] getMzValues(@Nullable double array[]);

    /**
     * Returns data points in given m/z and intensity ranges. Importantly, a new
     * instance of DataPointList is created by each call to this method.
     *
     * Note: this method may need to read data from disk, therefore it may be
     * quite slow.
     *
     * @return an array of float.
     */
    @Nonnull
    float[] getIntensityValues();

    /**
     * <p>
     * getIntensityValues.
     * </p>
     *
     * @param array
     *            an array of float.
     * @return an array of float.
     */
    @Nonnull
    float[] getIntensityValues(@Nullable float array[]);

    /**
     * <p>
     * setDataPoints.
     * </p>
     *
     * @param mzValues
     *            an array of double.
     * @param intensityValues
     *            an array of float.
     * @param size
     *            a {@link java.lang.Integer} object.
     */
    void setDataPoints(@Nonnull double mzValues[],
            @Nonnull float intensityValues[], @Nonnull Integer size);

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
