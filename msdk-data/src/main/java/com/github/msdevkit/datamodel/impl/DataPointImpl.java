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

import javax.annotation.concurrent.Immutable;

import com.github.msdevkit.datamodel.DataPoint;

/**
 * This class represents one data point of a spectrum (m/z and intensity pair).
 * Data point is immutable once created, so we can pass data points by
 * reference.
 */
@Immutable
class DataPointImpl implements DataPoint {

    private double mz, intensity;

    /**
     * @param mz
     * @param intensity
     */
    DataPointImpl(double mz, double intensity) {
	this.mz = mz;
	this.intensity = intensity;
    }

    @Override
    public double getIntensity() {
	return intensity;
    }

    @Override
    public double getMz() {
	return mz;
    }

    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof DataPoint))
	    return false;
	DataPoint dp = (DataPoint) obj;
	return (this.mz == dp.getMz()) && (this.intensity == dp.getIntensity());
    }

    @Override
    public int hashCode() {
	return (int) (this.mz + this.intensity);
    }

    @Override
    public String toString() {
	String str = "m/z: " + mz + ", intensity: " + intensity;
	return str;
    }

}
