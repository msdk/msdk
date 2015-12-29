/* 
 * (C) Copyright 2015 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
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

public class ResampleFilterMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @SuppressWarnings("null")
    @Test
    public void testResampleFilter() throws MSDKException {

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "orbitrap_300-600mz.mzML");
        Assert.assertTrue("Cannot read test data", inputFile.canRead());
        MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
        RawDataFile rawFile = importer.execute();
        Assert.assertNotNull(rawFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // Create the data needed by the Crop Filter Method
        DataPointStore store = DataPointStoreFactory.getMemoryDataStore();

        // Execute the filter
        ResampleFilterAlgorithm resampleFilter = new ResampleFilterAlgorithm(
                10.0, store);
        MSDKFilteringMethod filterMethod = new MSDKFilteringMethod(rawFile,
                resampleFilter, store);
        RawDataFile newRawFile = filterMethod.execute();
        // The result of the method can't be Null
        Assert.assertNotNull(newRawFile);
        Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);

        List<MsScan> newScans = newRawFile.getScans();

        // Check the new scans are between the new range limits
        for (MsScan newScan : newScans) {
            Assert.assertNotNull(newScan);
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroBinSize() throws MSDKException {
        new ResampleFilterAlgorithm(0.0,
                DataPointStoreFactory.getTmpFileDataStore());
    }
}
