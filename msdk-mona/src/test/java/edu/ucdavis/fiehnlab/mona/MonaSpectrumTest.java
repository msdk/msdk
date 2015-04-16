package edu.ucdavis.fiehnlab.mona;

import static org.junit.Assert.assertTrue;
import io.github.msdk.datamodel.MSDKObjectBuilder;
import io.github.msdk.datamodel.rawdata.DataPoint;
import io.github.msdk.datamodel.rawdata.DataPointList;
import io.github.msdk.datamodel.rawdata.MassSpectrum;

import java.util.List;

import com.google.common.collect.Range;

public class MonaSpectrumTest {

    MassSpectrum spectrum;

    @org.junit.Before
    public void setUp() throws Exception {
        spectrum = new MonaSpectrum(3841762);
    }

    @org.junit.After
    public void tearDown() throws Exception {
        spectrum = null;
    }

    @org.junit.Test
    public void testGetSpectrumType() throws Exception {
        assertTrue(spectrum.getSpectrumType() != null);
    }

    @org.junit.Test
    public void testGetNumberOfDataPoints() throws Exception {
        assertTrue(spectrum.getDataPoints().size() == 5);
    }

    @org.junit.Test
    public void testGetDataPoints() throws Exception {
        assertTrue(spectrum.getDataPoints() != null);
    }

    @org.junit.Test
    public void testGetDataPointsByMass() throws Exception {
        assertTrue(spectrum.getDataPointsByMz(Range.singleton(303.2200)).size() == 1);
    }

    @org.junit.Test
    public void testGetDataPointsByIntensity() throws Exception {

        List<DataPoint> list = spectrum.getDataPointsByIntensity(Range.closed(0.6F, 1.0F));
        assertTrue(list.size() == 4);
    }

    @org.junit.Test
    public void testSetDataPoints() throws Exception {
        DataPointList list = MSDKObjectBuilder.getDataPointList();
        spectrum.getDataPoints(list);
        assertTrue(list.size() == 5);
    }

    @org.junit.Test
    public void testGetMzRange() throws Exception {
        Range<Double> range = spectrum.getMzRange();
        assertTrue(range.lowerEndpoint().equals(303.2200));
        assertTrue(range.upperEndpoint().equals(864.5626));
    }

    @org.junit.Test
    public void testGetHighestDataPoint() throws Exception {
        assertTrue(spectrum.getHighestDataPoint() != null);
        assertTrue(spectrum.getHighestDataPoint().getIntensity() == 1);
    }

    @org.junit.Test
    public void testGetTIC() throws Exception {
        assertTrue(spectrum.getTIC() != null);
        assertTrue(spectrum.getTIC() > 3.2 && spectrum.getTIC() < 3.4);
    }
}