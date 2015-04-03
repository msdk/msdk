package edu.ucdavis.fiehnlab.mona.util;

import io.github.msdk.datamodel.rawdata.DataPoint;

/**
 * simple impl
 * Created by Gert on 3/23/2015.
 */
public class DataPointImpl implements DataPoint {

    private Double mz;
    private Float intensity;

    public DataPointImpl(Double mz, Float intensity){
        this.mz = mz;
        this.intensity = intensity;
    }

    @Override
    public Double getMz() {
        return mz;
    }

    @Override
    public Float getIntensity() {
        return intensity;
    }

    @Override
    public String toString(){
        return mz+ ":" + intensity;
    }
}
