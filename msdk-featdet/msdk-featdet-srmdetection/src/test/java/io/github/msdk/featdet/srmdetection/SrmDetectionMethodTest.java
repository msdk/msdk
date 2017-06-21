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

package io.github.msdk.featdet.srmdetection;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import io.github.msdk.io.nativeformats.ThermoRawImportMethod;

public class SrmDetectionMethodTest {

  private static final String TEST_DATA_PATH = "src/test/resources/";


  @Test
  public void test_mzML() throws MSDKException {

    // Create the data structures
    final DataPointStore dataStore = DataPointStoreFactory.getMemoryDataStore();

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "SRM.mzML");
    Assert.assertTrue("Cannot read test data", inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // SRM detection method
    SrmDetectionMethod srmMethod = new SrmDetectionMethod(rawFile, dataStore);
    final List<Chromatogram> chromatograms = srmMethod.execute();
    Assert.assertEquals(1.0, srmMethod.getFinishedPercentage(), 0.0001);

    // Verify data
    Assert.assertEquals(36, chromatograms.size());

    // m/z
    Assert.assertEquals(481.9, chromatograms.get(0).getMz(), 0.0001);
    Assert.assertEquals(722.35, chromatograms.get(18).getMz(), 0.0001);
  }


  @Test
  public void test_Thermo() throws MSDKException {

    // Run this test only on Windows
    Assume.assumeTrue(System.getProperty("os.name").startsWith("Windows"));

    // Create the data structures
    final DataPointStore dataStore = DataPointStoreFactory.getMemoryDataStore();

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "Thermo-SRM.raw");
    Assert.assertTrue("Cannot read test data", inputFile.canRead());
    ThermoRawImportMethod importer = new ThermoRawImportMethod(inputFile, dataStore);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // SRM detection method
    SrmDetectionMethod srmMethod = new SrmDetectionMethod(rawFile, dataStore);
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
