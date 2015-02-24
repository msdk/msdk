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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

/**
 * Raw data file
 */
public interface RawDataFile {

    /**
     * Returns the name of this data file (can be a descriptive name, not
     * necessarily the original file name)
     */
    @Nonnull
    String getName();

    /**
     * Change the name of this data file
     */
    void setName(@Nonnull String name);

    void addScan(@Nonnull MsScan scan);

    void removeScan(@Nonnull MsScan scan);

    /**
     * Returns an immutable list of all scans. The list can be safely iterated
     * on, as it cannot be modified by another thread.
     */
    @Nonnull
    List<MsScan> getScans();

    /**
     * Returns an immutable list of all scans. The list can be safely iterated
     * on, as it cannot be modified by another thread.
     */
    @Nonnull
    List<MsScan> getScans(@Nonnull Integer msLevel,
	    @Nonnull Range<Double> rtRange);

    /**
     * Returns immutable list of MS levels of scans in this file. Items in the
     * list are sorted in ascending order.
     */
    @Nonnull
    List<Integer> getMSLevels();

    /**
     * 
     * @param scan
     *            Desired scan number
     * @return Desired scan, or null if no scan exists with that number
     */
    @Nullable
    MsScan getScan(int scanNumber);

    @Nonnull
    Range<Double> getRawDataMZRange();

    @Nonnull
    Range<Double> getRawDataScanRange();

    @Nonnull
    Range<Double> getRawDataRTRange();

    @Nonnull
    Range<Double> getRawDataMZRange(@Nonnull Integer msLevel);

    @Nonnull
    Range<Double> getRawDataScanRange(@Nonnull Integer msLevel);

    @Nonnull
    Range<Double> getRawDataRTRange(@Nonnull Integer msLevel);

    /**
     * Remove all data associated to this file from the disk.
     */
    void dispose();
}
