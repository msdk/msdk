package io.github.msdk.util;

import javax.annotation.Nonnull;

import com.google.common.collect.Range;

public class ConstantPPMTolerance implements MZTolerance {

    // PPM conversion factor.
    private static final Double MILLION = 1_000_000.0;
    private final @Nonnull Double ppmTolerance;

    /**
     * <p>
     * Constructor for ConstantPPMTolerance.
     * </p>
     *
     * @param tolerancePPM
     *            a {@link java.lang.Double} object.
     */
    public ConstantPPMTolerance(final @Nonnull Double tolerancePPM) {
        ppmTolerance = tolerancePPM;
    }

    /**
     * <p>
     * Getter for the field <code>ppmTolerance</code>.
     * </p>
     *
     * @return a {@link java.lang.Double} object.
     */
    public @Nonnull Double getPpmTolerance() {
        return ppmTolerance;
    }

    /**
     * <p>
     * Return the mass tolerance range that is constant in ppm, but changes vs.
     * m/z.
     * </p>
     *
     * @param mzValue
     *            a {@link java.lang.Double} object.
     * @return a {@link com.google.common.collect.Range} object.
     */
    public @Nonnull Range<Double> getToleranceRange(
            final @Nonnull Double mzValue) {
        final @Nonnull Double absoluteTolerance = mzValue / MILLION
                * ppmTolerance;
        return Range.closed(mzValue - absoluteTolerance, mzValue
                + absoluteTolerance);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return ppmTolerance + " ppm";
    }
}
