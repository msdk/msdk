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
import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import org.apache.commons.collections4.map.MultiKeyMap;



public class SliceSparseMatrixTest {
	
	private static final String TEST_DATA_PATH = "src/test/resources/";
	private static RawDataFile rawFile;
	
	
	@BeforeClass
	public static void loadData() throws MSDKException {
		
	    // Import the file
	    File inputFile = new File(TEST_DATA_PATH + "orbitrap_300-600mz.mzML");
	    Assert.assertTrue("Cannot read test data", inputFile.canRead());
	    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
	    rawFile = importer.execute();
	    Assert.assertNotNull(rawFile);
	    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);
	  }
	
	
	@Test
	public void testSliceSparseMatrix() throws MSDKException,IOException{
		SliceSparseMatrix objSliceSparseMatrix = new SliceSparseMatrix(rawFile);
		MultiKeyMap slice = objSliceSparseMatrix.getSlice(30200000, 0, 208);
		int size = slice.size();
		for(int i=0;i<size;i++){
			Assert.assertTrue(slice.containsKey(new Integer(new Integer(i)),new Integer(30200000)));
		}
		Assert.assertEquals(209, size);
	}
}
