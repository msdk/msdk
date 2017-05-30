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

package io.github.msdk.rawdata.filters;

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

public class SGFilterMethodTest {
  private static final String TEST_DATA_PATH = "src/test/resources/";


  @Test
  public void testSGFilter() throws MSDKException {

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "orbitrap_300-600mz.mzML");
    Assert.assertTrue("Cannot read test data", inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    DataPointStore store = DataPointStoreFactory.getMemoryDataStore();
    // Execute the filter
    SGFilterAlgorithm SGFilter = new SGFilterAlgorithm(11, store);
    MSDKFilteringMethod filterMethod = new MSDKFilteringMethod(rawFile, SGFilter, store);
    RawDataFile newRawFile = filterMethod.execute();
    // The result of the method can't be Null
    Assert.assertNotNull(newRawFile);
    Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);

    List<MsScan> newScans = newRawFile.getScans();

    // Check the new scans are between the new range limits

    for (MsScan newScan : newScans) {
      Assert.assertNotNull(newScan);
      Assert.assertTrue(newScan.getNumberOfDataPoints() > 0);

    }

  }


  @Test
  public void testSGFilterWrongParameters() throws MSDKException {

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "orbitrap_300-600mz.mzML");
    Assert.assertTrue("Cannot read test data", inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    DataPointStore store = DataPointStoreFactory.getMemoryDataStore();

    // Execute the filter with wrong parameters
    SGFilterAlgorithm sgFilter = new SGFilterAlgorithm(110, store);
    MSDKFilteringMethod filterMethod = new MSDKFilteringMethod(rawFile, sgFilter, store);
    RawDataFile newRawFile = filterMethod.execute();
    // The result of the method can't be Null
    Assert.assertNotNull(newRawFile);
    Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);

    List<MsScan> newScans = newRawFile.getScans();
    List<MsScan> scans = rawFile.getScans();

    // The resulting scans should be equal to the input scans
    for (int i = 0; i < scans.size(); i++) {
      Assert.assertNotNull(newScans.get(i));
      MsScan inputScan = scans.get(i);
      MsScan newScan = newScans.get(i);

      // Get the mz and intensities values from the input scan
      double mzValues[] = inputScan.getMzValues();
      float intensityValues[] = inputScan.getIntensityValues();

      // Get the mz and intensities values from the filtered scan
      double newMzValues[] = newScan.getMzValues();
      float newIntensityValues[] = newScan.getIntensityValues();

      // They should contain the same number of data points
      Assert.assertEquals(inputScan.getNumberOfDataPoints(), newScan.getNumberOfDataPoints());

      for (int j = 0; j < inputScan.getNumberOfDataPoints(); j++) {
        Assert.assertEquals(mzValues[j], newMzValues[j], 0.0001);
        Assert.assertEquals(intensityValues[j], newIntensityValues[j], 0.0001);
      }

    }

  }

}
