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

/**
 * <p>ChromatogramUtil class.</p>
 *
 */
public class ChromatogramUtil {

	private static ChromatogramDataPointList dataPointList = MSDKObjectBuilder.getChromatogramDataPointList();

	/**
	 * Returns the range of ChromatographyInfo of all data points in this
	 * feature.
	 *
	 * @param chromatogram a {@link io.github.msdk.datamodel.chromatograms.Chromatogram} object.
	 * @return a {@link com.google.common.collect.Range} object.
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
	 *
	 * @param chromatogram a {@link io.github.msdk.datamodel.chromatograms.Chromatogram} object.
	 * @return a {@link com.google.common.collect.Range} object.
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
	 *
	 * @param chromatogram a {@link io.github.msdk.datamodel.chromatograms.Chromatogram} object.
	 * @return a int.
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

	/**
	 * <p>calculateMz.</p>
	 *
	 * @param intensityValues an array of float.
	 * @param mzValues an array of double.
	 * @param method a {@link io.github.msdk.util.ChromatogramUtil.calcMethod} object.
	 * @return a {@link java.lang.Double} object.
	 */
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
				if (intensityValues[i] > max / 2) {
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

	/**
	 * Returns the retention time of this feature.
	 *
	 * @param chromatogram a {@link io.github.msdk.datamodel.chromatograms.Chromatogram} object.
	 * @return a float.
	 */
	@SuppressWarnings("null")
	public static float getRt(@Nonnull Chromatogram chromatogram) {
		chromatogram.getDataPoints(dataPointList);

		// Find the maximum intensity index
		float[] intensityValues = dataPointList.getIntensityBuffer();
		int max = 0;
		for (int i = 1; i < dataPointList.getSize(); i++) {
			if (intensityValues[i] > intensityValues[max]) {
				max = i;
			}
		}

		ChromatographyInfo[] rtBuffer = dataPointList.getRtBuffer();
		return rtBuffer[max].getRetentionTime();
	}

	/**
	 * Returns the start retention time of this feature.
	 *
	 * @param chromatogram a {@link io.github.msdk.datamodel.chromatograms.Chromatogram} object.
	 * @return a float.
	 */
	@SuppressWarnings("null")
	public static float getRtStart(@Nonnull Chromatogram chromatogram) {
		chromatogram.getDataPoints(dataPointList);
		Range<ChromatographyInfo> rtRange = dataPointList.getRtRange();
		return rtRange.lowerEndpoint().getRetentionTime();
	}

	/**
	 * Returns the end retention time of this feature.
	 *
	 * @param chromatogram a {@link io.github.msdk.datamodel.chromatograms.Chromatogram} object.
	 * @return a float.
	 */
	@SuppressWarnings("null")
	public static float getRtEnd(@Nonnull Chromatogram chromatogram) {
		chromatogram.getDataPoints(dataPointList);
		Range<ChromatographyInfo> rtRange = dataPointList.getRtRange();
		return rtRange.upperEndpoint().getRetentionTime();
	}

	/**
	 * Returns the duration of this feature.
	 *
	 * @param chromatogram a {@link io.github.msdk.datamodel.chromatograms.Chromatogram} object.
	 * @return a float.
	 */
	@SuppressWarnings("null")
	public static float getDuration(@Nonnull Chromatogram chromatogram) {
		chromatogram.getDataPoints(dataPointList);
		Range<ChromatographyInfo> rtRange = dataPointList.getRtRange();
		float start = rtRange.lowerEndpoint().getRetentionTime();
		float end = rtRange.upperEndpoint().getRetentionTime();
		return end - start;
	}

	/**
	 * Returns the maximum height of this feature.
	 *
	 * @param chromatogram a {@link io.github.msdk.datamodel.chromatograms.Chromatogram} object.
	 * @return a float.
	 */
	@SuppressWarnings("null")
	public static float getMaxHeight(@Nonnull Chromatogram chromatogram) {
		chromatogram.getDataPoints(dataPointList);

		// Find the maximum intensity
		float[] intensityValues = dataPointList.getIntensityBuffer();
		float max = intensityValues[0];
		for (int i = 1; i < dataPointList.getSize(); i++) {
			if (intensityValues[i] > max) {
				max = intensityValues[i];
			}
		}

		return max;
	}

	/**
	 * Returns the area of this feature.
	 *
	 * @param chromatogram a {@link io.github.msdk.datamodel.chromatograms.Chromatogram} object.
	 * @return a float.
	 */
	@SuppressWarnings("null")
	public static float getArea(@Nonnull Chromatogram chromatogram) {
		chromatogram.getDataPoints(dataPointList);
		float area = 0, rtDifference = 0, intensityStart = 0, intensityEnd = 0;
		float[] intensityValues = dataPointList.getIntensityBuffer();
		ChromatographyInfo[] rtValues = dataPointList.getRtBuffer();
		for (int i = 0; i < dataPointList.getSize() - 1; i++) {
			rtDifference = rtValues[i + 1].getRetentionTime() - rtValues[i].getRetentionTime();
			intensityStart = intensityValues[i];
			intensityEnd = intensityValues[i + 1];
			area += (rtDifference * (intensityStart + intensityEnd) / 2);
		}
		return area;
	}

	/**
	 * Returns the full width at half maximum (FWHM) of this feature.
	 *
	 * @param chromatogram a {@link io.github.msdk.datamodel.chromatograms.Chromatogram} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double getFwhm(@Nonnull Chromatogram chromatogram) {
		float height = getMaxHeight(chromatogram);
		float rt = getRt(chromatogram);
		double rtValues[] = findRTs(height / 2, rt, chromatogram);
		Double fwhm = rtValues[1] - rtValues[0];
		if (fwhm < 0) {
			fwhm = null;
		}
		return fwhm;
	}

	/**
	 * Returns the tailing factor of this feature.
	 *
	 * @param chromatogram a {@link io.github.msdk.datamodel.chromatograms.Chromatogram} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double getTailingFactor(@Nonnull Chromatogram chromatogram) {
		float height = getMaxHeight(chromatogram);
		float rt = getRt(chromatogram);
		double rtValues[] = findRTs(height * 0.05, rt, chromatogram);

		if (rtValues[1] == 0)
			rtValues[1] = getRtEnd(chromatogram);

		Double tf = (rtValues[1] - rtValues[0]) / (2 * (rt - rtValues[0]));
		if (tf < 0) {
			tf = null;
		}
		return tf;
	}

	/**
	 * Returns the asymmetry factor of this feature.
	 *
	 * @param chromatogram a {@link io.github.msdk.datamodel.chromatograms.Chromatogram} object.
	 * @return a {@link java.lang.Double} object.
	 */
	public static Double getAsymmetryFactor(@Nonnull Chromatogram chromatogram) {
		float height = getMaxHeight(chromatogram);
		float rt = getRt(chromatogram);
		double rtValues3[] = findRTs(height * 0.1, rt, chromatogram);
		Double af = (rtValues3[1] - rt) / (rt - rtValues3[0]);
		if (af < 0) {
			af = null;
		}
		return af;
	}

	private static double[] findRTs(double intensity, double rt, Chromatogram chromatogram) {

		double lastDiff1 = intensity, lastDiff2 = intensity, currentDiff;
		double x1 = 0, x2 = 0, x3 = 0, x4 = 0, y1 = 0, y2 = 0, y3 = 0, y4 = 0, currentRT;

		float[] intensityValues = dataPointList.getIntensityBuffer();
		ChromatographyInfo[] rtValues = dataPointList.getRtBuffer();

		// Find the data points closet to input intensity on both side of the
		// apex
		for (int i = 1; i < dataPointList.getSize() - 1; i++) {

			currentDiff = Math.abs(intensity - intensityValues[i]);
			currentRT = rtValues[i].getRetentionTime();

			if (currentDiff < lastDiff1 & currentDiff > 0 & currentRT <= rt) {
				x1 = rtValues[i].getRetentionTime();
				y1 = intensityValues[i];
				x2 = rtValues[i + 1].getRetentionTime();
				y2 = intensityValues[i + 1];
				lastDiff1 = currentDiff;
			} else if (currentDiff < lastDiff2 & currentDiff > 0 & currentRT >= rt) {
				x3 = rtValues[i - 1].getRetentionTime();
				y3 = intensityValues[i - 1];
				x4 = rtValues[i].getRetentionTime();
				y4 = intensityValues[i];
				lastDiff2 = currentDiff;
			}

		}

		// Calculate RT value for input intensity based on linear regression
		double slope, intercept, rt1, rt2;
		if (y1 > 0) {
			slope = (y2 - y1) / (x2 - x1);
			intercept = y1 - (slope * x1);
			rt1 = (intensity - intercept) / slope;
		} else { // Straight drop of peak to 0 intensity
			rt1 = x2;
		}
		if (y4 > 0) {
			slope = (y4 - y3) / (x4 - x3);
			intercept = y3 - (slope * x3);
			rt2 = (intensity - intercept) / slope;
		} else { // Straight drop of peak to 0 intensity
			rt2 = x3;
		}

		return new double[] { rt1, rt2 };
	}

}
