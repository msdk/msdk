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
import javax.annotation.concurrent.Immutable;

import com.google.common.collect.Range;

/**
 * This class represents m/z tolerance. Tolerance is set using absolute (m/z)
 * and relative (ppm) values. The tolerance range is calculated as the maximum
 * of the absolute and relative values.
 */
@Immutable
public class MZTolerance {

    // PPM conversion factor.
    private static final Double MILLION = 1000000.0;

    // Tolerance has absolute (in m/z) and relative (in ppm) values
    private final @Nonnull Double mzTolerance;
    private final @Nonnull Double ppmTolerance;

    public MZTolerance(final @Nonnull Double toleranceMZ,
            final @Nonnull Double tolerancePPM) {
        mzTolerance = toleranceMZ;
        ppmTolerance = tolerancePPM;
    }

    public @Nonnull Double getMzTolerance() {
        return mzTolerance;
    }

    public @Nonnull Double getPpmTolerance() {
        return ppmTolerance;
    }

    @SuppressWarnings("null")
    public @Nonnull Range<Double> getToleranceRange(
            final @Nonnull Double mzValue) {
        final @Nonnull Double absoluteTolerance = Math.max(mzTolerance,
                mzValue / MILLION * ppmTolerance);
        return Range.closed(mzValue - absoluteTolerance,
                mzValue + absoluteTolerance);
    }

    @Override
    public String toString() {
        return mzTolerance + " m/z or " + ppmTolerance + " ppm";
    }
}
