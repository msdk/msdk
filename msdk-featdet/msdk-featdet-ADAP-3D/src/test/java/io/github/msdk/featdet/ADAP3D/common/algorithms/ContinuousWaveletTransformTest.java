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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.featdet.ADAP3D.datamodel.Result;
import io.github.msdk.io.mzml.MzMLFileImportMethod;


public class ContinuousWaveletTransformTest {
	
	private static final String TEST_DATA_PATH = "src/test/resources/";
	private static RawDataFile rawFile;
	
	@SuppressWarnings("null")
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

	@SuppressWarnings("null")
	@Test
	public void testCWT() throws MSDKException,IOException{
		
		List<MsScan> listOfScans = 	rawFile.getScans();
		ArrayList<Double> rtBuffer = new ArrayList<Double>();
		double mzBuffer[] = new double[10000];
		float intensityBuffer[] = new float[10000];
		ArrayList<Double> correctIntensityBuffer = new ArrayList<Double>();
		
		for(int l=0;l<listOfScans.size();l++){
			
			float totalIntensity=0;
			MsScan scan = listOfScans.get(l);
			mzBuffer = scan.getMzValues();
			intensityBuffer = scan.getIntensityValues();
			for(int k=0;k<mzBuffer.length;k++){
				if(mzBuffer[k]>=387.1273 && mzBuffer[k]<=387.1405){
					totalIntensity += intensityBuffer[k];
				}
			}
			
			correctIntensityBuffer.add(new Double(totalIntensity));	
			rtBuffer.add(new Double((scan.getRetentionTime())/60));
			
		}
		
		double[] x = new double[rtBuffer.size()];
		double[] signal = new double[correctIntensityBuffer.size()];
		
		for (int i = 0; i < correctIntensityBuffer.size(); i++) {
		    x[i] = rtBuffer.get(i).doubleValue();
		    signal[i] = correctIntensityBuffer.get(i).doubleValue();		   
		  }
		
		ContinuousWaveletTransform continuousWavelet = new ContinuousWaveletTransform(1, 10, 1);
		continuousWavelet.setX(x);
		continuousWavelet.setSignal(signal);
		continuousWavelet.setPeakWidth(0.00, 10.00);
		continuousWavelet.setcoefAreaRatioTolerance(5);
		
		List<Result> peakList = continuousWavelet.findPeaks();
		boolean peakAssertion = false;
//			File outputFile = new File(TEST_DATA_PATH + "output.txt");
//			StringBuffer buffer = new StringBuffer ();
//			FileWriter writer = new FileWriter(outputFile);
			
			for(int j=0;j<peakList.size();j++){
//				buffer.append(peakList.get(j).curLeftBound).append(",").append(peakList.get(j).curRightBound).append("\r\n");
//				writer.write(buffer.toString());
//				buffer = new StringBuffer();
				if(peakList.get(j).curLeftBound==113 && peakList.get(j).curRightBound==130){
					peakAssertion = true;
				}
			}
//			writer.close();
			Assert.assertEquals(true, peakAssertion);
		
	}
	
}
