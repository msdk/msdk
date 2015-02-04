/* 
 * Copyright 2015 MSDK Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.github.msdevkit.datamodel.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.msdevkit.datamodel.ChromatographyData;
import com.github.msdevkit.datamodel.MassSpectrumType;
import com.github.msdevkit.datamodel.MsMsScan;
import com.github.msdevkit.datamodel.MsScan;
import com.github.msdevkit.datamodel.RawDataFile;
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
