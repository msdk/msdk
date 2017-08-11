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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.impl.SimpleFeature;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.featdet.ADAP3D.common.algorithms.SliceSparseMatrix;
import io.github.msdk.io.mzxml.MzXMLFileImportMethod;

public class PeakDetectionTest {

  private static RawDataFile rawFile;
  private static SliceSparseMatrix objSliceSparseMatrix;

  private static Path getResourcePath(String resource) throws MSDKException {
    final URL url = PeakDetectionTest.class.getClassLoader().getResource(resource);
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
    objSliceSparseMatrix = new SliceSparseMatrix(rawFile);
    Assert.assertNotNull(rawFile);
  }

  @Test
  public void testPeakDetection() throws MSDKException, IOException {

    ADAP3DFeatureDetectionMethod ob = new ADAP3DFeatureDetectionMethod(objSliceSparseMatrix);
    List<SimpleFeature> featureList = ob.execute();
    Assert.assertNotNull(featureList);

    Path path = getResourcePath("output.csv");
    File inputFile = path.toFile();
    BufferedReader reader = new BufferedReader(new FileReader(inputFile));
    String line = null;
    int index = 0;

    while ((line = reader.readLine()) != null) {
      String[] values = line.split(",");
      Assert.assertEquals((double)Double.parseDouble(values[0]), (double)featureList.get(index).getMz(),0.1);
      Assert.assertEquals((double)Double.parseDouble(values[3]), (double)featureList.get(index).getHeight(),0.1);
      Assert.assertEquals((double)Double.parseDouble(values[4]), (double)featureList.get(index).getRetentionTime(),0.1);
      index++;
    }
    reader.close();
  }

}
