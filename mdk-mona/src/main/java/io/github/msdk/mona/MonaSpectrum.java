package io.github.msdk.mona;

import com.google.common.collect.Range;
import io.github.msdk.datamodel.rawdata.IDataPoint;
import io.github.msdk.datamodel.rawdata.IDataPointList;
import io.github.msdk.datamodel.rawdata.IMassSpectrum;
import io.github.msdk.datamodel.rawdata.MassSpectrumType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * basic implementation of a Mona Mass Spectrum
 * <p/>
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 3/17/15
 * Time: 2:39 PM
 */
public class MonaSpectrum implements IMassSpectrum {

    /**
     * unless otherwise said, MoNA spectra are always centroided
     */
    MassSpectrumType spectrumType = MassSpectrumType.CENTROIDED;

    IDataPointList dataPoints;

    /**
     *
     */
    protected MonaSpectrum() {

    }

    @Nonnull
    @Override
    public MassSpectrumType getSpectrumType() {
        return this.spectrumType;
    }

    @Override
    public void setSpectrumType(@Nonnull MassSpectrumType spectrumType) {
        this.spectrumType = spectrumType;
    }

    @Override
    public int getNumberOfDataPoints() {
        return 0;
    }

    @Nonnull
    @Override
    public IDataPointList getDataPoints() {
        return dataPoints;
    }

    @Override
    public void getDataPoints(IDataPointList list) {

        list.clear();

        for (IDataPoint dataPoint : dataPoints) {
            list.add(dataPoint);
        }
    }

    @Nonnull
    @Override
    public IDataPointList getDataPointsByMass(@Nonnull Range<Double> mzRange) {

        IDataPointList list =
        for(IDataPoint dataPoint : dataPoints){

        }
        return null;


    }

    @Nonnull
    @Override
    public List<IDataPoint> getDataPointsByIntensity(@Nonnull Range<Double> intensityRange) {
        return null;
    }

    @Override
    public void setDataPoints(@Nonnull IDataPointList newDataPoints) {

    }

    @Nonnull
    @Override
    public Range<Double> getMzRange() {
        return null;
    }

    @Nullable
    @Override
    public IDataPoint getHighestDataPoint() {
        IDataPoint point = null;

        for (IDataPoint p : getDataPoints()) {
            if (point == null) {
                point = p;
            } else {
                if (p.getIntensity() > point.getIntensity()) {
                    point = p;
                }
            }

        }
        return point;
    }

    @Nonnull
    @Override
    public Double getTIC() {

        Double tic = 0.0;

        for (IDataPoint point : getDataPoints()) {
            tic = point.getIntensity() + tic;
        }
        return tic;
    }
}
