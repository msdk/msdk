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
import io.github.msdk.datamodel.chromatograms.ChromatogramDataPointList;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.util.DataPointSorter;

/**
 * Basic implementation of DataPointList.
 * 
 * Important: this class is not thread-safe.
 */
class SimpleChromatogramDataPointList implements ChromatogramDataPointList {

    /**
     * Array for ChromatographyInfo values. Its length defines the capacity of
     * this list.
     */
    private @Nonnull ChromatographyInfo[] rtBuffer;

    /**
     * Array for intensity values. Its length is always the same as the RT
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
    SimpleChromatogramDataPointList() {
        this(100);
    }

    /**
     * Creates a new data point list with given internal array capacity.
     * 
     * @param initialCapacity
     *            Initial size of the RT and intensity arrays.
     */
    SimpleChromatogramDataPointList(@Nonnull Integer initialCapacity) {
        Preconditions.checkArgument(initialCapacity > 0,
                "Initial capacity of a list must be >0");
        rtBuffer = new ChromatographyInfo[initialCapacity];
        intensityBuffer = new float[initialCapacity];
        size = 0;
    }

    /**
     * Creates a new data point list backed by given arrays. Arrays are
     * referenced, not cloned.
     * 
     * @param rtBuffer
     *            array of ChromatographyInfo values
     * @param intensityBuffer
     *            array of intensity values
     * @param size
     *            size of the list, must be <= length of both arrays
     * @throws IllegalArgumentException
     *             if the initial array length < size
     */
    SimpleChromatogramDataPointList(@Nonnull ChromatographyInfo rtBuffer[],
            @Nonnull float intensityBuffer[], int size) {
        Preconditions.checkArgument(rtBuffer.length >= size);
        Preconditions.checkArgument(intensityBuffer.length >= size);
        this.rtBuffer = rtBuffer;
        this.intensityBuffer = intensityBuffer;
        this.size = size;
    }

    /**
     * {@inheritDoc}
     *
     * Returns the current RT array
     */
    @Override
    @Nonnull
    public ChromatographyInfo[] getRtBuffer() {
        return rtBuffer;
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
     * Replaces the RT and intensity arrays with new ones
     */
    @Override
    public void setBuffers(@Nonnull ChromatographyInfo[] rtBuffer,
            @Nonnull float[] intensityBuffer, int newSize) {

        if (rtBuffer.length != intensityBuffer.length) {
            throw new IllegalArgumentException(
                    "The length of the rt and intensity arrays must be equal");
        }

        // Check if the RT array is properly sorted
        for (int pos = 1; pos < newSize; pos++) {
            if (rtBuffer[pos - 1].compareTo(rtBuffer[pos]) > 0)
                throw (new MSDKRuntimeException(
                        "The RT array is not properly sorted. It should be sorted from lowest to highest."));
        }

        // Update arrays
        this.rtBuffer = rtBuffer;
        this.intensityBuffer = intensityBuffer;

        // Update the size
        this.size = newSize;
    }

    /**
     * {@inheritDoc}
     *
     * Copy data from another DataPointList
     */
    @Override
    public void copyFrom(@Nonnull ChromatogramDataPointList list) {
        if (rtBuffer.length < list.getSize()) {
            rtBuffer = new ChromatographyInfo[list.getSize()];
            intensityBuffer = new float[list.getSize()];
        }

        // Copy data
        System.arraycopy(list.getRtBuffer(), 0, rtBuffer, 0, list.getSize());
        System.arraycopy(list.getIntensityBuffer(), 0, intensityBuffer, 0,
                list.getSize());

        // Update the size
        this.size = list.getSize();
    }

    /**
     * {@inheritDoc}
     *
     * Returns the RT range, assuming the RT array is sorted.
     */
    @Override
    @Nullable
    public Range<ChromatographyInfo> getRtRange() {
        if (size == 0)
            return null;
        return Range.closed(rtBuffer[0], rtBuffer[size - 1]);
    }

    /**
     * {@inheritDoc}
     *
     * Returns the current size of the array
     */
    @Override
    public int getSize() {
        return size;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the current size of the array
     */
    public void setSize(int newSize) {

        if (newSize < 0)
            throw new IllegalArgumentException("Size cannot be negative");

        if (newSize > rtBuffer.length)
            throw new MSDKRuntimeException(
                    "Not enough allocated space to change the size of data point list");

        this.size = newSize;

        // Ensure the arrays are sorted in m/z order
        if (newSize > 0)
            DataPointSorter.sortDataPoints(rtBuffer, intensityBuffer, size);
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
        if ((o == null) || (!(o instanceof ChromatogramDataPointList)))
            return false;

        // Cast o to DataPointlist
        ChromatogramDataPointList otherList = (ChromatogramDataPointList) o;

        // Size must be equal
        if (otherList.getSize() != size)
            return false;

        // Get the arrays of the other list
        final ChromatographyInfo otherRtBuffer[] = otherList.getRtBuffer();
        final float otherIntensityBuffer[] = otherList.getIntensityBuffer();

        // Check the array contents
        for (int i = 0; i < size; i++) {
            if (rtBuffer[i] != otherRtBuffer[i])
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
            long bits = Double.doubleToLongBits(rtBuffer[i].hashCode());
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
            builder.append(rtBuffer[i]);
            builder.append(":");
            builder.append(intensityBuffer[i]);
        }
        builder.append("]");
        return builder.toString();
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public ChromatogramDataPointList selectDataPoints(
            @Nonnull Range<ChromatographyInfo> rtRange,
            @Nonnull Range<Float> intensityRange) {

        final ChromatogramDataPointList newList = MSDKObjectBuilder
                .getChromatogramDataPointList();

        // Find how many data points will pass the conditions
        int numOfGoodDataPoints = 0;
        for (int i = 0; i < size; i++) {
            if (!rtRange.contains(rtBuffer[i]))
                continue;
            if (!intensityRange.contains(intensityBuffer[i]))
                continue;
            numOfGoodDataPoints++;
        }

        // Allocate space for the data points
        newList.allocate(numOfGoodDataPoints);
        final ChromatographyInfo newRtBuffer[] = newList.getRtBuffer();
        final float newIntensityBuffer[] = newList.getIntensityBuffer();

        // Copy the actual data point values
        int newIndex = 0;
        for (int i = 0; i < size; i++) {
            if (!rtRange.contains(rtBuffer[i]))
                continue;
            if (!intensityRange.contains(intensityBuffer[i]))
                continue;
            newRtBuffer[newIndex] = rtBuffer[i];
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

        if (rtBuffer.length >= newSize)
            return;

        ChromatographyInfo[] rtBufferNew = new ChromatographyInfo[newSize];
        float[] intensityBufferNew = new float[newSize];

        if (size > 0) {
            System.arraycopy(getRtBuffer(), 0, rtBufferNew, 0, size);
            System.arraycopy(getIntensityBuffer(), 0, intensityBufferNew, 0,
                    size);
        }

        rtBuffer = rtBufferNew;
        intensityBuffer = intensityBufferNew;

    }

    /** {@inheritDoc} */
    @Override
    public void add(@Nonnull ChromatographyInfo rt, float intensity) {

        // Make sure we have enough space to add a new data point
        if (size == rtBuffer.length) {
            allocate(size * 2);
        }

        // Add the data
        rtBuffer[size] = rt;
        intensityBuffer[size] = intensity;

        // If the RT value is larger than the last one, let's simply add it to
        // the end. Otherwise, we have to call the setSize() method that will
        // sort the lists in RT order
        if ((size == 0) || (rtBuffer[size].compareTo(rtBuffer[size - 1]) > 0)) {
            size++;
        } else {
            setSize(size + 1);
        }

    }

}
