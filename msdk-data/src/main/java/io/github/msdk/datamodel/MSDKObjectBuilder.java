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

import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.DataPoint;
import io.github.msdk.datamodel.rawdata.DataPointList;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;

import javax.annotation.Nonnull;

/**
 * Object builder
 */
public class MSDKObjectBuilder {

    public static final @Nonnull DataPoint getDataPoint(double mz,
            float intensity) {
        return new SimpleDataPoint(mz, intensity);
    }

    public static final @Nonnull RawDataFile getRawDataFile() {
        return new SimpleRawDataFile();
    }

    public static final @Nonnull MsScan getMsScan(@Nonnull RawDataFile dataFile) {
        return new SimpleMsScan(dataFile);
    }

    public static final @Nonnull ChromatographyInfo getChromatographyInfo1D(
            SeparationType separationType, float rt1) {
        return new SimpleChromatographyInfo(rt1, null, null, separationType);
    }

    public static final @Nonnull ChromatographyInfo getChromatographyInfo2D(
            SeparationType separationType, float rt1, float rt2) {
        if (separationType.getFeatureDimensions() < 2) {
            throw new IllegalArgumentException(
                    "2D ChromatographyInfo requires at least 2 feature dimensions. Provided separation type "
                            + separationType
                            + " has "
                            + separationType.getFeatureDimensions());
        }
        // TODO add further validation
        return new SimpleChromatographyInfo(rt1, rt2, null, separationType);
    }

    public static final @Nonnull ChromatographyInfo getImsInfo(
            SeparationType separationType, float rt1, float ionDriftTime) {
        if (separationType.getFeatureDimensions() < 2) {
            throw new IllegalArgumentException(
                    "2D ChromatographyInfo requires at least 2 feature dimensions. Provided separation type "
                            + separationType
                            + " has "
                            + separationType.getFeatureDimensions());
        }
        if (separationType != SeparationType.IMS) {
            throw new IllegalArgumentException(
                    "2D ChromatographyInfo for IMS separation requires IMS separation type. Provided separation type "
                            + separationType);
        }
        return new SimpleChromatographyInfo(rt1, null, ionDriftTime,
                separationType);
    }

    /**
     * Creates a new DataPointList instance with no data points and initial
     * capacity of 100.
     * 
     * @return New DataPointList
     */
    public static final @Nonnull DataPointList getDataPointList() {
        return new DataPointArrayList(100);
    }

    /**
     * Creates a new DataPointList instance with no data points and given
     * initial capacity.
     * 
     * @param capacity
     *            Initial capacity of the list
     * @return New DataPointList
     */
    public static final @Nonnull DataPointList getDataPointList(
            @Nonnull Integer capacity) {
        return new DataPointArrayList(capacity);
    }

    /**
     * Creates a clone of a given DataPointList, with a capacity set to the
     * current size of the source list.
     * 
     * @param sourceList
     *            Source DataPointList that
     * @return Cloned DataPointList
     */
    public static final @Nonnull DataPointList getDataPointList(
            @Nonnull DataPointList sourceList) {
        return getDataPointList(sourceList, sourceList.size());

    }

    /**
     * Creates a clone of a given DataPointList, with a given capacity.
     * 
     * @param sourceList
     *            Source DataPointList that
     * @param capacity
     *            Initial capacity of the list. Must be equal or higher than
     *            sourceList.size()
     * @return Cloned DataPointList
     */
    public static final @Nonnull DataPointList getDataPointList(
            @Nonnull DataPointList sourceList, @Nonnull Integer capacity) {

        if (capacity < sourceList.size())
            throw new IllegalArgumentException(
                    "Requested capacity must be >= size of the source list");

        // Create the new list
        final DataPointList newList = getDataPointList(capacity);

        // Copy data
        System.arraycopy(sourceList.getMzBuffer(), 0, newList.getMzBuffer(), 0,
                sourceList.size());
        System.arraycopy(sourceList.getIntensityBuffer(), 0,
                newList.getIntensityBuffer(), 0, sourceList.size());

        return newList;

    }
}
