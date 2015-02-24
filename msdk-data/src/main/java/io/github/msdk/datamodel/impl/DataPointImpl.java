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

import io.github.msdk.datamodel.DataPoint;

import javax.annotation.concurrent.Immutable;

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
