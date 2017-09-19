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

package io.github.msdk.featuredetection.srm;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import io.github.msdk.datamodel.Chromatogram;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.featuredetection.srm.SrmDetectionMethod;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import io.github.msdk.io.nativeformats.ThermoRawImportMethod;

public class SrmDetectionMethodTest {

  @Test
  public void test_mzML() throws Exception {

    // Import the file
    final URL url = getClass().getClassLoader().getResource("SRM.mzML");
    File inputFile = new File(url.toURI());
    Assert.assertTrue("Cannot read test data", inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // SRM detection method
    SrmDetectionMethod srmMethod = new SrmDetectionMethod(rawFile);
    final List<Chromatogram> chromatograms = srmMethod.execute();
    Assert.assertEquals(1.0, srmMethod.getFinishedPercentage(), 0.0001);

    // Verify data
    Assert.assertEquals(36, chromatograms.size());
    for (int i = 0; i < 36; i++) {
      Assert.assertNotNull(chromatograms.get(i));
    }
    
    // m/z
    Assert.assertEquals(407.706, chromatograms.get(0).getMz(), 0.001);
    Assert.assertEquals(1084.486, chromatograms.get(17).getMz(), 0.001);
    Assert.assertEquals(1042.516, chromatograms.get(34).getMz(), 0.001);
  }

  @Test
  public void test_Thermo() throws Exception {

    // Run this test only on Windows
    Assume.assumeTrue(System.getProperty("os.name").startsWith("Windows"));

    // Import the file
    final URL url = getClass().getClassLoader().getResource("Thermo-SRM.raw");
    File inputFile = new File(url.toURI());
    Assert.assertTrue("Cannot read test data", inputFile.canRead());
    ThermoRawImportMethod importer = new ThermoRawImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // SRM detection method
    SrmDetectionMethod srmMethod = new SrmDetectionMethod(rawFile);
    final List<Chromatogram> chromatograms = srmMethod.execute();
    Assert.assertEquals(1.0, srmMethod.getFinishedPercentage(), 0.0001);
    Assert.assertEquals(3, chromatograms.size());

    // Verify chromatogram 1
    Chromatogram chromatogram = chromatograms.get(0);
    Assert.assertEquals(926, chromatogram.getNumberOfDataPoints(), 0.0001);
    Assert.assertEquals(2, chromatogram.getIsolations().size());
    Assert.assertEquals(149.0, chromatogram.getIsolations().get(0).getPrecursorMz(), 0.0001);

    // Verify chromatogram 2
    chromatogram = chromatograms.get(1);
    Assert.assertEquals(926, chromatogram.getNumberOfDataPoints(), 0.0001);
    Assert.assertEquals(2, chromatogram.getIsolations().size());
    Assert.assertEquals(165.0, chromatogram.getIsolations().get(0).getPrecursorMz(), 0.0001);

    // Verify chromatogram 3
    chromatogram = chromatograms.get(2);
    Assert.assertEquals(926, chromatogram.getNumberOfDataPoints(), 0.0001);
    Assert.assertEquals(2, chromatogram.getIsolations().size());
    Assert.assertEquals(912.2, chromatogram.getIsolations().get(0).getPrecursorMz(), 0.0001);

  }
}
