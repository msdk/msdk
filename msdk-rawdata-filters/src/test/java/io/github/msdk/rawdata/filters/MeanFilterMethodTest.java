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

import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.io.mzml.MzMLFileImportMethod;

public class MeanFilterMethodTest {

  @Test
  public void testMeanFilter() throws Exception {

    // Import the file
    File inputFile =
        new File(this.getClass().getClassLoader().getResource("orbitrap_300-600mz.mzML").toURI());
    Assert.assertTrue("Cannot read test data", inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();

    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // Execute the filter
    MeanFilterAlgorithm meanFilter = new MeanFilterAlgorithm(3.5);
    MSDKFilteringMethod filterMethod = new MSDKFilteringMethod(rawFile, meanFilter);
    RawDataFile newRawFile = filterMethod.execute();

    Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);

    // The result of the method can't be Null
    Assert.assertNotNull(newRawFile);

    List<MsScan> newScans = newRawFile.getScans();

    // Check the new scans are not null
    for (MsScan newScan : newScans) {
      Assert.assertNotNull(newScan);
    }

    // Test windowLength == 0 -> the resulting scan should be the equal to
    // the input scan
    meanFilter = new MeanFilterAlgorithm(0.0);
    filterMethod = new MSDKFilteringMethod(rawFile, meanFilter);
    newRawFile = filterMethod.execute();

    Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);
    Assert.assertNotNull(newRawFile);

    List<MsScan> inputScans = rawFile.getScans();
    newScans = newRawFile.getScans();

    Assert.assertEquals(inputScans.size(), newScans.size(), 0.0001);

    // Check the new scans are not null
    for (int i = 0; i < inputScans.size(); i++) {
      Assert.assertNotNull(newScans.get(i));

      // The resulting scan should be equal to the input scan
      MsScan inputScan = inputScans.get(i);
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
        Assert.assertEquals(mzValues[j], newMzValues[j], 0.00001);
        Assert.assertEquals(intensityValues[j], newIntensityValues[j], 0.00001);
      }

    }

    // Test windowLength == 100000 -> all the dataPoints should have the
    // same intensity
    meanFilter = new MeanFilterAlgorithm(10000.0);
    filterMethod = new MSDKFilteringMethod(rawFile, meanFilter);
    newRawFile = filterMethod.execute();

    Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);
    Assert.assertNotNull(newRawFile);

    inputScans = rawFile.getScans();
    newScans = newRawFile.getScans();

    Assert.assertEquals(inputScans.size(), newScans.size(), 0.0001);
    // Check the new scans are not null
    for (int i = 0; i < inputScans.size(); i++) {
      Assert.assertNotNull(newScans.get(i));
      MsScan newScan = newScans.get(i);
      MsScan inputScan = inputScans.get(i);

      float intensityValues[] = newScan.getIntensityValues();

      float intensityAverage = inputScan.getTIC() / inputScan.getNumberOfDataPoints();

      for (float intValue : intensityValues) {
        Assert.assertEquals(intensityAverage, intValue, 0.0001);
      }
    }
  }
}
