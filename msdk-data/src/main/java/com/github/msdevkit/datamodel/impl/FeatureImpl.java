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

import com.github.msdevkit.datamodel.ChromatographyData;
import com.github.msdevkit.datamodel.DataPoint;
import com.github.msdevkit.datamodel.Feature;
import com.github.msdevkit.datamodel.FeatureShape;
import com.github.msdevkit.datamodel.FeatureType;
import com.github.msdevkit.datamodel.IsotopePattern;
import com.github.msdevkit.datamodel.PeakListRow;
import com.github.msdevkit.datamodel.RawDataFile;
import com.google.common.collect.Range;

/**
 * This class is a simple implementation of the peak interface.
 */
public class FeatureImpl implements Feature {

    private @Nonnull FeatureType peakStatus = FeatureType.UNKNOWN;
    private RawDataFile dataFile;

    // Scan numbers
    private int scanNumbers[];

    private DataPoint dataPointsPerScan[];

    // M/Z, RT, Height and Area
    private double mz, rt, height, area;

    // Boundaries of the peak raw data points
    private Range<Double> rtRange, mzRange, intensityRange;

    // Number of representative scan
    private int representativeScan;

    // Number of most intense fragment scan
    private int fragmentScanNumber;

    // Isotope pattern. Null by default but can be set later by deisotoping
    // method.
    private IsotopePattern isotopePattern;
    private int charge = 0;

    FeatureImpl() {
    }

    /**
     * Initializes a new peak using given values
     * 
     */
    public FeatureImpl(RawDataFile dataFile, double MZ, double RT,
	    double height, double area, int[] scanNumbers,
	    DataPoint[] dataPointsPerScan, FeatureType peakStatus,
	    int representativeScan, int fragmentScanNumber,
	    Range<Double> rtRange, Range<Double> mzRange,
	    Range<Double> intensityRange) {

	if (dataPointsPerScan.length == 0) {
	    throw new IllegalArgumentException(
		    "Cannot create a SimplePeak instance with no data points");
	}

	this.dataFile = dataFile;
	this.mz = MZ;
	this.rt = RT;
	this.height = height;
	this.area = area;
	this.scanNumbers = scanNumbers;
	this.peakStatus = peakStatus;
	this.representativeScan = representativeScan;
	this.fragmentScanNumber = fragmentScanNumber;
	this.rtRange = rtRange;
	this.mzRange = mzRange;
	this.intensityRange = intensityRange;
	this.dataPointsPerScan = dataPointsPerScan;

    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return super.toString();
    }

    /**
     * @see com.github.msdevkit.datamodel.Feature#getRawDataPointsIntensityRange()
     */
    public @Nonnull Range<Double> getRawDataPointsIntensityRange() {
	return intensityRange;
    }

    /**
     * @see com.github.msdevkit.datamodel.Feature#getRawDataPointsMZRange()
     */
    public @Nonnull Range<Double> getRawDataPointsMZRange() {
	return mzRange;
    }

    /**
     * @see com.github.msdevkit.datamodel.Feature#getRawDataPointsRTRange()
     */
    public @Nonnull Range<Double> getRawDataPointsRTRange() {
	return rtRange;
    }

    /**
     * @see com.github.msdevkit.datamodel.Feature#getRepresentativeScanNumber()
     */
    public int getRepresentativeScanNumber() {
	return representativeScan;
    }

    public int getMostIntenseFragmentScanNumber() {
	return fragmentScanNumber;
    }

    @Override
    public IsotopePattern getIsotopePattern() {
	return isotopePattern;
    }

    @Override
    public void setIsotopePattern(@Nonnull IsotopePattern isotopePattern) {
	this.isotopePattern = isotopePattern;
    }

    @Override
    public PeakListRow getParentPeakListRow() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public FeatureType getFeatureType() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setFeatureType(FeatureType newStatus) {
	// TODO Auto-generated method stub

    }

    @Override
    public Double getMZ() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setMZ(Double newMZ) {
	// TODO Auto-generated method stub

    }

    @Override
    public ChromatographyData getChromatographyData() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setChromatographyData(ChromatographyData chromData) {
	// TODO Auto-generated method stub

    }

    @Override
    public Double getHeight() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setHeight(Double newHeight) {
	// TODO Auto-generated method stub

    }

    @Override
    public Double getArea() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setArea(Double newArea) {
	// TODO Auto-generated method stub

    }

    @Override
    public FeatureShape getFeatureShape() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setFeatureShape(FeatureShape rawData) {
	// TODO Auto-generated method stub

    }

    @Override
    public Integer getCharge() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setCharge(Integer charge) {
	// TODO Auto-generated method stub

    }

}
