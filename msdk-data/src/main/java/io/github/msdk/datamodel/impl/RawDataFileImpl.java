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

import io.github.msdk.datamodel.rawdata.IMsScan;
import io.github.msdk.datamodel.rawdata.IRawDataFile;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;

/**
 * RawDataFile implementation.
 */
class RawDataFileImpl extends DataPointStoreImpl implements IRawDataFile {

    private @Nonnull String rawDataFileName;
    private @Nonnull IRawDataFile originalRawDataFile;
    private final List<IMsScan> scans;

    RawDataFileImpl() {
	rawDataFileName = "New file";
	originalRawDataFile = this;
	scans = new ArrayList<IMsScan>();
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
    public void addScan(@Nonnull IMsScan scan) {
	// TODO Auto-generated method stub

    }

    @Override
    public void removeScan(@Nonnull IMsScan scan) {
	// TODO Auto-generated method stub

    }

    @Override
    public @Nonnull List<IMsScan> getScans() {
	// TODO Auto-generated method stub
	return Lists.newArrayList();
    }

    @Override
    public @Nonnull List<IMsScan> getScans(@Nonnull Integer msLevel, @Nonnull Range<Double> rtRange) {
	// TODO Auto-generated method stub
	return Lists.newArrayList();
    }

    @Override
    public @Nonnull List<Integer> getMSLevels() {
	// TODO Auto-generated method stub
	return Lists.newArrayList();
    }

    @Override
    public IMsScan getScan(int scanNumber) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public @Nonnull Range<Double> getRawDataMZRange() {
	// TODO Auto-generated method stub
	return Range.all();
    }

    @Override
    public @Nonnull Range<Double> getRawDataScanRange() {
	// TODO Auto-generated method stub
	return Range.all();
    }

    @Override
    public @Nonnull Range<Double> getRawDataRTRange() {
	// TODO Auto-generated method stub
	return Range.all();
    }

    @Override
    public @Nonnull Range<Double> getRawDataMZRange(@Nonnull Integer msLevel) {
	// TODO Auto-generated method stub
	return Range.all();
    }

    @Override
    public @Nonnull Range<Double> getRawDataScanRange(@Nonnull Integer msLevel) {
	// TODO Auto-generated method stub
	return Range.all();
    }

    @Override
    public @Nonnull Range<Double> getRawDataRTRange(@Nonnull Integer msLevel) {
	// TODO Auto-generated method stub
	return Range.all();
    }

}