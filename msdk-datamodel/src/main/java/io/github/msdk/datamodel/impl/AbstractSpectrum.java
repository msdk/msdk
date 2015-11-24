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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.msspectra.MsSpectrum;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;

/**
 * Simple implementation of the MassSpectrum interface, which stores its data in
 * a data point store.
 */
abstract class AbstractSpectrum implements MsSpectrum {

    private final @Nonnull DataPointStore dataPointStore;

    private Object dataStoreId = null;

    private @Nullable Range<Double> mzRange;
    private @Nonnull Float totalIonCurrent;

    private @Nonnull MsSpectrumType spectrumType;

    AbstractSpectrum(@Nonnull DataPointStore dataPointStore) {
        Preconditions.checkNotNull(dataPointStore);
        this.dataPointStore = dataPointStore;
        totalIonCurrent = 0f;
        spectrumType = MsSpectrumType.CENTROIDED;
    }

    /** {@inheritDoc} */
    @Override
    public void getDataPoints(@Nonnull MsSpectrumDataPointList dataPointList) {
        final Object dataStoreIdCopy = dataStoreId;
        if (dataStoreIdCopy == null)
            throw (new MSDKRuntimeException("Missing data store ID"));
        Preconditions.checkNotNull(dataPointStore);
        Preconditions.checkNotNull(dataPointList);
        dataPointStore.readDataPoints(dataStoreIdCopy, dataPointList);
    }

    /** {@inheritDoc} */
    public void getDataPointsByMzAndIntensity(
            @Nonnull MsSpectrumDataPointList dataPointList,
            @Nonnull Range<Double> mzRange,
            @Nonnull Range<Float> intensityRange) {
        final Object dataStoreIdCopy = dataStoreId;
        if (dataStoreIdCopy == null)
            throw (new MSDKRuntimeException("Missing data store ID"));
        final MsSpectrumDataPointList orgDataPointList = MSDKObjectBuilder
                .getMsSpectrumDataPointList();
        dataPointStore.readDataPoints(dataStoreIdCopy, orgDataPointList);
        dataPointList.copyFrom(
                orgDataPointList.selectDataPoints(mzRange, intensityRange));
    }

    /** {@inheritDoc} */
    @SuppressWarnings("null")
    synchronized public void setDataPoints(
            @Nonnull MsSpectrumDataPointList newDataPoints) {
        Preconditions.checkNotNull(newDataPoints);
        if (dataStoreId != null)
            dataPointStore.removeDataPoints(dataStoreId);
        dataStoreId = dataPointStore.storeDataPoints(newDataPoints);
        mzRange = newDataPoints.getMzRange();
        totalIonCurrent = newDataPoints.getTIC();
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public MsSpectrumType getSpectrumType() {
        return spectrumType;
    }

    /** {@inheritDoc} */
    @Override
    public void setSpectrumType(@Nonnull MsSpectrumType spectrumType) {
        this.spectrumType = spectrumType;
    }

    /**
     * <p>getTIC.</p>
     *
     * @return a {@link java.lang.Float} object.
     */
    @Nonnull
    public Float getTIC() {
        return totalIonCurrent;
    }

    /** {@inheritDoc} */
    @Override
    public Range<Double> getMzRange() {
        return mzRange;
    }
}
