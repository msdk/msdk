package edu.ucdavis.fiehnlab.mona;

import com.eclipsesource.json.JsonObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Range;
import edu.ucdavis.fiehnlab.mona.pojo.Spectra;
import edu.ucdavis.fiehnlab.mona.util.DataPoint;
import io.github.msdk.datamodel.rawdata.IDataPoint;
import io.github.msdk.datamodel.rawdata.IDataPointList;
import io.github.msdk.datamodel.rawdata.IMassSpectrum;
import io.github.msdk.datamodel.rawdata.MassSpectrumType;
import edu.ucdavis.fiehnlab.mona.util.PointList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
public class MonaSpectrum implements IMassSpectrum,IMonaConfiguration {

    private Logger logger = Logger.getLogger("mona");

    /**
     * unless otherwise said, MoNA spectra are always centroided
     */
    private MassSpectrumType spectrumType = MassSpectrumType.CENTROIDED;

    /**
     * storage of dataPoints
     */
    private IDataPointList dataPoints;

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

        URL url = new URL(MONA_URL+"/spectra/"+id);

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
            addDataPoint(Double.parseDouble(v[0]),Double.parseDouble(v[1]));
        }

        Collections.sort(dataPoints, new Comparator<IDataPoint>() {
            @Override
            public int compare(IDataPoint o1, IDataPoint o2) {
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
    protected void addDataPoint(Double mass, Double intensity){
        if(this.dataPoints == null){
            this.dataPoints = new PointList();
        }
        this.dataPoints.add(new DataPoint(mass,intensity));
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
        IDataPointList list = new PointList();
        for(IDataPoint dataPoint : dataPoints){
            if(mzRange.contains(dataPoint.getMz())){
                list.add(dataPoint);
            }
        }
        return list;
    }

    @Nonnull
    @Override
    public List<IDataPoint> getDataPointsByIntensity(@Nonnull Range<Double> intensityRange) {

        IDataPointList list = new PointList();
        for(IDataPoint dataPoint : dataPoints){
            if(intensityRange.contains(dataPoint.getIntensity())){
                list.add(dataPoint);
            }
        }
        return list;
    }

    @Override
    public void setDataPoints(@Nonnull IDataPointList newDataPoints) {
        this.dataPoints = newDataPoints;
    }

    @Nonnull
    @Override
    public Range<Double> getMzRange() {
        return Range.open(getDataPoints().get(0).getMz(),getDataPoints().get(getDataPoints().size() - 1).getMz());
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
