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

package io.github.msdk.datamodel.featuretables;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.datamodel.rawdata.RawDataFile;

/**
 * <p>Sample interface.</p>
 */
public interface Sample {

    /**
     * <p>getName.</p>
     *
     * @return Short descriptive name
     */
    @Nonnull
    String getName();

    /**
     * Change the name
     *
     * @param name a {@link java.lang.String} object.
     */
    void setName(@Nonnull String name);

    /**
     * Returns a raw data file or null if this sample has no associated raw
     * data.
     *
     * @return a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
     */
    @Nullable
    RawDataFile getRawDataFile();

    /**
     * Sets the raw data file reference.
     *
     * @param rawDataFile a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
     */
    void setRawDataFile(@Nullable RawDataFile rawDataFile);

    /**
     * Returns the original file name and path where the file was loaded from,
     * or null if this file was created by MSDK.
     *
     * @return Original filename and path.
     */
    @Nullable
    File getOriginalFile();

    /**
     * Sets the original file
     *
     * @param originalFile a {@link java.io.File} object.
     */
    void setOriginalFile(@Nullable File originalFile);

}
