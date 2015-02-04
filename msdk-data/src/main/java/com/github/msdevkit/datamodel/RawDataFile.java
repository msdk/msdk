/* 
 * Copyright 2015 MSDK Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.github.msdevkit.datamodel;

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
