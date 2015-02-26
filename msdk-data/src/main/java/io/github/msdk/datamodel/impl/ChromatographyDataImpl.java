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

import io.github.msdk.datamodel.rawdata.IChromatographyData;

public class ChromatographyDataImpl implements IChromatographyData {

    private Double retentionTime, secondaryRetentionTime, ionDriftTime;

    /**
     * @return the retentionTime
     */
    @Override
    public Double getRetentionTime() {
	return retentionTime;
    }

    /**
     * @return the secondaryRetentionTime
     */
    @Override
    public Double getSecondaryRetentionTime() {
	return secondaryRetentionTime;
    }

    /**
     * @return the ionDriftTime
     */
    @Override
    public Double getIonDriftTime() {
	return ionDriftTime;
    }

    @Override
    public int compareTo(IChromatographyData o) {
	// TODO Auto-generated method stub
	return 0;
    }

}
