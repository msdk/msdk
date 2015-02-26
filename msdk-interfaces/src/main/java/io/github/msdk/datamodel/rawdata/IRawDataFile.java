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

package io.github.msdk.datamodel.rawdata;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

/**
 * Raw data file
 */
public interface IRawDataFile {

    /**
     * Returns the name of this data file (can be a descriptive name, not
     * necessarily the original file name)
     * 
     * @return
     */

    @Nonnull
    String getName();

    /**
     * Change the name of this data file
     */
    void setName(@Nonnull String name);

    @Nonnull
    List<IMsFunction> getMsFunctions();

    /**
     * 
     */
    void addScan(@Nonnull IMsScan scan);

    /**
     * 
     * @param scan
     */
    void removeScan(@Nonnull IMsScan scan);

    /**
     * 
     * @return
     */
    int getNumberOfScans();
    
    /**
     * Returns an immutable list of all scans. The list can be safely iterated
     * on, as it cannot be modified by another thread.
     */
    @Nonnull
    List<IMsScan> getScans();

    /**
     * 
     * @param function
     * @return
     */
    @Nonnull
    List<IMsScan> getScans(IMsFunction function);



    /**
     * Returns an immutable list of all scans. The list can be safely iterated
     * over, as it cannot be modified by another thread.
     */
    @Nonnull
    List<IMsScan> getScans(
	    @Nonnull Range<IChromatographyData> chromatographyRange);
    
    /**
     * Returns an immutable list of all scans. The list can be safely iterated
     * over, as it cannot be modified by another thread.
     */
    @Nonnull
    List<IMsScan> getScans(@Nonnull IMsFunction function,
	    @Nonnull Range<IChromatographyData> chromatographyRange);

    /**
     * Important: index != scan number. Index is unique, 1-based index in the
     * file. If a scan is removed from the file, the index of other scans
     * automatically shifts.
     * 
     * @param scan
     *            Desired scan number
     * @return Desired scan, or null if no scan exists with that number
     */
    @Nullable
    IMsScan getScanByIndex(Integer index);

    /**
     * 
     * @return
     */
    @Nonnull
    Range<Double> getRawDataMzRange();

    /**
     * 
     * @return
     */
    @Nonnull
    Range<Double> getRawDataScanningRange();

    @Nonnull
    Range<Double> getRawDataRTRange();

    @Nonnull
    Range<Double> getRawDataMZRange(@Nonnull Integer msLevel);

    @Nonnull
    Range<Double> getRawDataScanRange(@Nonnull Integer msLevel);

    @Nonnull
    Range<Double> getRawDataRTRange(@Nonnull Integer msLevel);

    /**
     * Remove all data associated with this file from the disk. After this
     * method is called, any subsequent method calls on this object will throw
     * IllegalStateException.
     */
    void dispose();
}
