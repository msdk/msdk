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

import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.DataPoint;
import io.github.msdk.datamodel.rawdata.DataPointList;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsMsScan;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.MsScanType;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;

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
    private ChromatographyInfo chromData;
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
//	buf.append(getMSLevel());
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
    public void getDataPoints(DataPointList list) {
	// TODO Auto-generated method stub
	
    }


    @Override
    @Nonnull
    public List<DataPoint> getDataPointsByIntensity(
	    @Nonnull Range<Double> intensityRange) {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public void setDataPoints(@Nonnull DataPointList newDataPoints) {
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
    public RawDataFile getRawDataFile() {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public void setRawDataFile(@Nonnull RawDataFile newDataFile) {
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
    @Nonnull
    public MsFunction getMsFunction() {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public void setMsFunction(@Nonnull MsFunction newFunction) {
	// TODO Auto-generated method stub
	
    }


    @Override
    @Nullable
    public ChromatographyInfo getChromatographyData() {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public void setChromatographyData(@Nullable ChromatographyInfo chromData) {
	// TODO Auto-generated method stub
	
    }


    @Override
    @Nullable
    public Range<Double> getScanningRange() {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public void setScanningRange(@Nullable Range<Double> newScanRange) {
	// TODO Auto-generated method stub
	
    }


    @Override
    @Nonnull
    public PolarityType getPolarity() {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public void setPolarity(@Nonnull PolarityType newPolarity) {
	// TODO Auto-generated method stub
	
    }


    @Override
    public MsScan clone() {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    @Nonnull
    public MsScanType getMsScanType() {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public void setMsScanType(@Nonnull MsScanType newType) {
	// TODO Auto-generated method stub
	
    }


}
