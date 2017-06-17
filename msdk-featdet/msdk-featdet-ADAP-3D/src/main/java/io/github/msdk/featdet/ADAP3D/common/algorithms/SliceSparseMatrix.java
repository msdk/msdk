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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.featdet.ADAP3D.datamodel.SparseMatrixTriplet;
import org.apache.commons.collections4.map.MultiKeyMap;

/**
 * <p>
 * SliceSparseMatrix class is used for slicing the sparse matrix of raw data as per given mz value.
 * slice contains intensities for one mz value for different scans.
 * </p>
 *
 * @author plusik
 * @version $Id: $Id
 */
public class SliceSparseMatrix {
	
	private final MultiKeyMap  tripletMap;
	
	/**
	 * <p>
	 * This constructor takes raw data file and creat the triplet map which contains information
	 * such as mz,intensity,rt,scan number
	 * </p>
	 *
	 * @param rawFile a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
	 */
	public SliceSparseMatrix(RawDataFile rawFile){
		List<MsScan> listOfScans = 	rawFile.getScans();
		List<SparseMatrixTriplet> listOfTriplet = new ArrayList<SparseMatrixTriplet>();
	    
	    for(int i=0;i<listOfScans.size();i++){
	    	MsScan scan = listOfScans.get(i);
	    	double mzBuffer[];
	    	float intensityBuffer[];
	    	float rt;
	    	mzBuffer = scan.getMzValues();
	    	intensityBuffer = scan.getIntensityValues();
	    	rt = scan.getRetentionTime();
	    	
	    	for(int j=0;j<mzBuffer.length;j++){
	    		SparseMatrixTriplet triplet = new SparseMatrixTriplet();
	    		triplet.intensity = intensityBuffer[j];
	    		triplet.mz = (int)(mzBuffer[j]*100000);
	    		triplet.scanNumber  = i;
	    		triplet.rt = rt;
	    		triplet.removed = false;
	    		listOfTriplet.add(triplet);
	    	}
	    }
	    
	   	    
	    
	    Comparator<SparseMatrixTriplet> compare = new Comparator<SparseMatrixTriplet>() {
			
			@Override
			public int compare(SparseMatrixTriplet o1, SparseMatrixTriplet o2) {
				
				Integer  scan1 = o1.scanNumber;
				Integer  scan2 = o2.scanNumber;
				int scanCompare = scan1.compareTo(scan2);
				
				if(scanCompare!=0){
					return scanCompare;
				}
				else {
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
		
		tripletMap = getTripletMap(filterListOfTriplet);
	}
		
	/**
	 * <p>
	 * This method returns the MultiKeyMap slice of data for given mz,lowerScanBound,upperScanBound
	 * </p>
	 *
	 * @param mz a int.
	 * @param lowerScanBound a int.
	 * @param upperScanBound a int.
	 * @return a {@link org.apache.commons.collections4.map.MultiKeyMap} object.
	 */
	public MultiKeyMap getSlice(int mz,int lowerScanBound,int upperScanBound){
		
		MultiKeyMap  sliceMap = new MultiKeyMap ();
				
		for(int i = lowerScanBound;i<=upperScanBound;i++){
			SparseMatrixTriplet triplet = (SparseMatrixTriplet)tripletMap.get(new Integer(i),new Integer(mz));
			if(tripletMap.containsKey(new Integer(i),new Integer(mz))){
				sliceMap.put(i, mz,triplet);
			}
			else{
				sliceMap.put(i, mz, null);
			}
		}
			
		return sliceMap;
	}
	
	 /**
	   * <p>
	   * This method returns the MultiKeyMap of SparseMatrixTriplet objects.
	   * ScanNumber and Mz are keys and SparseMatrixTriplet object is value.
	   * </p>
	   */
	private  MultiKeyMap getTripletMap(List<SparseMatrixTriplet> filterListOfTriplet){
		MultiKeyMap  tripletMap = new MultiKeyMap ();
		for(int i=0;i<filterListOfTriplet.size();i++){
			tripletMap.put(filterListOfTriplet.get(i).scanNumber, filterListOfTriplet.get(i).mz,filterListOfTriplet.get(i));
		}
		return tripletMap;
	}

}
