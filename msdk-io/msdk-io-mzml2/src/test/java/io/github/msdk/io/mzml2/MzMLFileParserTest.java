
/*
 * (C) Copyright 2015-2016 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1 as published by the Free
 * Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation.
 */

package io.github.msdk.io.mzml2;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;

public class MzMLFileParserTest {

  private static final String TEST_DATA_PATH = "src/test/resources/";

  @Test
  public void testValidmzMLfile() throws IOException, MSDKException, XMLStreamException {

    final String inputFileName = TEST_DATA_PATH + "mzML_with_UV.mzML";
    final File inputFile = new File(inputFileName);

    MzMLFileParser mzParser = new MzMLFileParser(inputFile);
    RawDataFile rawFile = mzParser.execute();
    Assert.assertNotNull(rawFile);
    MzMLSpectrum spectrum = (MzMLSpectrum) rawFile.getScans().get(14);
    Assert.assertNotNull(spectrum);
    Assert.assertNotNull(spectrum.getMzValues());
    Assert.assertNotNull(spectrum.getIntensityValues());
    Assert.assertEquals(new Integer(spectrum.getMzValues().length),
        spectrum.getNumberOfDataPoints());
    Assert.assertEquals(new Integer(spectrum.getIntensityValues().length),
        spectrum.getNumberOfDataPoints());
    Assert.assertEquals(new Integer(2114), spectrum.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, spectrum.getSpectrumType());
    Assert.assertEquals(new Float(9.939699e06), spectrum.getTIC(), 10);
    Assert.assertEquals(new Double(100.175651550293), spectrum.getMzRange().lowerEndpoint(),
        0.000001);
    Assert.assertEquals(new Double(999.832214355469), spectrum.getMzRange().upperEndpoint(),
        0.000001);
    Assert.assertEquals("+ c ESI Q1MS [100.000-1000.000]", spectrum.getScanDefinition());
    Assert.assertEquals(new Integer(1), spectrum.getMsFunction().getMsLevel());
    Assert.assertEquals(PolarityType.POSITIVE, spectrum.getPolarity());
    Assert.assertEquals(new Float(18.89235 * 60), spectrum.getRetentionTime());

    Assert.assertNotNull(new File(inputFileName));
    MzMLFileParser mzParser2 = new MzMLFileParser(inputFileName);
    RawDataFile rawFile2 = mzParser2.execute();
    Assert.assertNotNull(rawFile2);
    MzMLSpectrum spectrum2 = (MzMLSpectrum) rawFile2.getScans().get(17);
    Assert.assertNotNull(spectrum2);
    Assert.assertNotNull(spectrum2.getMzValues());
    Assert.assertNotNull(spectrum2.getIntensityValues());
    Assert.assertEquals(new Integer(spectrum2.getMzValues().length),
        spectrum2.getNumberOfDataPoints());
    Assert.assertEquals(new Integer(spectrum2.getIntensityValues().length),
        spectrum2.getNumberOfDataPoints());
    Assert.assertEquals(new Integer(2117), spectrum2.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, spectrum2.getSpectrumType());
    Assert.assertEquals(new Float(43900.85546875), spectrum2.getTIC(), 10);
    Assert.assertEquals(new Double(100.300285339355), spectrum2.getMzRange().lowerEndpoint(),
        0.000001);
    Assert.assertEquals(new Double(999.323547363281), spectrum2.getMzRange().upperEndpoint(),
        0.000001);
    Assert.assertEquals("- c ESI Q1MS [100.000-1000.000]", spectrum2.getScanDefinition());
    Assert.assertEquals(new Integer(1), spectrum2.getMsFunction().getMsLevel());
    Assert.assertEquals(PolarityType.NEGATIVE, spectrum2.getPolarity());
    Assert.assertEquals(new Float(18.919083333333 * 60), spectrum2.getRetentionTime());
  }
}
