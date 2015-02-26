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

import io.github.msdk.datamodel.rawdata.IMsMsScan;
import io.github.msdk.datamodel.rawdata.IMsScan;
import io.github.msdk.datamodel.rawdata.IRawDataFile;
import io.github.msdk.datamodel.rawdata.MsMsExperimentType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

/**
 * Simple implementation of the Scan interface.
 */
public class MsMsScanImpl extends MsScanImpl implements IMsMsScan {

    private IMsScan parentScan;
    private Double precursorMZ;
    private Integer precursorCharge;
    private Double activationEnergy;

    public MsMsScanImpl(IRawDataFile dataFile) {
	super(dataFile);
    }

    @Override
    @Nonnull
    public Double getTIC() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    @Nullable
    public Double getActivationEnergy() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setActivationEnergy(@Nullable Double activationEnergy) {
	// TODO Auto-generated method stub
	
    }

    @Override
    @Nullable
    public Integer getPrecursorCharge() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setPrecursorCharge(@Nullable Integer charge) {
	// TODO Auto-generated method stub
	
    }


    @Override
    @Nullable
    public Range<Double> getPrecursorMzRange() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setPrecursorMzRange(@Nullable Range<Double> precursorMzRange) {
	// TODO Auto-generated method stub
	
    }

    @Override
    @Nonnull
    public MsMsExperimentType getMsMsExperimentType() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setMsMsExperimentType(@Nonnull MsMsExperimentType newType) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public IMsMsScan clone() {
	// TODO Auto-generated method stub
	return null;
    }



}
