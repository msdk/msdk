package edu.ucdavis.fiehnlab.mona.util;

import io.github.msdk.datamodel.rawdata.IDataPoint;

/**
 * simple impl
 * Created by Gert on 3/23/2015.
 */
public class DataPoint implements IDataPoint {

    private Double mz, intensity;

    public DataPoint(Double mz, Double intensity){
        this.mz = mz;
        this.intensity = intensity;
    }

    @Override
    public Double getMz() {
        return mz;
    }

    @Override
    public Double getIntensity() {
        return intensity;
    }

    @Override
    public String toString(){
        return mz+ ":" + intensity;
    }
}
