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
 * A basic mass spectrum interface, which can be extended by other, more
 * specialized interfaces. It may represent a single scan in raw MS data, a
 * calculated isotope pattern, a predicted fragmentation spectrum of a molecule,
 * etc.
 * 
 * This interface provides a convenient data structure for storing large amount
 * of data points. Internally, it is implemented by two arrays, one for m/z
 * values (double[]) and one for intensity values (float[]). The arrays are
 * resized dynamically, as in ArrayList. It provides all List methods, therefore
 * it supports iteration. Furthermore, it provides direct access to the
 * underlying buffer arrays, for more efficient data handling operations. The
 * access through these arrays is always preferred, as iteration via the List
 * interface has to create a new DataPoint instance for each visited data point.
 * DataPointList always keeps the data points sorted in the m/z order.
 * 
 * This data structure is not thread-safe.
 * 
 */
public interface MassSpectrum extends List<DataPoint> {

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
     * Returns the current m/z buffer array. The size of the array might be
     * larger than the actual size of this DataPointList, therefore data
     * operations should always use the size returned by the size() method and
     * not the length of the returned array. The returned array reflects only
     * the current state of this list - if more data points are added, the
     * internal buffer might be replaced with a larger array.
     * 
     * @return Current m/z buffer
     */
    @Nonnull
    double[] getMzBuffer();

    /**
     * Returns the current intensity buffer array. The size of the array might
     * be larger than the actual size of this DataPointList, therefore data
     * operations should always use the size returned by the size() method and
     * not the length of the returned array. The returned array reflects only
     * the current state of this list - if more data points are added, the
     * internal buffer might be replaced with a larger array.
     * 
     * @return Current intensity buffer
     */
    @Nonnull
    float[] getIntensityBuffer();

    /**
     * Sets the internal buffers to given arrays. The arrays will be referenced
     * directly without cloning. The m/z buffer contents must be sorted in
     * ascending order.
     * 
     * @param mzBuffer
     *            New m/z buffer
     * @param intensityBuffer
     *            New intensity buffer
     * @param size
     *            Number of data point items in the buffers. This might be
     *            smaller than the actual length of the buffer arrays.
     */
    void setBuffers(@Nonnull double[] mzBuffer,
            @Nonnull float[] intensityBuffer, int size);

    /**
     * Returns data points in given m/z range, sorted in m/z order.
     * 
     * Note: this method may need to read data from disk, therefore it may be
     * quite slow.
     * 
     * @return Data points (m/z and intensity pairs) of this spectrum
     */
    @Nonnull
    MassSpectrum getSubSpectrumByMass(@Nonnull Range<Double> mzRange);

    /**
     * Returns data points over given intensity, sorted in m/z order.
     * 
     * Note: this method may need to read data from disk, therefore it may be
     * quite slow.
     * 
     * @return Data points (m/z and intensity pairs) of this spectrum
     */
    @Nonnull
    MassSpectrum getSubSpectrumByIntensity(@Nonnull Range<Double> intensityRange);

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
