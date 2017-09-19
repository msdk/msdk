/*
 * (C) Copyright 2015-2017 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1 as published by the Free
 * Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation.
 */

package io.github.msdk.featuredetection.chromatogrambuilder;

import java.util.Vector;

import javax.annotation.Nonnull;

import org.apache.commons.math3.stat.descriptive.rank.Median;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;

import io.github.msdk.MSDKRuntimeException;

class BuildingChromatogram {

  private final @Nonnull Vector<Double> mzValues = new Vector<>();
  private final @Nonnull Vector<Float> rtValues = new Vector<>();
  private final @Nonnull Vector<Float> intensityValues = new Vector<>();

  // Number of scans in a segment that is currently being connected
  private int buildingSegmentLength = 0;

  // Number of connected segments, which have been committed by
  // commitBuildingSegment()
  private int numOfCommittedSegments = 0;

  int getNumberOfCommittedSegments() {
    return numOfCommittedSegments;
  }

  /**
   * <p>
   * Getter for the field <code>buildingSegmentLength</code>.
   * </p>
   *
   * @return a float.
   */
  public float getBuildingSegmentLength() {

    if (buildingSegmentLength < 2)
      return 0.0f;

    float firstRT = rtValues.get(rtValues.size() - buildingSegmentLength);
    float lastRT = rtValues.lastElement();

    return (lastRT - firstRT);
  }

  void removeBuildingSegment() {
    final int newSize = mzValues.size() - buildingSegmentLength;
    mzValues.setSize(newSize);
    rtValues.setSize(newSize);
    intensityValues.setSize(newSize);
    buildingSegmentLength = 0;
  }

  void commitBuildingSegment() {
    numOfCommittedSegments++;
    buildingSegmentLength = 0;
  }

  void addDataPoint(@Nonnull Float rt, @Nonnull Double mz, @Nonnull Float intensity) {
    Preconditions.checkNotNull(rt);
    rtValues.add(rt);
    mzValues.add(mz);
    intensityValues.add(intensity);
    buildingSegmentLength++;
  }

  double getLastMz() {
    if (mzValues.isEmpty())
      throw new MSDKRuntimeException("Cannot return the last data point of an empty chromatogram");
    return mzValues.lastElement();
  }

  float getLastIntensity() {
    if (intensityValues.isEmpty())
      throw new MSDKRuntimeException("Cannot return the last data point of an empty chromatogram");
    return intensityValues.lastElement();

  }

  float getHeight() {
    float maxIntensity = 0f;
    for (float i : intensityValues) {
      maxIntensity = Math.max(maxIntensity, i);
    }
    return maxIntensity;
  }

  float[] getRtValues(float[] array) {
    if (array.length < rtValues.size())
      array = new float[rtValues.size() * 2];

    for (int i = 0; i < rtValues.size(); i++) {
      array[i] = rtValues.get(i);
    }
    return array;
  }

  double[] getMzValues(double[] array) {
    if (array.length < mzValues.size())
      array = new double[mzValues.size() * 2];

    for (int i = 0; i < mzValues.size(); i++) {
      array[i] = mzValues.get(i);
    }
    return array;
  }

  float[] getIntensityValues(float[] array) {
    if (array.length < intensityValues.size())
      array = new float[intensityValues.size() * 2];

    for (int i = 0; i < intensityValues.size(); i++) {
      array[i] = intensityValues.get(i);
    }
    return array;
  }

  int size() {
    return mzValues.size();
  }

  @Nonnull
  Double calculateMz() {
    if (mzValues.isEmpty())
      throw new MSDKRuntimeException("Cannot calculate the m/z value of an empty chromatogram");

    // Convert the m/z values to an array
    double mzDoubleValues[] = Doubles.toArray(mzValues);

    // Calculate the final m/z value as a median of all m/z values
    Median median = new Median();
    double medianValue = median.evaluate(mzDoubleValues);

    return medianValue;

  }

}
