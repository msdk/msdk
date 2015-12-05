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
package io.github.msdk.filtering;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.datapointstore.DataPointStoreFactory;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import java.io.File;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class MeanFilterMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @Test
    public void testMeanFilter() throws MSDKException {

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "orbitrap_300-600mz.mzML");
        Assert.assertTrue("Cannot read test data", inputFile.canRead());
        MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
        RawDataFile rawFile = importer.execute();

        Assert.assertNotNull(rawFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // Create the data needed by the Mean Filter Method
        DataPointStore store = DataPointStoreFactory.getMemoryDataStore();

        // Execute the filter
        MeanFilterMethod meanFilter = new MeanFilterMethod(rawFile, 3.5, store);
        RawDataFile newRawFile = meanFilter.execute();

        Assert.assertEquals(1.0, meanFilter.getFinishedPercentage(), 0.0001);

        // The result of the method can't be Null
        Assert.assertNotNull(newRawFile);

        List<MsScan> newScans = newRawFile.getScans();

        // Check the new scans are not null
        for (MsScan newScan : newScans) {
            Assert.assertNotNull(newScan);
        }

    }
}
