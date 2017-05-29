/*
 * (C) Copyright 2015-2016 by MSDK Development Team
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.Assert;

import io.github.msdk.MSDKException;
import io.github.msdk.featdet.ADAP3D.common.algorithms.ContinuousWaveletTransform;
import io.github.msdk.featdet.ADAP3D.datamodel.Result;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;

import io.github.msdk.datamodel.rawdata.MsScan;

public class ContinuousWaveletTransformTest {
	
	private static final String TEST_DATA_PATH = "..\\msdk-featdet-ADAP-3D\\src\\test\\resources\\";
	private static RawDataFile rawFile;

	@SuppressWarnings("null")
	@Test
	public void testEICFile() throws MSDKException,IOException{
		
		File inputFile = new File(TEST_DATA_PATH+"eic.txt");
		Assert.assertTrue("Cannot read test data", inputFile.canRead());
		
		ArrayList<Double> signalList = new ArrayList<Double>();
		ArrayList<Double> xList = new ArrayList<Double>();
		FileReader readFile = new FileReader(inputFile);
		BufferedReader reader = new BufferedReader(readFile);
		String fileData = reader.readLine();
		

		while ((fileData = reader.readLine()) != null) {
			String[] seprateFileData=fileData.split(",");
			signalList.add(Double.parseDouble(seprateFileData[0]));   
			xList.add(Double.parseDouble(seprateFileData[1]));
        }
		reader.close();
		
		double[] x = new double[xList.size()];
		double[] signal = new double[signalList.size()];
		
		for (int i = 0; i < xList.size(); i++) {
		    x[i] = xList.get(i).doubleValue();
		    signal[i] = signalList.get(i).doubleValue();
		  }
		
		ContinuousWaveletTransform continuousWavelet = new ContinuousWaveletTransform(1, 10, 1);
		continuousWavelet.setX(x);
		continuousWavelet.setSignal(signal);
		continuousWavelet.setPeakWidth(0.001, 2.000);
		continuousWavelet.setcoefAreaRatioTolerance(50);
		
		List<Result> peakList = continuousWavelet.findPeaks();
		File outputFile = new File(TEST_DATA_PATH + "output.txt");
		StringBuffer buffer = new StringBuffer ();
		FileWriter writer = new FileWriter(outputFile);
		
		for(int j=0;j<peakList.size();j++){
			buffer.append(peakList.get(j).curLeftBound).append(",").append(peakList.get(j).curRightBound).append("\r\n");
			writer.write(buffer.toString());
			Assert.assertEquals(157,peakList.get(j).curLeftBound);
			Assert.assertEquals(178,peakList.get(j).curRightBound);			
		}
		writer.close();
		
	}
	
	@SuppressWarnings("null")
	@Test
	public void testMZMLFile() throws MSDKException,IOException{
		
		File inputFile = new File(TEST_DATA_PATH+"orbitrap_300-600mz.mzML");
		Assert.assertTrue("Cannot read test data", inputFile.canRead());
		MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
		rawFile = importer.execute();
		List<MsScan> listOfScans = 	rawFile.getScans();
		double rtBuffer[] = new double[10000];
		double mzBuffer[] = new double[10000];
		float intensityBuffer[] = new float[10000];
		
		for(int i=0;i<listOfScans.size();i++){
			MsScan scan = listOfScans.get(i);
			mzBuffer = scan.getMzValues(mzBuffer);
			intensityBuffer = scan.getIntensityValues(intensityBuffer);
			rtBuffer[i] = scan.getChromatographyInfo().getRetentionTime();
		}
		Assert.assertNotNull(mzBuffer);
		Assert.assertNotNull(intensityBuffer);
		Assert.assertNotNull(rtBuffer);		
	}
}
