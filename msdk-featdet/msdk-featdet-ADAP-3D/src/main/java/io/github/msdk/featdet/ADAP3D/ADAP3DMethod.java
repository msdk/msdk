/*
 * (C) Copyright 2015-2016 by MSDK Development Team
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

package io.github.msdk.featdet.ADAP3D;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Range;
import io.github.msdk.featdet.ADAP3D.datamodel.PeakInfo;
import io.github.msdk.featdet.ADAP3D.common.algorithms.FeatureTools;
import com.google.common.collect.Range;
import io.github.msdk.featdet.ADAP3D.common.algorithms.ContinuousWaveletTransform;
import static io.github.msdk.featdet.ADAP3D.common.algorithms.SignalToNoise.filterBySNStaticWindowSweep;
import static io.github.msdk.featdet.ADAP3D.common.algorithms.SignalToNoise.filterBySNWindowInOutSweep;
import static io.github.msdk.featdet.ADAP3D.common.algorithms.SignalToNoise.findSNUsingWaveletCoefficents;
import static io.github.msdk.featdet.ADAP3D.deconvolutioncpptools.UseMsconvertCWT.tryCallingCppFindPeaks;

/**
*
* @author owen myers
* Modified by Dharak Shah to include in MSDK
*/

public class ADAP3DMethod {
	 public static List<PeakInfo> DeconvoluteSignal(
	            final double[] retentionTimes, final double[] intensity, final double mz,
	            final double snrThreshold,
	            final double minimumFeatHeight,
	            final Range<Double> peakWidth,
	            final double coefAreaRatioTolerance,
	            final int lowerPeakWidthForCWTScales, // in units of scans
	            final int upperPeakWidthForCWTScales, // in units of scans
	            final Map informationSN)
	            //final String SNCode,// determines which signal to noise estimator is used
	            //final double SNWindowMultiplier) // determions how many time peak width out for window in SN calc.
	    {
	        // For now lets always have an increment that results in 10 different scales
	        double scaleIncrement = ((double) upperPeakWidthForCWTScales - (double) lowerPeakWidthForCWTScales)/9.0;
	        // over 9 and not 10 because we want to include the largest scale and the lowerscale 
	        // -> this is done in the construction of the CWT object
	        
	        ContinuousWaveletTransform tryNewCWT = new ContinuousWaveletTransform((double) lowerPeakWidthForCWTScales,
	                                                                                (double) upperPeakWidthForCWTScales,
	                                                                                scaleIncrement);
	        tryNewCWT.setSignal(intensity);
	        tryNewCWT.setX(retentionTimes);
	        
	        tryNewCWT.buildRidgelines();
	        tryNewCWT.filterRidgelines();
	        double[][] newPeaks = tryNewCWT.findBoundries();
	        
	        // For debugging
	        //tryNewCWT.signalWaveletInnerProductOnePoint(1,5);
	        //tryNewCWT.getCoefficientsForAllScales();
	        //tryNewCWT.findMaximaForThisScale(4);
	        //tryNewCWT.buildRidgelines();
	  
	        
	     
	        
	        //These two lines are the old way
	        //double[][] oldPeaks;
	        //oldPeaks =  tryCallingCppFindPeaks(intensity,retentionTimes,snrThreshold,peakWidth.lowerEndpoint());
	        
	        

	        // Check and make sure the given features abolute height is large enough
	        List <Double> doneLBound = new ArrayList<Double>();
	        List <Double> doneRBound = new ArrayList<Double>();
	        
	        List <Double> bestCoefficient = new ArrayList<Double>();
	        
	        ////////////////////////////// Parse data from C++ ////////////////////////////////////
	        for (int i = 0; i<intensity.length; i++){
	            int curLBound = (int) newPeaks[0][i];
	            int curRBound = (int) newPeaks[1][i];
	            double curCoef = newPeaks[2][i];
	            if ((curLBound==0)&&(curRBound==0)){
	                doneLBound.add(new Double(0));
	                doneRBound.add(new Double(0));
	                bestCoefficient.add(new Double(0));
	            }
	            else{
	                double highestPoint = 0;
	                for(int alpha = curLBound; alpha<(curRBound+1); alpha++){
	                    if (intensity[alpha]>=highestPoint){
	                        highestPoint = intensity[alpha];
	                    }
	                }
	                /////////////////// minimum feature height check //////////////////////////////////
	                if(highestPoint>= minimumFeatHeight){
	                    doneLBound.add(new Double(curLBound));
	                    doneRBound.add(new Double(curRBound));
	                    bestCoefficient.add(new Double(curCoef));
	                }
	            }
	            
	        }
	        
	        
	        
	        final double[][] boundsMatrix = new double[2][doneLBound.size()];
	        for (int i = 0; i<doneLBound.size(); i++){
	            boundsMatrix[0][i] = doneLBound.get(i);
	            boundsMatrix[1][i] = doneRBound.get(i);   
	        }
	       
	        List<PeakInfo> resolvedPeaks = new ArrayList<PeakInfo>();
	        
	        if ((boundsMatrix == null)||
	                ((((int)boundsMatrix[0][0]==0)&&((int)boundsMatrix[1][0]==0))&&(boundsMatrix[0].length==1))){
	            return resolvedPeaks;

	        } else {

	          

	            
	            
	            for (int i = 0; i < boundsMatrix[0].length;i++) {

	                // Get peak start and end.
	                //final int peakLeft = findRTIndex(retentionTimes, boundsMatrix[0][i]);
	                //final int peakRight = findRTIndex(retentionTimes, boundsMatrix[1][i]);
	                int peakLeft = (int) boundsMatrix[0][i];
	                int peakRight = (int) boundsMatrix[1][i];
	                if ((peakLeft==0)&&(peakRight==0)){
	                    continue;
	                }
	                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	                /////////////////////////// Fix Boundries /////////////////////////////////////////////////////////////////////////////
	                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	                    peakLeft = FeatureTools.fixLeftBoundry(intensity, peakLeft);


	                    peakRight = FeatureTools.fixRightBoundry(intensity, peakRight);

	                
	                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	                ///////////////////////////Peak Width /////////////////////////////////////////////////////////////////////////////
	                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	                // The old way could detect the same peak more than once if the wavlet scales were too large.
	                // If the left bounds were the same and there was a null point before the right bounds it would
	                //make the same peak twice.
	                // To avoid the above see if the peak duration range is met before going into
	                // the loop
	                double retentionTimeRight = retentionTimes[peakRight];
	                double retentionTimeLeft = retentionTimes[peakLeft];
	                if(! peakWidth.contains(retentionTimeRight- retentionTimeLeft))
	                {
	                    continue;
	                }
	                
	                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	                ///////////////////////////Cropped Peak width/////////////////////////////////////////////////////////////////////////////
	                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	                // It is possible for the bounds to contain null or 0 intensity points. 
	                // For excample if for some reason the
	                // CWT finds a peak with only a single point in it but it thinks the bounds are a couple
	                // of points to the left and right then it would pass the above if statement. To make 
	                // sure we get rid of thes points we need to do one more check wich is below.
	                int croppedPeakLeft=-1;
	                int croppedPeakRight=-1;
	                
	                boolean allZero=true;
	                for (int alpha=peakLeft; alpha<peakRight; alpha++){
	                    double curInt = intensity[alpha];
	                    if (curInt!=0.0){
	                        
	                        allZero = false;
	                        break;
	                    }
	                }
	                if (allZero){
	                    continue;
	                }
	                for (int alpha=peakLeft; alpha<peakRight; alpha++){
	                    double curInt = intensity[alpha];
	                    if (curInt!=0.0){
	                        
	                        croppedPeakLeft = alpha;
	                        break;
	                    }
	                }
	                for (int alpha=peakRight; alpha>peakLeft; alpha--){
	                    double curInt = intensity[alpha];
	                    if (curInt!=0.0){
	                        croppedPeakRight = alpha;
	                        break;
	                    }
	                }
	                // Everything could be zero
//	                if (croppedPeakRight==-1){
//	                    croppedPeakRight = peakRight;
//	                }
//	                if (croppedPeakLeft==-1){
//	                    croppedPeakLeft = peakLeft;
//	                }
	                
	                // the most left and right points could/should be zero so by adding/subtracting from alpha we can make sure that  remains the case
	                // Otherwise the peak width is not acuretly being represented.
	                if (croppedPeakLeft!=peakLeft){
	                    croppedPeakLeft-=1;
	                }
	                if (croppedPeakRight!=peakRight){
	                    croppedPeakRight+=1;
	                }
	                if(croppedPeakLeft==-2){
	                    System.out.println("bug");
	                }
	                
	                
	                
	                double croppedRetentionTimeRight = retentionTimes[croppedPeakRight];
	                double croppedRetentionTimeLeft = retentionTimes[croppedPeakLeft];
	                if(! peakWidth.contains(croppedRetentionTimeRight- croppedRetentionTimeLeft))
	                {
	                    continue;
	                }
	                
	                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	                ///////////////////////////Number of Zero Points /////////////////////////////////////////////////////////////////////////////
	                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	                
	                // ALSO lest make sure the total number of non-zero points is greater than the number of 0.0 points
	                int numZeros = 0;
	                int numNotZero = 0;
	                double epsilon = 0.0001;
	                for (int alpha=peakLeft; alpha<=peakRight;alpha++){
	                    if (intensity[alpha]< epsilon){
	                        numZeros +=1;
	                    }
	                    else {
	                        numNotZero += 1;
	                    }
	                }
	                if (numZeros>=numNotZero){
	                    continue;
	                }
	                
	                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	                ///////////////////////////Signal to Noise /////////////////////////////////////////////////////////////////////////////
	                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	                double curBestCoef = bestCoefficient.get(i);
	                // Now find and check signal to noise ratio.
	                String SNCode = (String) informationSN.get("code"); // determines which signal to noise estimator is used
	                double curSN = 0.0;
	                if (SNCode.equals("Intensity Window Estimator")){
	                    //double curSN = filterBySNRandWindowSelect(intensity,peakLeft,peakRight);
	                    double curSN1 = filterBySNWindowInOutSweep(intensity,peakLeft,peakRight);
	                    double curSN2 = filterBySNStaticWindowSweep(intensity,peakLeft,peakRight);
	                    curSN = java.lang.Math.max(curSN1,curSN2);
	                    
	                }
	                else if (SNCode.equals("Wavelet Coefficient Estimator")){
	                    // determions how many time peak width out for window in SN calc.
	                    double SNWindowMultiplier = (double) informationSN.get("multiplier");
	                    boolean absWaveCoeffs = (boolean) informationSN.get("absolutewavecoeffs");
	                    curSN = findSNUsingWaveletCoefficents(tryNewCWT.returnAllCoefficients(),
	                                                        curBestCoef, peakLeft, peakRight,SNWindowMultiplier,
	                                                        absWaveCoeffs);
	                }
	                if (curSN<snrThreshold){
	                    continue;
	                }
	                if (curSN>1E12){
	                    curSN=1E12;
	                }
	        
	                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	                ///////////////////////////  Sharpness     /////////////////////////////////////////////////////////////////////////////
	                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	                
	                // find and check the "sharpness" right now we get the angle between the mean slope found
	                // from averaging the slope betweein peak point and points on right and left
	                //double avgSlopesAngle = sharpnessAngleAvgSlopes(retentionTimes, intensity, peakLeft, peakRight);
//	                double avgAnglesAngle = sharpnessAngleAvgAngles(retentionTimes, intensity, peakLeft, peakRight);
//	                if (avgAnglesAngle>(sharpnessAngleThresh)) {
//	                    continue;
//	                }
	                 
	                //double yangSharp = FeatureTools.sharpnessYang(retentionTimes,intensity,peakLeft,peakRight);
	                
	                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	                ///////////////////////////Mean of Boundry Mean of Signal //////////////////////////////////////////////////////////////
	                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	                
	                // compare the ratio of the mean boundry height to the mean of the signal
	                double meanOfSignal = FeatureTools.findMeanOfSignal(intensity,peakLeft,peakRight);
	                double meanBoundary = (intensity[peakLeft]+intensity[peakRight])/2.0;
	                double differenceSigBnd = meanOfSignal-meanBoundary;
	                
	                
	                if (differenceSigBnd/minimumFeatHeight < 0.2){
	                    continue;
	                }
	                
	                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	                ///////////////////////////Coefficient area/////////////////////////////////////////////////////////////////////////////
	                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	                
//	                double signalMin = findMinIntensityOfSignal(intensity, peakLeft,  peakRight);
//	                for (int alpha = peakLeft; alpha<=peakRight; alpha++){
//	                     intensity[alpha] = intensity[alpha]-signalMin;                       
//	                }
	                double curArea = FeatureTools.trapazoidAreaUnderCurve( intensity,retentionTimes, peakLeft,  peakRight);
//	                for (int alpha = peakLeft; alpha<=peakRight; alpha++){
//	                     intensity[alpha] = intensity[alpha]+signalMin;                       
//	                }
	                
	                double normedCoef = curBestCoef/curArea;
	                
	                if (normedCoef<coefAreaRatioTolerance){
	                    continue;
	                }
	                
	                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	                ///////////////////////////DONE WITH CHECKS/////////////////////////////////////////////////////////////////////////////
	                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	                
	                //Find some more info about the peak
	                double peakHeight =0.0;
	                int peakIndex = 0;
	                double peakRT=0.0;
	                for (int alpha=peakLeft;alpha<=peakRight; alpha++){
	                    if (intensity[alpha]>peakHeight){
	                        peakHeight = intensity[alpha];
	                        peakIndex = alpha;
	                        peakRT = retentionTimes[alpha];
	                    }
	                }
	                PeakInfo curPeakInfo = new PeakInfo();
	                
	                curPeakInfo.retTime = peakRT;
	                curPeakInfo.intensity = peakHeight;
	                curPeakInfo.retTimeStart = retentionTimes[peakLeft];
	                curPeakInfo.retTimeEnd = retentionTimes[peakRight];
	                curPeakInfo.peakIndex = peakIndex;
	                curPeakInfo.leftApexIndex = peakLeft;
	                curPeakInfo.rightApexIndex = peakRight;
	                curPeakInfo.mzValue = mz;
	                curPeakInfo.signalToNoiseRatio = curSN;
	                curPeakInfo.coeffOverArea = normedCoef;
	                
	                resolvedPeaks.add(curPeakInfo);
	            }
	        }

	        return resolvedPeaks;

}
}
