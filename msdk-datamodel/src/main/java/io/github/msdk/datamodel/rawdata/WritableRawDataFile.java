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

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.files.FileType;

/**
 * A writable raw data file.
 * 
 * @see RawDataFile
 */
public interface WritableRawDataFile extends RawDataFile {

    /**
     * Updates the original filename.
     * 
     * @param newOriginalFile
     *            New original filename.
     */
    void setOriginalFile(@Nullable File newOriginalFile);

    /**
     * Updates the file type of this raw data file.
     * 
     * @param FileType
     *            New file type
     */
    void setRawDataFileType(@Nonnull FileType rawDataFileType);
    
    /**
     * Adds a new scan to this file.
     * 
     * @param scan
     *            Scan to add.
     */
    void addScan(@Nonnull MsScan scan);

    /**
     * Removes a scan from this file.
     * 
     * @param scan
     *            Scan to remove.
     */
    void removeScan(@Nonnull MsScan scan);

    /**
     * Adds a new chromatogram to this file.
     * 
     * @param scan
     *            Scan to add.
     */
    void addChromatogram(@Nonnull Chromatogram chromatogram);

    /**
     * Removes a chromatogram from this file.
     * 
     * @param scan
     *            Scan to remove.
     */
    void removeChromatogram(@Nonnull Chromatogram chromatogram);

}
