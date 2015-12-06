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
import io.github.msdk.filtering.scanfilters.ResampleFilterAlgorithm;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import java.io.File;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

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
        ResampleFilterAlgorithm resampleFilter = new ResampleFilterAlgorithm(10.0, store);
        MSDKFilteringMethod filterMethod = new MSDKFilteringMethod(rawFile, resampleFilter, store);
        RawDataFile newRawFile = filterMethod.execute();
        // The result of the method can't be Null
        Assert.assertNotNull(newRawFile);
        Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);

        List<MsScan> newScans = newRawFile.getScans();
        List<MsScan> scans = rawFile.getScans();
        // Check the new scans are between the new range limits
        for (MsScan newScan : newScans) {
            Assert.assertNotNull(newScan);
        }

        // Execute the filter with a big bin size
        resampleFilter = new ResampleFilterAlgorithm(10000000.0, store);
        filterMethod = new MSDKFilteringMethod(rawFile, resampleFilter, store);
        newRawFile = filterMethod.execute();
        // The result of the method can't be Null
        Assert.assertNotNull(newRawFile);
        Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);

        newScans = newRawFile.getScans();
        MsSpectrumDataPointList dataPoints = MSDKObjectBuilder.getMsSpectrumDataPointList();
        MsSpectrumDataPointList newDataPoints = MSDKObjectBuilder.getMsSpectrumDataPointList();
               
        // Check the new scans are between the new range limits
        for (int i = 0; i < newScans.size(); i++) {
            dataPoints.clear();
            newDataPoints.clear();
            
            MsScan newScan = newScans.get(i);
            MsScan scan = scans.get(i);
            
            Assert.assertNotNull(newScan);
            
            scan.getDataPoints(dataPoints);
            newScan.getDataPoints(newDataPoints);
            Assert.assertEquals(1, newDataPoints.getSize(), 0.001);

            float intensityAverage = dataPoints.getTIC() / dataPoints.getSize();
            float intensityValues[] = newDataPoints.getIntensityBuffer();
            
            Assert.assertEquals(intensityValues[0], intensityAverage, 0.0001);
        }

        // Execute the filter with a zero bin size
        resampleFilter = new ResampleFilterAlgorithm(0.0, store);
        filterMethod = new MSDKFilteringMethod(rawFile, resampleFilter, store);
        newRawFile = filterMethod.execute();
        // The result of the method can't be Null
        Assert.assertNotNull(newRawFile);
        Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);
        newScans = newRawFile.getScans();

        Assert.assertEquals(scans.size(), newScans.size(), 0.0001);

        // The resulting data points should be exactly the same as the input
        for (int i = 0; i < scans.size(); i++) {
            dataPoints.clear();
            newDataPoints.clear();
            
            MsScan newScan = newScans.get(i);
            MsScan scan = scans.get(i);
            Assert.assertNotNull(newScan);
            scan.getDataPoints(dataPoints);
            newScan.getDataPoints(newDataPoints);

            float newIntensityValues[] = newDataPoints.getIntensityBuffer();
            float intensityValues[] = newDataPoints.getIntensityBuffer();

            for (int j = 0; j < newDataPoints.getSize(); j++) {
                Assert.assertEquals(intensityValues[j], newIntensityValues[j], 0.0001);
            }
        }

        // Execute the filter with a negative value bin size
        resampleFilter = new ResampleFilterAlgorithm(-1000.0, store);
        filterMethod = new MSDKFilteringMethod(rawFile, resampleFilter, store);
        newRawFile = filterMethod.execute();
        // The result of the method can't be Null
        Assert.assertNotNull(newRawFile);
        Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);
        newScans = newRawFile.getScans();

        scans = rawFile.getScans();

        Assert.assertEquals(scans.size(), newScans.size(), 0.0001);

        // The resulting data points should be exactly the same as the input
        for (int i = 0; i < scans.size(); i++) {
            dataPoints.clear();
            newDataPoints.clear();
            
            MsScan newScan = newScans.get(i);
            MsScan scan = scans.get(i);
            Assert.assertNotNull(newScan);
            scan.getDataPoints(dataPoints);
            newScan.getDataPoints(newDataPoints);

            float newIntensityValues[] = newDataPoints.getIntensityBuffer();
            float intensityValues[] = newDataPoints.getIntensityBuffer();

            for (int j = 0; j < newDataPoints.getSize(); j++) {
                Assert.assertEquals(intensityValues[j], newIntensityValues[j], 0.0001);
            }
        }
    }
}
