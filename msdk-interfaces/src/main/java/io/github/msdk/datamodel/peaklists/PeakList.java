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

package io.github.msdk.datamodel.peaklists;

import io.github.msdk.datamodel.rawdata.RawDataFile;

import java.util.List;

/* 
 * WARNING: the interfaces in this package are still under construction
 */

/**
 * 
 */
public interface PeakList {

    /**
     * @return Short descriptive name for the peak list
     */
    String getName();

    /**
     * Change the name of this peak list
     */
    void setName(String name);

    /**
     * Returns number of raw data files participating in the peak list
     */
    int getNumberOfRawDataFiles();

    /**
     * Returns all raw data files participating in the peak list
     */
    List<RawDataFile> getRawDataFiles();

    /**
     * Returns true if this peak list contains given file
     */
    boolean hasRawDataFile(RawDataFile file);

    /**
     * Returns a raw data file
     * 
     * @param position
     *            Position of the raw data file in the matrix (running numbering
     *            from left 0,1,2,...)
     */
    RawDataFile getRawDataFile(int position);

    /**
     * Returns number of rows in the alignment result
     */
    int getNumberOfRows();

    /**
     * Returns all peak list rows
     */
    List<PeakListRow> getRows();

    /**
     * Add a new row to the peak list
     */
    void addRow(PeakListRow row);

    /**
     * Removes a row from this peak list
     * 
     */
    void removeRow(PeakListRow row);

    /**
     * Remove all data associated to this peak list from the disk.
     */
    void dispose();

}
