package io.github.msdk.featdet.ADAP3D.common.algorithms;

import java.util.List;
import java.util.Random;

import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;

public class CurveTool {
	
	/**
	 * <p>
	 *  listOfScans is used for getting scan objects from raw data file. 
	 * </p>
	 */
	private static List<MsScan> listOfScans;
	
	public CurveTool(RawDataFile rawFile){
		listOfScans = rawFile.getScans();
	}
	
	public int estimateFwhmMs(int numberOfScansForFWHMCalc){
		
		double mzBuffer[];
    	float intensityBuffer[];
    	Float rt;
    	
		for(int i=0;i<=numberOfScansForFWHMCalc;i++){
			Random generator = new Random(); 
			int randInt = generator.nextInt(listOfScans.size());
			MsScan scan = listOfScans.get(randInt);
			if(scan==null)
	    		continue;
			
			mzBuffer = scan.getMzValues();
	    	intensityBuffer = scan.getIntensityValues();
	    	rt = scan.getRetentionTime();
	    	
	    	float maxIntensity = intensityBuffer[0];
	    	int maxIntenistyIndex = 0;
	    	for (int j = 1; j < intensityBuffer.length; j++) {
	    	    if (intensityBuffer[j] > maxIntensity) {
	    	    	maxIntensity = intensityBuffer[j];
	    	    	maxIntenistyIndex = j;
	    	    }
	    	}
		}
		return 0;
	}
}
