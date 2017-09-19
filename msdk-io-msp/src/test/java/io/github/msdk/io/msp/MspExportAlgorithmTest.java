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

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.SimpleMsSpectrum;
import io.github.msdk.util.MsSpectrumUtil;

public class MspExportAlgorithmTest {


  @Test
  public void testExportMSP() throws MSDKException, IOException {

    File tmpFile = File.createTempFile("msdktest", "msp");
    tmpFile.deleteOnExit();

    MsSpectrum testSpectrum = new SimpleMsSpectrum(new double[] {10.0, 20.0, 30.0},
        new float[] {100f, 200f, 300f}, 3, MsSpectrumType.CENTROIDED);

    MspExportAlgorithm.exportSpectrum(tmpFile, testSpectrum);

    MspSpectrum importedSpectrum = MspImportAlgorithm.parseMspFromFile(tmpFile);

    tmpFile.delete();

    Assert.assertEquals(testSpectrum.getNumberOfDataPoints(),
        importedSpectrum.getNumberOfDataPoints());

    String testSpectrumAsString = MsSpectrumUtil.msSpectrumToString(testSpectrum);
    String importedSpectrumAsString = MsSpectrumUtil.msSpectrumToString(importedSpectrum);
    Assert.assertEquals(testSpectrumAsString, importedSpectrumAsString);

    Range<Double> all = Range.all();
    Float testSpectrumTIC = MsSpectrumUtil.getTIC(testSpectrum.getMzValues(),
        testSpectrum.getIntensityValues(), testSpectrum.getNumberOfDataPoints(), all);
    Float importedSpectrumTIC = MsSpectrumUtil.getTIC(importedSpectrum.getMzValues(),
        importedSpectrum.getIntensityValues(), importedSpectrum.getNumberOfDataPoints(), all);

    Assert.assertEquals(testSpectrumTIC, importedSpectrumTIC);

  }

}
