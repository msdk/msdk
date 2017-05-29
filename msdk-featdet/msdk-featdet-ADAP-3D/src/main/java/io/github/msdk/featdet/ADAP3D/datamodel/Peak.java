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
package io.github.msdk.featdet.ADAP3D.datamodel;

import java.io.Serializable;

import java.util.NavigableMap;
import java.util.Map;
import java.util.TreeMap;

import io.github.msdk.featdet.ADAP3D.common.algorithms.Math;

/**
 * Class Peak contains all information about a peak as well as
 * its chromatogram, and some methods to get the information
 * 
 * @author aleksandrsmirnov
 * Modified by Dharak Shah to include in MSDK
 */
public class Peak implements Cloneable, Serializable {
    private NavigableMap <Double, Double> chromatogram; // (retTime, intensity) - pairs
    private NavigableMap <Double, Double> mzValues; // (retTime, mzValue) - pairs
    private PeakInfo info;
    
    private double retTimeMin;
    private double retTimeMax;
    private double mzMin;
    private double mzMax;
    
    private double apexIntensity;
    private double apexRetTime;
    private double apexMZ;
    
    private double norm;
    private double shift;
    
    // ------------------------------------------------------------------------
    // ----- Contsructors -----------------------------------------------------
    // ------------------------------------------------------------------------
    
    public Peak(final Peak peak) {
        info = new PeakInfo(peak.info);
        shift = peak.shift;
        chromatogram = new TreeMap <> (peak.chromatogram);
        
        retTimeMin = peak.retTimeMin;
        retTimeMax = peak.retTimeMax;
        
        mzMin = peak.mzMin;
        mzMax = peak.mzMax;
        
        apexIntensity = peak.apexIntensity;
        apexRetTime = peak.apexRetTime;
        apexMZ = peak.apexMZ;
        
        norm = peak.norm;
    }
    
    public Peak(final NavigableMap <Double, Double> chromatogram, 
            final PeakInfo info) 
    {
        this(chromatogram, info.mzValue);
        
        //this.info = new PeakInfo(info);
        this.info = info;
    }
    
    public Peak(final NavigableMap <Double, Double> chromatogram, 
            final double mz) 
    {   
        this.info = new PeakInfo(); info.mzValue = mz;
        this.shift = 0.0;
        this.chromatogram = new TreeMap <> (chromatogram);
        
        retTimeMin = Double.MAX_VALUE;
        retTimeMax = 0.0;
        
        for (Map.Entry<Double, Double> entry : chromatogram.entrySet()) {
            double retTime = entry.getKey();
            double intensity = entry.getValue();
            
            if (retTime > retTimeMax) retTimeMax = retTime;
            if (retTime < retTimeMin) retTimeMin = retTime;

            if (intensity > apexIntensity) {
                apexIntensity = intensity;
                apexRetTime = retTime;
            }
        }
        
        apexMZ = mzMin = mzMax = mz;
        
        norm = java.lang.Math.sqrt(Math
                .continuous_dot_product(chromatogram, chromatogram));
    }
    
    // ------------------------------------------------------------------------
    // ----- Methods ----------------------------------------------------------
    // ------------------------------------------------------------------------
    
    public void setShift(double shift) {this.shift = shift;};
    
    @Override
    public Peak clone() {return new Peak(chromatogram, info);}
    
    // ------------------------------------------------------------------------
    // ----- Properties -------------------------------------------------------
    // ------------------------------------------------------------------------
    
    public NavigableMap <Double, Double> getChromatogram() {return chromatogram;};
    public PeakInfo getInfo() {return info;};
    
    public double getRetTime() {return apexRetTime;};
    public double getMZ() {return apexMZ;};
    public double getIntensity() {return apexIntensity;};
    
    public double getNorm() {return norm;}
    
    @Override
    public String toString() {
        return "#" + this.info.peakID + ": mz=" + this.apexMZ + " rt=" + this.apexRetTime;
    }
}
