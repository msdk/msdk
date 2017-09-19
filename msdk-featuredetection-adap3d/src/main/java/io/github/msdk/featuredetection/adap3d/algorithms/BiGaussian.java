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
package io.github.msdk.featuredetection.adap3d.algorithms;


import java.lang.Math;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import io.github.msdk.featuredetection.adap3d.algorithms.SliceSparseMatrix.Triplet;


/**
 * <p>
 * BiGaussian Class is used for fitting BiGaussian on EIC. BiGaussian is composed of 2 halves of
 * Gaussian with different standard deviations. It depends on 4 parameters (height, mu, sigmaLeft,
 * sigmaRight) and computed by the formula
 * </p>
 *
 * <p>
 * f(x) = height * exp(-(x-mu)^2 / (2 * sigmaRight^2)) if x > mu
 * </p>
 * <p>
 * f(x) = height * exp(-(x-mu)^2 / (2 * sigmaLeft^2)) if x < mu
 * </p>
 */
public class BiGaussian {


  enum Direction {
    RIGHT, LEFT
  }


  // This is used for storing BiGaussian parameters inside constructor.
  public final double maxHeight;
  public final int mu;
  public final double sigmaLeft;
  public final double sigmaRight;

  /**
   * <p>
   * Inside BiGaussian Constructor we're determining 4 BiGaussian ADAP3DFeatureDetectionParameters. MaxHeight, Mu,
   * SigmaLeft and SigmaRight.
   * </p>
   * 
   * @param horizontalSlice a {@link java.util.List} object. This
   *        is horizontal slice from the sparse matrix.
   * @param roundedmz a {@link java.lang.Double} object. It's rounded m/z value. Original m/z value
   *        multiplied by 10000.
   * @param leftBound a {@link java.lang.Integer} object. This is minimum scan number.
   * @param rightBound a {@link java.lang.Integer} object. This is maximum scan number.
   */
  BiGaussian(List<Triplet> horizontalSlice, int roundedmz, int leftBound, int rightBound) {

    // This is max height for BiGaussian fit. It's in terms of intensities.
    maxHeight = horizontalSlice.stream().map(x -> x != null ? x.intensity : 0.0)
        .max(Double::compareTo).orElse(0.0);

    // Below logic is for finding BiGaussian parameters.
    mu = getScanNumber(horizontalSlice, maxHeight);
    double halfHeight = (double) maxHeight / 2;


    double interpolationLeftSideX = InterpolationX(horizontalSlice, mu, halfHeight, leftBound,
        rightBound, roundedmz, Direction.LEFT);
    // This is sigma left for BiGaussian.
    sigmaLeft = (mu - interpolationLeftSideX) / Math.sqrt(2 * Math.log(2));



    double interpolationRightSideX = InterpolationX(horizontalSlice, mu, halfHeight, leftBound,
        rightBound, roundedmz, Direction.RIGHT);
    // This is sigma right for BiGaussian.
    sigmaRight = ((interpolationRightSideX - mu)) / Math.sqrt(2 * Math.log(2));
  }


  /**
   * <p>
   * InterpolationX is used to calculate X value of Halfwidth-halfheight point of either left or
   * right half of BiGaussian.
   * </p>
   * 
   * @param mu a {@link java.lang.Integer} object. This is X value for maximum height in terms of
   *        scan number.
   * @param halfHeight a {@link java.lang.Double} object. This is m/z value from the raw file.
   * @param leftBound a {@link java.lang.Integer} object. This is minimum scan number.
   * @param rightBound a {@link java.lang.Integer} object. This is maximum scan number.
   * @param roundedmz a {@link java.lang.Integer} object. This is rounded m/z value.
   * @param direction a {@link Enum} object. This decides for which half we want to calculate X
   *        value.
   */
  private double InterpolationX(List<Triplet> horizontalSlice, int mu, double halfHeight,
      int leftBound, int rightBound, int roundedmz, Direction direction) {

    int i = mu;
    double Y1 = Double.NaN;
    double Y2 = Double.NaN;
    int step = direction == Direction.RIGHT ? 1 : -1;

    Comparator<Triplet> compareScanMz = new Comparator<Triplet>() {

      @Override
      public int compare(Triplet o1, Triplet o2) {

        int scan1 = o1.scanListIndex;
        int scan2 = o2.scanListIndex;
        int scanCompare = Integer.compare(scan1, scan2);

        if (scanCompare != 0) {
          return scanCompare;
        } else {
          int mz1 = o1.mz;
          int mz2 = o2.mz;
          return Integer.compare(mz1, mz2);
        }
      }
    };


    Collections.sort(horizontalSlice, compareScanMz);

    while (leftBound <= i && i <= rightBound) {

      i += step;
      Triplet searchTriplet1 = new Triplet();
      searchTriplet1.mz = roundedmz;
      searchTriplet1.scanListIndex = i;
      int index1 = Collections.binarySearch(horizontalSlice, searchTriplet1, compareScanMz);
      if (index1 >= 0) {
        SliceSparseMatrix.Triplet triplet1 = horizontalSlice.get(index1);

        if (triplet1.intensity != 0 && triplet1.intensity < halfHeight) {
          Triplet searchTriplet2 = new Triplet();
          searchTriplet2.mz = roundedmz;
          searchTriplet2.scanListIndex = i - step;
          Y1 = (double) triplet1.intensity;
          int index2 = Collections.binarySearch(horizontalSlice, searchTriplet2, compareScanMz);
          if (index2 >= 0) {
            SliceSparseMatrix.Triplet triplet2 = horizontalSlice.get(index2);
            if (triplet2.intensity != 0) {
              Y2 = (double) triplet2.intensity;
              break;
            }
          }
        }
      }


    }
    if (Y1 == Double.NaN || Y2 == Double.NaN)
      throw new IllegalArgumentException("Cannot find BiGaussian.");
    /*
     * I've used the formula of line passing through points (x1,y1) and (x2,y2) in interpolationX.
     * Those are the points which are exactly above and below of halfHeight(halfMaxIntensity). I've
     * to find exact point between those two points. I've y-value for that point as halfHeight but I
     * don't have x-value. X-value is scan number and Y-value is intensity.
     */

    double X = ((halfHeight - Y1) * ((i - step) - i) / (Y2 - Y1)) + i;
    return X;
  }

  /**
   * <p>
   * This method is used for getting scan number for given intensity value.
   * </p>
   * 
   * @param height a {@link java.lang.Double} object. This is intensity value from the horizontal
   *        slice from sparse matrix.
   */
  private int getScanNumber(List<Triplet> horizontalSlice, double height) {
    int mu = 0;
    Iterator<Triplet> iterator = horizontalSlice.iterator();

    while (iterator.hasNext()) {
      Triplet triplet = iterator.next();
      if (triplet.intensity == height) {
        mu = triplet.scanListIndex;
        break;
      }
    }
    return mu;
  }

  /**
   * <p>
   * This method is used calculating BiGaussian values for EIC.
   * </p>
   *
   * @param x a {@link java.lang.Integer} object. This is scan number.
   * @return a double.
   */
  public double getValue(int x) {

    double sigma = x >= mu ? sigmaRight : sigmaLeft;
    double exponentialTerm = Math.exp(-1 * Math.pow(x - mu, 2) / (2 * Math.pow(sigma, 2)));
    return maxHeight * exponentialTerm;

  }
}
