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
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.featuredetection.adap3d.algorithms.ContinuousWaveletTransform;
import io.github.msdk.featuredetection.adap3d.algorithms.SliceSparseMatrix;
import io.github.msdk.featuredetection.adap3d.algorithms.SliceSparseMatrix.Triplet;
import io.github.msdk.io.mzxml.MzXMLFileImportMethod;

public class SliceSparseMatrixTest {

  private static RawDataFile rawFile;
  private static SliceSparseMatrix objSliceSparseMatrix;

  private static Path getResourcePath(String resource) throws MSDKException {
    final URL url = SliceSparseMatrixTest.class.getClassLoader().getResource(resource);
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
  public void getHorizontalSlice() throws MSDKException, IOException {
    List<Triplet> slice = objSliceSparseMatrix.getHorizontalSlice(181.0596, 50, 77);
    Assert.assertEquals(28, slice.size());
  }

  @Test
  public void getVerticalSlice() throws MSDKException, IOException {
    List<SliceSparseMatrix.VerticalSliceDataPoint> slice = objSliceSparseMatrix.getVerticalSlice(5);
    Assert.assertEquals(5389, slice.size());
  }

  @Test
  public void testFindNextMaxIntensity() throws MSDKException, IOException {
    Assert.assertEquals(9695762.0, objSliceSparseMatrix.findNextMaxIntensity().intensity, 0);
  }

  @Test
  public void testGetRetentionTimeGetIntensity() throws MSDKException, IOException {
    List<Triplet> slice = objSliceSparseMatrix.getHorizontalSlice(181.0596, 50, 77);
    List<ContinuousWaveletTransform.DataPoint> listOfDataPoint =
        objSliceSparseMatrix.getCWTDataPoint(slice);
    Assert.assertNotNull(listOfDataPoint);
  }

  @Test
  public void testRemoveDataPoints() throws MSDKException, IOException {
    List<Triplet> updatedTripletList = objSliceSparseMatrix.removeDataPoints(1810596, 50, 77);
    for (int i = 0; i < updatedTripletList.size(); i++) {
      SliceSparseMatrix.Triplet triplet = updatedTripletList.get(i);
      if (triplet.removed == 1) {
        Assert.assertEquals(51, triplet.scanListIndex);
        break;
      }
    }
  }
}
