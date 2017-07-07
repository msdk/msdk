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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.lang.Math;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.collections4.keyvalue.MultiKey;

/**
 * <p>
 * BiGaussian Class is used for fitting bigaussian on EIC.
 * </p>
 */
public class BiGaussian {
	
	//This is used to store horizontal slice of sparse matrix.
	@SuppressWarnings("rawtypes")
	private final MultiKeyMap horizontalSlice;
	
	//This is used to store all the intensities of slice. 
	private final List<Double> listOfIntensities;
	
	//This is used for storing bigaussian parameters inside constructor.
	private final double biGaussianParams[];
	
	/**
	 * <p>
	 * Inside BiGaussian Constructor we're determining 4 BiGaussian Parameters. MaxHeight, Mu, SigmaLeft and SigmaRight.
	 * </p>
	 * 
	 * @param horizontalSlice a {@link org.apache.commons.collections4.map.MultiKeyMap} object. This is horizontal slice from the sparse matrix.
	 * @param mz  a {@link java.lang.Double} object. This is m/z value from the raw file.
	 * @param leftBound a {@link java.lang.Integer} object. This is minimum scan number.
	 * @param rightBound a {@link java.lang.Integer} object. This is maximum scan number.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	BiGaussian(MultiKeyMap horizontalSlice,double mz,int leftBound,int rightBound){
		
		this.horizontalSlice = horizontalSlice;
		
		List<SliceSparseMatrix.Triplet> listOfTripletsInSlice = new ArrayList<SliceSparseMatrix.Triplet>(horizontalSlice.values());
		
		listOfIntensities = new ArrayList<Double>();
		
		for(int i=0;i<listOfTripletsInSlice.size();i++){
			double intensity = listOfTripletsInSlice.get(i)!=null?listOfTripletsInSlice.get(i).intensity:0;
			listOfIntensities.add(intensity);
		}
		
		
		
		Comparator<Double> compareIntensities = new Comparator<Double>() {
			
			@Override
			public int compare(Double o1, Double o2) {
				Double intensity1 = o1;
				Double intensity2 = o2;
				return intensity1.compareTo(intensity2);
			}
		};
		
		Collections.sort(listOfIntensities, compareIntensities);
		
		//This is max height for BiGaussian fit. It's in terms of intensities.
		double maxHeight = listOfIntensities.get(listOfIntensities.size()-1);
		
		//Below logic is for finding BiGaussian parameters.		
		int mu = getScanNumber(maxHeight);
		int roundedmz = (int) Math.round(mz * 10000); 
		double halfHeight = (double) maxHeight/2; 
		biGaussianParams = new double[4];
		
		biGaussianParams[0] = maxHeight;
		biGaussianParams[1] = mu;
	
		double  interpolationLeftSideY1 = 0;
		double interpolationLeftSideY2 = 0;
		
		for(int i=mu-1;i>=leftBound;i--){
			
			SliceSparseMatrix.Triplet triplet1 = ((SliceSparseMatrix.Triplet)horizontalSlice.get(i,roundedmz));
			
			if(triplet1!=null){
				interpolationLeftSideY1 = triplet1.intensity;
			}
			
			if(interpolationLeftSideY1 <halfHeight && triplet1!=null){
				SliceSparseMatrix.Triplet triplet2 =  ((SliceSparseMatrix.Triplet)horizontalSlice.get(i+1,roundedmz));
				if(triplet2!=null){
					interpolationLeftSideY2 = triplet2.intensity;
					break;
				}
			}
		}
		
		double interpolationLeftSideX = ((halfHeight - interpolationLeftSideY1)*(getScanNumber(interpolationLeftSideY2)-getScanNumber(interpolationLeftSideY1))
				/(interpolationLeftSideY2 - interpolationLeftSideY1))+getScanNumber(interpolationLeftSideY1);
		
		//This is signa left for BiGaussian. 
		biGaussianParams[2] = (mu - interpolationLeftSideX)/Math.sqrt(2*Math.log(2)); 
		
		double  interpolationRightSideY1 = 0;
		double interpolationRightSideY2 = 0;
		
		for(int i=mu+1;i<=rightBound;i++){
			
			SliceSparseMatrix.Triplet triplet1 = ((SliceSparseMatrix.Triplet)horizontalSlice.get(i,roundedmz));
			
			if(triplet1!=null){
				interpolationRightSideY1 = triplet1.intensity;
			}
			
			if(interpolationRightSideY1 <halfHeight && triplet1!=null){
				SliceSparseMatrix.Triplet triplet2 =  ((SliceSparseMatrix.Triplet)horizontalSlice.get(i-1,roundedmz));
				if(triplet2!=null){
					interpolationRightSideY2 = triplet2.intensity;
					break;
				}
			}
				
		}
		
		double interpolationRightSideX = ((halfHeight - interpolationRightSideY1)*(getScanNumber(interpolationRightSideY2)-getScanNumber(interpolationRightSideY1))
				/(interpolationLeftSideY2 - interpolationRightSideY1))+getScanNumber(interpolationRightSideY1);
		
		//This is signa right for BiGaussian. 
		biGaussianParams[3] = ((interpolationRightSideX - mu))/Math.sqrt(2*Math.log(2));
		
	}
	
	/**
	 * <p>
	 * This method is used for getting scan number for given intensity value.
	 * </p>
	 * 
	 * @param height  a {@link java.lang.Double} object. This is intensity value from the horizontal slice from sparse matrix.
	 */
	@SuppressWarnings("rawtypes")
	private int getScanNumber(double height){
		int mu = 0;
		MapIterator iterator = horizontalSlice.mapIterator();
		
		while (iterator.hasNext()) {
			iterator.next();

		    MultiKey mk = (MultiKey) iterator.getKey();
		    double intensity = ((SliceSparseMatrix.Triplet)(iterator.getValue()))!= null?
		    		((SliceSparseMatrix.Triplet)(iterator.getValue())).intensity:0;

		    if(intensity == height){
		    	 mu = (int)mk.getKey(0);
		    	 break;
		    }
		}
		return mu;
	}	
	
	/**
	 * <p>
	 * This method is used calculating bigaussian values for EIC.
	 * </p>
	 * 
	 * @param x  a {@link java.lang.Integer} object. This is scan number.
	 */
	public double getBiGaussianValue(int x){
		
		double biGaussianValue = 0;
		double exponentialTerm = 0;
		
		if(x >= biGaussianParams[1]){
			exponentialTerm = Math.exp(-1 * Math.pow(x-biGaussianParams[1], 2) / (2 * Math.pow(biGaussianParams[3],2)));
			biGaussianValue = biGaussianParams[0] * exponentialTerm;
		}
		
		else{
			exponentialTerm = Math.exp(-1 * Math.pow(x-biGaussianParams[1], 2) / (2 * Math.pow(biGaussianParams[2],2)));
			biGaussianValue = biGaussianParams[0] * exponentialTerm;
		}
		
		return biGaussianValue;
	}
}
