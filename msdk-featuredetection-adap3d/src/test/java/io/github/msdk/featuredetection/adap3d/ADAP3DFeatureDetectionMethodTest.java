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
package io.github.msdk.featuredetection.adap3d;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.Feature;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.io.mzxml.MzXMLFileImportMethod;

public class ADAP3DFeatureDetectionMethodTest {

  private static RawDataFile rawFile;

  private static Path getResourcePath(String resource) throws MSDKException {
    final URL url = ADAP3DFeatureDetectionMethodTest.class.getClassLoader().getResource(resource);
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
  @Ignore("Temporarily ignored, until ADAP3D work is resumed")
  public void testPeakDetection() throws MSDKException, IOException {

    ADAP3DFeatureDetectionMethod ob =
        new ADAP3DFeatureDetectionMethod(rawFile, new ADAP3DFeatureDetectionParameters());
    List<Feature> featureList = ob.execute();
    Assert.assertNotNull(featureList);
        
    Path path = getResourcePath("output.csv");
    File inputFile = path.toFile();
    BufferedReader reader = new BufferedReader(new FileReader(inputFile));
    String line = null;
    int index = 0;

    while ((line = reader.readLine()) != null) {
      String[] values = line.split(",");
      Feature feature = featureList.get(index);
      float[] retentionTimeArray = feature.getChromatogram().getRetentionTimes();
      Assert.assertEquals((double) Double.parseDouble(values[0]), (double) feature.getMz(), 0.1);
      Assert.assertEquals((double) Double.parseDouble(values[1]), (double) retentionTimeArray[0],
          0.1);
      Assert.assertEquals((double) Double.parseDouble(values[2]),
          (double) retentionTimeArray[retentionTimeArray.length - 1], 0.1);
      Assert.assertEquals((double) Double.parseDouble(values[3]), (double) feature.getHeight(),
          0.1);
      Assert.assertEquals((double) Double.parseDouble(values[4]),
          (double) feature.getRetentionTime(), 0.1);
      index++;
    }
    reader.close();
  }

}
