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

import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.DataPoint;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;

import javax.annotation.Nonnull;

/**
 * Object builder
 */
public class MSDKObjectBuilder {

    public static final @Nonnull DataPoint getDataPoint(double mz,
	    float intensity) {
	return new SimpleDataPoint(mz, intensity);
    }

    public static final @Nonnull RawDataFile getRawDataFile() {
	return new SimpleRawDataFile();
    }

    public static final @Nonnull MsScan getMsScan(@Nonnull RawDataFile dataFile) {
	return new SimpleMsScan(dataFile);
    }

    public static final @Nonnull ChromatographyInfo getChromatographyData() {
	return new SimpleChromatographyData();
    }

}
