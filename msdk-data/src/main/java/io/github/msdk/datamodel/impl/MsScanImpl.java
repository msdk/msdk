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

    /**
     * @see io.github.msdk.datamodel.MsScan#getScanNumber()
     */
    @Override
    public @Nonnull Integer getScanNumber() {
	return scanNumber;
    }

    @Override
    public void setScanNumber(@Nonnull Integer scanNumber) {
	this.scanNumber = scanNumber;
    }

    /**
     * @param scanNumber
     *            The scanNumber to set.
     */
    public void setScanNumber(int scanNumber) {
	this.scanNumber = scanNumber;
    }

    @Override
    public @Nullable Integer getMSLevel() {
	return msLevel;
    }

    @Override
    public void setMSLevel(@Nullable Integer msLevel) {
	this.msLevel = msLevel;
    }

    /**
     * @see io.github.msdk.datamodel.MsScan#getFragmentScanNumbers()
     */
    @Override
    public @Nonnull List<MsMsScan> getFragmentScans() {
	return fragmentScans;
    }

    @Override
    public @Nonnull RawDataFile getDataFile() {
	return dataFile;
    }

    @Override
    public @Nonnull PolarityType getPolarity() {
	return PolarityType.UNKNOWN;
    }

    @Override
    public Range<Double> getScanRange() {
	return null;
    }

    @Override
    public double getTIC() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public ChromatographyData getChromatographyData() {
	return chromData;
    }

    @Override
    public void setChromatographyData(@Nullable ChromatographyData chromData) {
	this.chromData = chromData;
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
}
