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

import io.github.msdk.datamodel.rawdata.MsScan;

import javax.annotation.Nonnull;

import com.google.common.collect.Range;

public class ConstantDaTolerance implements MzTolerance, MzToleranceProvider {

    private final @Nonnull Double mzTolerance;

    /**
     * <p>
     * Constructor for ConstantDaTolerance.
     * </p>
     *
     * @param mzTolerance
     *            a {@link java.lang.Double} object.
     */
    public ConstantDaTolerance(final @Nonnull Double mzTolerance) {
        this.mzTolerance = mzTolerance;
    }

    /**
     * <p>
     * Getter for the field <code>mzTolerance</code>.
     * </p>
     *
     * @return a {@link java.lang.Double} object.
     */
    public @Nonnull Double getMzTolerance() {
        return mzTolerance;
    }

    /**
     * <p>
     * Return the mass tolerance range that is constant vs. m/z.
     * </p>
     *
     * @param mzValue
     *            a {@link java.lang.Double} object.
     * @return a {@link com.google.common.collect.Range} object.
     */
    public @Nonnull Range<Double> getToleranceRange(
            final @Nonnull Double mzValue) {
        return Range.closed(mzValue - mzTolerance, mzValue + mzTolerance);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return mzTolerance + " Da";
    }

    @Override
    public MzTolerance getMzTolerance(MsScan scan) {
        return this;
    }
}
