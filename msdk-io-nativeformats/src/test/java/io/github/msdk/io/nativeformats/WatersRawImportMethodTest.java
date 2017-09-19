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

package io.github.msdk.io.nativeformats;

import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.PolarityType;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.util.MsSpectrumUtil;

public class WatersRawImportMethodTest {

  private static final String TEST_DATA_PATH = "src/test/resources/";


  @Test
  public void test20150813() throws Exception {

    // Run this test only on Windows
    assumeTrue(System.getProperty("os.name").startsWith("Windows"));

    // Create the data structures
    double mzBuffer[];
    float intensityBuffer[];

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "20150813-63.raw");
    Assert.assertTrue(inputFile.canRead());
    WatersRawImportMethod importer = new WatersRawImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // The file has 3185 scans
    List<MsScan> scans = rawFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(3185, scans.size());

    // 1st scan, #1
    MsScan scan1 = scans.get(0);
    Assert.assertEquals(new Integer(1), scan1.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan1.getSpectrumType());
    Assert.assertEquals(new Integer(1), scan1.getMsLevel());
    Assert.assertEquals(0.226f, scan1.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.POSITIVE, scan1.getPolarity());
    mzBuffer = scan1.getMzValues();
    intensityBuffer = scan1.getIntensityValues();
    Assert.assertEquals(248, (int) scan1.getNumberOfDataPoints());
    Float scan1maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan1.getNumberOfDataPoints());
    Assert.assertEquals(9.55E5f, scan1maxInt, 1E4f);

    // 3000th scan, #3
    MsScan scan3000 = scans.get(2999);
    Assert.assertEquals(new Integer(3000), scan3000.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan3000.getSpectrumType());
    Assert.assertEquals(new Integer(1), scan3000.getMsLevel());
    Assert.assertEquals(636.228f, scan3000.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.NEGATIVE, scan3000.getPolarity());
    mzBuffer = scan3000.getMzValues();
    intensityBuffer = scan3000.getIntensityValues();
    Assert.assertEquals(224, (int) scan3000.getNumberOfDataPoints());
    Float scan3000maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan3000.getNumberOfDataPoints());
    Assert.assertEquals(4.23E5f, scan3000maxInt, 1E4f);
  }

}
