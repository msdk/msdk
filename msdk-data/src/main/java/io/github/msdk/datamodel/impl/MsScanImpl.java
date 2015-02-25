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

import io.github.msdk.datamodel.ChromatographyData;
import io.github.msdk.datamodel.MsMsScan;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.PolarityType;
import io.github.msdk.datamodel.RawDataFile;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

/**
 * Simple implementation of the Scan interface.
 */
public class MsScanImpl extends SpectrumImpl implements MsScan {

    private final RawDataFile dataFile;
    private Integer scanNumber, msLevel;
    private ChromatographyData chromData;
    private final List<MsMsScan> fragmentScans;

    public MsScanImpl(@Nonnull RawDataFile dataFile) {
	super((RawDataFileImpl) dataFile);
	this.dataFile = dataFile;
	this.fragmentScans = new ArrayList<MsMsScan>();
    }


    @Override
    public String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append("#");
	buf.append(getScanNumber());
	buf.append(" @");
	// buf.append(rtFormat.format(getRetentionTime()));
	buf.append(" MS");
	buf.append(getMSLevel());
	switch (getSpectrumType()) {
	case CENTROIDED:
	    buf.append(" c");
	    break;
	case PROFILE:
	    buf.append(" p");
	    break;
	case THRESHOLDED:
	    buf.append(" t");
	    break;
	}

	return buf.toString();
    }


    @Override
    @Nonnull
    public RawDataFile getDataFile() {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    @Nullable
    public ChromatographyData getChromatographyData() {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public void setChromatographyData(@Nullable ChromatographyData chromData) {
	// TODO Auto-generated method stub
	
    }


    @Override
    @Nonnull
    public Integer getScanNumber() {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public void setScanNumber(@Nonnull Integer scanNumber) {
	// TODO Auto-generated method stub
	
    }


    @Override
    @Nullable
    public Integer getMSLevel() {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public void setMSLevel(@Nullable Integer msLevel) {
	// TODO Auto-generated method stub
	
    }


    @Override
    @Nonnull
    public Double getTIC() {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    @Nullable
    public Range<Double> getScanRange() {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    @Nonnull
    public PolarityType getPolarity() {
	// TODO Auto-generated method stub
	return null;
    }
}
