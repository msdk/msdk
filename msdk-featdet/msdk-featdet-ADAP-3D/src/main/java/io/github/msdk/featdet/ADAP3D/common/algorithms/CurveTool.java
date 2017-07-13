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

import java.util.List;
import java.util.Random;
import java.lang.Math;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import io.github.msdk.featdet.ADAP3D.common.algorithms.SliceSparseMatrix.Triplet;

/**
 * <p>
 * CurveTool class is used for estimation of Full width half maximum.
 * </p>
 */
public class CurveTool {
  
  private static final double EXPONENT_FACTOR = 0.2;
  private static final double EXPONENT_SHIFT = Math.exp(-1 / EXPONENT_FACTOR);
  private static final double EXPONENT_HEIGHT = 1.0 / (1.0 - EXPONENT_SHIFT);
  private static final double FWHM_CONSTANT = 2.35482;
  private SliceSparseMatrix objSliceSparseMatrix;

  /**
   * <p>
   * CurveTool constructor takes object of SliceSparseMatrix class.
   * </p>
   */
  public CurveTool(SliceSparseMatrix sliceSparseMatrix) {
    objSliceSparseMatrix = sliceSparseMatrix;
  }

  /**
   * <p>
   * estimateFwhmMs method estimates the FWHM for given number of random scans.
   * 
   * @param numberOfScansForFWHMCalc a {@link java.lang.Integer} object. This is number scans
   *        required for estimation of fwhm.
   * 
   * @return fwhm a {@link java.lang.Double} object.This is Full width half maximum.
   *         </p>
   */
  public double estimateFwhmMs(int numberOfScansForFWHMCalc) {

    double sigma = 0;
    int countProperIteration = 0;
    int countTotalIteration = 0;

    while (countProperIteration < numberOfScansForFWHMCalc) {
      countTotalIteration++;


      if (countTotalIteration > objSliceSparseMatrix.getSizeOfRawDataFile()) {
        System.out.println(countTotalIteration);
        throw new IllegalArgumentException("Cannot calculate FWHM.");
      }

      Random generator = new Random();
      int randInt = generator.nextInt(objSliceSparseMatrix.getSizeOfRawDataFile());
      List<SliceSparseMatrix.VerticalSliceDataPoint> verticalSlice =
          objSliceSparseMatrix.getVerticalSlice(randInt);

      if (verticalSlice == null)
        continue;

      WeightedObservedPoints obs = new WeightedObservedPoints();

      for (SliceSparseMatrix.VerticalSliceDataPoint datapoint : verticalSlice) {
        obs.add(datapoint.mz, datapoint.intensity);
      }

      try {
        double[] parameters = GaussianCurveFitter.create().fit(obs.toList());
        sigma += FWHM_CONSTANT * parameters[2];

      } catch (MathIllegalArgumentException e) {
        continue;
      }
      countProperIteration++;
    }
    double fwhm = sigma / numberOfScansForFWHMCalc;
    return fwhm;
  }

  
  public static double similarityValue(double[] referenceEIC, double[] gaussianValues,
      int leftBound, int rightBound) {
    double diffArea = 0.0;
    // This is the implementation of trapezoid.
    for (int j = 0; j < rightBound - leftBound; j++) {
      diffArea += Math.abs(0.5 * ((referenceEIC[j] - gaussianValues[j])
          + (referenceEIC[j + 1] - gaussianValues[j + 1])));
    }

    // Here similarity value is calculated.
    double curSimilarity =
        ((Math.exp(-diffArea / EXPONENT_FACTOR)) - EXPONENT_SHIFT) * EXPONENT_HEIGHT;
    return curSimilarity;
  }

  /**
   * <p>
   * normalize method is used for normalizing EIC by calculating its area and dividing each
   * intensity by the area.
   * 
   * @param roundedMz a {@link java.lang.Integer} object.This is m/z value which is multiplied by
   *        10000 because of it's use in sparse matrix.
   * @param leftBound a {@link java.lang.Integer} object. This is lowest scan number from which peak
   *        determining starts.
   * @param rightBound a {@link java.lang.Integer} object. This is highest scan number on which peak
   *        determining ends.
   * @param referenceEIC a {@link java.lang.Double} array. This array contains normalize intensities
   *        for given m/z value.(Intensities/area)
   * 
   * @return area a {@link java.lang.Double} object. This is area of normalize intensity points.
   *         </p>
   */
  public static double normalize(MultiKeyMap<Integer, Triplet> slice, int leftBound, int rightBound,
      int roundedMz, double[] referenceEIC) {

    double[] intensityValues = new double[rightBound - leftBound + 1];

    // Here area has been calculated for normalizing the intensities.
    for (int i = 0; i < rightBound - leftBound + 1; i++) {
      SliceSparseMatrix.Triplet obj = slice.get(i + leftBound, roundedMz);
      intensityValues[i] = obj == null ? 0.0 : obj.intensity;
    }

    return normalize(intensityValues, referenceEIC);
  }

  /**
   * <p>
   * normalize method is used for normalizing values by calculating its area and dividing each
   * value by the area.
   * </p>
   * 
   * @param values a {@link java.lang.Double} array. This array will have values to be normalized.
   * @param normValues a {@link java.lang.Double} array. This array will have normalized values.
   * 
   * @return area a {@link java.lang.Double} object. This is area of normalize intensity points.
   */
  public static double normalize(double[] values, double[] normValues) {

    double area = 0.0;
    // Here area has been calculated for normalizing the intensities.
    for (int i = 0; i < values.length - 1; i++) {
      area += 0.5 * (values[i] + values[i + 1]);
    }

    for (int i = 0; i < values.length; i++) {
      normValues[i] = values[i] / area;
    }

    return area;
  }
}
