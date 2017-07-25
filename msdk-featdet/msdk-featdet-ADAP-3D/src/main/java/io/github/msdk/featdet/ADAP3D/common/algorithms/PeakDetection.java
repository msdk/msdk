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
import java.util.List;

import org.apache.commons.collections4.map.MultiKeyMap;

import io.github.msdk.featdet.ADAP3D.common.algorithms.SliceSparseMatrix.Triplet;
import io.github.msdk.featdet.ADAP3D.datamodel.Result;

public class PeakDetection {

  /**
   * <p>
   * GoodPeakInfo class is used to save information of good peaks.
   * </p>
   */
  public static class GoodPeakInfo {
    double mz;
    int upperScanBound;
    int lowerScanBound;
  }

  private SliceSparseMatrix objSliceSparseMatrix;
  private Parameters objParameters;

  /**
   * <p>
   * Constructor
   * </p>
   * 
   * @param objSliceSparseMatrix is sparse matrix created from raw data file.
   * @param objParameter contains all the necessary parameters to find the peak.
   */
  PeakDetection(SliceSparseMatrix objSliceSparseMatrix, Parameters objParameter) {
    this.objSliceSparseMatrix = objSliceSparseMatrix;
    this.objParameters = objParameter;
  }

  /**
   * <p>
   * This method executes the iteration method to find good peaks.
   * </p>
   * 
   * @param maxIteration a {@link java.lang.Integer} object. This is the maximum number of times
   *        iteration method will be executed.
   * @return peakList a list of {@link GoodPeakInfo} object type. This contains information of good
   *         peaks.
   */
  public List<GoodPeakInfo> execute(int maxIteration) {

    CurveTool objCurveTool = new CurveTool(objSliceSparseMatrix);
    double fwhm = objCurveTool.estimateFwhmMs(objParameters.getRandomNumOfScan());
    int roundedFWHM = objSliceSparseMatrix.roundMZ(fwhm);
    int maxCount = 0;
    Triplet maxIntensityTriplet = objSliceSparseMatrix.findNextMaxIntensity();
    List<GoodPeakInfo> peakList = new ArrayList<GoodPeakInfo>();

    while (maxCount < maxIteration) {
      GoodPeakInfo goodPeak = iteration(maxIntensityTriplet, roundedFWHM);
      if (goodPeak != null)
        peakList.add(goodPeak);

      maxIntensityTriplet = objSliceSparseMatrix.findNextMaxIntensity();
      maxCount++;
    }


    return peakList;
  }


  /**
   * <p>
   * This method executes the iteration method to find good peaks.
   * </p>
   * 
   * @return peakList a list of {@link GoodPeakInfo} object type. This contains information of good
   *         peaks.
   */
  public List<GoodPeakInfo> execute(double intensityThreshold) {


    CurveTool objCurveTool = new CurveTool(objSliceSparseMatrix);
    double fwhm = objCurveTool.estimateFwhmMs(objParameters.getRandomNumOfScan());
    int roundedFWHM = objSliceSparseMatrix.roundMZ(fwhm);


    Triplet maxIntensityTriplet = objSliceSparseMatrix.findNextMaxIntensity();
    List<GoodPeakInfo> peakList = new ArrayList<GoodPeakInfo>();


    while (maxIntensityTriplet.intensity > intensityThreshold) {
      GoodPeakInfo goodPeak = iteration(maxIntensityTriplet, roundedFWHM);
      if (goodPeak != null)
        peakList.add(goodPeak);

      maxIntensityTriplet = objSliceSparseMatrix.findNextMaxIntensity();
    }

    return peakList;
  }

  /**
   * <p>
   * This method finds if there's a good peak or not.
   * </p>
   * 
   * @param triplet a
   *        {@link io.github.msdk.featdet.ADAP3D.common.algorithms.SliceSparseMatrix.Triplet}
   *        object. This is the element of sparse matrix.
   * @param fwhm a {@link java.lang.Double} object. This is estimated full width half max.
   * 
   * @return objPeakInfo a {@link GoodPeakInfo} object. This contains information of good peak. If
   *         there's no good peak it'll return null.
   */
  private GoodPeakInfo iteration(Triplet triplet, int fwhm) {

    GoodPeakInfo objPeakInfo = null;
    int lowerScanBound = triplet.scanNumber - objParameters.getDelta() < 0 ? 0
        : triplet.scanNumber - objParameters.getDelta();
    int upperScanBound =
        triplet.scanNumber + objParameters.getDelta() >= objSliceSparseMatrix.getSizeOfRawDataFile()
            ? objSliceSparseMatrix.getSizeOfRawDataFile() - 1
            : triplet.scanNumber + objParameters.getDelta();

    // Here we're getting horizontal slice.
    MultiKeyMap<Integer, Triplet> slice =
        objSliceSparseMatrix.getHorizontalSlice(triplet.mz, lowerScanBound, upperScanBound);

    // Below CWT is called to get bounds of peak.
    ContinuousWaveletTransform continuousWavelet =
        new ContinuousWaveletTransform(1, objParameters.getLargeScaleIn(), 1);
    List<ContinuousWaveletTransform.DataPoint> listOfDataPoint =
        new ArrayList<ContinuousWaveletTransform.DataPoint>();
    listOfDataPoint = objSliceSparseMatrix.getCWTDataPoint(slice);

    continuousWavelet.setX(listOfDataPoint);
    continuousWavelet.setSignal(listOfDataPoint);
    continuousWavelet.setPeakWidth(objParameters.getMinPeakWidth(),
        objParameters.getMaxPeakWidth());
    continuousWavelet.setcoefAreaRatioTolerance(objParameters.getCoefAreaRatioTolerance());

    // Peaks are detected from CWT.
    List<Result> peakList = continuousWavelet.findPeaks();

    // If there's no peak detected.
    if (peakList.isEmpty()) {
      removeDataPoints(triplet.mz - fwhm, triplet.mz + fwhm, lowerScanBound, upperScanBound);
    }

    else {
      Peak3DTest objPeak3DTest = new Peak3DTest(objSliceSparseMatrix, fwhm);
      BiGaussianSimilarityTest objBiGaussianTest = new BiGaussianSimilarityTest();

      boolean remove = true;

      for (int i = 0; i < peakList.size(); i++) {



        MultiKeyMap<Integer, Triplet> curSlice = objSliceSparseMatrix.getHorizontalSlice(triplet.mz,
            peakList.get(i).curLeftBound, peakList.get(i).curRightBound);

        double sliceMaxIntensity = curSlice.values().stream()
            .map(x -> x != null ? x.intensity : 0.0).max(Double::compareTo).orElse(0.0);
        int scanNumber = curSlice.values().stream()
            .map(x -> x != null && x.intensity == sliceMaxIntensity ? x.scanNumber : 0)
            .max(Integer::compareTo).orElse(0);

        // If there's no peak at apex.
        if (scanNumber != triplet.scanNumber) {
          if (remove) {
            removeDataPoints(triplet.mz - fwhm, triplet.mz + fwhm, lowerScanBound, upperScanBound);
            remove = false;
          }
          restoreDataPoints(triplet.mz - fwhm, triplet.mz + fwhm, peakList.get(i).curLeftBound,
              peakList.get(i).curRightBound);
        }

        // If there's peak at apex.
        else {
          Peak3DTest.Result peak = objPeak3DTest.execute(triplet.mz, peakList.get(i).curLeftBound,
              peakList.get(i).curRightBound, objParameters.getPeakSimilarityThreshold());
          boolean goodPeak = objBiGaussianTest.execute(curSlice, peakList.get(i).curLeftBound,
              peakList.get(i).curRightBound, triplet.mz,
              objParameters.getBiGaussianSimilarityThreshold());

          // If there's good peak
          if (peak.goodPeak && goodPeak) {
            removeDataPoints(peak.lowerMzBound, peak.upperMzBound, peakList.get(i).curLeftBound,
                peakList.get(i).curRightBound);
            objPeakInfo = new GoodPeakInfo();
            objPeakInfo.mz = (double) triplet.mz / 10000;
            objPeakInfo.lowerScanBound = peakList.get(i).curLeftBound;
            objPeakInfo.upperScanBound = peakList.get(i).curRightBound;

          } else {
            removeDataPoints(peak.lowerMzBound, peak.upperMzBound, peakList.get(i).curLeftBound,
                peakList.get(i).curRightBound);
          }
        }

      }
    }
    return objPeakInfo;
  }

  /**
   * <p>
   * This method removes data point in loop by calling removeDataPoints method from
   * SliceSparseMatrix.
   * </p>
   * 
   * @param lowerMZ a {@link java.lang.Integer} object. This is the lower m/z boundary from which
   *        data point removal starts.
   * @param upperMZ a {@link java.lang.Integer} object. This is the lower m/z boundary at which data
   *        point removal ends.
   * @param lowerScanBound a {@link java.lang.Integer} object. This is the lower scan boundary from
   *        which data point removal starts.
   * @param upperScanBound a {@link java.lang.Integer} object. This is the upper scan boundary at
   *        which data point removal ends.
   * 
   */
  private void removeDataPoints(int lowerMZ, int upperMZ, int lowerScanBound, int upperScanBound) {
    for (int i = lowerMZ; i <= upperMZ; i++) {
      objSliceSparseMatrix.removeDataPoints(i, lowerScanBound, upperScanBound);
    }
  }

  /**
   * <p>
   * This method restores data point in loop by calling restoreDataPoints method from
   * SliceSparseMatrix.
   * </p>
   * 
   * @param lowerMZ a {@link java.lang.Integer} object. This is the lower m/z boundary from which
   *        data point restoration starts.
   * @param upperMZ a {@link java.lang.Integer} object. This is the lower m/z boundary at which data
   *        point restoration ends.
   * @param lowerScanBound a {@link java.lang.Integer} object. This is the lower scan boundary from
   *        which data point restoration starts.
   * @param upperScanBound a {@link java.lang.Integer} object. This is the upper scan boundary at
   *        which data point restoration ends.
   */
  private void restoreDataPoints(int lowerMZ, int upperMZ, int lowerScanBound, int upperScanBound) {
    for (int i = lowerMZ; i <= upperMZ; i++) {
      objSliceSparseMatrix.restoreDataPoints(i, lowerScanBound, upperScanBound);
    }
  }
}
