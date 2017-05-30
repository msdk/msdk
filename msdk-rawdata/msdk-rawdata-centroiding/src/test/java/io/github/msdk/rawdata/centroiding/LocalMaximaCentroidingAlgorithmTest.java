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

package io.github.msdk.rawdata.centroiding;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import io.github.msdk.util.MsSpectrumUtil;

public class LocalMaximaCentroidingAlgorithmTest {

  private static final String TEST_DATA_PATH = "src/test/resources/";


  @Test
  public void testOrbitrap() throws MSDKException {

    // Create the data structures
    DataPointStore dataStore = DataPointStoreFactory.getMemoryDataStore();

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "profile_orbitrap.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);
    List<MsScan> scans = rawFile.getScans();
    Assert.assertNotNull(scans);

    MsScan lastScan = scans.get(scans.size() - 1);

    LocalMaximaCentroidingAlgorithm centroider = new LocalMaximaCentroidingAlgorithm(dataStore);
    final MsScan centroidedScan = centroider.centroidScan(lastScan);

    double mzBuffer[] = centroidedScan.getMzValues();
    float intensityBuffer[] = centroidedScan.getIntensityValues();
    int numOfDataPoints = centroidedScan.getNumberOfDataPoints();

    Assert.assertTrue(numOfDataPoints > 50);

    Integer basePeak = MsSpectrumUtil.getBasePeakIndex(intensityBuffer, numOfDataPoints);

    Assert.assertEquals(3.537E7f, intensityBuffer[basePeak], 1E5);
    Assert.assertEquals(281.24761852, mzBuffer[basePeak], 0.000001);

    rawFile.dispose();

  }

}
