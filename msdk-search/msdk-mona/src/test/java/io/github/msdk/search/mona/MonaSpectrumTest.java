/* 
 * (C) Copyright 2015 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */

package io.github.msdk.search.mona;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Range;

import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrum;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;

public class MonaSpectrumTest {

    MsSpectrum spectrum;

    @Before
    public void setUp() throws Exception {
        spectrum = new MonaSpectrum(3841762);
    }

    @After
    public void tearDown() throws Exception {
        spectrum = null;
    }

    @Ignore("Ignored because MoNA API is throwing HTTP 500 error")
    @Test 
    public void testGetSpectrumType() throws Exception {
        assertTrue(spectrum.getSpectrumType() != null);
    }

    @Ignore("Ignored because MoNA API is throwing HTTP 500 error")
    @Test
    public void testGetDataPointsByMz() throws Exception {
        MsSpectrumDataPointList list = MSDKObjectBuilder
                .getMsSpectrumDataPointList();
        spectrum.getDataPointsByMzAndIntensity(list, Range.singleton(303.2200),
                (Range) Range.all());
        assertTrue(list.getSize() == 1);
    }

    @Ignore("Ignored because MoNA API is throwing HTTP 500 error")
    @Test
    public void testGetDataPoints() throws Exception {
        MsSpectrumDataPointList list = MSDKObjectBuilder
                .getMsSpectrumDataPointList();
        spectrum.getDataPoints(list);
        assertTrue(list.getSize() == 5);
    }

    @Ignore("Ignored because MoNA API is throwing HTTP 500 error")
    @Test
    public void testGetMzRange() throws Exception {
        Range<Double> range = spectrum.getMzRange();
        assertTrue(range.lowerEndpoint().equals(303.2200));
        assertTrue(range.upperEndpoint().equals(864.5626));
    }

    @Ignore("Ignored because MoNA API is throwing HTTP 500 error")
    @Test
    public void testGetTIC() throws Exception {
        assertTrue(spectrum.getTIC() != null);
        assertTrue(spectrum.getTIC() > 3.2 && spectrum.getTIC() < 3.4);
    }
}