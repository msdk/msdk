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

package io.github.msdk.util;

import java.util.List;

import javax.annotation.Nonnull;

import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.SeparationType;

public class FeatureTableUtil {

	/**
	 * Re-calculates the average m/z and RT values for a feature table
	 */

	public static void recalculateAverages(@Nonnull FeatureTable featureTable) {
		List<FeatureTableRow> rows = featureTable.getRows();
		Double mz;
		Float rt;
		double totalMz;
		float totalRt;
		int mzCount, rtCount;
		FeatureTableColumn column;

		for (FeatureTableRow row : rows) {
			List<Sample> samples = featureTable.getSamples();

			totalMz = 0;
			totalRt = 0;
			mzCount = 0;
			rtCount = 0;
			for (Sample sample : samples) {
				column = featureTable.getColumn(ColumnName.MZ.getName(), sample);
				mz = row.getData(column);
				if (mz != null) {
					totalMz += mz;
					mzCount++;
				}

				column = featureTable.getColumn(ColumnName.RT.getName(), sample);
				rt = row.getData(column);
				if (rt != null) {
					totalRt += rt;
					rtCount++;
				}
			}

			// Update m/z
			column = featureTable.getColumn(ColumnName.MZ.getName(), null);
			Double newMz = totalMz / mzCount;
			row.setData(column, newMz);

			// Update ppm
			column = featureTable.getColumn("Ion Annotation", null);
			IonAnnotation ionAnnotation = row.getData(column);
			Double ionMz = ionAnnotation.getExpectedMz();
			if (ionMz != null) {
				column = featureTable.getColumn(ColumnName.PPM.getName(), null);
				Double diff = Math.abs(newMz-ionMz);
				row.setData(column, (diff/ionMz)*1000000);
			}

			// Update RT
			column = featureTable.getColumn("Chromatography Info", null);
			ChromatographyInfo currentChromatographyInfo = row.getData(column);
			SeparationType separationType;
			if (currentChromatographyInfo == null) {
				separationType = SeparationType.UNKNOWN;
			} else {
				separationType = currentChromatographyInfo.getSeparationType();
			}
			ChromatographyInfo chromatographyInfo = MSDKObjectBuilder
					.getChromatographyInfo1D(separationType, totalRt / rtCount);
			row.setData(column, chromatographyInfo);

		}
	}

}
