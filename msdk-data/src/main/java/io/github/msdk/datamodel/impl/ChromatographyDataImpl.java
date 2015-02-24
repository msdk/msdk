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

import io.github.msdk.datamodel.ChromatographyData;

import javax.annotation.Nullable;

public class ChromatographyDataImpl implements ChromatographyData {

    private Double retentionTime, secondaryRetentionTime, ionDriftTime;

    /**
     * @return the retentionTime
     */
    @Override
    public Double getRetentionTime() {
	return retentionTime;
    }

    /**
     * @param retentionTime
     *            the retentionTime to set
     */
    @Override
    public void setRetentionTime(@Nullable Double retentionTime) {
	this.retentionTime = retentionTime;
    }

    /**
     * @return the secondaryRetentionTime
     */
    @Override
    public Double getSecondaryRetentionTime() {
	return secondaryRetentionTime;
    }

    /**
     * @param secondaryRetentionTime
     *            the secondaryRetentionTime to set
     */
    @Override
    public void setSecondaryRetentionTime(@Nullable Double secondaryRetentionTime) {
	this.secondaryRetentionTime = secondaryRetentionTime;
    }

    /**
     * @return the ionDriftTime
     */
    @Override
    public Double getIonDriftTime() {
	return ionDriftTime;
    }

    /**
     * @param ionDriftTime
     *            the ionDriftTime to set
     */
    @Override
    public void setIonDriftTime(@Nullable Double ionDriftTime) {
	this.ionDriftTime = ionDriftTime;
    }

}
