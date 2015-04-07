package edu.ucdavis.fiehnlab.mona.util;

import com.google.common.collect.Range;
import io.github.msdk.datamodel.rawdata.DataPoint;
import io.github.msdk.datamodel.rawdata.DataPointList;

import javax.annotation.Nonnull;
import java.util.ArrayList;

/**
 * Basic implementation of a data point list
 */
public class PointListImpl extends ArrayList<DataPoint> implements DataPointList {
    @Nonnull
    @Override
    public Range<Double> getMzRange() {
        //TODO needs some form of better impl based on the content of the list
        //return Range.all();
        throw new NoSuchMethodError("still working on this");
    }

    @Nonnull
    @Override
    public double[] getMzBuffer() {
        return new double[0];
    }

    @Nonnull
    @Override
    public float[] getIntensityBuffer() {
        return new float[0];
    }

    @Override
    public void setBuffers(@Nonnull double[] mzBuffer, @Nonnull float[] intensityBuffer, int size) {

    }
}
