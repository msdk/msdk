/* 
 * (C) Copyright 2015-2016 by MSDK Development Team
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

package io.github.msdk.io.mzml2;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Range;

import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.ActivationInfo;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.MsScanType;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.util.tolerances.MzTolerance;

public class MzMLSpectrum implements MsScan {
	private HashMap<String, String> cvParamValues;
	private MzMLBinaryDataInfo mzBinaryDataInfo;
	private MzMLBinaryDataInfo intensityBinaryDataInfo;
	private MappedByteBufferInputStream mappedByteBufferInputStream;

	public MzMLSpectrum() {
		cvParamValues = new HashMap<>();
	}

	public void add(String accession, String value) {
		cvParamValues.put(accession, value);
	}

	public HashMap<String, String> getSpectrumData() {
		return cvParamValues;
	}

	public int getSpectrumDataSize() {
		return cvParamValues.size();
	}

	public MzMLBinaryDataInfo getMzBinaryDataInfo() {
		return mzBinaryDataInfo;
	}

	public void setMzBinaryDataInfo(MzMLBinaryDataInfo mzBinaryDataInfo) {
		this.mzBinaryDataInfo = mzBinaryDataInfo;
	}

	public MzMLBinaryDataInfo getIntensityBinaryDataInfo() {
		return intensityBinaryDataInfo;
	}

	public void setIntensityBinaryDataInfo(MzMLBinaryDataInfo intensityBinaryDataInfo) {
		this.intensityBinaryDataInfo = intensityBinaryDataInfo;
	}

	public MappedByteBufferInputStream getMappedByteBufferInputStream() {
		return mappedByteBufferInputStream;
	}

	public void setMappedByteBufferInputStream(MappedByteBufferInputStream mappedByteBufferInputStream) {
		this.mappedByteBufferInputStream = mappedByteBufferInputStream;
	}

	// TODO Configure implemented methods
	@Override
	public MsSpectrumType getSpectrumType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getNumberOfDataPoints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getMzValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float[] getIntensityValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Float getTIC() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Range<Double> getMzRange() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MzTolerance getMzTolerance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RawDataFile getRawDataFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getScanNumber() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getScanDefinition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MsFunction getMsFunction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MsScanType getMsScanType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Range<Double> getScanningRange() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PolarityType getPolarity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActivationInfo getSourceInducedFragmentation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IsolationInfo> getIsolations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Float getRetentionTime() {
		// TODO Auto-generated method stub
		return null;
	}
}
