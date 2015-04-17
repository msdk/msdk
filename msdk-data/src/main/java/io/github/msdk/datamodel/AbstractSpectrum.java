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

import io.github.msdk.datamodel.rawdata.DataPoint;
import io.github.msdk.datamodel.rawdata.DataPointList;
import io.github.msdk.datamodel.rawdata.MassSpectrum;
import io.github.msdk.datamodel.rawdata.MassSpectrumType;
import io.github.msdk.datapointstore.DataPointStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

/**
 * Simple implementation of the MassSpectrum interface, which stores its data in
 * a data point store.
 */
abstract class AbstractSpectrum implements MassSpectrum {

    private final @Nonnull DataPointStore dataPointStore;

    private Object dataStoreId = null;

    private @Nullable Range<Double> mzRange;
    private @Nullable DataPoint highestDataPoint;
    private @Nonnull Float totalIonCurrent;

    private @Nonnull MassSpectrumType spectrumType;

    AbstractSpectrum(@Nonnull DataPointStore dataPointStore) {
        Preconditions.checkNotNull(dataPointStore);
        this.dataPointStore = dataPointStore;
        totalIonCurrent = 0f;
        spectrumType = MassSpectrumType.CENTROIDED;
    }

    @Override
    public @Nonnull DataPointList getDataPoints() {
        Preconditions.checkNotNull(dataStoreId);
        DataPointList storedData = dataPointStore.readDataPoints(dataStoreId);
        return storedData;
    }

    @Override
    public void getDataPoints(@Nonnull DataPointList list) {
        Preconditions.checkNotNull(dataStoreId);
        dataPointStore.readDataPoints(dataStoreId, list);
    }

    @Override
    @Nonnull
    public DataPointList getDataPointsByMz(@Nonnull Range<Double> mzRange) {
        Preconditions.checkNotNull(dataStoreId);
        DataPointList storedData = dataPointStore.readDataPoints(dataStoreId);
        final Range<Float> all = Range.all();
        return storedData.selectDataPoints(mzRange, all);
    }

    @Nonnull
    public DataPointList getDataPointsByIntensity(
            @Nonnull Range<Float> intensityRange) {
        Preconditions.checkNotNull(dataStoreId);
        DataPointList storedData = dataPointStore.readDataPoints(dataStoreId);
        final Range<Double> all = Range.all();
        return storedData.selectDataPoints(all, intensityRange);
    }

    @Nonnull
    public DataPointList getDataPointsByMzAndIntensity(
            @Nonnull Range<Double> mzRange, @Nonnull Range<Float> intensityRange) {
        Preconditions.checkNotNull(dataStoreId);
        DataPointList storedData = dataPointStore.readDataPoints(dataStoreId);
        return storedData.selectDataPoints(mzRange, intensityRange);
    }

    /**
     * Updates the data points of this mass spectrum. If this MassSpectrum has
     * been added to a raw data file or a peak list, the data points will be
     * immediately stored in a temporary file. Therefore, the DataPointList in
     * the parameter can be reused for other purposes.
     * 
     * Note: this method may need to write data to disk, therefore it may be
     * quite slow.
     * 
     * @param newDataPoints
     *            New data points
     */
    synchronized public void setDataPoints(@Nonnull DataPointList newDataPoints) {
        if (dataStoreId != null)
            dataPointStore.removeDataPoints(dataStoreId);
        dataStoreId = dataPointStore.storeDataPoints(newDataPoints);
        mzRange = newDataPoints.getMzRange();
        totalIonCurrent = newDataPoints.getTIC();
    }

    @Override
    public @Nonnull MassSpectrumType getSpectrumType() {
        return spectrumType;
    }

    @Override
    public void setSpectrumType(@Nonnull MassSpectrumType spectrumType) {
        this.spectrumType = spectrumType;
    }

    /**
     * Returns the m/z range of this mass spectrum (minimum and maximum m/z
     * values of all data points, inclusive).
     * 
     * @return m/z range of this mass spectrum
     */
    @Override
    @Nullable
    public Range<Double> getMzRange() {
        return mzRange;
    }

    /**
     * Returns the top intensity data point, also called "base peak". May return
     * null if there are no data points in this spectrum.
     * 
     * @return Highest data point
     */

    @Override
    @Nullable
    public DataPoint getHighestDataPoint() {
        return highestDataPoint;
    }

    /**
     * Returns the sum of intensities of all data points (total ion current or
     * TIC).
     * 
     * @return total ion current
     */
    @Nonnull
    public Float getTIC() {
        return totalIonCurrent;
    }

}
