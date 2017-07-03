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

import io.github.msdk.featdet.ADAP3D.common.algorithms.SliceSparseMatrix.SparseMatrixTriplet;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class PeakSimilarityTest {

	public List<Double> peakSimilarityFunction(SliceSparseMatrix objsliceSparseMatrix,double mz,int leftBound,int rightBound,double fwhm){
		
		int arrayCount = rightBound-leftBound+1;
		double param_c = 0.2;
		double param_b = Math.exp(-1/param_c);
		double param_a = 1.0/(1.0-param_b);
		double area1 = 0.0; 
		double area2 = 0.0;
		double diffArea = 0.0;
		double[] normzlizedEIC = new double[arrayCount];
		double curSimilarity = 1.0;
		int curInc = 0;
		int curInc2 = 0;
		int subtractFromCurInc = 0;
		double peakSimilarityThreshold = 0;
		double epsilon = 0;
		List<Double> similarityValues = new ArrayList<Double>();
		int mzIndex = objsliceSparseMatrix.mzValues.indexOf(objsliceSparseMatrix.roundMZ(mz));
		MultiKeyMap slice = objsliceSparseMatrix.getHorizontalSlice(mz, leftBound, rightBound);
		
		for(int i=leftBound;i<rightBound;i++){
			SliceSparseMatrix.SparseMatrixTriplet obj1= (SliceSparseMatrix.SparseMatrixTriplet)slice.get(i, mz);
			SliceSparseMatrix.SparseMatrixTriplet obj2= (SliceSparseMatrix.SparseMatrixTriplet)slice.get(i+1, mz);
			area1 += 0.5* (obj1.intensity+obj2.intensity);
		}
		
		for(int i=0;i<arrayCount;i++){
			normzlizedEIC[i]= ((SliceSparseMatrix.SparseMatrixTriplet)slice.get(i, mz)).intensity/(float) area1;			
		}
		
		while(curSimilarity>peakSimilarityThreshold){
			curInc += 1;
			int curMZ = objsliceSparseMatrix.mzValues.get(mzIndex+curInc);
			double[] curEIC = new double[arrayCount];
			if(curInc>=2*fwhm)
				break;
			
			MultiKeyMap curSlice = objsliceSparseMatrix.getHorizontalSlice(mz+curInc, leftBound, rightBound);
			
			for(int i=rightBound;i<rightBound;i++){
				SliceSparseMatrix.SparseMatrixTriplet obj1= (SliceSparseMatrix.SparseMatrixTriplet)curSlice.get(i, curMZ);
				SliceSparseMatrix.SparseMatrixTriplet obj2= (SliceSparseMatrix.SparseMatrixTriplet)curSlice.get(i+1,curMZ);
				area2 += 0.5* (obj1.intensity+obj2.intensity);
			}
			
			if(area2<epsilon){
				subtractFromCurInc += 1;
				continue;
			}
			
			for(int i=0;i<arrayCount;i++){
				curEIC[i]= ((SliceSparseMatrix.SparseMatrixTriplet)slice.get(i, curMZ)).intensity/(float) area2;			
			}
			
			for(int j=0;j<arrayCount-1;j++){
				diffArea += Math.abs(0.5*((normzlizedEIC[j] - curEIC[j])+(normzlizedEIC[j+1] - curEIC[j+1])));
			}
			
			curSimilarity = (Math.exp(-diffArea/param_c)-param_b)*param_a;
			
			if(curSimilarity>peakSimilarityThreshold)
				similarityValues.add(curSimilarity);
		}
		
		curInc-=1;
		curInc -= subtractFromCurInc;
		
		curSimilarity = 1.0;
		subtractFromCurInc = 0;
		
		while(curSimilarity>peakSimilarityThreshold){
			curInc2 += 1;
			int curMZ = objsliceSparseMatrix.mzValues.get(mzIndex-curInc2);
			double[] curEIC = new double[arrayCount];
			if(((mz-curInc2)<0) || (curInc>=2*fwhm))
				break;
			
			MultiKeyMap curSlice = objsliceSparseMatrix.getHorizontalSlice(mz-curInc2, leftBound, rightBound);
			
			for(int i=leftBound;i<rightBound;i++){
				SliceSparseMatrix.SparseMatrixTriplet obj1= (SliceSparseMatrix.SparseMatrixTriplet)curSlice.get(i, curMZ);
				SliceSparseMatrix.SparseMatrixTriplet obj2= (SliceSparseMatrix.SparseMatrixTriplet)curSlice.get(i+1, curMZ);
				area2 += 0.5* (obj1.intensity+obj2.intensity);
			}
			
			if(area2<epsilon){
				subtractFromCurInc += 1;
				continue;
			}
			
			for(int i=0;i<arrayCount;i++){
				curEIC[i]= ((SliceSparseMatrix.SparseMatrixTriplet)slice.get(i, curMZ)).intensity/(float) area2;			
			}
			
			for(int j=0;j<arrayCount-1;j++){
				diffArea += Math.abs(0.5*((normzlizedEIC[j] - curEIC[j])+(normzlizedEIC[j+1] - curEIC[j+1])));
			}
			
			curSimilarity = (Math.exp(-diffArea/param_c)-param_b)*param_a;
			
			if(curSimilarity>peakSimilarityThreshold)
				similarityValues.add(curSimilarity);
		}
		
		curInc2-=1;
		curInc2 -= subtractFromCurInc;
		return similarityValues;
		
	}
}
