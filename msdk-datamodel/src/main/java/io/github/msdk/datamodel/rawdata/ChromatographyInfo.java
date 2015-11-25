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

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Represents the chromatography information of an MS scan or a detected
 * feature. For convenience, this interface is immutable, so it can be passed by
 * reference and safely used by multiple threads. This interface also extends
 * Comparable, so we can use the Range class to define ranges of retention times
 * etc. The comparator method should compare two instances by retention time,
 * secondary retention time, and ion drift time in this order.
 */
@Immutable
public interface ChromatographyInfo extends Comparable<ChromatographyInfo> {

    /**
     * Returns retention time in seconds, or null if no retention time is
     * defined.
     *
     * @return Retention time in seconds, or null.
     */
    @Nullable
    Float getRetentionTime();

    /**
     * Returns secondary retention time in seconds (for two-dimensional
     * separations such as GCxGC-MS), or null if no secondary retention time is
     * defined.
     *
     * @return Secondary retention time in seconds, or null.
     */
    @Nullable
    Float getSecondaryRetentionTime();

    /**
     * Returns ion drift time in ms, or null if no drift time is defined. Drift
     * time is used in ion mobility experiments.
     *
     * @return Drift time in ms, or null.
     */
    @Nullable
    Float getIonDriftTime();

    /**
     * Returns the separation type used for separation of molecules.
     *
     * @return the seperation type. Returns {@link SeparationType#UNKNOWN} for
     *         unknown separations.
     */
    SeparationType getSeparationType();

}
