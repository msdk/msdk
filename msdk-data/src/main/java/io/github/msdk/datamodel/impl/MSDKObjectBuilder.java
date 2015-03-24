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

import io.github.msdk.datamodel.rawdata.DataPoint;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.MsMsScan;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;

import javax.annotation.Nonnull;

/**
 * Object builder
 */
public class MSDKObjectBuilder {

    public static final @Nonnull DataPoint getDataPoint(double mz,
	    double intensity) {
	return new DataPointImpl(mz, intensity);
    }

    public static final @Nonnull DataPoint[] getDataPointArray(
	    final double mz[], final double intensities[]) {
	assert mz.length == intensities.length;
	final DataPoint dpArray[] = new DataPoint[mz.length];
	for (int i = 0; i < mz.length; i++)
	    dpArray[i] = new DataPointImpl(mz[i], intensities[i]);
	return dpArray;
    }

    public static final @Nonnull RawDataFile getRawDataFile() {
	return new RawDataFileImpl();
    }

    public static final @Nonnull MsScan getMsScan(@Nonnull RawDataFile dataFile) {
	return new MsScanImpl(dataFile);
    }

    public static final @Nonnull MsMsScan getMsMsScan(
	    @Nonnull RawDataFile dataFile) {
	return new MsMsScanImpl(dataFile);
    }

    public static final @Nonnull ChromatographyInfo getChromatographyData() {
	return new ChromatographyDataImpl();
    }

}
