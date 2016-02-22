/* 
 * (C) Copyright 2015-2016 by MSDK Development Team
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

package io.github.msdk.io.txt;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.msspectra.MsSpectrum;

public class TxtImportAlgorithmTest {

    @Test
    public void test4Peaks() throws MSDKException {

        String spectrumText = "10.0 20.0\n20.0 20.0\n30.0 100.0\n40.0 50.0";

        MsSpectrum spectrum = TxtImportAlgorithm
                .parseMsSpectrum(spectrumText);

        Assert.assertEquals(new Integer(4), spectrum.getNumberOfDataPoints());

        Assert.assertEquals(10.0, spectrum.getMzValues()[0], 0.0000001);
        Assert.assertEquals(40.0, spectrum.getMzValues()[3], 0.0000001);
        Assert.assertEquals(20.0f, spectrum.getIntensityValues()[0], 0.0001);
        Assert.assertEquals(100.0f, spectrum.getIntensityValues()[2], 0.00001);

    }

}
