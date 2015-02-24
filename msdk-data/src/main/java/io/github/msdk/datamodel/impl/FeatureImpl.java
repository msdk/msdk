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
import io.github.msdk.datamodel.DataPoint;
import io.github.msdk.datamodel.Feature;
import io.github.msdk.datamodel.FeatureShape;
import io.github.msdk.datamodel.FeatureType;
import io.github.msdk.datamodel.IsotopePattern;
import io.github.msdk.datamodel.PeakListRow;
import io.github.msdk.datamodel.RawDataFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    private @Nonnull Range<Double> rtRange, mzRange, intensityRange;

    // Number of representative scan
    private int representativeScan;

    // Number of most intense fragment scan
    private int fragmentScanNumber;

    // Isotope pattern. Null by default but can be set later by deisotoping
    // method.
    private IsotopePattern isotopePattern;
    private int charge = 0;

    @SuppressWarnings("null")
    FeatureImpl() {
	this.rtRange=Range.all();
	this.mzRange=Range.all();
	this.intensityRange=Range.all();
    }

    /**
     * Initializes a new peak using given values
     * 
     */
     FeatureImpl(RawDataFile dataFile, double MZ, double RT,
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
     * @see io.github.msdk.datamodel.Feature#getRawDataPointsIntensityRange()
     */
    public @Nonnull Range<Double> getRawDataPointsIntensityRange() {
	return intensityRange;
    }

    /**
     * @see io.github.msdk.datamodel.Feature#getRawDataPointsMZRange()
     */
    public @Nonnull Range<Double> getRawDataPointsMZRange() {
	return mzRange;
    }

    /**
     * @see io.github.msdk.datamodel.Feature#getRawDataPointsRTRange()
     */
    public @Nonnull Range<Double> getRawDataPointsRTRange() {
	return rtRange;
    }

    /**
     * @see io.github.msdk.datamodel.Feature#getRepresentativeScanNumber()
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
    public void setIsotopePattern(@Nullable IsotopePattern isotopePattern) {
	this.isotopePattern = isotopePattern;
    }

    @Override
    public @Nonnull PeakListRow getParentPeakListRow() {
	// TODO Auto-generated method stub
	return MSDKObjectBuilder.getPeakListRow();
    }

    @Override
    public @Nonnull FeatureType getFeatureType() {
	// TODO Auto-generated method stub
	return FeatureType.DETECTED;
    }

    @Override
    public void setFeatureType(@Nonnull FeatureType newStatus) {
	// TODO Auto-generated method stub

    }

    @Override
    public @Nonnull Double getMZ() {
	// TODO Auto-generated method stub
	return 0.0;
    }

    @Override
    public void setMZ(@Nonnull Double newMZ) {
	// TODO Auto-generated method stub

    }

    @Override
    public ChromatographyData getChromatographyData() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setChromatographyData(@Nullable ChromatographyData chromData) {
	// TODO Auto-generated method stub

    }

    @Override
    public @Nonnull Double getHeight() {
	// TODO Auto-generated method stub
	return 0.0;
    }

    @Override
    public void setHeight(@Nonnull Double newHeight) {
	// TODO Auto-generated method stub

    }

    @Override
    public Double getArea() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setArea(@Nullable Double newArea) {
	// TODO Auto-generated method stub

    }

    @Override
    public FeatureShape getFeatureShape() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setFeatureShape(@Nullable FeatureShape rawData) {
	// TODO Auto-generated method stub

    }

    @Override
    public Integer getCharge() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setCharge(@Nullable Integer charge) {
	// TODO Auto-generated method stub

    }

}
