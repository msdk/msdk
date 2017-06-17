
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
    Assert.assertEquals(spectrum.getMzBinaryDataInfo().getArrayLength(),
        spectrum.getMzValues().length);

    Assert.assertNotNull(new File(inputFileName));
    MzMLFileParser mzParser2 = new MzMLFileParser(inputFileName);
    RawDataFile rawFile2 = mzParser2.execute();
    Assert.assertNotNull(rawFile2);
    MzMLSpectrum spectrum2 = (MzMLSpectrum) rawFile2.getScans().get(17);
    Assert.assertNotNull(spectrum2);
    Assert.assertNotNull(spectrum2.getMzValues());
    Assert.assertEquals(spectrum2.getMzBinaryDataInfo().getArrayLength(),
        spectrum2.getMzValues().length);
  }
}
