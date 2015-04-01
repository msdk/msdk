package edu.ucdavis.fiehnlab.mona;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Range;
import edu.ucdavis.fiehnlab.mona.pojo.Spectra;
import edu.ucdavis.fiehnlab.mona.util.DataPointImpl;
import io.github.msdk.datamodel.rawdata.DataPointList;
import io.github.msdk.datamodel.rawdata.DataPoint;
import io.github.msdk.datamodel.rawdata.MassSpectrum;
import io.github.msdk.datamodel.rawdata.MassSpectrumType;
import edu.ucdavis.fiehnlab.mona.util.PointListImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * basic implementation of a Mona Mass Spectrum
 * <p/>
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 3/17/15
 * Time: 2:39 PM
 */
public class MonaSpectrum implements MassSpectrum,MonaConfiguration {

    private Logger logger = Logger.getLogger("mona");

    /**
     * unless otherwise said, MoNA spectra are always centroided
     */
    private MassSpectrumType spectrumType = MassSpectrumType.CENTROIDED;

    /**
     * storage of dataPoints
     */
    private DataPointList dataPoints;

    /**
     * build a new mona spectrum from the given record
     */
    public MonaSpectrum(Spectra monaRecord) {
        this.build(monaRecord);
    }

    /**
     * build a spectrum directly from an id, while accessing the mona repository
     *
     * @param id
     */
    public MonaSpectrum(long id) throws IOException {

        URL url = new URL(MONA_URL+"/rest/spectra/"+id);

        ObjectMapper mapper = new ObjectMapper();
        Spectra spectra = mapper.readValue(url.openStream(), Spectra.class);

        build(spectra);
    }

    /**
     * actual builder
     * @param monaRecord
     */
    protected void build(Spectra monaRecord){

        logger.info("received: " + monaRecord.getId());

        //convert to internal model
        for(String s : monaRecord.getSpectrum().split(" ")){
            String v[] = s.split(":");
            addDataPoint(Double.parseDouble(v[0]),Float.parseFloat(v[1]));
        }

        Collections.sort(dataPoints, new Comparator<DataPoint>() {
            @Override
            public int compare(DataPoint o1, DataPoint o2) {
                return o1.getMz().compareTo(o2.getMz());
            }
        });

        logger.info("spectra build");
    }

    /**
     * adds a datapoint internally
     * @param mass
     * @param intensity
     */
    protected void addDataPoint(Double mass, Float intensity){
        if(this.dataPoints == null){
            this.dataPoints = new PointListImpl();
        }
        this.dataPoints.add(new DataPointImpl(mass,intensity));
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


    @Nonnull
    @Override
    public DataPointList getDataPoints() {
        return dataPoints;
    }

    @Override
    public void getDataPoints(DataPointList list) {
        list.clear();
        for (DataPoint dataPoint : dataPoints) {
            list.add(dataPoint);
        }
    }

    @Nonnull
    @Override
    public DataPointList getDataPointsByMass(@Nonnull Range<Double> mzRange) {
        DataPointList list = new PointListImpl();
        for(DataPoint dataPoint : dataPoints){
            if(mzRange.contains(dataPoint.getMz())){
                list.add(dataPoint);
            }
        }
        return list;
    }

    @Nonnull
    @Override
    public List<DataPoint> getDataPointsByIntensity(@Nonnull Range<Float> intensityRange) {

        DataPointList list = new PointListImpl();
        for(DataPoint dataPoint : dataPoints){
            if(intensityRange.contains(dataPoint.getIntensity())){
                list.add(dataPoint);
            }
        }
        return list;
    }

    @Override
    public void setDataPoints(@Nonnull DataPointList newDataPoints) {
        this.dataPoints = newDataPoints;
    }

    @Nonnull
    @Override
    public Range<Double> getMzRange() {
        return Range.open(getDataPoints().get(0).getMz(),getDataPoints().get(getDataPoints().size() - 1).getMz());
    }

    @Nullable
    @Override
    public DataPoint getHighestDataPoint() {
        DataPoint point = null;

        for (DataPoint p : getDataPoints()) {
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
    public Float getTIC() {
        Float tic = 0.0F;

        for (DataPoint point : getDataPoints()) {
            tic = point.getIntensity() + tic;
        }
        return tic;
    }
}
