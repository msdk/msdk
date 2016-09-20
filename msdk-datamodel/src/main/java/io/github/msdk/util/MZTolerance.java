package io.github.msdk.util;

import javax.annotation.Nonnull;

import com.google.common.collect.Range;

public interface MZTolerance {
	
    /**
     * <p>
     * Get a m/z tolerance range for a given m/z.
     * </p>
     *
     * @param mzValue
     *            a {@link java.lang.Double} object.
     * @return a {@link com.google.common.collect.Range} object.
     */
	public @Nonnull Range<Double> getToleranceRange(
			final @Nonnull Double mzValue);
}
