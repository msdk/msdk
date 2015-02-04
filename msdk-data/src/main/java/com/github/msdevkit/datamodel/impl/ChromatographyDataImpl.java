/* 
 * Copyright 2015 MSDK Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.github.msdevkit.datamodel.impl;

import javax.annotation.Nullable;

import com.github.msdevkit.datamodel.ChromatographyData;

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
