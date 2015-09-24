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
 * This class represents rt tolerance. Tolerance is set using either a absolute
 * (min) or relative (%) value.
 */
@Immutable
public class RTTolerance {

    // Tolerance can be either absolute (min) or relative (%).
    private final @Nonnull Double rtTolerance;
    private final boolean isAbsolute;

    public RTTolerance(final double rtTolerance, final boolean isAbsolute) {
        this.rtTolerance = rtTolerance;
        this.isAbsolute = isAbsolute;
    }

    public boolean isAbsolute() {
        return isAbsolute;
    }

    public double getTolerance() {
        return rtTolerance;
    }

    public Range<Double> getToleranceRange(final double rtValue) {
        final double absoluteTolerance = isAbsolute ? rtTolerance : rtValue
                * rtTolerance;
        return Range.closed(rtValue - absoluteTolerance, rtValue
                + absoluteTolerance);
    }

    public boolean checkWithinTolerance(final double rt1, final double rt2) {
        return getToleranceRange(rt1).contains(rt2);
    }

    @Override
    public String toString() {
        return isAbsolute ? rtTolerance + " min" : 100.0 * rtTolerance + " %";
    }

}
