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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.msspectra.MsSpectrum;

public class TxtImportAlgorithmTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static String spectrumText = "10.0 100.0\n20.0 200.0\n30.0 300.0\n40.0 400.0";

    @Test
    public void test4Peaks() throws MSDKException {

        MsSpectrum spectrum = TxtImportAlgorithm
                .parseMsSpectrum(spectrumText);

        Assert.assertEquals(new Integer(4), spectrum.getNumberOfDataPoints());

        Assert.assertEquals(10.0, spectrum.getMzValues()[0], 0.0000001);
        Assert.assertEquals(40.0, spectrum.getMzValues()[3], 0.0000001);
        Assert.assertEquals(100.0f, spectrum.getIntensityValues()[0], 0.0001);
        Assert.assertEquals(300.0f, spectrum.getIntensityValues()[2], 0.00001);

    }

    @Test
    public void test4PeaksFromFile() throws IOException {
        File file = folder.newFile();
        FileWriter writer = new FileWriter(file);
        writer.write(spectrumText);
        writer.close();

        MsSpectrum spectrum = TxtImportAlgorithm
                .parseMsSpectrum(new FileReader(file));

        Assert.assertEquals(new Integer(4), spectrum.getNumberOfDataPoints());

        Assert.assertEquals(10.0, spectrum.getMzValues()[0], 0.0000001);
        Assert.assertEquals(40.0, spectrum.getMzValues()[3], 0.0000001);
        Assert.assertEquals(100.0f, spectrum.getIntensityValues()[0], 0.0001);
        Assert.assertEquals(300.0f, spectrum.getIntensityValues()[2], 0.00001);
    }

}
