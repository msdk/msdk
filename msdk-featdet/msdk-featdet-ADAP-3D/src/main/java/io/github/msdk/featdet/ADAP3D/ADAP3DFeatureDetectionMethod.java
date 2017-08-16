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
package io.github.msdk.featdet.ADAP3D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.features.Feature;
import io.github.msdk.datamodel.impl.SimpleChromatogram;
import io.github.msdk.datamodel.impl.SimpleFeature;
import io.github.msdk.featdet.ADAP3D.common.algorithms.CurveTool;
import io.github.msdk.featdet.ADAP3D.common.algorithms.Parameters;
import io.github.msdk.featdet.ADAP3D.common.algorithms.ADAP3DPeakDetectionAlgorithm;
import io.github.msdk.featdet.ADAP3D.common.algorithms.SliceSparseMatrix;
import io.github.msdk.datamodel.rawdata.RawDataFile;

/**
 * <p>
 * This class is used to run the whole ADAP3D algorithm and get peaks.
 * </p>
 *
 */
public class ADAP3DFeatureDetectionMethod implements MSDKMethod<List<Feature>> {


  private SliceSparseMatrix objSliceSparseMatrix;

  // percent of average peak width (found from initial high intensity peaks)
  // used for determining allowance of peak widths lower bound.
  private static final double LOW_BOUND_PEAK_WIDTH_PERCENT = 0.75;

  private List<Feature> finalFeatureList;

  private ADAP3DPeakDetectionAlgorithm objPeakDetection;

  private boolean canceled = false;


  /**
   * <p>
   * Constructor
   * </p>
   * 
   * @param rawFile {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
   */
  ADAP3DFeatureDetectionMethod(RawDataFile rawFile) {
    objSliceSparseMatrix = new SliceSparseMatrix(rawFile);
  }

  /**
   * <p>
   * This method performs 3 steps:<br>
   * Step 1. Run ADAP3DPeakDetectionAlgorithm.execute with the default parameters and detect 20 highest peaks. <br>
   * Step 2. Estimate new parameters for ADAP3DPeakDetectionAlgorithm from those 20 peaks. <br>
   * Step 3. Run ADAP3DPeakDetectionAlgorithm.execute with the new parameters and detect all other peaks.
   * </p>
   * 
   * @return newFeatureList a list of {@link io.github.msdk.datamodel.features.Feature}
   * 
   */
  public List<Feature> execute() {

    // Here fwhm across all the scans of raw data file is determined.
    CurveTool objCurveTool = new CurveTool(objSliceSparseMatrix);
    double fwhm = objCurveTool.estimateFwhmMs();
    int roundedFWHM = objSliceSparseMatrix.roundMZ(fwhm);

    Parameters objParameters = new Parameters();

    // Here first 20 peaks are determined to estimate the parameters to determine remaining peaks.
    objPeakDetection = new ADAP3DPeakDetectionAlgorithm(objSliceSparseMatrix);
    List<ADAP3DPeakDetectionAlgorithm.GoodPeakInfo> goodPeakList =
        objPeakDetection.execute(20, objParameters, roundedFWHM);

    // If the algorithm's execution is stopped, execute method of PeakdDtection class will return
    // null. Hence execute method of this class will also return null.
    if (canceled)
      return null;

    // Here we're making features for first 20 peaks and add it into the list of feature.
    finalFeatureList = new ArrayList<Feature>();
    getADAP3DPeakFeature(goodPeakList, finalFeatureList);

    // Estimation of parameters.
    double[] peakWidth = new double[goodPeakList.size()];
    double avgCoefOverArea = 0.0;
    double avgPeakWidth = 0.0;

    // Average peak width has been determined in terms of retention time.
    for (int i = 0; i < goodPeakList.size(); i++) {
      peakWidth[i] = objSliceSparseMatrix.getRetentionTime(goodPeakList.get(i).upperScanBound) / 60
          - objSliceSparseMatrix.getRetentionTime(goodPeakList.get(i).lowerScanBound) / 60;
      avgPeakWidth += peakWidth[i];
      avgCoefOverArea += goodPeakList.get(i).objResult.coefOverArea;
    }

    avgPeakWidth = avgPeakWidth / goodPeakList.size();
    avgCoefOverArea = avgCoefOverArea / goodPeakList.size();

    int highestWaveletScale = (int) (avgPeakWidth * 60 / 2);
    double coefOverAreaThreshold = avgCoefOverArea / 1.5;


    List<Double> peakWidthList = Arrays.asList(ArrayUtils.toObject(peakWidth));
    double peakDurationLowerBound = avgPeakWidth - LOW_BOUND_PEAK_WIDTH_PERCENT * avgPeakWidth;

    double peakDurationUpperBound =
        Collections.max(peakWidthList) + LOW_BOUND_PEAK_WIDTH_PERCENT * avgPeakWidth;

    // set the estimated parameters.
    objParameters.setLargeScaleIn(highestWaveletScale);
    objParameters.setMinPeakWidth(peakDurationLowerBound);
    objParameters.setMaxPeakWidth(peakDurationUpperBound);
    objParameters.setCoefAreaRatioTolerance(coefOverAreaThreshold);

    // run the algorithm with new parameters to determine the remaining peaks.
    List<ADAP3DPeakDetectionAlgorithm.GoodPeakInfo> newGoodPeakList =
        objPeakDetection.execute(objParameters, roundedFWHM);

    // If the algorithm's execution is stopped, execute method of PeakdDtection class will return
    // null. Hence execute method of this class will also return null.
    if (canceled)
      return null;

    // Here we're making features for remaining peaks and add it into the list of feature.
    getADAP3DPeakFeature(newGoodPeakList, finalFeatureList);

    return finalFeatureList;
  }



  /**
   * <p>
   * This method takes list of GoodPeakInfo and returns list of type SimpleFeature. This method also
   * builds Chromatogram for each good peak.
   * </p>
   */
  private void getADAP3DPeakFeature(List<ADAP3DPeakDetectionAlgorithm.GoodPeakInfo> goodPeakList,
      List<Feature> featureList) {

    int lowerScanBound;
    int upperScanBound;

    for (ADAP3DPeakDetectionAlgorithm.GoodPeakInfo goodPeakInfo : goodPeakList) {

      lowerScanBound = goodPeakInfo.lowerScanBound;
      upperScanBound = goodPeakInfo.upperScanBound;
      double mz = goodPeakInfo.mz;
      float[] rtArray = objSliceSparseMatrix.getRetentionTimeArray(lowerScanBound, upperScanBound);
      float[] intensityArray = objSliceSparseMatrix.getIntensities(goodPeakInfo);
      double[] mzArray = new double[upperScanBound - lowerScanBound + 1];

      for (int j = 0; j < upperScanBound - lowerScanBound + 1; j++) {
        mzArray[j] = mz;
      }

      SimpleChromatogram chromatogram = new SimpleChromatogram();
      chromatogram.setDataPoints(rtArray, mzArray, intensityArray,
          upperScanBound - lowerScanBound + 1);

      SimpleFeature feature = new SimpleFeature();
      feature.setArea(CurveTool.normalize(intensityArray));
      feature.setHeight(goodPeakInfo.maxHeight);

      int maxHeightScanNumber = goodPeakInfo.maxHeightScanNumber;
      float retentionTime = (float) objSliceSparseMatrix.getRetentionTime(maxHeightScanNumber);

      feature.setRetentionTime(retentionTime);
      feature.setMz(mz);
      feature.setChromatogram(chromatogram);
      featureList.add(feature);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Float getFinishedPercentage() {
    if (objPeakDetection != null)
      return objPeakDetection.getFinishedPercent();
    else
      return (float) 0;
  }

  /** {@inheritDoc} */
  @Override
  public List<Feature> getResult() {
    return finalFeatureList;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    if (objPeakDetection != null) {
      objPeakDetection.cancel();
      canceled = true;
    }
  }
}
