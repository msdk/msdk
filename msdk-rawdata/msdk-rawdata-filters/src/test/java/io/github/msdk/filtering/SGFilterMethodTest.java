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
import io.github.msdk.datamodel.msspectra.MsIon;
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
        
        MsScan scan = MSDKObjectBuilder.getMsScan(store, 1, MSDKObjectBuilder.getMsFunction(1));
        dataPointList.clear();
        dataPointList.add(400, 0);
        dataPointList.add(405, 100);
        dataPointList.add(410, 200);
        dataPointList.add(415, 300);
        dataPointList.add(420, 400);
        dataPointList.add(425, 500);
        dataPointList.add(430, 600);
        dataPointList.add(435, 700);
        dataPointList.add(445, 800);
        dataPointList.add(445, 900);
        dataPointList.add(450, 0);
        dataPointList.add(455, 100);
        dataPointList.add(460, 200);
        dataPointList.add(465, 300);
        dataPointList.add(470, 400);
        dataPointList.add(475, 500);
        dataPointList.add(480, 600);
        dataPointList.add(485, 700);
        dataPointList.add(490, 800);
        dataPointList.add(495, 900);
        scan.setDataPoints(dataPointList);
        // Execute the filter
        MsScan newScan = SGFilter.performFilter(scan);
        
        dataPointList.clear();
        newScan.setDataPoints(dataPointList);
        
        System.out.println(dataPointList.getSize());
        for(MsIon ion: dataPointList){
            System.out.println(ion.getMz() + " - " +ion.getIntensity());
        }
    }

}
