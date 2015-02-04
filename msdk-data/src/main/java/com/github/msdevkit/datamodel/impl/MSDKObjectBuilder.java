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
import com.github.msdevkit.datamodel.IonType;
import com.github.msdevkit.datamodel.IsotopePattern;
import com.github.msdevkit.datamodel.MsMsScan;
import com.github.msdevkit.datamodel.MsScan;
import com.github.msdevkit.datamodel.PeakList;
import com.github.msdevkit.datamodel.PeakListRowAnnotation;
import com.github.msdevkit.datamodel.RawDataFile;

/**
 * Object builder
 */
public class MSDKObjectBuilder {

    public static final @Nonnull DataPoint getDataPoint(double mz,
	    double intensity) {
	return new DataPointImpl(mz, intensity);
    }

    public static final @Nonnull IonType getIonType() {
	return new IonTypeImpl();
    }

    public static final @Nonnull DataPoint[] getDataPointArray(
	    final double mz[], final double intensities[]) {
	assert mz.length == intensities.length;
	final DataPoint dpArray[] = new DataPoint[mz.length];
	for (int i = 0; i < mz.length; i++)
	    dpArray[i] = new DataPointImpl(mz[i], intensities[i]);
	return dpArray;
    }

    public static final @Nonnull Feature getFeature() {
	return new FeatureImpl();
    }

    public static final @Nonnull IsotopePattern getIsotopePattern(
	    @Nonnull PeakList peakList) {
	assert peakList instanceof DataPointStoreImpl;
	return new IsotopePatternImpl((DataPointStoreImpl) peakList);
    }

    public static final @Nonnull PeakListRowAnnotation getPeakListRowAnnotation() {
	return new PeakListRowAnnotationImpl();
    }

    public static final @Nonnull PeakList getPeakList() {
	return new PeakListImpl();
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

    public static final @Nonnull ChromatographyData getChromatographyData() {
	return new ChromatographyDataImpl();
    }

}
