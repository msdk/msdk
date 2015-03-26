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
import io.github.msdk.datamodel.rawdata.MassSpectrum;
import io.github.msdk.datamodel.rawdata.MassSpectrumType;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

/**
 * Simple implementation of the MassSpectrum interface, which stores its data in
 * a data store.
 */
abstract class AbstractSpectrum implements MassSpectrum {

    private final @Nonnull DataPointStore dataPointStore;

    private @Nonnull double[] mzBuffer;
    private @Nonnull float[] intensityBuffer;
    private int size;

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
    public @Nonnull MassSpectrumType getSpectrumType() {
        return spectrumType;
    }

    @Override
    public void setSpectrumType(@Nonnull MassSpectrumType spectrumType) {
        this.spectrumType = spectrumType;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterator<DataPoint> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object[] toArray() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean add(DataPoint e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean remove(Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends DataPoint> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends DataPoint> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    @Override
    public DataPoint get(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DataPoint set(int index, DataPoint element) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void add(int index, DataPoint element) {
        // TODO Auto-generated method stub

    }

    @Override
    public DataPoint remove(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int indexOf(Object o) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ListIterator<DataPoint> listIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ListIterator<DataPoint> listIterator(int index) {
        return null;
    }

    @Override
    public List<DataPoint> subList(int fromIndex, int toIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @Nonnull
    public double[] getMzBuffer() {
        return mzBuffer;
    }

    @Override
    @Nonnull
    public float[] getIntensityBuffer() {
        return intensityBuffer;
    }

    @Override
    public void setBuffers(@Nonnull double[] mzBuffer,
            @Nonnull float[] intensityBuffer, int size) {
        this.mzBuffer = mzBuffer;
        this.intensityBuffer = intensityBuffer;
        this.size = size;
    }

    @Override
    @Nonnull
    public MassSpectrum getSubSpectrumByMass(@Nonnull Range<Double> mzRange) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @Nonnull
    public MassSpectrum getSubSpectrumByIntensity(
            @Nonnull Range<Double> intensityRange) {
        // TODO Auto-generated method stub
        return null;
    }

}
