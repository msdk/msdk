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

import io.github.msdk.datamodel.rawdata.DataPoint;
import io.github.msdk.datamodel.rawdata.DataPointList;
import io.github.msdk.datamodel.rawdata.MassSpectrum;
import io.github.msdk.datamodel.rawdata.MassSpectrumType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

/**
 * Simple implementation of the MassSpectrum interface, which stores its data in
 * a data store.
 */
abstract class AbstractSpectrum implements MassSpectrum {

    private final @Nonnull DataPointStore dataPointStore;

    private int dataStoreId = -1;
    private @Nonnull Range<Double> mzRange;
    private MassSpectrumType spectrumType;
    private DataPoint highestDataPoint;

    AbstractSpectrum(@Nonnull DataPointStore dataPointStore) {
	this.dataPointStore = dataPointStore;
	mzRange = Range.singleton(0d);
    }

    @Override
    @Nonnull
    public Range<Double> getMzRange() {
	return mzRange;
    }

    @Override
    @Nullable
    public DataPoint getHighestDataPoint() {
	return highestDataPoint;
    }

    @Override
    public synchronized @Nonnull DataPointList getDataPoints() {
	DataPointList storedData = dataPointStore.readDataPoints(dataStoreId);
	return storedData;
    }

    @Override
    @Nonnull
    public DataPointList getDataPointsByMass(@Nonnull Range<Double> mzRange) {
	return null;
    }

    @Override
    public @Nonnull MassSpectrumType getSpectrumType() {
	return spectrumType;
    }

    @Override
    public void setSpectrumType(@Nonnull MassSpectrumType spectrumType) {
	this.spectrumType = spectrumType;
    }

}
