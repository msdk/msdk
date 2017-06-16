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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.lang.Math;


import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.featdet.ADAP3D.datamodel.CWTInputDataPoint;
import io.github.msdk.featdet.ADAP3D.datamodel.SparseMatrixTriplet;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;


/**
 * <p>
 * SliceSparseMatrix class is used for slicing the sparse matrix of raw data as per given mz value.
 * slice contains intensities for one mz value for different scans.
 * </p>
 */
public class SliceSparseMatrix {
	
	private final MultiKeyMap tripletMap;
	private final List<SparseMatrixTriplet> filterListOfTriplet;
	private int maxIntensityIndex=0;
	private final int roundMz = 100;
	private final List<MsScan> listOfScans;
	
	 /**
	   * <p>
	   * This constructor takes raw data file and creat the triplet map which contains information
	   * such as mz,intensity,rt,scan number
	   * </p>
	   */
	public SliceSparseMatrix(RawDataFile rawFile){
		listOfScans = rawFile.getScans();
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
	    		triplet.mz = (int)Math.round(mzBuffer[j]*roundMz);
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
		
		filterListOfTriplet = new ArrayList<SparseMatrixTriplet>();
		SparseMatrixTriplet currTriplet = new SparseMatrixTriplet();
		SparseMatrixTriplet lastFilterTriplet = new SparseMatrixTriplet();
		tripletMap = new MultiKeyMap ();
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
				tripletMap.put(currTriplet.scanNumber, currTriplet.mz,currTriplet);
				index++;
			}
			
		}
		filterListOfTriplet.size();
	}
		
	 /**
	   * <p>
	   * This method returns the MultiKeyMap slice of data for given mz,lowerScanBound,upperScanBound
	   * </p>
	   */
	public MultiKeyMap getSlice(double mz,int lowerScanBound,int upperScanBound){
		
		int roundmz = (int)Math.round(mz*roundMz);
		MultiKeyMap  sliceMap = new MultiKeyMap ();
				
		for(int i = lowerScanBound;i<=upperScanBound;i++){
			SparseMatrixTriplet triplet = (SparseMatrixTriplet)tripletMap.get(new Integer(i),new Integer(roundmz));
			if(tripletMap.containsKey(new Integer(i),new Integer(roundmz))){
				sliceMap.put(i, roundmz,triplet);
			}
			else{
				sliceMap.put(i, roundmz, null);
			}
		}
			
		return sliceMap;
	}
	
	/**
	   * <p>
	   * This method finds next maximum intensity from filterListOfTriplet
	   * </p>
	   */
	public SparseMatrixTriplet findNextMaxIntensity(){
		
		SparseMatrixTriplet tripletObject = null;
		 Comparator<SparseMatrixTriplet> compare = new Comparator<SparseMatrixTriplet>() {
				
				@Override
				public int compare(SparseMatrixTriplet o1, SparseMatrixTriplet o2) {
					
					Float  intensity1 = o1.intensity;
					Float  intensity2 = o2.intensity;
					int intensityCompare = intensity2.compareTo(intensity1);
					return intensityCompare;
				}
			};
			Collections.sort(filterListOfTriplet,compare);
			
			for(int i=maxIntensityIndex;i<filterListOfTriplet.size();i++){
				if(filterListOfTriplet.get(i).removed == false){
					tripletObject = filterListOfTriplet.get(i);
					maxIntensityIndex=i;
					break;
				}
				
			}
			return tripletObject;
	}
	
	/**
	   * <p>
	   * This method returns sorted list of CWTInputDataPoint object.Object contain retention time and intensity values 
	   * </p>
	   */
	public List<CWTInputDataPoint> getCWTDataPoint(MultiKeyMap slice){
		
		MapIterator iterator = slice.mapIterator();
		List<CWTInputDataPoint> listOfDataPoint = new ArrayList<CWTInputDataPoint>();
		
		while (iterator.hasNext())  {
			CWTInputDataPoint dataPoint = new CWTInputDataPoint();
			iterator.next();
			MultiKey sliceKey = (MultiKey) iterator.getKey();
			 SparseMatrixTriplet triplet = (SparseMatrixTriplet)slice.get(sliceKey);
			 if(triplet != null){
				 dataPoint.rt = triplet.rt/60;
				 dataPoint.intensity = triplet.intensity;
				 listOfDataPoint.add(dataPoint);
				}
			 else{
				 MsScan scan = listOfScans.get((int) sliceKey.getKey(0));
				 dataPoint.rt = scan.getRetentionTime()/60;
				 dataPoint.intensity = 0.0;
				 listOfDataPoint.add(dataPoint);
			 }
		  }
		Comparator<CWTInputDataPoint> compare = new Comparator<CWTInputDataPoint>() {
			
			@Override
			public int compare(CWTInputDataPoint o1, CWTInputDataPoint o2) {
				Double rt1 = o1.rt;
				Double rt2 = o2.rt;
				return rt1.compareTo(rt2);
			}
		};
		
		Collections.sort(listOfDataPoint,compare);
		
		return listOfDataPoint;
	}
}
