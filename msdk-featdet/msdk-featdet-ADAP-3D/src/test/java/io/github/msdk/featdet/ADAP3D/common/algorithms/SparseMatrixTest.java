package io.github.msdk.featdet.ADAP3D.common.algorithms;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.featdet.ADAP3D.datamodel.SparseMatrixTriplet;
import io.github.msdk.io.mzml.MzMLFileImportMethod;




public class SparseMatrixTest {
	
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
	public void testSparseMatrix() throws MSDKException,IOException{
		
		List<MsScan> listOfScans = 	rawFile.getScans();
	
	
		
		List<SparseMatrixTriplet> listOfTriplet = new ArrayList<SparseMatrixTriplet>();
	    
	    for(int i=0;i<listOfScans.size();i++){
	    	MsScan scan = listOfScans.get(i);
	    	double mzBuffer[];
	    	float intensityBuffer[];
	    	mzBuffer = scan.getMzValues();
	    	intensityBuffer = scan.getIntensityValues();
	    	
	    	for(int j=0;j<mzBuffer.length;j++){
	    		SparseMatrixTriplet triplet = new SparseMatrixTriplet();
	    		triplet.intensity = intensityBuffer[j];
	    		triplet.mz = (int)mzBuffer[j];
	    		triplet.scanNumber  = i;
	    		listOfTriplet.add(triplet);
	    	}
	    }
	    
	   	    
	    
	    Comparator<SparseMatrixTriplet> compare = new Comparator<SparseMatrixTriplet>() {
			
			@Override
			public int compare(SparseMatrixTriplet o1, SparseMatrixTriplet o2) {
				// TODO Auto-generated method stub
				Integer  scan1 = o1.scanNumber;
				Integer  scan2 = o2.scanNumber;
				int scanCompare = scan1.compareTo(scan2);
				
				if(scanCompare!=0){
					return scanCompare;
				}
				else{
					Integer  mz1 = o1.mz;
					Integer  mz2 = o2.mz;
					return mz1.compareTo(mz2);
				}
			}
		};
		
		
		Collections.sort(listOfTriplet, compare);	
		
		List<SparseMatrixTriplet> filterListOfTriplet = new ArrayList<SparseMatrixTriplet>();
		SparseMatrixTriplet currTriplet = new SparseMatrixTriplet();
		SparseMatrixTriplet lastFilterTriplet = new SparseMatrixTriplet();
		int index = 0;
		filterListOfTriplet.add(listOfTriplet.get(0));
		for(int i=1;i<listOfTriplet.size();i++){
			currTriplet = listOfTriplet.get(i);
			lastFilterTriplet = filterListOfTriplet.get(index);
			if(currTriplet.mz == lastFilterTriplet.mz && currTriplet.scanNumber == lastFilterTriplet.scanNumber){
				lastFilterTriplet.intensity += currTriplet.intensity;
			}
			else{
				filterListOfTriplet.add(currTriplet);
				index++;
			}
		}
	    Assert.assertNotNull(filterListOfTriplet);
	}
}
