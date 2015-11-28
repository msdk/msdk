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

import com.google.common.collect.Range;
import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.datapointstore.DataPointStoreFactory;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.rawdataimport.mzml.MzMLFileImportMethod;
import java.io.File;
import java.util.List;
import org.junit.Test;
import org.junit.Assert;

public class CropFilterMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @SuppressWarnings("null")
    @Test
    public void testCropFilter() throws MSDKException {

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "orbitrap_300-600mz.mzML");
        Assert.assertTrue("Cannot read test data", inputFile.canRead());
        MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
        RawDataFile rawFile = importer.execute();
        Assert.assertNotNull(rawFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // Create the data needed by the Crop Filter Method
        List<MsScan> scans = rawFile.getScans();
        Range<Float> rtRange = Range.closed(scans.get(50).getChromatographyInfo().getRetentionTime(), scans.get(scans.size() - 30).getChromatographyInfo().getRetentionTime());
        Range<Double> scanRange = scans.get(0).getMzRange();
        Range<Double> mzRange = Range.closed(scanRange.lowerEndpoint()+10, scanRange.upperEndpoint()-10);
        DataPointStore store = DataPointStoreFactory.getMemoryDataStore();

        // Execute the filter
        CropFilterMethod cropFilter = new CropFilterMethod(rawFile, mzRange, rtRange, store);
        RawDataFile newRawFile = cropFilter.execute();

        Assert.assertEquals(1.0, cropFilter.getFinishedPercentage(), 0.0001);
        
        // The result of the method can't be Null
        Assert.assertNotNull(newRawFile);

        List<MsScan> newScans = newRawFile.getScans();

        // Check the new scans are between the new range limits
        for (MsScan newScan : newScans) {
            Assert.assertNotNull(newScan);
            Assert.assertTrue(rtRange.contains(newScan.getChromatographyInfo().getRetentionTime()));
            Assert.assertTrue(mzRange.encloses(newScan.getMzRange()));
        }
     
    }
}
