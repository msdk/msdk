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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
   * @param mz a {@link java.lang.Double} object. This is m/z value from the raw file.
   * @param leftBound a {@link java.lang.Integer} object. This is minimum scan number.
   * @param rightBound a {@link java.lang.Integer} object. This is maximum scan number.
   */
  BiGaussian(MultiKeyMap<Integer, Triplet> horizontalSlice, double mz, int leftBound,
      int rightBound) {

    // This is max height for BiGaussian fit. It's in terms of intensities.
    maxHeight = horizontalSlice.values().stream().map(x -> x != null ? x.intensity : 0.0)
        .max(Double::compareTo).orElse(0.0);

    // Below logic is for finding BiGaussian parameters.
    mu = getScanNumber(horizontalSlice, maxHeight);
    int roundedmz = (int) Math.round(mz * 10000);
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
    double interpolationY1 = 0;
    double interpolationY2 = 0;
    int previousX2 = direction == Direction.RIGHT ? 1 : -1;

    while (leftBound <= i && i <= rightBound) {

      i += previousX2;
      SliceSparseMatrix.Triplet triplet1 = horizontalSlice.get(i, roundedmz);

      if (triplet1 != null && triplet1.intensity < halfHeight) {
        interpolationY1 = triplet1.intensity;
        SliceSparseMatrix.Triplet triplet2 = horizontalSlice.get(i - previousX2, roundedmz);
        if (triplet2 != null) {
          interpolationY2 = triplet2.intensity;
          break;
        }
      }



    }

    double interpolationX = ((halfHeight - interpolationY1) * ((i - previousX2) - i)
        / (interpolationY2 - interpolationY1)) + i;
    return interpolationX;
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

      MultiKey<Integer> mk = (MultiKey<Integer>) iterator.getKey();
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
