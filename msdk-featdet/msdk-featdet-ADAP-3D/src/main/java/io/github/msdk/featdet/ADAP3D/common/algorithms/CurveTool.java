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

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

/**
 * <p>
 * CurveTool class is used for estimation of Full width half maximum.
 * </p>
 */
public class CurveTool {

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
        sigma += 2.35482 * parameters[2];

      } catch (MathIllegalArgumentException e) {
        continue;
      }
      countProperIteration++;
    }
    double fwhm = sigma / numberOfScansForFWHMCalc;
    return fwhm;
  }

}
