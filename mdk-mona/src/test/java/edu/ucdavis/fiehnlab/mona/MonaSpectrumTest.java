package edu.ucdavis.fiehnlab.mona;

import com.google.common.collect.Range;
import edu.ucdavis.fiehnlab.mona.util.PointList;
import io.github.msdk.datamodel.rawdata.IDataPoint;
import io.github.msdk.datamodel.rawdata.IDataPointList;
import io.github.msdk.datamodel.rawdata.IMassSpectrum;

import java.util.List;

import static org.junit.Assert.*;

public class MonaSpectrumTest {

    IMassSpectrum spectrum;

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
        assertTrue(spectrum.getDataPointsByMass(Range.singleton(303.2200)).size() == 1);
    }

    @org.junit.Test
    public void testGetDataPointsByIntensity() throws Exception {

        List<IDataPoint> list = spectrum.getDataPointsByIntensity(Range.closed(0.6, 1.0));
        assertTrue(list.size() == 4);
    }

    @org.junit.Test
    public void testSetDataPoints() throws Exception {
        IDataPointList list = new PointList();
        spectrum.getDataPoints(list);
        assertTrue(list.size() == 5);
    }

    @org.junit.Test
    public void testGetMzRange() throws Exception {
        Range<Double> range = spectrum.getMzRange();
        assertTrue(range.lowerEndpoint().equals(303.2200));
        assertTrue(range.upperEndpoint().equals(864.56257));
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