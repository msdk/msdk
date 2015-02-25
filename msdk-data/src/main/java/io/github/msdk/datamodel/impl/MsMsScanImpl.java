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

import io.github.msdk.datamodel.MsMsExperimentType;
import io.github.msdk.datamodel.MsMsScan;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.RawDataFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

/**
 * Simple implementation of the Scan interface.
 */
public class MsMsScanImpl extends MsScanImpl implements MsMsScan {

    private MsScan parentScan;
    private Double precursorMZ;
    private Integer precursorCharge;
    private Double activationEnergy;

    public MsMsScanImpl(RawDataFile dataFile) {
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
    public Double getPrecursorMz() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setPrecursorMz(@Nullable Double precursorMZ) {
	// TODO Auto-generated method stub
	
    }

    @Override
    @Nullable
    public Range<Double> getIsolationWidth() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setIsolationWidth(@Nullable Range<Double> isolationWidth) {
	// TODO Auto-generated method stub
	
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
    @Nonnull
    public MsMsExperimentType getExperimentType() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setExperimentType(@Nonnull MsMsExperimentType newType) {
	// TODO Auto-generated method stub
	
    }



}
