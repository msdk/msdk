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

import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.msspectra.MsIon;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.util.DataPointSorter;
import io.github.msdk.util.DataPointSorter.SortingDirection;
import io.github.msdk.util.DataPointSorter.SortingProperty;

/**
 * Basic implementation of DataPointList.
 * 
 * Important: this class is not thread-safe.
 */
class SimpleMSSpectrumDataPointList implements MsSpectrumDataPointList {

    /**
     * Array for m/z values. Its length defines the capacity of this list.
     */
    private @Nonnull double[] mzBuffer;

    /**
     * Array for intensity values. Its length is always the same as the m/z
     * buffer length.
     */
    private @Nonnull float[] intensityBuffer;

    /**
     * Current size of the list
     */
    private int size;

    /**
     * Creates a new data point list with internal array capacity of 100.
     */
    SimpleMSSpectrumDataPointList() {
        this(100);
    }

    /**
     * Creates a new data point list with given internal array capacity.
     * 
     * @param initialCapacity
     *            Initial size of the m/z and intensity arrays.
     */
    SimpleMSSpectrumDataPointList(@Nonnull Integer initialCapacity) {
        Preconditions.checkArgument(initialCapacity > 0,
                "Initial capacity of a list must be >0");
        mzBuffer = new double[initialCapacity];
        intensityBuffer = new float[initialCapacity];
        size = 0;
    }

    /**
     * Creates a new data point list backed by given arrays. Arrays are
     * referenced, not cloned.
     * 
     * @param mzBuffer
     *            array of m/z values
     * @param intensityBuffer
     *            array of intensity values
     * @param size
     *            size of the list, must be <= length of both arrays
     * @throws IllegalArgumentException
     *             if the initial array length < size
     */
    SimpleMSSpectrumDataPointList(@Nonnull double mzBuffer[],
            @Nonnull float intensityBuffer[], int size) {
        Preconditions.checkArgument(mzBuffer.length >= size);
        Preconditions.checkArgument(intensityBuffer.length >= size);
        this.mzBuffer = mzBuffer;
        this.intensityBuffer = intensityBuffer;
        this.size = size;
    }

    /**
     * {@inheritDoc}
     *
     * Returns the current m/z array
     */
    @Override
    @Nonnull
    public double[] getMzBuffer() {
        return mzBuffer;
    }

    /**
     * {@inheritDoc}
     *
     * Returns the current intensity array
     */
    @Override
    @Nonnull
    public float[] getIntensityBuffer() {
        return intensityBuffer;
    }

    /**
     * {@inheritDoc}
     *
     * Replaces the m/z and intensity arrays with new ones
     */
    @Override
    public void setBuffers(@Nonnull double[] mzBuffer,
            @Nonnull float[] intensityBuffer, int newSize) {

        if (mzBuffer.length != intensityBuffer.length) {
            throw new IllegalArgumentException(
                    "The length of the m/z and intensity arrays must be equal");
        }

        // Update arrays
        this.mzBuffer = mzBuffer;
        this.intensityBuffer = intensityBuffer;

        // Update the size
        setSize(newSize);
    }

    /**
     * {@inheritDoc}
     *
     * Copy data from another DataPointList
     */
    @Override
    public void copyFrom(@Nonnull MsSpectrumDataPointList list) {
        if (mzBuffer.length < list.getSize()) {
            mzBuffer = new double[list.getSize()];
            intensityBuffer = new float[list.getSize()];
        }

        // Copy data
        System.arraycopy(list.getMzBuffer(), 0, mzBuffer, 0, list.getSize());
        System.arraycopy(list.getIntensityBuffer(), 0, intensityBuffer, 0,
                list.getSize());

        // Update the size
        this.size = list.getSize();

    }

    /**
     * {@inheritDoc}
     *
     * Returns the m/z range, assuming the m/z array is sorted.
     */
    @Override
    @Nullable
    public Range<Double> getMzRange() {
        if (size == 0)
            return null;
        return Range.closed(mzBuffer[0], mzBuffer[size - 1]);
    }

    /** {@inheritDoc} */
    @Override
    public int getSize() {
        return size;
    }

    /** {@inheritDoc} */
    public void setSize(int newSize) {

        if (newSize < 0)
            throw new IllegalArgumentException("Size cannot be negative");

        if (newSize > mzBuffer.length)
            throw new MSDKRuntimeException(
                    "Not enough allocated space to change the size of data point list");

        this.size = newSize;

        // Ensure the arrays are sorted in m/z order
        if (newSize > 0) {
            DataPointSorter.sortDataPoints(mzBuffer, intensityBuffer, newSize,
                    SortingProperty.MZ, SortingDirection.ASCENDING);
        }

    }

    /**
     * {@inheritDoc}
     *
     * The equals() method compares the contents of the two data point lists,
     * and ignores their internal array sizes (capacities).
     */
    @Override
    public boolean equals(Object o) {

        // o must be a non-null DataPointList
        if ((o == null) || (!(o instanceof MsSpectrumDataPointList)))
            return false;

        // Cast o to DataPointlist
        MsSpectrumDataPointList otherList = (MsSpectrumDataPointList) o;

        // Size must be equal
        if (otherList.getSize() != size)
            return false;

        // Get the arrays of the other list
        final double otherMzBuffer[] = otherList.getMzBuffer();
        final float otherIntensityBuffer[] = otherList.getIntensityBuffer();

        // Check the array contents
        for (int i = 0; i < size; i++) {
            if (mzBuffer[i] != otherMzBuffer[i])
                return false;
            if (intensityBuffer[i] != otherIntensityBuffer[i])
                return false;
        }

        // No difference found, return true
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * The hashCode() code is inspired by Arrays.hashCode(double[] or float[])
     */
    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < size; i++) {
            long bits = Double.doubleToLongBits(mzBuffer[i]);
            result = 31 * result + (int) (bits ^ (bits >>> 32));
            result = 31 * result + Float.floatToIntBits(intensityBuffer[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * toString() method
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < size; i++) {
            if (i > 0)
                builder.append(", ");
            builder.append(mzBuffer[i]);
            builder.append(":");
            builder.append(intensityBuffer[i]);
        }
        builder.append("]");
        return builder.toString();
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Float getTIC() {
        float tic = 0f;
        for (int i = 0; i < size; i++) {
            tic += intensityBuffer[i];
        }
        return tic;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public MsSpectrumDataPointList selectDataPoints(
            @Nonnull Range<Double> mzRange,
            @Nonnull Range<Float> intensityRange) {

        final MsSpectrumDataPointList newList = MSDKObjectBuilder
                .getMsSpectrumDataPointList();

        // Find how many data points will pass the conditions
        int numOfGoodDataPoints = 0;
        for (int i = 0; i < size; i++) {
            if (!mzRange.contains(mzBuffer[i]))
                continue;
            if (!intensityRange.contains(intensityBuffer[i]))
                continue;
            numOfGoodDataPoints++;
        }

        // Allocate space for the data points
        newList.allocate(numOfGoodDataPoints);
        final double newMzBuffer[] = newList.getMzBuffer();
        final float newIntensityBuffer[] = newList.getIntensityBuffer();

        // Copy the actual data point values
        int newIndex = 0;
        for (int i = 0; i < size; i++) {
            if (!mzRange.contains(mzBuffer[i]))
                continue;
            if (!intensityRange.contains(intensityBuffer[i]))
                continue;
            newMzBuffer[newIndex] = mzBuffer[i];
            newIntensityBuffer[newIndex] = intensityBuffer[i];
            newIndex++;
        }

        // Commit the changes
        newList.setSize(numOfGoodDataPoints);

        return newList;
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        this.size = 0;
    }

    /** {@inheritDoc} */
    @Override
    public void allocate(int newSize) {

        if (mzBuffer.length >= newSize)
            return;

        double[] mzBufferNew = new double[newSize];
        float[] intensityBufferNew = new float[newSize];

        if (size > 0) {
            System.arraycopy(getMzBuffer(), 0, mzBufferNew, 0, size);
            System.arraycopy(getIntensityBuffer(), 0, intensityBufferNew, 0,
                    size);
        }

        mzBuffer = mzBufferNew;
        intensityBuffer = intensityBufferNew;

    }

    /** {@inheritDoc} */
    @Override
    public void add(double mz, float intensity) {

        // Make sure we have enough space to add a new data point
        if (size == mzBuffer.length) {
            allocate(size * 2);
        }

        // Add the data
        mzBuffer[size] = mz;
        intensityBuffer[size] = intensity;

        // If the m/z value is larger than the last one, let's simply add it to
        // the end. Otherwise, we have to call the setSize() method that will
        // sort the lists in m/z order
        if ((size == 0) || (mzBuffer[size] > mzBuffer[size - 1])) {
            size++;
        } else {
            setSize(size + 1);
        }

    }

    private static class MsIonIterator implements Iterator<MsIon>, MsIon {

        private final MsSpectrumDataPointList list;
        private int index = -1;

        MsIonIterator(MsSpectrumDataPointList list) {
            this.list = list;
        }

        @Override
        public double getMz() {
            return list.getMzBuffer()[index];
        }

        @Override
        public float getIntensity() {
            return list.getIntensityBuffer()[index];
        }

        @Override
        public boolean hasNext() {
            return index < (list.getSize() - 2);
        }

        @Override
        public MsIon next() {
            index++;
            return this;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    /** {@inheritDoc} */
    @Override
    public Iterator<MsIon> iterator() {
        return new MsIonIterator(this);
    }

}
