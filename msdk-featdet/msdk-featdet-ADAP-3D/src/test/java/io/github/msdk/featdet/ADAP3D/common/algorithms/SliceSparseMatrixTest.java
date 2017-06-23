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
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzml.MzMLFileImportMethod;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;



public class SliceSparseMatrixTest {
	
	private static final String TEST_DATA_PATH = "src/test/resources/";
	private static RawDataFile rawFile;
	private static SliceSparseMatrix objSliceSparseMatrix;
	
	
	
	@BeforeClass
	public static void loadData() throws MSDKException {
		
	    // Import the file
	    File inputFile = new File(TEST_DATA_PATH + "orbitrap_300-600mz.mzML");
	    Assert.assertTrue("Cannot read test data", inputFile.canRead());
	    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
	    rawFile = importer.execute();
	    objSliceSparseMatrix = new SliceSparseMatrix(rawFile);
	    Assert.assertNotNull(rawFile);
	    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);
	  }
	
	
	@Test
	public void getHorizontalSlice() throws MSDKException,IOException{
		MultiKeyMap slice = objSliceSparseMatrix.getHorizontalSlice(301.15106201171875, 0, 208);
		int size = slice.size();
		for(int i=0;i<size;i++){
			Assert.assertTrue(slice.containsKey(new Integer(i),new Integer(3011511)));
		}
		Assert.assertEquals(209, size);
	}
	
	@Test
	public void getVerticalSlice() throws MSDKException,IOException{
		List<SliceSparseMatrix.VerticalSliceDataPoint> slice = objSliceSparseMatrix.getVerticalSlice(5);
		Assert.assertEquals(46004, slice.size());
	}
	
	@Test
	public void testFindNextMaxIntensity() throws MSDKException,IOException{
		double intensityValues[] = {8538462.0,8521695.0,8365356.0};
		for(int i=0;i<3;i++){
			Assert.assertEquals(intensityValues[i], objSliceSparseMatrix.findNextMaxIntensity().intensity,0);
		}
	}
	
	@Test
	public void testGetRetentionTimeGetIntensity() throws MSDKException,IOException{
		MultiKeyMap slice = objSliceSparseMatrix.getHorizontalSlice(301.15106201171875, 0, 208);
		int size = slice.size();
		List<ContinuousWaveletTransform.DataPoint> listOfDataPoint = objSliceSparseMatrix.getCWTDataPoint(slice);
		Assert.assertNotNull(listOfDataPoint);
	}
	
	@Test
	public void testRemoveDataPoints() throws MSDKException,IOException{
		MultiKeyMap updatedTripletMap = objSliceSparseMatrix.removeDataPoints(301.15106201171875, 0, 10);
		for(int i=0;i<11;i++){
			SliceSparseMatrix.SparseMatrixTriplet triplet = (SliceSparseMatrix.SparseMatrixTriplet)updatedTripletMap.get(new Integer(i),new Integer(3011511));
			if(triplet!=null)
				Assert.assertTrue(triplet.removed);
		}
	}
}
