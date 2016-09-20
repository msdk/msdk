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

package io.github.msdk.util.tolerances;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.google.common.collect.Range;

/**
 * This class represents the maximum m/z tolerance for a set of independent
 * tolerances (e.g. constant Da and ppm tolerances).
 */
@Immutable
public class MaximumMzTolerance implements MzTolerance {

    private final MzTolerance[] tolerances;

    /**
     * <p>
     * Constructor for MaximumMzTolerance.
     * </p>
     * 
     * @param first
     *            an object that implements the
     *            {@link io.github.msdk.util.tolerances.MzTolerance} interface.
     * @param second
     *            an object that implements the
     *            {@link io.github.msdk.util.tolerances.MzTolerance} interface.
     * @param additionalTolerances
     *            an argument list of objects implementing the
     *            {@link io.github.msdk.util.tolerances.MzTolerance} interface.
     */
    public MaximumMzTolerance(MzTolerance first, MzTolerance second,
            MzTolerance... additionalTolerances) {
        this.tolerances = new MzTolerance[2 + additionalTolerances.length];
        this.tolerances[0] = first;
        this.tolerances[1] = second;
        for (int i = 0; i < additionalTolerances.length; i++) {
            this.tolerances[i + 2] = additionalTolerances[i];
        }
    }

    /**
     * <p>
     * getToleranceRange.
     * </p>
     *
     * @param mzValue
     *            a {@link java.lang.Double} object.
     * @return a {@link com.google.common.collect.Range} object.
     */
    public @Nonnull Range<Double> getToleranceRange(
            final @Nonnull Double mzValue) {
        Range<Double> range = tolerances[0].getToleranceRange(mzValue);
        for (int i = 1; i < tolerances.length; i++) {
            range = range.span(tolerances[i].getToleranceRange(mzValue));
        }
        return range;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Arrays.toString(tolerances);
    }
}
