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

import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.SeparationType;

class SimpleChromatographyInfo implements ChromatographyInfo {

    private Float retentionTime, secondaryRetentionTime, ionDriftTime;

    private SeparationType separationType;

    SimpleChromatographyInfo(Float retentionTime, Float secondaryRetentionTime,
            Float ionDriftTime, SeparationType separationType) {
        this.retentionTime = retentionTime;
        this.secondaryRetentionTime = secondaryRetentionTime;
        this.ionDriftTime = ionDriftTime;
        this.separationType = separationType;
    }

    /**
     * @return the retentionTime
     */
    @Override
    public Float getRetentionTime() {
        return retentionTime;
    }

    /**
     * @return the secondaryRetentionTime
     */
    @Override
    public Float getSecondaryRetentionTime() {
        return secondaryRetentionTime;
    }

    /**
     * @return the ionDriftTime
     */
    @Override
    public Float getIonDriftTime() {
        return ionDriftTime;
    }

    @Override
    public int compareTo(ChromatographyInfo o) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public SeparationType getSeparationType() {
        return separationType;
    }

}
