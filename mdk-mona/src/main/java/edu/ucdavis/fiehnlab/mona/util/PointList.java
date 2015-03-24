package edu.ucdavis.fiehnlab.mona.util;

import io.github.msdk.datamodel.rawdata.IDataPoint;
import io.github.msdk.datamodel.rawdata.IDataPointList;

import javax.annotation.Nonnull;
import java.util.ArrayList;

/**
 * Created by Gert on 3/18/2015.
 */
public class PointList extends ArrayList<IDataPoint> implements IDataPointList {
    @Nonnull
    @Override
    public double[] getMzBuffer() {
        return new double[0];
    }

    @Nonnull
    @Override
    public double[] getIntensityBuffer() {
        return new double[0];
    }

    @Override
    public void setBuffers(@Nonnull double[] mzBuffer, @Nonnull double[] intensityBuffer, int size) {

    }
}
