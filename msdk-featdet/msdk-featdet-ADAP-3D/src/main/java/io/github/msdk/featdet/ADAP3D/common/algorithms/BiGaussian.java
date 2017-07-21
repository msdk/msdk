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


import java.lang.Math;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.MultiKeyMap;

import io.github.msdk.featdet.ADAP3D.common.algorithms.SliceSparseMatrix.Triplet;

import org.apache.commons.collections4.keyvalue.MultiKey;

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
   * Inside BiGaussian Constructor we're determining 4 BiGaussian Parameters. MaxHeight, Mu,
   * SigmaLeft and SigmaRight.
   * </p>
   * 
   * @param horizontalSlice a {@link org.apache.commons.collections4.map.MultiKeyMap} object. This
   *        is horizontal slice from the sparse matrix.
   * @param roundedmz a {@link java.lang.Double} object. It's rounded m/z value. Original m/z value
   *        multiplied by 10000.
   * @param leftBound a {@link java.lang.Integer} object. This is minimum scan number.
   * @param rightBound a {@link java.lang.Integer} object. This is maximum scan number.
   */
  BiGaussian(MultiKeyMap<Integer, Triplet> horizontalSlice, int roundedmz, int leftBound,
      int rightBound) {

    // This is max height for BiGaussian fit. It's in terms of intensities.
    maxHeight = horizontalSlice.values().stream().map(x -> x != null ? x.intensity : 0.0)
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
  private double InterpolationX(MultiKeyMap<Integer, Triplet> horizontalSlice, int mu,
      double halfHeight, int leftBound, int rightBound, int roundedmz, Direction direction) {

    int i = mu;
    Double Y1 = null;
    Double Y2 = null;
    int step = direction == Direction.RIGHT ? 1 : -1;

    while (leftBound <= i && i <= rightBound) {

      i += step;
      SliceSparseMatrix.Triplet triplet1 = horizontalSlice.get(i, roundedmz);

      if (triplet1 != null && triplet1.intensity < halfHeight) {
        Y1 = (double) triplet1.intensity;
        SliceSparseMatrix.Triplet triplet2 = horizontalSlice.get(i - step, roundedmz);
        if (triplet2 != null) {
          Y2 = (double) triplet2.intensity;
          break;
        }
      }


    }
    if (Y1 == null || Y2 == null)
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
  private int getScanNumber(MultiKeyMap<Integer, Triplet> horizontalSlice, double height) {
    int mu = 0;
    MapIterator<MultiKey<? extends Integer>, Triplet> iterator = horizontalSlice.mapIterator();

    while (iterator.hasNext()) {
      iterator.next();

      MultiKey<? extends Integer> mk = (MultiKey<? extends Integer>) iterator.getKey();
      double intensity = iterator.getValue() != null ? (iterator.getValue()).intensity : 0;

      if (intensity == height) {
        mu = (int) mk.getKey(0);
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
   */
  public double getValue(int x) {

    double sigma = x >= mu ? sigmaRight : sigmaLeft;
    double exponentialTerm = Math.exp(-1 * Math.pow(x - mu, 2) / (2 * Math.pow(sigma, 2)));
    return maxHeight * exponentialTerm;

  }
}
