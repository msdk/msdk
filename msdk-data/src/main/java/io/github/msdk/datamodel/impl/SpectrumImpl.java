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
import io.github.msdk.datamodel.rawdata.IMassSpectrum;
import io.github.msdk.datamodel.rawdata.IMassSpectrumType;
import io.github.msdk.util.DataPointSorter;
import io.github.msdk.util.SortingDirection;
import io.github.msdk.util.SortingProperty;

import java.util.ArrayList;
import java.util.Arrays;

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
    private IMassSpectrumType spectrumType;
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
    public synchronized @Nonnull IDataPoint[] getDataPoints() {
	IDataPoint storedData[] = dataPointStore.readDataPoints(dataStoreId);
	return storedData;
    }

    @Override
    @Nonnull
    public IDataPoint[] getDataPointsByMass(@Nonnull Range<Double> mzRange) {
	final IDataPoint[] dataPoints = getDataPoints();
	int startIndex, endIndex;
	for (startIndex = 0; startIndex < dataPoints.length; startIndex++) {
	    if (dataPoints[startIndex].getMz() >= mzRange.lowerEndpoint())
		break;
	}

	for (endIndex = startIndex; endIndex < dataPoints.length; endIndex++) {
	    if (dataPoints[endIndex].getMz() > mzRange.upperEndpoint())
		break;
	}

	IDataPoint pointsWithinRange[] = new IDataPoint[endIndex - startIndex];

	// Copy the relevant points
	System.arraycopy(dataPoints, startIndex, pointsWithinRange, 0, endIndex
		- startIndex);

	return pointsWithinRange;
    }

    @Override
    @Nonnull
    public IDataPoint[] getDataPointsOverIntensity(double intensity) {
	IDataPoint[] dataPoints = getDataPoints();
	int index;
	ArrayList<IDataPoint> points = new ArrayList<IDataPoint>();

	for (index = 0; index < dataPoints.length; index++) {
	    if (dataPoints[index].getIntensity() >= intensity)
		points.add(dataPoints[index]);
	}

	IDataPoint pointsOverIntensity[] = points.toArray(new IDataPoint[0]);

	return pointsOverIntensity;
    }

    @Override
    public synchronized void setDataPoints(@Nonnull IDataPoint[] newDataPoints) {

	// Remove previous data, if any
	if (dataStoreId != -1) {
	    dataPointStore.removeStoredDataPoints(dataStoreId);
	}

	// Sort the data points by m/z, because getDataPoints() guarantees the
	// returned array is sorted by m/z
	Arrays.sort(newDataPoints, new DataPointSorter(SortingProperty.MZ,
		SortingDirection.ASCENDING));

	dataStoreId = dataPointStore.storeDataPoints(newDataPoints);

	numberOfDataPoints = newDataPoints.length;
	// mzRange = ScanUtils.findMzRange(newDataPoints);
	// highestDataPoint = ScanUtils.findTopDataPoint(newDataPoints);
	// isCentroided = ScanUtils.isCentroided(newDataPoints);

    }

    @Override
    public @Nonnull IMassSpectrumType getSpectrumType() {
	return spectrumType;
    }

    @Override
    public void setSpectrumType(@Nonnull IMassSpectrumType spectrumType) {
	this.spectrumType = spectrumType;
    }

}
