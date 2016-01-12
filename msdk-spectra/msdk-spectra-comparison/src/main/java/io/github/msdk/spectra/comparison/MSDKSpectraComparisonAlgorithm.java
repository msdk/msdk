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

package io.github.msdk.spectra.comparison;

import javax.annotation.Nonnull;

/**
 * <p>
 * MSDKSpectraComparisonAlgorithm interface.
 * </p>
 */
public interface MSDKSpectraComparisonAlgorithm {

    /**
     * Compares two MS spectra and returns a score.
     * 
     * @param mzValuesSpectrum1
     * @param intensityValuesSpectrum1
     * @param sizeSpectrum1
     * @param mzValuesSpectrum2
     * @param intensityValuesSpectrum2
     * @param sizeSpectrum2
     * @return
     */
    @Nonnull
    Double compareSpectra(@Nonnull double mzValuesSpectrum1[],
            @Nonnull float intensityValuesSpectrum1[],
            @Nonnull Integer sizeSpectrum1, @Nonnull double mzValuesSpectrum2[],
            @Nonnull float intensityValuesSpectrum2[],
            @Nonnull Integer sizeSpectrum2);

}
