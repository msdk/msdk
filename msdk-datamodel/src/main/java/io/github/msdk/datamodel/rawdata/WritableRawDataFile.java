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
     * {@inheritDoc}
     *
     * Updates the original filename.
     */
    void setOriginalFile(@Nullable File newOriginalFile);

    /**
     * {@inheritDoc}
     *
     * Updates the file type of this raw data file.
     */
    void setRawDataFileType(@Nonnull FileType rawDataFileType);
    
    /**
     * {@inheritDoc}
     *
     * Adds a new scan to this file.
     */
    void addScan(@Nonnull MsScan scan);

    /**
     * {@inheritDoc}
     *
     * Removes a scan from this file.
     */
    void removeScan(@Nonnull MsScan scan);

    /**
     * {@inheritDoc}
     *
     * Adds a new chromatogram to this file.
     */
    void addChromatogram(@Nonnull Chromatogram chromatogram);

    /**
     * {@inheritDoc}
     *
     * Removes a chromatogram from this file.
     */
    void removeChromatogram(@Nonnull Chromatogram chromatogram);

}
