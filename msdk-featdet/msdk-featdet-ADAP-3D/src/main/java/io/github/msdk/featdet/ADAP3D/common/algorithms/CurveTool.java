package io.github.msdk.featdet.ADAP3D.common.algorithms;

import java.util.Random;

import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.MultiKeyMap;

import java.io.File;
import java.io.FileWriter;
import java.lang.Math;

public class CurveTool {
	
	private static final String TEST_DATA_PATH = "src/test/resources/";
	
	private SliceSparseMatrix objSliceSparseMatrix;
		
	public CurveTool(SliceSparseMatrix sliceSparseMatrix){
		objSliceSparseMatrix = sliceSparseMatrix;
	}
	
	public int estimateFwhmMs(int numberOfScansForFWHMCalc){
		
    	double sigma = 0;
    	int countProperIteration=0;
    	int countTotalIteration=0;    	
    	
		while(countProperIteration < numberOfScansForFWHMCalc){
			countTotalIteration++;
			int matrixIndex = 0;
			
			
			if(countTotalIteration>objSliceSparseMatrix.getSizeOfRawDataFile())
				throw new IllegalArgumentException("Cannot calculate FWHM.");
			
			Random generator = new Random(); 
			int randInt = generator.nextInt(objSliceSparseMatrix.getSizeOfRawDataFile());
			MultiKeyMap verticalSlice = objSliceSparseMatrix.getVerticalSlice(randInt);
			
			if(verticalSlice==null)
	    		continue;
			double intensityBuffer[] = new double[verticalSlice.size()];
			MapIterator iterator = verticalSlice.mapIterator();
	    	WeightedObservedPoints obs = new WeightedObservedPoints();
	    	
	    	while (iterator.hasNext()) {
	    	   iterator.next();
	    	   double sliceValue = (double) iterator.getValue();
	    	   obs.add(matrixIndex,sliceValue);
	    	   intensityBuffer[matrixIndex] = sliceValue;
	    	   matrixIndex++;
	    	}
	    	
	    	try{
	    		double[] parameters = GaussianCurveFitter.create().fit(obs.toList());
	    		createDataFile(intensityBuffer,parameters,randInt);
		    	sigma  +=  2.35482*parameters[2];
		    	
	    	}
	    	catch(Exception e){
	    		continue;
	    	}
	    	countProperIteration++;
		}
		double fwhm = sigma/numberOfScansForFWHMCalc;
		return roundFWHM(fwhm);
	}
	
	private int roundFWHM(double fwhm){
		int roundedFWHM = (int) Math.round(fwhm+0.5);
		return roundedFWHM;
	}
	
	private void createDataFile(double intensityBuffer[],double[] parameters,int scanNumber){
		double gaussianY [] = new double[intensityBuffer.length];
		String filename = "output"+ scanNumber +".txt";
		try{
			File outputFile = new File(TEST_DATA_PATH + filename);
			StringBuffer buffer = new StringBuffer ();
			FileWriter writer = new FileWriter(outputFile);
			
			for(int i=0;i<intensityBuffer.length;i++){
				gaussianY[i] = parameters[0]*Math.exp(-Math.pow((i-parameters[1]), 2)/(2*Math.pow(parameters[2],2)));
				buffer.append(i).append(",").append(intensityBuffer[i]).append(",").append(gaussianY[i]).append("\r\n");
				writer.write(buffer.toString());
				buffer = new StringBuffer();
			}
			writer.close();
		}
		
		catch(Exception e){
			
		}
		
	}
}
