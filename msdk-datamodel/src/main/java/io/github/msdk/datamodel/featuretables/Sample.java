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
 * 
 */
public interface Sample {

    /**
     * @return Short descriptive name
     */
    @Nonnull
    String getName();

    /**
     * Change the name
     */
    void setName(@Nonnull String name);

    /**
     * Returns a raw data file or null if this sample has no associated raw
     * data.
     */
    @Nullable
    RawDataFile getRawDataFile();

    /**
     * Sets the raw data file reference.
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
     */
    void setOriginalFile(@Nullable File originalFile);

}
