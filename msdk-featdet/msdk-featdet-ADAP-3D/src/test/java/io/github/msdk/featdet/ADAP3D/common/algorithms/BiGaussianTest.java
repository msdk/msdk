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
import java.io.FileWriter;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.netcdf.NetCDFFileImportMethod;

public class BiGaussianTest {
	
	  private static final String TEST_DATA_PATH = "src/test/resources/";
	  private static RawDataFile rawFile;
	  private static SliceSparseMatrix objSliceSparseMatrix;
	  
	  @BeforeClass
	  public static void loadData() throws MSDKException {
	    // Import the file
	    File inputFile = new File(TEST_DATA_PATH + "test_output.cdf");
	    Assert.assertTrue("Cannot read test data", inputFile.canRead());
	    NetCDFFileImportMethod importer = new NetCDFFileImportMethod(inputFile);
	    rawFile = importer.execute();
	    objSliceSparseMatrix = new SliceSparseMatrix(rawFile);
	    Assert.assertNotNull(rawFile);
	    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);
	  }
	  
	  
	  @Test
	  public void testgetBiGaussianValue() throws MSDKException{
		  MultiKeyMap horizontalSlice = objSliceSparseMatrix.getHorizontalSlice(140.1037, 1, 23);
		  BiGaussian objBiGaussian = new BiGaussian(horizontalSlice,140.1037, 1, 23);
		  double biGaussianValue = objBiGaussian.getBiGaussianValue(6);
		  Assert.assertEquals(23971, biGaussianValue,1);		
	  } 
}
