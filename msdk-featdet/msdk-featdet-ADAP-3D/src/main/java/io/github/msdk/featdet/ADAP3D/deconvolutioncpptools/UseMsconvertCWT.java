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
package io.github.msdk.featdet.ADAP3D.deconvolutioncpptools;
import io.github.msdk.featdet.ADAP3D.deconvolutioncpptools.NativeLoader;


/**
 *
 * @author owenmyers
 * Modified by Dharak Shah to include in MSDK
 */
public class UseMsconvertCWT {
    static {
        //ystem.load("/Users/owenmyers/Google Drive/Xiuxia/CppWavelet/libadapwavelet.so");
        //System.load("/Users/owenmyers/Google Drive/Xiuxia/XCMSMZminProject/MZminStuff/FinalOurPeakPicking/peak_picking/owen-mod-mzmine2-master/target/classes/lib/macosx-x86_64/libadapwavelet.so");
        NativeLoader loader = new NativeLoader();
        loader.loadLibrary("adapwavelet");
        
        //System.load("/Users/owenmyers/Google Drive/Xiuxia/XCMSMZminProject/MZminStuff/FinalOurPeakPicking/peak_picking/adap-cpp-deconvolution/libadapwavelet.so");
    }
    
    // Too hard to return 2D array. Going to return both arrays as one. since there will not be more peaks than there are
    // data points in the intesity array, the left bound array will be the first N points of teh returned array 
    // and the right bound array will be the next N points where N is the length of intesities array.
    private native double[] findPeaks(double [] intensities, double [] scanTimes, int lengthArrays, double SNR, double RTtol);
    
    public static double[][] tryCallingCppFindPeaks(double [] intensities, double [] scanTimes, double SNR, double RTtol){
        int numPts = scanTimes.length;
        double [][] peakBounds = new double[3][numPts];
        

        double[] peakBounds1D = new UseMsconvertCWT().findPeaks(intensities, scanTimes, numPts,  SNR, RTtol);
        for (int i=0; i<numPts; i++){
            peakBounds[0][i]= peakBounds1D[i];
        }
        for (int i=numPts; i<(2*numPts); i++){
            peakBounds[1][i-numPts]= peakBounds1D[i];
        }
        for (int i=(2*numPts); i<(3*numPts); i++){
            peakBounds[2][i-2*numPts]= peakBounds1D[i];
        }
        return peakBounds;
    }
}
