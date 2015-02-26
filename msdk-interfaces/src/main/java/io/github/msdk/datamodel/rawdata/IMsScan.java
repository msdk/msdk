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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

/**
 * Represents a single MS scan in a raw data file. This interface extends
 * MassSpectrum, therefore the actual data points can be accessed through the
 * inherited methods of MassSpectrum.
 */
public interface IMsScan extends IMassSpectrum, Cloneable {

    /**
     * Returns the raw data file that contains this scan. Each MsScan must be
     * associated with a RawDataFile from the moment its instance is created,
     * and this association cannot change. Note that the MsScan will not appear
     * in the list returned by RawDataFile.getScans() unless it is explicitly
     * added by RawDataFile.addScan().
     * 
     * @return RawDataFile containing this MsScan.
     * @see IRawDataFile
     */
    @Nonnull
    IRawDataFile getDataFile();

    /**
     * Returns the number of this scan, represented by an integer, typically
     * positive. In most cases, the scan number will be unique within the file.
     * However, the data model does not guarantee that, and some cases might
     * thus exist where multiple scans with the same number are present in the
     * file.
     * 
     * @return Scan number
     */
    @Nonnull
    Integer getScanNumber();

    /**
     * Returns the chromatography data associated with this scan. Null is
     * returned if no chromatography data is available.
     * 
     * @return Associated chromatography data.
     */
    @Nullable
    IChromatographyData getChromatographyData();

    /**
     * Updates the associated chromatography data.
     * 
     * @param chromData
     */
    void setChromatographyData(@Nullable IChromatographyData chromData);

    /**
     * Updates the scan number.
     * 
     * @param scanNumber
     *            New scan number.
     */
    void setScanNumber(@Nonnull Integer scanNumber);

    /**
     * 
     * @return MS level
     */
    @Nonnull
    IMsFunction getMsFunction();

    /**
     * This is different from MassSpectrum.getMzRange() - EXPLAIN
     * @return the actual scanning range of the instrument
     */
    @Nullable
    Range<Double> getScanningRange();

    /**
     * 
     * @param newScanRange
     */
    void setScanRange(@Nullable Range<Double> newScanRange);

    /**
     * If unknown, PolarityType.UNKNOWN is returned.
     * 
     * @return
     */
    @Nonnull
    IPolarityType getPolarity();

    void setPolarity(@Nonnull IPolarityType newPolarity);

    /**
     * Returns a deep clone of this object.
     * 
     * @return A clone.
     */
    IMsScan clone();

}
