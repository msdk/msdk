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
import io.github.msdk.datamodel.MassSpectrumType;
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
    public int getPrecursorCharge() {
	return precursorCharge;
    }

    @Override
    public void setPrecursorCharge(int precursorCharge) {
	this.precursorCharge = precursorCharge;
    }

    @Override
    public MsScan getParentScan() {
	return parentScan;
    }

    @Override
    public void setParentScan(@Nullable MsScan parentScan) {
	this.parentScan = parentScan;
    }

    @Override
    public double getActivationEnergy() {
	return activationEnergy;
    }

    @Override
    public void setActivationEnergy(double activationEnergy) {
	this.activationEnergy = activationEnergy;
    }

    @Override
    public ChromatographyData getChromatographyData() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public @Nonnull MassSpectrumType getSpectrumType() {
	// TODO Auto-generated method stub
	return MassSpectrumType.CENTROIDED;
    }

    @Override
    public Double getPrecursorMz() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Range<Double> getIsolationWidth() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setPrecursorMz(@Nullable Double precursorMZ) {
	// TODO Auto-generated method stub

    }

}
