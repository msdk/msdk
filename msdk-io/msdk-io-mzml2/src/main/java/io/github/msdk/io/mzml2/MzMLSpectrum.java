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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Range;

import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.ActivationInfo;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.MsScanType;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.util.tolerances.MzTolerance;

public class MzMLSpectrum implements MsScan {
	private HashMap<String, String> spectrumData;
	private ArrayList<Long> binaryDataPositions;

	public MzMLSpectrum() {
		spectrumData = new HashMap<>();
		binaryDataPositions = new ArrayList<>();
	}

	public void add(String accession, String value) {
		spectrumData.put(accession, value);
	}

	public HashMap<String, String> getSpectrumData() {
		return spectrumData;
	}

	public int getSpectrumDataSize() {
		return spectrumData.size();
	}

	public ArrayList<Long> getBinaryDataPositions() {
		return binaryDataPositions;
	}

	public void addBinaryDataPosition(long pos) {
		binaryDataPositions.add(pos);
	}

	// TODO Configure implemented methods
	@Override
	public MsSpectrumType getSpectrumType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSpectrumType(MsSpectrumType spectrumType) {
		// TODO Auto-generated method stub

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
	public double[] getMzValues(double[] array) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float[] getIntensityValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float[] getIntensityValues(float[] array) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDataPoints(double[] mzValues, float[] intensityValues, Integer size) {
		// TODO Auto-generated method stub

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
	public void setRawDataFile(RawDataFile newRawDataFile) {
		// TODO Auto-generated method stub

	}

	@Override
	public Integer getScanNumber() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setScanNumber(Integer scanNumber) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getScanDefinition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setScanDefinition(String scanDefinition) {
		// TODO Auto-generated method stub

	}

	@Override
	public MsFunction getMsFunction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMsFunction(MsFunction newFunction) {
		// TODO Auto-generated method stub

	}

	@Override
	public MsScanType getMsScanType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMsScanType(MsScanType newMsScanType) {
		// TODO Auto-generated method stub

	}

	@Override
	public ChromatographyInfo getChromatographyInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setChromatographyInfo(ChromatographyInfo chromatographyInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public Range<Double> getScanningRange() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setScanningRange(Range<Double> newScanRange) {
		// TODO Auto-generated method stub

	}

	@Override
	public PolarityType getPolarity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPolarity(PolarityType newPolarity) {
		// TODO Auto-generated method stub

	}

	@Override
	public ActivationInfo getSourceInducedFragmentation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSourceInducedFragmentation(ActivationInfo newFragmentationInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<IsolationInfo> getIsolations() {
		// TODO Auto-generated method stub
		return null;
	}
}
