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
import io.github.msdk.featdet.ADAP3D.common.algorithms.PeakDetection;
import io.github.msdk.featdet.ADAP3D.common.algorithms.SliceSparseMatrix;
import io.github.msdk.datamodel.rawdata.RawDataFile;

/**
 * <p>
 * This class is used to run the whole ADAP3D algorithm and get peaks.
 * </p>
 *
 */
public class ADAP3DFeatureDetectionMethod implements MSDKMethod<List<Feature>>{

    
  private SliceSparseMatrix objSliceSparseMatrix;

  private static final double LOW_BOUND_PEAK_WIDTH_PERCENT = 0.75;
  
  private List<Feature> finalFeatureList;
  
  private boolean canceled = false;

  ADAP3DFeatureDetectionMethod(RawDataFile rawFile) {
    objSliceSparseMatrix = new SliceSparseMatrix(rawFile);
  }

  /**
   * <p>
   * This method finds first 20 peaks from raw file and calculate parameters. Then it runs algorithm
   * with new parameters and find rest of the peaks. It returns the list of SimpleFeature which
   * includes chromatogram.
   * </p>
   * 
   * @return newFeatureList a list of {@link io.github.msdk.datamodel.features.Feature}
   * 
   */
  public List<Feature> execute() {

    CurveTool objCurveTool = new CurveTool(objSliceSparseMatrix);
    double fwhm = objCurveTool.estimateFwhmMs();
    int roundedFWHM = objSliceSparseMatrix.roundMZ(fwhm);

    Parameters objParameters = new Parameters();

    PeakDetection objPeakDetection = new PeakDetection(objSliceSparseMatrix);
    List<PeakDetection.GoodPeakInfo> goodPeakList =
        objPeakDetection.execute(1000, objParameters, roundedFWHM);
    finalFeatureList = new ArrayList<Feature>();
    finalFeatureList = getFeature(goodPeakList,finalFeatureList);

    double[] peakWidth = new double[goodPeakList.size()];
    double avgCoefOverArea = 0.0;
    double avgPeakWidth = 0.0;

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

    objParameters.setLargeScaleIn(highestWaveletScale);
    objParameters.setMinPeakWidth(peakDurationLowerBound);
    objParameters.setMaxPeakWidth(peakDurationUpperBound);
    objParameters.setCoefAreaRatioTolerance(coefOverAreaThreshold);


    List<PeakDetection.GoodPeakInfo> newGoodPeakList =
        objPeakDetection.execute(objParameters, roundedFWHM);
    finalFeatureList = getFeature(newGoodPeakList,finalFeatureList);
    
    if(canceled)
      return null;

    return finalFeatureList;
  }



  /**
   * <p>
   * This method takes list of GoodPeakInfo and returns list of type SimpleFeature. This method also
   * builds Chromatogram for each good peak.
   * </p>
   * 
   * @return featureList a list of {@link io.github.msdk.datamodel.features.Feature}
   */
  private List<Feature> getFeature(List<PeakDetection.GoodPeakInfo> goodPeakList,List<Feature> featureList) {

    int lowerScanBound;
    int upperScanBound;

    for (PeakDetection.GoodPeakInfo goodPeakInfo : goodPeakList) {

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

    return featureList;

  }

  /** {@inheritDoc} */
  @Override
  public Float getFinishedPercentage() {
    return (float) (finalFeatureList.size() == 0?0:1);
  }

  /** {@inheritDoc} */
  @Override
  public List<Feature> getResult() {
    return finalFeatureList;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    this.canceled = true;    
  }
}
