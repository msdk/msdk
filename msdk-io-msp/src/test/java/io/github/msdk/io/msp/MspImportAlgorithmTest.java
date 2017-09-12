/*
 * (C) Copyright 2015-2017 by MSDK Development Team
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

package io.github.msdk.io.msp;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.io.msp.MspImportAlgorithm;
import io.github.msdk.io.msp.MspSpectrum;

public class MspImportAlgorithmTest {

  private static final String TEST_DATA_PATH = "src/test/resources/";

  @Test
  public void testSampleMSP() throws MSDKException, IOException {

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "sample.msp");

    MspSpectrum spectrum = MspImportAlgorithm.parseMspFromFile(inputFile);

    Assert.assertEquals(new Integer(18), spectrum.getNumberOfDataPoints());
    Assert.assertEquals(26.0, spectrum.getMzValues()[0], 0.0000001);
    Assert.assertEquals(651.0f, spectrum.getIntensityValues()[17], 0.00001);

  }

}
