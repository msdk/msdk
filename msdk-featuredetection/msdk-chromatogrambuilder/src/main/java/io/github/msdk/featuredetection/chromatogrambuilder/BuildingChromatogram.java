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

package io.github.msdk.featuredetection.chromatogrambuilder;

import java.util.Vector;

import javax.annotation.Nonnull;

import org.apache.commons.math3.stat.descriptive.rank.Median;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;

import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.chromatograms.ChromatogramDataPointList;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;

class BuildingChromatogram {

    // All data points of this chromatogram
    private final @Nonnull ChromatogramDataPointList dataPoints = MSDKObjectBuilder
            .getChromatogramDataPointList();

    private final @Nonnull Vector<Double> mzValues = new Vector<>();

    // Number of scans in a segment that is currently being connected
    private int buildingSegmentLength = 0;

    // Number of connected segments, which have been committed by
    // commitBuildingSegment()
    private int numOfCommittedSegments = 0;

    int getNumberOfCommittedSegments() {
        return numOfCommittedSegments;
    }

    /**
     * <p>Getter for the field <code>buildingSegmentLength</code>.</p>
     *
     * @return a float.
     */
    @SuppressWarnings("null")
    public float getBuildingSegmentLength() {

        if (buildingSegmentLength < 2)
            return 0.0f;
        ChromatographyInfo rtBuffer[] = dataPoints.getRtBuffer();

        ChromatographyInfo firstChromInfo = rtBuffer[dataPoints.getSize()
                - buildingSegmentLength];
        ChromatographyInfo lastChromInfo = rtBuffer[dataPoints.getSize() - 1];

        if ((firstChromInfo == null) || (lastChromInfo == null))
            throw new MSDKRuntimeException(
                    "Scans do not contain retention times");

        float firstRT = firstChromInfo.getRetentionTime();
        float lastRT = lastChromInfo.getRetentionTime();

        return (lastRT - firstRT);
    }

    void removeBuildingSegment() {
        final int newSize = dataPoints.getSize() - buildingSegmentLength;
        dataPoints.setSize(newSize);
        mzValues.setSize(newSize);
        buildingSegmentLength = 0;
    }

    void commitBuildingSegment() {
        numOfCommittedSegments++;
        buildingSegmentLength = 0;
    }

    void addDataPoint(@Nonnull ChromatographyInfo rt, @Nonnull Double mz,
            @Nonnull Float intensity) {
        Preconditions.checkNotNull(rt);
        dataPoints.add(rt, intensity);
        mzValues.add(mz);
        buildingSegmentLength++;
    }

    double getLastMz() {
        if (mzValues.isEmpty())
            throw new MSDKRuntimeException(
                    "Cannot return the last data point of an empty chromatogram");
        return mzValues.lastElement();
    }

    float getLastIntensity() {
        if (dataPoints.getSize() == 0)
            throw new MSDKRuntimeException(
                    "Cannot return the last data point of an empty chromatogram");
        return dataPoints.getIntensityBuffer()[dataPoints.getSize() - 1];
    }

    float getHeight() {
        float maxIntensity = 0f;
        float intensityBuffer[] = dataPoints.getIntensityBuffer();
        for (int i = 0; i < dataPoints.getSize(); i++) {
            maxIntensity = Math.max(maxIntensity, intensityBuffer[i]);
        }
        return maxIntensity;
    }

    @Nonnull
    ChromatogramDataPointList getDataPoints() {
        return dataPoints;
    }

    @Nonnull
    Double calculateMz() {
        if (mzValues.isEmpty())
            throw new MSDKRuntimeException(
                    "Cannot calculate the m/z value of an empty chromatogram");

        // Convert the m/z values to an array
        double mzDoubleValues[] = Doubles.toArray(mzValues);

        // Calculate the final m/z value as a median of all m/z values
        Median median = new Median();
        double medianValue = median.evaluate(mzDoubleValues);

        return medianValue;

    }

}
