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
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.filtering.scanfilters.SGFilterAlgorithm;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import java.io.File;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

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
        MsSpectrumDataPointList dataPointList = MSDKObjectBuilder.getMsSpectrumDataPointList();
            
        for (MsScan newScan : newScans) {
            Assert.assertNotNull(newScan);
            dataPointList.clear();
            newScan.getDataPoints(dataPointList);
            Assert.assertTrue(dataPointList.getSize() > 0);
           
        }
        
        // Execute the filter with wrong parameters
        SGFilter = new SGFilterAlgorithm(110, store);
        filterMethod = new MSDKFilteringMethod(rawFile, SGFilter, store);
        newRawFile = filterMethod.execute();
        // The result of the method can't be Null
        Assert.assertNotNull(newRawFile);
        Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);
        
        newScans = newRawFile.getScans();
            
        for (MsScan newScan : newScans) {
            Assert.assertNull(newScan);           
        }
        
        
    }

}
