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
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.files.FileType;

/**
 * Raw data file, typically obtained by loading data from one of the supported
 * file formats. A raw data file is a collection of scans (MsScan).
 *
 * @see MsScan
 */
public interface RawDataFile {

    /**
     * Returns the name of this raw data file. This can be any descriptive name,
     * not necessarily the original file name.
     *
     * @return Raw data file name
     */
    @Nonnull
    String getName();

    /**
     * Updates the name of this raw data file.
     *
     * @param name
     *            New name
     */
    void setName(@Nonnull String name);

    /**
     * Returns the original file name and path where the file was loaded from,
     * or null if this file was created by MSDK.
     *
     * @return Original filename and path.
     */
    @Nullable
    File getOriginalFile();

    /**
     * Sets the original file name and path of the raw data file.
     *
     * @param newOriginalFile
     *          Original filename and path.
     */
    void setOriginalFile(@Nullable File newOriginalFile);
    
    /**
     * Returns the file type of this raw data file.
     *
     * @return Raw data file type
     */
    @Nonnull
    FileType getRawDataFileType();

    /**
     * Sets the file type of this raw data file.
     *
     * @param rawDataFileType
     *          Raw data file type
     */
    void setRawDataFileType(@Nonnull FileType rawDataFileType);
    
    /**
     * Returns all MS functions found in this raw data file.
     *
     * @return A list of MS functions.
     */
    @Nonnull
    List<MsFunction> getMsFunctions();

    /**
     * Returns an immutable list of all scans. The list can be safely iterated
     * over, as it cannot be modified by another thread.
     *
     * @return A list of all scans.
     */
    @Nonnull
    List<MsScan> getScans();

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
     * Returns an immutable list of all chromatograms. The list can be safely
     * iterated over, as it cannot be modified by another thread.
     *
     * @return A list of all chromatograms.
     */
    @Nonnull
    List<Chromatogram> getChromatograms();

    /**
     * Adds a new chromatogram to this file.
     *
     * @param chromatogram a {@link io.github.msdk.datamodel.chromatograms.Chromatogram} object.
     */
    void addChromatogram(@Nonnull Chromatogram chromatogram);

    /**
     * Removes a chromatogram from this file.
     *
     * @param chromatogram a {@link io.github.msdk.datamodel.chromatograms.Chromatogram} object.
     */
    void removeChromatogram(@Nonnull Chromatogram chromatogram);

    /**
     * Remove all data associated with this file from the disk. After this
     * method is called, any subsequent method calls on this object will throw
     * IllegalStateException.
     */
    void dispose();

}
