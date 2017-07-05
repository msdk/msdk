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

import org.apache.commons.collections4.map.MultiKeyMap;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class PeakSimilarityTest {
	
	public static class PeakSimilarityResult{
		List<Double> similarityValues;
		boolean goodPeak;
		int lowerMzBound;
		int upperMzBound;
	}

	public PeakSimilarityResult peakSimilarityFunction(SliceSparseMatrix objsliceSparseMatrix,double mz,int leftBound,int rightBound,double fwhm){
		
		double roundedFWHM = fwhm*10000;
		int arrayCount = rightBound-leftBound+1;
		
		double[] normzlizedEIC = new double[arrayCount];
		List<Double> upperSimilarityValues = new ArrayList<Double>();
		List<Double> lowerSimilarityValues = new ArrayList<Double>();
		int roundedMz = objsliceSparseMatrix.roundMZ(mz);
		int mzIndex = objsliceSparseMatrix.mzValues.indexOf(roundedMz);
		MultiKeyMap slice = objsliceSparseMatrix.getHorizontalSlice(mz, leftBound, rightBound);
		double numSimEitherSideThreshold = fwhm/2;
		
		normzlizedEIC(slice,leftBound,rightBound,roundedMz,normzlizedEIC);
		
		int upperMzBound = findSimilarity(objsliceSparseMatrix,leftBound,rightBound,roundedMz,
				roundedFWHM,mzIndex,normzlizedEIC,upperSimilarityValues,true);
		
		int lowerMzBound = findSimilarity(objsliceSparseMatrix,leftBound,rightBound,roundedMz,
				roundedFWHM,mzIndex,normzlizedEIC,lowerSimilarityValues,false);
		
		List<Double> similarityValues = new ArrayList<Double>(upperSimilarityValues);
		similarityValues.addAll(lowerSimilarityValues);
				
		PeakSimilarityResult objPeakSimilarityResult = new PeakSimilarityResult();
		objPeakSimilarityResult.similarityValues = similarityValues;
		objPeakSimilarityResult.lowerMzBound = lowerMzBound;
		objPeakSimilarityResult.upperMzBound = upperMzBound;
		
		int lowerBoundryDiff = roundedMz-lowerMzBound;
		int upperBoundryDiff = upperMzBound - roundedMz;
		
		if((upperBoundryDiff>=numSimEitherSideThreshold) && (lowerBoundryDiff>=numSimEitherSideThreshold) && 
				(upperBoundryDiff+lowerBoundryDiff>=fwhm)){
			objPeakSimilarityResult.goodPeak = true;
		}
		else{
			objPeakSimilarityResult.goodPeak = false;
		}
		
		return objPeakSimilarityResult;
		
	}
	
	private int findSimilarity(SliceSparseMatrix objsliceSparseMatrix,int leftBound,int rightBound,int roundedMz,
			double roundedFWHM,int mzIndex,double[] normzlizedEIC ,List<Double> similarityValues,boolean upperBound){
		
		double param_c = 0.2;
		double param_b = Math.exp(-1/param_c);
		double param_a = 1.0/(1.0-param_b);
		double curSimilarity = 1.0;
		int curInc = 0;
		double peakSimilarityThreshold = 0.7;
		double epsilon = 1E-8;
		int curMZ = 0;
		int arrayCount = rightBound-leftBound+1;
		double area = 0.0;
		double diffArea = 0.0;
		int subtractFromCurInc = 0;
		int mzDiff = 0;
		int curMzIndex = 0;
		
		while(curSimilarity>peakSimilarityThreshold){
			
			curInc += 1;
			
			if(upperBound == true)
				curMzIndex = mzIndex+curInc;
			else
				curMzIndex = mzIndex-curInc;
			
			
			if(objsliceSparseMatrix.mzValues.get(curMzIndex)!= null)
				curMZ = objsliceSparseMatrix.mzValues.get(curMzIndex);
			else
				curMZ = 0;
			
			if(upperBound == true)
				mzDiff = curMZ- roundedMz;
			else
				mzDiff = roundedMz - curMZ;
			
			if((curMZ==0) && (mzDiff>=2*roundedFWHM))
				break;
			
			double originalCurMZ = (double)curMZ/10000;
			double[] curEIC = new double[arrayCount];
			
			
			MultiKeyMap curSlice = objsliceSparseMatrix.getHorizontalSlice(originalCurMZ, leftBound, rightBound);
			area = normzlizedEIC(curSlice,leftBound,rightBound,curMZ,curEIC);
			
			if(area<epsilon){
				subtractFromCurInc += 1;
				continue;
			}
						
			for(int j=0;j<arrayCount-1;j++){
				diffArea += Math.abs(0.5*((normzlizedEIC[j] - curEIC[j])+(normzlizedEIC[j+1] - curEIC[j+1])));
			}
			
			curSimilarity = ((Math.exp(-diffArea/param_c))-param_b)*param_a;
			
			if(curSimilarity>peakSimilarityThreshold)
				similarityValues.add(curSimilarity);
		}
		
		curInc-=1;
		curInc -= subtractFromCurInc;
		
		curSimilarity = 1.0;
		subtractFromCurInc = 0;
		return curMZ;
	} 
	
	private double normzlizedEIC(MultiKeyMap slice,int leftBound,int rightBound,int roundedMz, double[] normzlizedEIC){
		
		double area = 0.0; 
		int arrayCount = rightBound-leftBound+1;
		
		for(int i=leftBound;i<rightBound;i++){
			SliceSparseMatrix.SparseMatrixTriplet obj1= (SliceSparseMatrix.SparseMatrixTriplet)slice.get(i, roundedMz);
			SliceSparseMatrix.SparseMatrixTriplet obj2= (SliceSparseMatrix.SparseMatrixTriplet)slice.get(i+1, roundedMz);
			if(obj1==null && obj2!=null){
				area += 0.5* (0+obj2.intensity);
			}
			else if(obj2==null && obj1!=null){
				area += 0.5* (obj1.intensity+0);
			}
			else if (obj1==null && obj2==null) {
				area += 0;
			}
			
			else{
				area += 0.5* (obj1.intensity+obj2.intensity);
			}
			
		}
		
		for(int i=0;i<arrayCount;i++){
			if((SliceSparseMatrix.SparseMatrixTriplet)slice.get(i+leftBound, roundedMz) != null){
				normzlizedEIC[i]= ((SliceSparseMatrix.SparseMatrixTriplet)slice.get(i+leftBound, roundedMz)).intensity/(float) area;	
			}
			else{
				normzlizedEIC[i] = 0;
			}	
		}

		return area;
	} 
}
