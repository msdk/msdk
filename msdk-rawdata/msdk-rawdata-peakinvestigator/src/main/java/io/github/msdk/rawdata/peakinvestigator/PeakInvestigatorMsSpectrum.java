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

package io.github.msdk.rawdata.peakinvestigator;

import java.util.TreeMap;

import com.google.common.collect.Range;

import io.github.msdk.datamodel.msspectra.MsSpectrum;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.util.tolerances.MzTolerance;

/**
 * This class is used provide m/z-dependent mass tolerances from
 * PeakInvestigator by decorating an existing instance of MsSpectrum.
 *
 */
public class PeakInvestigatorMsSpectrum implements MsSpectrum, MzTolerance {

	private final MsSpectrum spectrum;
	private final TreeMap<Double, Error> errors;
	private double multiplier = 1.0;

	public PeakInvestigatorMsSpectrum(MsSpectrum spectrum, TreeMap<Double, Error> errors) {
		this.spectrum = spectrum;
		this.errors = errors;
	}

	public double getMultiplier() {
		return this.multiplier;
	}

	public void setMultiplier(double multiplier) {
		this.multiplier = multiplier;
	}

	@Override
	public MsSpectrumType getSpectrumType() {
		return MsSpectrumType.CENTROIDED;
	}

	@Override
	public void setSpectrumType(MsSpectrumType spectrumType) {
		throw new RuntimeException("This class should not have a MsSpectrumType set.");
	}

	@Override
	public Integer getNumberOfDataPoints() {
		return spectrum.getNumberOfDataPoints();
	}

	@Override
	public double[] getMzValues() {
		return spectrum.getMzValues();
	}

	@Override
	public double[] getMzValues(double[] array) {
		return spectrum.getMzValues(array);
	}

	@Override
	public float[] getIntensityValues() {
		return spectrum.getIntensityValues();
	}

	@Override
	public float[] getIntensityValues(float[] array) {
		return spectrum.getIntensityValues(array);
	}

	@Override
	public void setDataPoints(double[] mzValues, float[] intensityValues, Integer size) {
		throw new RuntimeException("This class should never set its data points dynamically.");
	}

	@Override
	public Float getTIC() {
		return spectrum.getTIC();
	}

	@Override
	public Range<Double> getMzRange() {
		return spectrum.getMzRange();
	}

	@Override
	public Range<Double> getToleranceRange(Double mzValue) {
		Error error = errors.get(mzValue);
		double mzError = multiplier * error.MZ_ERROR;
		if (error.MIN_ERROR > mzError) {
			mzError = error.MIN_ERROR;
		}

		return Range.closed(mzValue - mzError, mzValue + mzError);
	}

	public Error getError(Double mzValue) {
		return errors.get(mzValue);
	}

	public static class Error {
		public final double MZ_ERROR;
		public final float INTENSITY_ERROR;
		public final double MIN_ERROR;

		public Error(double mzError, float intensityError, double minError) {
			MZ_ERROR = mzError;
			INTENSITY_ERROR = intensityError;
			MIN_ERROR = minError;
		}
	}
}
