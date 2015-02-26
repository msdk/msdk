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

import io.github.msdk.datamodel.rawdata.IChromatographyData;
import io.github.msdk.datamodel.rawdata.IDataPoint;
import io.github.msdk.datamodel.rawdata.IDataPointList;
import io.github.msdk.datamodel.rawdata.IMsFunction;
import io.github.msdk.datamodel.rawdata.IMsMsScan;
import io.github.msdk.datamodel.rawdata.IMsScan;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.IRawDataFile;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

/**
 * Simple implementation of the Scan interface.
 */
public class MsScanImpl extends SpectrumImpl implements IMsScan {

    private final IRawDataFile dataFile;
    private Integer scanNumber, msLevel;
    private IChromatographyData chromData;
    private final List<IMsMsScan> fragmentScans;

    public MsScanImpl(@Nonnull IRawDataFile dataFile) {
	super((RawDataFileImpl) dataFile);
	this.dataFile = dataFile;
	this.fragmentScans = new ArrayList<IMsMsScan>();
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
    public void getDataPoints(IDataPointList list) {
	// TODO Auto-generated method stub
	
    }


    @Override
    @Nonnull
    public List<IDataPoint> getDataPointsByIntensity(
	    @Nonnull Range<Double> intensityRange) {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public void setDataPoints(@Nonnull IDataPointList newDataPoints) {
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
    public IRawDataFile getRawDataFile() {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public void setRawDataFile(@Nonnull IRawDataFile newDataFile) {
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
    public IMsFunction getMsFunction() {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public void setMsFunction(@Nonnull IMsFunction newFunction) {
	// TODO Auto-generated method stub
	
    }


    @Override
    @Nullable
    public IChromatographyData getChromatographyData() {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public void setChromatographyData(@Nullable IChromatographyData chromData) {
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
    public IMsScan clone() {
	// TODO Auto-generated method stub
	return null;
    }


}
