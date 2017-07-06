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

/**
 * <p>
 * PeakSimilarityTest Class.
 * </p>
 */
//PeakSimilarity test is used for finding adjacent similar peaks for given mz value and it decides whether the peak is good or not.
public class PeakSimilarityTest {
	
	/**
	 * <p>
	 * PeakSimilarityResult class is used for returning lower and upper mz bound,boolean good peak value and list of similarity value.
	 * </p>
	 */
	//Object of this class will return lowest mz boundary and highest mz boundary of adjacent similar peaks for given mz value.
	//It will also return if the peak is good or not for given mz value.
	public static class PeakSimilarityResult{
		List<Double> similarityValues;
		boolean goodPeak;
		int lowerMzBound;
		int upperMzBound;
	}

	/**
	 * <p>
	 * peakSimilarityFunction method is used for getting similarity values for adjacent peaks, upper and 
	 * lower mz bounds and decides whether the peak is good or bad.
	 * @param objsliceSparseMatrix a {@link io.github.msdk.featdet.ADAP3D.common.algorithms.SliceSparseMatrix} object.
	 * @param mz a {@link java.lang.Double} object.
	 * @param leftBound a {@link java.lang.Integer} object.
	 * @param rightBound a {@link java.lang.Integer} object.
	 * @param fwhm a {@link java.lang.Double} object.
	 * 
	 * @return a {@link io.github.msdk.featdet.ADAP3D.common.algorithms.PeakSimilarityTest.PeakSimilarityResult} object
	 * </p>
	 */
	public PeakSimilarityResult peakSimilarityFunction(SliceSparseMatrix objsliceSparseMatrix,double mz,int leftBound,int rightBound,double fwhm){
		
		//Here I'm rounding Full width half max(fwhm) and mz value by factor of 10000. 
		//I've done the same for creating sparse matrix in SliceSparseMatrix Class.
		double roundedFWHM = fwhm*10000;
		
		//Here normzlizedEICArray size has been defined  based on right side boundary and left side boundary as exact same
		//number of values as rightBound-leftBound+1.
		int arrayCount = rightBound-leftBound+1;
		
		//normzlizedEICArray is used for storing normalized intensities.
		double[] normzlizedEICArray = new double[arrayCount];
		List<Double> upperSimilarityValues = new ArrayList<Double>();
		List<Double> lowerSimilarityValues = new ArrayList<Double>();
		int roundedMz = objsliceSparseMatrix.roundMZ(mz);
		//mzIndex is index of given mz from the sorted list of all mz values from raw file. 
		int mzIndex = objsliceSparseMatrix.mzValues.indexOf(roundedMz);
		//slice is used to store horizontal row from sparse matrix for given mz, left boundary and right boundary.
		// left boundary and right boundary are used in form of scan numbers.
		MultiKeyMap slice = objsliceSparseMatrix.getHorizontalSlice(mz, leftBound, rightBound);
		double numSimEitherSideThreshold = fwhm/2;
		//normzlizedEIC method is used for normalizing intensities for given mz value. It updates normzlizedEICArray.
		normzlizedEIC(slice,leftBound,rightBound,roundedMz,normzlizedEICArray);
		
		//Here we're getting highest mz value for which the peak is similar to given mz value.
		int upperMzBound = findSimilarity(objsliceSparseMatrix,leftBound,rightBound,roundedMz,
				roundedFWHM,mzIndex,normzlizedEICArray,upperSimilarityValues,true);
		
		//Here we're getting lowest mz value for which the peak is similar to given mz value.
		int lowerMzBound = findSimilarity(objsliceSparseMatrix,leftBound,rightBound,roundedMz,
				roundedFWHM,mzIndex,normzlizedEICArray,lowerSimilarityValues,false);
		
		List<Double> similarityValues = new ArrayList<Double>(upperSimilarityValues);
		similarityValues.addAll(lowerSimilarityValues);
		
		//Assigning values to object.
		PeakSimilarityResult objPeakSimilarityResult = new PeakSimilarityResult();
		objPeakSimilarityResult.similarityValues = similarityValues;
		objPeakSimilarityResult.lowerMzBound = lowerMzBound;
		objPeakSimilarityResult.upperMzBound = upperMzBound;
		
		int lowerBoundryDiff = roundedMz-lowerMzBound;
		int upperBoundryDiff = upperMzBound - roundedMz;
		
		//This is the condition for determing whether the peak is good or not.
		if((upperBoundryDiff>=numSimEitherSideThreshold) && (lowerBoundryDiff>=numSimEitherSideThreshold) && 
				(upperBoundryDiff+lowerBoundryDiff>=fwhm)){
			objPeakSimilarityResult.goodPeak = true;
		}
		else{
			objPeakSimilarityResult.goodPeak = false;
		}
		
		return objPeakSimilarityResult;
		
	}
	
	/**
	 * <p>
	 * findSimilarity method is used for getting similarity values and lower and upper mz bounds for adjacent peaks.
	 * @param objsliceSparseMatrix a {@link io.github.msdk.featdet.ADAP3D.common.algorithms.SliceSparseMatrix} object.
	 * @param roundedMz a {@link java.lang.Integer} object.
	 * @param leftBound a {@link java.lang.Integer} object.
	 * @param rightBound a {@link java.lang.Integer} object.
	 * @param roundedFWHM a {@link java.lang.Double} object.
	 * @param mzIndex a {@link java.lang.Integer} object.
	 * @param normzlizedEIC a {@link java.lang.Double} array.
	 * @param similarityValues a {@link java.lang.Double} empty list.
	 * @param upperBound a {@link java.lang.Boolean} object.
	 * 
	 * @return curMZ a {@link java.lang.Double} object.
	 * </p>
	 */
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
			
			//This condition is used to determine whether we're finding similar peaks for mz values lower or upper.
			//than given mz value.curMzIndex maintains index of cur mz in sorted mz value list. 
			if(upperBound == true)
				curMzIndex = mzIndex+curInc;
			else
				curMzIndex = mzIndex-curInc;
			
			//This condition checks whether we've mz values above or below given mz value.
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
			
			//for getting slice of sparse matrix we need to provide original mz values which are there in raw file.
			double originalCurMZ = (double)curMZ/10000;
			//curEIC will store normalized intensities for adjacent mz values.
			double[] curEIC = new double[arrayCount];
			
			//Here current horizontal slice from sparse matrix is stored adjacent mz value.
			MultiKeyMap curSlice = objsliceSparseMatrix.getHorizontalSlice(originalCurMZ, leftBound, rightBound);
			area = normzlizedEIC(curSlice,leftBound,rightBound,curMZ,curEIC);
			
			//if area is too small continue.
			if(area<epsilon){
				subtractFromCurInc += 1;
				continue;
			}
			
			diffArea = 0;
			//This is the implementation of trapezoid.
			for(int j=0;j<arrayCount-1;j++){
				diffArea += Math.abs(0.5*((normzlizedEIC[j] - curEIC[j])+(normzlizedEIC[j+1] - curEIC[j+1])));
			}
			
			//Here similarity value is calculated.
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
	
	/**
	 * <p>
	 * normzlizedEIC method is used for normalizing EIC and calculating area.
	 * @param objsliceSparseMatrix a {@link io.github.msdk.featdet.ADAP3D.common.algorithms.SliceSparseMatrix} object.
	 * @param roundedMz a {@link java.lang.Integer} object.
	 * @param leftBound a {@link java.lang.Integer} object.
	 * @param rightBound a {@link java.lang.Integer} object.
	 * @param normzlizedEIC a {@link java.lang.Double} array.
	 * 
	 * @return area a {@link java.lang.Double} object.
	 * </p>
	 */
	private double normzlizedEIC(MultiKeyMap slice,int leftBound,int rightBound,int roundedMz, double[] normzlizedEIC){
		
		double area = 0.0; 
		int arrayCount = rightBound-leftBound+1;
		
		//Here area has been calculated for normalizing the intensities.
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
		
		//Here intensities are normalized.
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
