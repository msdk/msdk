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

import com.google.common.collect.Range;

import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.io.mzml.MzMLFileImportMethod;

public class CropFilterMethodTest {

  @Test
  public void testCropFilter() throws Exception {

    // Import the file
    File inputFile =
        new File(this.getClass().getClassLoader().getResource("orbitrap_300-600mz.mzML").toURI());
    Assert.assertTrue("Cannot read test data", inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // Create the data needed by the Crop Filter Method
    List<MsScan> scans = rawFile.getScans();
    Range<Float> rtRange = Range.closed(scans.get(50).getRetentionTime(),
        scans.get(scans.size() - 30).getRetentionTime());
    Range<Double> scanRange = scans.get(0).getMzRange();
    Range<Double> mzRange =
        Range.closed(scanRange.lowerEndpoint() + 10, scanRange.upperEndpoint() - 10);

    // Execute the filter
    CropFilterAlgorithm cropFilter = new CropFilterAlgorithm(mzRange, rtRange);
    MSDKFilteringMethod filterMethod = new MSDKFilteringMethod(rawFile, cropFilter);
    RawDataFile newRawFile = filterMethod.execute();
    // The result of the method can't be Null
    Assert.assertNotNull(newRawFile);
    Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);

    List<MsScan> newScans = newRawFile.getScans();

    // Check the new scans are between the new range limits
    for (MsScan newScan : newScans) {
      Assert.assertNotNull(newScan);
      Assert.assertTrue(rtRange.contains(newScan.getRetentionTime()));
      Assert.assertTrue(mzRange.encloses(newScan.getMzRange()));
    }

  }


  @Test
  public void testNoMatch() throws Exception {

    // Import the file
    File inputFile =
        new File(this.getClass().getClassLoader().getResource("orbitrap_300-600mz.mzML").toURI());
    Assert.assertTrue("Cannot read test data", inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // Create the data needed by the Crop Filter Method
    Range<Float> rtRange = Range.all();
    Range<Double> mzRange = Range.closed(1000.0, 2000.0);

    // Execute the filter
    CropFilterAlgorithm cropFilter = new CropFilterAlgorithm(mzRange, rtRange);
    MSDKFilteringMethod filterMethod = new MSDKFilteringMethod(rawFile, cropFilter);
    RawDataFile newRawFile = filterMethod.execute();
    // The result of the method can't be Null
    Assert.assertNotNull(newRawFile);
    Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);

    // The result of the method can't be Null
    Assert.assertNotNull(newRawFile);

    List<MsScan> newScans = newRawFile.getScans();

    // The result should have all 209 scans
    Assert.assertEquals(209, newScans.size());

    for (MsScan scan : newScans) {

      // All scans should have zero data points
      Assert.assertEquals(new Integer(0), scan.getNumberOfDataPoints());

    }

  }
}
