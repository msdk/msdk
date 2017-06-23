package io.github.msdk.featdet.ADAP3D.common.algorithms;

import java.util.List;
import java.util.Random;

import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import java.io.File;
import java.io.FileWriter;
import java.lang.Math;

/**
 * <p>
 * CurveTool class is used for estimation of Full width half maximum.
 * </p>
 */
public class CurveTool {
	
	private static final String TEST_DATA_PATH = "src/test/resources/";
	
	private SliceSparseMatrix objSliceSparseMatrix;
	
	/**
	 * <p>
	 * CurveTool constructor takes object of SliceSparseMatrix class.
	 * </p>
	 */
	public CurveTool(SliceSparseMatrix sliceSparseMatrix){
		objSliceSparseMatrix = sliceSparseMatrix;
	}
	
	/**
	 * <p>
	 * estimateFwhmMs method estimates the FWHM for given number of random scans.
	 * 
	 * @param numberOfScansForFWHMCalc a {@link java.lang.Integer} object.
	 * 
	 * @return fwhm a {@link java.lang.Double} object.
	 * </p>
	 */
	public double estimateFwhmMs(int numberOfScansForFWHMCalc){
		
    	double sigma = 0;
    	int countProperIteration=0;
    	int countTotalIteration=0;    	
    	
		while(countProperIteration < numberOfScansForFWHMCalc){
			countTotalIteration++;
			int matrixIndex = 0;
			
			
			if(countTotalIteration>objSliceSparseMatrix.getSizeOfRawDataFile()){
				System.out.println(countTotalIteration);
				throw new IllegalArgumentException("Cannot calculate FWHM.");}
			
			Random generator = new Random(); 
			int randInt = generator.nextInt(objSliceSparseMatrix.getSizeOfRawDataFile());
			List<SliceSparseMatrix.VerticalSliceDataPoint> verticalSlice = objSliceSparseMatrix.getVerticalSlice(randInt);
			
			if(verticalSlice==null)
	    		continue;
			
	    	WeightedObservedPoints obs = new WeightedObservedPoints();
	    	
	    	for (SliceSparseMatrix.VerticalSliceDataPoint datapoint:verticalSlice) {
	    	   obs.add(datapoint.mz,datapoint.intensity);	    	  
	    	   matrixIndex++;
	    	}
	    	
	    	try{
	    		double[] parameters = GaussianCurveFitter.create().fit(obs.toList());
//	    		createDataFile(verticalSlice,parameters,randInt);
		    	sigma  +=  2.35482*parameters[2];
		    	
	    	}
	    	catch(Exception e){
	    		continue;
	    	}
	    	countProperIteration++;
		}
		double fwhm = sigma/numberOfScansForFWHMCalc;
		return fwhm;
	}
	
	/**
	 * <p>
	 * roundFWHM method rounds the values of FWHM.
	 * 
	 * @param fwhm a {@link java.lang.Double} object.
	 * 
	 * @return roundedFWHM a {@link java.lang.Integer} object.
	 * </p>
	 */
	private int roundFWHM(double fwhm){
		int roundedFWHM = (int) Math.round(fwhm+0.5);
		return roundedFWHM;
	}
	
	/**
	 * <p>
	 * createDataFile method creates text file for Gaussian results.
	 * 
	 * @param datapoint a {@link io.github.msdk.featdet.ADAP3D.common.algorithms.SliceSparseMatrix.VerticalSliceDataPoint} list.
	 * @param parameters a {@link java.lang.Double} array.
	 * @param scanNumber a {@link java.lang.Integer} object.
	 * </p>
	 */
	private void createDataFile(List<SliceSparseMatrix.VerticalSliceDataPoint> datapoint,double[] parameters,int scanNumber){
		double gaussianY [] = new double[datapoint.size()];
		String filename = "output"+ scanNumber +".txt";
		try{
			File outputFile = new File(TEST_DATA_PATH + filename);
			StringBuffer buffer = new StringBuffer ();
			FileWriter writer = new FileWriter(outputFile);
			
			for(int i=0;i<datapoint.size();i++){
				gaussianY[i] = parameters[0]*Math.exp(-Math.pow((datapoint.get(i).mz-parameters[1]), 2)/(2*Math.pow(parameters[2],2)));
				buffer.append(datapoint.get(i).mz).append(",").append(datapoint.get(i).intensity).append(",").append(gaussianY[i]).append("\r\n");
				writer.write(buffer.toString());
				buffer = new StringBuffer();
			}
			writer.close();
		}
		
		catch(Exception e){
			
		}
		
	}
}
