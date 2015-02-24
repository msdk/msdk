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

package io.github.msdk.datamodel;

import javax.annotation.Nullable;

/**
 * This class represents the chromatography information of each MS scan or
 * detected feature (peak).
 */
public interface ChromatographyData {

    /**
     * @return Retention time in minutes
     */
    @Nullable
    Double getRetentionTime();

    void setRetentionTime(@Nullable Double retentionTime);

    /**
     * @return Secondary retention time in minutes (for two-dimensional
     *         separations such as GCxGC-MS).
     */
    @Nullable
    Double getSecondaryRetentionTime();

    void setSecondaryRetentionTime(@Nullable Double retentionTime);

    /**
     * @return Drift time in ms, for ion mobility experiments.
     */
    @Nullable
    Double getIonDriftTime();

    void setIonDriftTime(@Nullable Double driftTime);

}
