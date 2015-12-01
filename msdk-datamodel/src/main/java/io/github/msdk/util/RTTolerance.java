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
 * (sec) or relative (%) value.
 */
@Immutable
public class RTTolerance {

    // Tolerance can be either absolute (sec) or relative (%).
    private final @Nonnull Double rtTolerance;
    private final boolean isAbsolute;

    /**
     * <p>Constructor for RTTolerance.</p>
     *
     * @param rtTolerance a double.
     * @param isAbsolute a boolean.
     */
    public RTTolerance(final double rtTolerance, final boolean isAbsolute) {
        this.rtTolerance = rtTolerance;
        this.isAbsolute = isAbsolute;
    }

    /**
     * <p>isAbsolute.</p>
     *
     * @return a boolean.
     */
    public boolean isAbsolute() {
        return isAbsolute;
    }

    /**
     * <p>getTolerance.</p>
     *
     * @return a double.
     */
    public double getTolerance() {
        return rtTolerance;
    }

    /**
     * <p>getToleranceRange.</p>
     *
     * @param rtValue a double.
     * @return a {@link com.google.common.collect.Range} object.
     */
    public Range<Double> getToleranceRange(final double rtValue) {
        final double absoluteTolerance = isAbsolute ? rtTolerance : rtValue
                * rtTolerance;
        return Range.closed(rtValue - absoluteTolerance, rtValue
                + absoluteTolerance);
    }

    /**
     * <p>checkWithinTolerance.</p>
     *
     * @param rt1 a double.
     * @param rt2 a double.
     * @return a boolean.
     */
    public boolean checkWithinTolerance(final double rt1, final double rt2) {
        return getToleranceRange(rt1).contains(rt2);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return isAbsolute ? rtTolerance + " sec" : 100.0 * rtTolerance + " %";
    }

}
