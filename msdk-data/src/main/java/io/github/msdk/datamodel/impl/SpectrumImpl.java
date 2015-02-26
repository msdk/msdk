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

import io.github.msdk.datamodel.rawdata.IDataPoint;
import io.github.msdk.datamodel.rawdata.IDataPointList;
import io.github.msdk.datamodel.rawdata.IMassSpectrum;
import io.github.msdk.datamodel.rawdata.MassSpectrumType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

/**
 * Simple implementation of the MassSpectrum interface, which stores its data in
 * a data store.
 */
abstract class SpectrumImpl implements IMassSpectrum {

    private final @Nonnull DataPointStoreImpl dataPointStore;

    private int dataStoreId = -1;
    private int numberOfDataPoints;
    private @Nonnull Range<Double> mzRange;
    private MassSpectrumType spectrumType;
    private IDataPoint highestDataPoint;

    SpectrumImpl(@Nonnull DataPointStoreImpl dataPointStore) {
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
    public IDataPoint getHighestDataPoint() {
	return highestDataPoint;
    }

    @Override
    public int getNumberOfDataPoints() {
	return numberOfDataPoints;
    }

    @Override
    public synchronized @Nonnull IDataPointList getDataPoints() {
	IDataPointList storedData = dataPointStore.readDataPoints(dataStoreId);
	return storedData;
    }

    @Override
    @Nonnull
    public IDataPointList getDataPointsByMass(@Nonnull Range<Double> mzRange) {
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
