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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.netcdf.NetCDFFileImportMethod;

public class Peak3DFunctionTest {

  private static RawDataFile rawFile;
  private static CurveTool objCurveTool;
  private static SliceSparseMatrix objSliceSparseMatrix;

  private static Path getResourcePath(String resource) throws MSDKException {
    final URL url = Peak3DFunctionTest.class.getClassLoader().getResource(resource);
    try {
      return Paths.get(url.toURI()).toAbsolutePath();
    } catch (URISyntaxException e) {
      throw new MSDKException(e);
    }
  }


  @BeforeClass
  public static void loadData() throws MSDKException {

    // Import the file
    String file = "test_output.cdf";
    Path path = getResourcePath(file);
    File inputFile = path.toFile();
    Assert.assertTrue("Cannot read test data", inputFile.canRead());
    NetCDFFileImportMethod importer = new NetCDFFileImportMethod(inputFile);
    rawFile = importer.execute();
    objSliceSparseMatrix = new SliceSparseMatrix(rawFile);
    objCurveTool = new CurveTool(objSliceSparseMatrix);
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);
  }

  @Test
  public void testPeakSimilarityFunction() throws MSDKException {

    double fwhm = objCurveTool.estimateFwhmMs(20);

    Peak3DTest objPeak3DTest = new Peak3DTest(objSliceSparseMatrix, fwhm);

    Peak3DTest.Result objResult = objPeak3DTest.execute(140.1037, 1, 23);
    Assert.assertEquals(4, objResult.similarityValues.size());
    Assert.assertEquals(1401028, objResult.lowerMzBound);
    Assert.assertEquals(1401047, objResult.upperMzBound);
  }

}
