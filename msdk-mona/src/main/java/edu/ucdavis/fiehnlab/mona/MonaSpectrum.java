package edu.ucdavis.fiehnlab.mona;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Range;
import edu.ucdavis.fiehnlab.mona.pojo.Spectra;
import edu.ucdavis.fiehnlab.mona.util.DataPointImpl;
import io.github.msdk.datamodel.peaklists.PeakListRowAnnotation;
import io.github.msdk.datamodel.rawdata.DataPointList;
import io.github.msdk.datamodel.rawdata.DataPoint;
import io.github.msdk.datamodel.rawdata.MassSpectrum;
import io.github.msdk.datamodel.rawdata.MassSpectrumType;
import edu.ucdavis.fiehnlab.mona.util.PointListImpl;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * A basic MoNA record, which describes a MassBank of Northern America Spectra. This is a readonly entity and should not be modified by the software in any possible way
 */
public class MonaSpectrum implements MassSpectrum,MonaConfiguration,PeakListRowAnnotation {

    /**
     * internal MoNa ID
     */
    private Long id;

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
     * chemical structure of this compound
     */
    private IAtomContainer atomContainer;

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

        this.id = id;

        URL url = getAccessionURL();

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

        //assign compound information

        String molFile = monaRecord.getBiologicalCompound().getMolFile();

        //done
        logger.finer("spectra build");
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

    @Nullable
    @Override
    public IAtomContainer getChemicalStructure() {
        return this.atomContainer;
    }

    @Override
    public void setChemicalStructure(@Nullable IAtomContainer structure) {
        //not supported
    }

    @Nullable
    @Override
    public IMolecularFormula getFormula() {
        return null;
    }

    @Override
    public void setFormula(@Nullable IMolecularFormula formula) {
        //not supported
    }

    @Nullable
    @Override
    public String getDescription() {
        return "Massbank of Northern America record, " + id;
    }

    @Override
    public void setDescription(@Nullable String description) {
        //not supported
    }

    @Nullable
    @Override
    public String getIdentificationMethod() {
        return null;
    }

    @Override
    public void setIdentificationMethod(@Nullable String idMethod) {
        //not supported
    }

    @Nullable
    @Override
    public String getDataBaseId() {
        return this.id.toString();
    }

    @Override
    public void setDataBaseId(@Nullable String dbId) {
        //not supported
    }

    @Nullable
    @Override
    public URL getAccessionURL(){
        try {
            return  new URL(MONA_URL+"/rest/spectra/"+id);
        } catch (MalformedURLException e) {
            throw new RuntimeException("malformed URL, should never actually happen!");
        }
    }

    @Override
    public void setAccessionURL(@Nullable URL dbURL) {
        //not supported
    }
}
