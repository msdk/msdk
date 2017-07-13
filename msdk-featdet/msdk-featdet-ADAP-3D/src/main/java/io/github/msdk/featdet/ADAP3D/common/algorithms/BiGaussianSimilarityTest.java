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
package io.github.msdk.featdet.ADAP3D.common.algorithms;

import org.apache.commons.collections4.map.MultiKeyMap;

import java.lang.Math;
import java.util.stream.IntStream;

import io.github.msdk.featdet.ADAP3D.common.algorithms.Peak3DTest.Result;
import io.github.msdk.featdet.ADAP3D.common.algorithms.SliceSparseMatrix.Triplet;

/**
 * <p>
 * BiGaussianSimilarityTest class is used for determining true or false peak by comparing BiGaussian
 * values with intensity values of given m/z and left and right bounds (variables leftBound and
 * rightBound).
 * </p>
 */
public class BiGaussianSimilarityTest {

  private static final double PEAKSIMILARITYTHRESHOLD = 0.25;

  /**
   * <p>
   * execute method is used for testing a peak with given m/z-value (variable mz) and left and right
   * bounds (variables leftBound and rightBound). Peak is tested by comparing BiGaussian values with
   * intensity values from slice of sparse matrix.
   * </p>
   * 
   * @param slice a {@link org.apache.commons.collections4.map.MultiKeyMap} object. This is
   *        horizontal slice from sparse matrix of given m/z value.
   * @param leftBound a {@link java.lang.Integer} object. This is lowest scan number from which peak
   *        determining starts.
   * @param rightBound a {@link java.lang.Integer} object. This is highest scan number on which peak
   *        determining ends.
   * @param mz a {@link java.lang.Double} object. It's double because original m/z value from raw
   *        file is passed in the method.
   * 
   * @return a {@link Result} object. Result object contains similarity values, lower and upper mz
   *         boundaries for adjacent similar peaks.
   *         </p>
   */
  public boolean execute(MultiKeyMap<Integer, Triplet> slice, int leftBound, int rightBound,
      double mz) {

    double[] referenceEIC = new double[rightBound - leftBound + 1];
    CurveTool.normalize(slice, leftBound, rightBound, (int) Math.round(mz * 10000), referenceEIC);

    BiGaussian objBiGaussian = new BiGaussian(slice, mz, leftBound, rightBound);
    double[] bigaussianValues = IntStream.range(0, referenceEIC.length)
        .mapToDouble(i -> objBiGaussian.getValue(leftBound + i)).toArray();

    double[] normBigaussianValues = new double[rightBound - leftBound + 1];
    CurveTool.normalize(bigaussianValues, normBigaussianValues);

    double similarityValue =
        CurveTool.similarityValue(referenceEIC, normBigaussianValues, leftBound, rightBound);

    return similarityValue > PEAKSIMILARITYTHRESHOLD;
  }
}
