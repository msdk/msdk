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

package io.github.msdk.datamodel;

import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;

/**
 * RawDataFile implementation.
 */
class SimpleRawDataFile implements RawDataFile {

    private @Nonnull String rawDataFileName;
    private @Nonnull RawDataFile originalRawDataFile;
    private final List<MsScan> scans;

    SimpleRawDataFile() {
        rawDataFileName = "New file";
        originalRawDataFile = this;
        scans = new ArrayList<MsScan>();
    }

    @Override
    public @Nonnull String getName() {
        // TODO Auto-generated method stub
        return "";
    }

    @Override
    public void setName(@Nonnull String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addScan(@Nonnull MsScan scan) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeScan(@Nonnull MsScan scan) {
        // TODO Auto-generated method stub

    }

    @Override
    public @Nonnull List<MsScan> getScans() {
        // TODO Auto-generated method stub
        return Lists.newArrayList();
    }

    @Override
    @Nullable
    public File getOriginalFile() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setOriginalFile(@Nullable File newOriginalFile) {
        // TODO Auto-generated method stub

    }

    @Override
    @Nonnull
    public List<MsFunction> getMsFunctions() {
        // TODO Auto-generated method stub
        return new ArrayList<MsFunction>();
    }

    @Override
    public int getNumberOfScans() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    @Nonnull
    public List<MsScan> getScans(MsFunction function) {
        // TODO Auto-generated method stub
        return new ArrayList<MsScan>();
    }

    @Override
    @Nonnull
    public List<MsScan> getScans(
            @Nonnull Range<ChromatographyInfo> chromatographyRange) {
        // TODO Auto-generated method stub
        return new ArrayList<MsScan>();
    }

    @Override
    @Nonnull
    public List<MsScan> getScans(@Nonnull MsFunction function,
            @Nonnull Range<ChromatographyInfo> chromatographyRange) {
        // TODO Auto-generated method stub
        return new ArrayList<MsScan>();
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

}