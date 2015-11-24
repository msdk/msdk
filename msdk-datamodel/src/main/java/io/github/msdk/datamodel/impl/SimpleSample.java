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

package io.github.msdk.datamodel.impl;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.rawdata.RawDataFile;

/**
 * Implementation of Sample
 */
class SimpleSample implements Sample {

    private @Nonnull String name;
    private RawDataFile rawDataFile;
    private File originalFile;

    SimpleSample(@Nonnull String name) {
        this(name, null);
    }

    SimpleSample(@Nonnull String name, @Nullable RawDataFile rawDataFile) {
        Preconditions.checkNotNull(name);
        this.name = name;
        this.rawDataFile = rawDataFile;
    }

    /** {@inheritDoc} */
    @Override
    public @Nonnull String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public void setName(@Nonnull String name) {
        Preconditions.checkNotNull(name);
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable RawDataFile getRawDataFile() {
        return rawDataFile;
    }

    /** {@inheritDoc} */
    @Override
    public void setRawDataFile(@Nullable RawDataFile rawDataFile) {
        this.rawDataFile = rawDataFile;
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable File getOriginalFile() {
        return originalFile;
    }

    /** {@inheritDoc} */
    @Override
    public void setOriginalFile(@Nullable File originalFile) {
        this.originalFile = originalFile;
    }

}
