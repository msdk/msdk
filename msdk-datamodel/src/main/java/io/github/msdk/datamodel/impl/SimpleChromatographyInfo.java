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

package io.github.msdk.datamodel.impl;

import javax.annotation.Nullable;

import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.SeparationType;

class SimpleChromatographyInfo implements ChromatographyInfo {

    private Float retentionTime, secondaryRetentionTime, ionDriftTime;

    private @Nullable SeparationType separationType;

    SimpleChromatographyInfo(@Nullable Float retentionTime,
            @Nullable Float secondaryRetentionTime,
            @Nullable Float ionDriftTime,
            @Nullable SeparationType separationType) {
        this.retentionTime = retentionTime;
        this.secondaryRetentionTime = secondaryRetentionTime;
        this.ionDriftTime = ionDriftTime;
        this.separationType = separationType;
    }

    /** {@inheritDoc} */
    @Override
    public Float getRetentionTime() {
        return retentionTime;
    }

    /** {@inheritDoc} */
    @Override
    public Float getSecondaryRetentionTime() {
        return secondaryRetentionTime;
    }

    /** {@inheritDoc} */
    @Override
    public Float getIonDriftTime() {
        return ionDriftTime;
    }

    /** {@inheritDoc} */
    @Override
    public SeparationType getSeparationType() {
        return separationType;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(ChromatographyInfo o) {
        int returnValue;

        // 1. Compare retention time
        returnValue = this.retentionTime.compareTo(o.getRetentionTime());

        // 2. Compare secondary retention time
        if (returnValue == 0) {
            if (this.secondaryRetentionTime != null
                    && o.getSecondaryRetentionTime() != null) {
                returnValue = this.secondaryRetentionTime
                        .compareTo(o.getSecondaryRetentionTime());
            } else if (this.secondaryRetentionTime == null
                    && o.getSecondaryRetentionTime() == null) {
                returnValue = 0;
            } else if (this.secondaryRetentionTime == null) {
                returnValue = -1;
            } else {
                returnValue = 1;
            }
        }

        // 3. Compare ion drift time
        if (returnValue == 0) {
            if (this.ionDriftTime != null && o.getIonDriftTime() != null) {
                returnValue = this.ionDriftTime.compareTo(o.getIonDriftTime());
            } else if (this.ionDriftTime == null && o.getIonDriftTime() == null) {
                returnValue = 0;
            } else if (this.ionDriftTime == null) {
                returnValue = -1;
            } else {
                returnValue = 1;
            }
        }

        return returnValue;
    }

}
