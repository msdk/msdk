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

import javax.annotation.Nonnull;

import com.google.common.collect.Range;

import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.chromatograms.ChromatogramDataPointList;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;

public class ChromatogramUtil {

	private static ChromatogramDataPointList dataPointList = MSDKObjectBuilder.getChromatogramDataPointList();

	/**
	 * Returns the range of ChromatographyInfo of all data points in this
	 * feature.
	 */
	@SuppressWarnings("null")
	@Nonnull
	public static Range<ChromatographyInfo> getDataPointsChromatographyRange(@Nonnull Chromatogram chromatogram) {
		chromatogram.getDataPoints(dataPointList);
		final ChromatographyInfo[] chromatographyInfo = dataPointList.getRtBuffer();
		final Range<ChromatographyInfo> chromatographyRange = Range.closed(chromatographyInfo[0],
				chromatographyInfo[dataPointList.getSize()]);
		return chromatographyRange;
	}

	/**
	 * Returns the range of intensity values of all data points in this feature.
	 */
	@SuppressWarnings("null")
	@Nonnull
	public static Range<Float> getDataPointsIntensityRange(@Nonnull Chromatogram chromatogram) {
		chromatogram.getDataPoints(dataPointList);
		final float[] intensities = dataPointList.getIntensityBuffer();
		final float lower = intensities[0];
		final float upper = intensities[dataPointList.getSize()];
		final Range<Float> intensityRange = Range.closed(lower, upper);
		return intensityRange;
	}

	/**
	 * Returns the number of data points for this feature.
	 */
	@SuppressWarnings("null")
	public static int getNumberOfDataPoints(@Nonnull Chromatogram chromatogram) {
		chromatogram.getDataPoints(dataPointList);
		return dataPointList.getSize();
	}

	/**
	 * Calculates the m/z value of the chromatogram based on the list of m/z
	 * values
	 */
	public enum calcMethod {
		allAverage, allMedian, fwhmAverage, fwhmMedian
	};

	public static Double calculateMz(@Nonnull float[] intensityValues, @Nonnull double[] mzValues,
			@Nonnull calcMethod method) {
		double newMz = 0;
		double sum = 0;
		int values = 0;

		switch (method) {
			case allAverage:
				for (int i = 0; i < mzValues.length; i++) {
					if (mzValues[i] > 0) {
						values++;
						sum = sum + mzValues[i];
					}
				}
				newMz = sum / values;
				break;
			case allMedian:
				double index = Math.floor(mzValues.length / 2);
				if (mzValues.length % 2 == 0) { // even
					sum = mzValues[(int) index] + mzValues[(int) index + 1];
					newMz = sum / 2;
				} else { // odd
					newMz = mzValues[(int) index];
				}
				break;
			case fwhmAverage:
				// Find the maximum intensity
				float max = intensityValues[0];
				for (int i = 1; i < intensityValues.length; i++) {
				    if (intensityValues[i] > max) {
				      max = intensityValues[i];
				    }
				}

				// Calculate m/z
				for (int i = 0; i < intensityValues.length; i++) {
				    if (intensityValues[i] > max/2) {
				    	values++;
						sum = sum + mzValues[i];
				    }
				}
				newMz = sum / values;
				break;
			case fwhmMedian:
				// TODO
		}

		return newMz;
	}
}
