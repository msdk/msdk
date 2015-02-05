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

package com.github.msdevkit.io.spectrumtypedetection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.msdevkit.MSDKAlgorithm;
import com.github.msdevkit.MSDKException;
import com.github.msdevkit.datamodel.DataPoint;
import com.github.msdevkit.datamodel.MassSpectrum;
import com.github.msdevkit.datamodel.MassSpectrumType;

/**
 * Auto-detection of spectrum type from data points. Determines if the spectrum
 * represented by given array of data points is centroided or continuous
 * (profile or thresholded). Profile spectra are easy to detect, because they
 * contain zero-intensity data points. However, distinguishing centroided from
 * thresholded spectra is not trivial. We use multiple checks for that purpose,
 * as described in the code comments.
 */
public class SpectrumTypeDetectionAlgorithm implements
	MSDKAlgorithm<MassSpectrumType> {

    private @Nonnull MassSpectrum inputSpectrum;
    private @Nullable MassSpectrumType result = null;
    private double finishedPercentage = 0.0;
    private boolean canceled = false;

    public SpectrumTypeDetectionAlgorithm(@Nonnull MassSpectrum inputSpectrum) {
	this.inputSpectrum = inputSpectrum;
    }

    @Override
    public double getFinishedPercentage() {
	return finishedPercentage;
    }

    @Override
    public void execute() throws MSDKException {
	DataPoint dataPoints[] = inputSpectrum.getDataPoints();
	result = detectSpectrumType(dataPoints);
	finishedPercentage = 1.0;
    }

    @Override
    public MassSpectrumType getResult() {
	return result;
    }

    @Override
    public void cancel() {
	this.canceled = true;
    }

    private MassSpectrumType detectSpectrumType(@Nonnull DataPoint[] dataPoints) {

	// If the spectrum has less than 5 data points, it should be centroided.
	if (dataPoints.length < 5)
	    return MassSpectrumType.CENTROIDED;

	// Go through the data points and find the highest one
	double maxIntensity = 0.0;
	int topDataPointIndex = 0;
	for (int i = 0; i < dataPoints.length; i++) {

	    // If the spectrum contains data points of zero intensity, it should
	    // be in profile mode
	    if (dataPoints[i].getIntensity() == 0.0) {
		return MassSpectrumType.PROFILE;
	    }

	    // Let's ignore the first and the last data point, because
	    // that would complicate our following checks
	    if ((i == 0) || (i == dataPoints.length - 1))
		continue;

	    // Update the maxDataPointIndex accordingly
	    if (dataPoints[i].getIntensity() > maxIntensity) {
		maxIntensity = dataPoints[i].getIntensity();
		topDataPointIndex = i;
	    }
	}

	// Check if canceled
	if (canceled)
	    return null;
	finishedPercentage = 0.3;

	// Now we have the index of the top data point (except the first and
	// the last). We also know the spectrum has at least 5 data points.
	assert topDataPointIndex > 0;
	assert topDataPointIndex < dataPoints.length - 1;
	assert dataPoints.length >= 5;

	// Calculate the m/z difference between the top data point and the
	// previous one
	final double topMzDifference = Math.abs(dataPoints[topDataPointIndex]
		.getMz() - dataPoints[topDataPointIndex - 1].getMz());

	// For 5 data points around the top one (with the top one in the
	// center), we check the distribution of the m/z values. If the spectrum
	// is continuous (thresholded), the distances between data points should
	// be more or less constant. On the other hand, centroided spectra
	// usually have unevenly distributed data points.
	for (int i = topDataPointIndex - 2; i < topDataPointIndex + 2; i++) {

	    // Check if the index is within acceptable range
	    if ((i < 1) || (i > dataPoints.length - 1))
		continue;

	    final double currentMzDifference = Math.abs(dataPoints[i].getMz()
		    - dataPoints[i - 1].getMz());

	    // Check if the m/z distance of the pair of consecutive data points
	    // falls within 25% tolerance of the distance of the top data point
	    // and its neighbor. If not, the spectrum should be centroided.
	    if ((currentMzDifference < 0.8 * topMzDifference)
		    || (currentMzDifference > 1.25 * topMzDifference)) {
		return MassSpectrumType.CENTROIDED;
	    }

	}

	// Check if canceled
	if (canceled)
	    return null;
	finishedPercentage = 0.7;

	// The previous check will detect most of the centroided spectra, but
	// there is a catch: some centroided spectra were produced by binning,
	// and the bins typically have regular distribution of data points, so
	// the above check would fail. Binning is normally used for
	// low-resolution spectra, so we can check the m/z difference the 3
	// consecutive data points (with the top one in the middle). If it goes
	// above 0.1, the spectrum should be centroided.
	final double mzDifferenceTopThree = Math
		.abs(dataPoints[topDataPointIndex - 1].getMz()
			- dataPoints[topDataPointIndex + 1].getMz());
	if (mzDifferenceTopThree > 0.1)
	    return MassSpectrumType.CENTROIDED;

	// Finally, we check the data points on the left and on the right of the
	// top one. If the spectrum is continuous (thresholded), their intensity
	// should decrease gradually from the top data point. Let's check if
	// their intensity is above 1/3 of the top data point. If not, the
	// spectrum should be centroided.
	final double thirdMaxIntensity = maxIntensity / 3;
	final double leftDataPointIntensity = dataPoints[topDataPointIndex - 1]
		.getIntensity();
	final double rightDataPointIntensity = dataPoints[topDataPointIndex + 1]
		.getIntensity();
	if ((leftDataPointIntensity < thirdMaxIntensity)
		|| (rightDataPointIntensity < thirdMaxIntensity))
	    return MassSpectrumType.CENTROIDED;

	// Check if canceled
	if (canceled)
	    return null;

	// If we could not find any sign that the spectrum is centroided, we
	// conclude it should be thresholded.
	return MassSpectrumType.THRESHOLDED;
    }

}
