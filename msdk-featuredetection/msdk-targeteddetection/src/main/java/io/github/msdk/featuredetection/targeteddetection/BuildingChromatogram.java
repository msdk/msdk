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

package io.github.msdk.featuredetection.targeteddetection;

import javax.annotation.Nonnull;

import com.google.common.collect.Range;

import io.github.msdk.datamodel.chromatograms.ChromatogramDataPointList;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;

class BuildingChromatogram {

    // All data points of this chromatogram
    private ChromatogramDataPointList dataPoints = MSDKObjectBuilder
            .getChromatogramDataPointList();

    // m/z array
    private int size = 0;
    private double[] mzValues = new double[100];

    void addDataPoint(@Nonnull ChromatographyInfo rt, @Nonnull Double mz,
            @Nonnull Float intensity) {

        // Make sure we have enough space to add a new data point
        if (size == mzValues.length) {
            allocate(size * 2);
        }

        // Add data point
        mzValues[size] = mz;
        dataPoints.add(rt, intensity);
        size++;
    }

    ChromatogramDataPointList getDataPoints() {
        return dataPoints;
    }

    double[] getMzValues() {
        return mzValues;
    }

    /**
     * <p>cropChromatogram.</p>
     *
     * @param rtRange a {@link com.google.common.collect.Range} object.
     * @param intensityTolerance a {@link java.lang.Double} object.
     * @param noiseLevel a {@link java.lang.Double} object.
     */
    public void cropChromatogram(Range<Double> rtRange,
            Double intensityTolerance, Double noiseLevel) {
        ChromatographyInfo[] chromatographyInfo = dataPoints.getRtBuffer();
        float[] intensityValues = dataPoints.getIntensityBuffer();

        // Find peak apex (=most intense data point which fulfill the criteria)
        Integer apexDataPoint = null;
        for (int i = 0; i < size; i++) {
            Float currentIntensity = intensityValues[i];
            Double currentRt = (double) chromatographyInfo[i]
                    .getRetentionTime();

            // Verify data point
            if ((apexDataPoint == null
                    || currentIntensity > intensityValues[apexDataPoint])
                    && rtRange.contains(currentRt)
                    && currentIntensity > noiseLevel) {
                apexDataPoint = i;
            }
        }

        if (apexDataPoint != null) {
            Integer startIndex = null, endIndex = null;

            // Find start data point
            for (int i = apexDataPoint - 1; i >= 0; i--) {

                // Verify the intensity is within the intensity tolerance
                if (intensityValues[i] > intensityValues[i + 1]
                        * (1 + intensityTolerance)
                        || intensityValues[i + 1] == 0) {
                    break;
                }
                startIndex = i;
            }

            // Find end data point
            for (int i = apexDataPoint + 1; i < size; i++) {

                // Verify the intensity is within the intensity tolerance
                if (intensityValues[i] > intensityValues[i - 1]
                        * (1 + intensityTolerance)
                        || intensityValues[i - 1] == 0) {
                    break;
                }
                endIndex = i;
            }

            // Shift the peakPoints
            int peakPoints = endIndex-startIndex+1;
            ChromatographyInfo[] newRtBuffer = new ChromatographyInfo[peakPoints];
            float[] newIntensityBuffer = new float[peakPoints];
            System.arraycopy(dataPoints.getRtBuffer(), startIndex, newRtBuffer, 0, peakPoints);
            System.arraycopy(dataPoints.getIntensityBuffer(), startIndex, newIntensityBuffer, 0, peakPoints);
            dataPoints.setBuffers(newRtBuffer, newIntensityBuffer, peakPoints);

            // Shift the m/z array
            double[] newMzValues = new double[peakPoints];
            System.arraycopy(mzValues, startIndex, newMzValues, 0, peakPoints);
            mzValues = newMzValues;
            size = peakPoints;

        } else {
            // Empty all lists
            mzValues = null;
            dataPoints = null;
        }

    }

    /**
     * <p>allocate.</p>
     *
     * @param newSize a int.
     */
    public void allocate(int newSize) {

        if (mzValues.length >= newSize)
            return;

        double[] mzValuesNew = new double[newSize];

        if (size > 0) {
            System.arraycopy(mzValues, 0, mzValuesNew, 0, size);
        }

        mzValues = mzValuesNew;
    }

    /**
     * <p>Getter for the field <code>size</code>.</p>
     *
     * @return a int.
     */
    public int getSize() {
        return size;
    }
}
