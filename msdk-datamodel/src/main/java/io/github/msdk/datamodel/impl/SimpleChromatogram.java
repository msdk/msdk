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

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.chromatograms.ChromatogramDataPointList;
import io.github.msdk.datamodel.chromatograms.ChromatogramType;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;

/**
 * Simple implementation of the Chromatogram interface.
 */
class SimpleChromatogram implements Chromatogram {

    private @Nonnull DataPointStore dataPointStore;
    private @Nullable RawDataFile dataFile;
    private @Nonnull Integer chromatogramNumber;
    private @Nonnull ChromatogramType chromatogramType;
    private @Nullable Double mz;
    private @Nonnull SeparationType separationType;
    private Object dataStoreId = null;

    private final @Nonnull List<IsolationInfo> isolations = new LinkedList<>();

    public SimpleChromatogram(@Nonnull DataPointStore dataPointStore,
            @Nonnull Integer chromatogramNumber,
            @Nonnull ChromatogramType chromatogramType,
            @Nonnull SeparationType separationType) {
        Preconditions.checkNotNull(chromatogramNumber);
        this.dataPointStore = dataPointStore;
        this.chromatogramNumber = chromatogramNumber;
        this.chromatogramType = chromatogramType;
        this.separationType = separationType;
    }

    @Override
    @Nullable
    public RawDataFile getRawDataFile() {
        return dataFile;
    }

    @Override
    public void setRawDataFile(@Nonnull RawDataFile newRawDataFile) {
        this.dataFile = newRawDataFile;
    }

    @Override
    @Nonnull
    public Integer getChromatogramNumber() {
        return chromatogramNumber;
    }

    @Override
    public void setChromatogramNumber(@Nonnull Integer chromatogramNumber) {
        this.chromatogramNumber = chromatogramNumber;
    }

    @Override
    @Nonnull
    public ChromatogramType getChromatogramType() {
        return chromatogramType;
    }

    @Override
    public void setChromatogramType(
            @Nonnull ChromatogramType newChromatogramType) {
        this.chromatogramType = newChromatogramType;
    }

    @Override
    public void getDataPoints(
            @Nonnull ChromatogramDataPointList dataPointList) {
        final Object dataStoreIdCopy = dataStoreId;
        if (dataStoreIdCopy == null)
            throw (new MSDKRuntimeException("Missing data store ID"));
        Preconditions.checkNotNull(dataPointStore);
        Preconditions.checkNotNull(dataPointList);
        dataPointStore.readDataPoints(dataStoreIdCopy, dataPointList);
    }

    @SuppressWarnings("null")
    synchronized public void setDataPoints(
            @Nonnull ChromatogramDataPointList newDataPoints) {
        Preconditions.checkNotNull(newDataPoints);
        if (dataStoreId != null)
            dataPointStore.removeDataPoints(dataStoreId);
        dataStoreId = dataPointStore.storeDataPoints(newDataPoints);
    }

    @Override
    @Nonnull
    public List<IsolationInfo> getIsolations() {
        return isolations;
    }

    @Override
    @Nonnull
    public SeparationType getSeparationType() {
        return separationType;
    }

    @Override
    public void setSeparationType(@Nonnull SeparationType separationType) {
        this.separationType = separationType;
    }

    @Override
    @Nonnull
    public Chromatogram clone(@Nonnull DataPointStore newStore) {
        Preconditions.checkNotNull(newStore);
        Chromatogram newChromatogram = MSDKObjectBuilder.getChromatogram(
                newStore, getChromatogramNumber(), getChromatogramType(),
                getSeparationType());

        final ChromatogramDataPointList dataPointList = MSDKObjectBuilder
                .getChromatogramDataPointList();
        getDataPoints(dataPointList);

        final RawDataFile rawDataFile2 = getRawDataFile();
        if (rawDataFile2 != null) {
            newChromatogram.setRawDataFile(rawDataFile2);
        }
        newChromatogram.getIsolations().addAll(getIsolations());
        newChromatogram.setDataPoints(dataPointList);
        return newChromatogram;
    }

    @Override
    @Nullable
    public Double getMz() {
        return mz;
    }

    @Override
    public void setMz(@Nullable Double newMz) {
        this.mz = newMz;
    }

}
