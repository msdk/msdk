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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.featuredetection.adap3d.algorithms.ContinuousWaveletTransform;
import io.github.msdk.featuredetection.adap3d.datamodel.Result;
import io.github.msdk.io.mzxml.MzXMLFileImportMethod;


public class ContinuousWaveletTransformTest {

  private static RawDataFile rawFile;

  private static Path getResourcePath(String resource) throws MSDKException {
    final URL url = ContinuousWaveletTransformTest.class.getClassLoader().getResource(resource);
    try {
      return Paths.get(url.toURI()).toAbsolutePath();
    } catch (URISyntaxException e) {
      throw new MSDKException(e);
    }
  }

  @BeforeClass
  public static void loadData() throws MSDKException {

    // Import the file
    String file = "tiny.mzXML";
    Path path = getResourcePath(file);
    File inputFile = path.toFile();
    Assert.assertTrue("Cannot read test data", inputFile.canRead());
    MzXMLFileImportMethod importer = new MzXMLFileImportMethod(inputFile);
    rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
  }

  @Test
  public void testCWT() throws MSDKException, IOException {

    List<MsScan> listOfScans = rawFile.getScans();
    ArrayList<Double> rtBuffer = new ArrayList<Double>();
    double mzBuffer[] = new double[10000];
    float intensityBuffer[] = new float[10000];
    ArrayList<Double> correctIntensityBuffer = new ArrayList<Double>();

    for (int l = 0; l < listOfScans.size(); l++) {

      float totalIntensity = 0;
      MsScan scan = listOfScans.get(l);
      mzBuffer = scan.getMzValues();
      intensityBuffer = scan.getIntensityValues();
      for (int k = 0; k < mzBuffer.length; k++) {
        if (mzBuffer[k] >= 181.0596 && mzBuffer[k] <= 181.0700) {
          totalIntensity += intensityBuffer[k];
        }
      }

      correctIntensityBuffer.add(new Double(totalIntensity));
      rtBuffer.add(new Double((scan.getRetentionTime()) / 60));

    }

    List<ContinuousWaveletTransform.DataPoint> listOfDataPoint =
        new ArrayList<ContinuousWaveletTransform.DataPoint>();

    for (int i = 0; i < correctIntensityBuffer.size(); i++) {
      ContinuousWaveletTransform.DataPoint datapoint = new ContinuousWaveletTransform.DataPoint();
      datapoint.rt = rtBuffer.get(i).doubleValue();
      datapoint.intensity = correctIntensityBuffer.get(i).doubleValue();
      listOfDataPoint.add(datapoint);
    }

    ContinuousWaveletTransform continuousWavelet = new ContinuousWaveletTransform(1, 10, 1);
    continuousWavelet.setX(listOfDataPoint);
    continuousWavelet.setSignal(listOfDataPoint);
    continuousWavelet.setPeakWidth(0.00, 10.00);
    continuousWavelet.setcoefAreaRatioTolerance(5);

    List<Result> peakList = continuousWavelet.findPeaks();
    boolean peakAssertion = false;


    for (int j = 0; j < peakList.size(); j++) {
      if (peakList.get(j).curLeftBound == 50 && peakList.get(j).curRightBound == 77) {
        peakAssertion = true;
        break;
      }
    }
    Assert.assertEquals(true, peakAssertion);
  }
}
