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
package io.github.msdk.filtering.scanfilters;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.datapointstore.DataPointStoreFactory;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsIon;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import java.io.File;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class MeanFilterAlgorithmTest {

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
        List<MsScan> scans = rawFile.getScans();
        DataPointStore store = DataPointStoreFactory.getMemoryDataStore();

        // Testing the filter with normal values
        for (MsScan scan : scans) {
            MeanFilterAlgorithm meanFilter = new MeanFilterAlgorithm(scan, 0.5, store);
            MsScan newScan = meanFilter.execute();
                       // The result of the method can't be Null
            Assert.assertNotNull(newScan);
        }

        // Test windowLength == 0 -> the resulting scan should be the equal to the input scan
        MsScan scanTest = scans.get(0);
        MeanFilterAlgorithm meanFilter = new MeanFilterAlgorithm(scanTest, 0, store);
        MsScan newScan = meanFilter.execute();
      
        // The result of the method can't be Null
        Assert.assertNotNull(newScan);

        // The resulting scan should be equal to the input scan
        MsSpectrumDataPointList dataPoints = MSDKObjectBuilder.getMsSpectrumDataPointList();
        scanTest.getDataPoints(dataPoints);

        // Get the mz and intensities values from the input scan
        double mzValues[] = dataPoints.getMzBuffer();
        float intensityValues[] = dataPoints.getIntensityBuffer();

        MsSpectrumDataPointList newDataPoints = MSDKObjectBuilder.getMsSpectrumDataPointList();
        newScan.getDataPoints(newDataPoints);

        // Get the mz and intensities values from the filtered scan
        double newMzValues[] = newDataPoints.getMzBuffer();
        float newIntensityValues[] = newDataPoints.getIntensityBuffer();

        // They should contain the same number of data points
        Assert.assertEquals(dataPoints.getSize(), newDataPoints.getSize(), 0.0001);

        for (int i = 0; i < newDataPoints.getSize(); i++) {
            Assert.assertEquals(mzValues[i], newMzValues[i], 0.0001);
            Assert.assertEquals(intensityValues[i], newIntensityValues[i], 0.0001);
        }

        // Test windowLength == 100000 -> all the dataPoints should have the same intensity
        meanFilter = new MeanFilterAlgorithm(scanTest, 100000, store);
        newScan = meanFilter.execute();
        newDataPoints.clear();
        newScan.getDataPoints(newDataPoints);
        float intensityAverage = scanTest.getTIC()/dataPoints.getSize();

        for (MsIon ion : newDataPoints) {
            Assert.assertEquals(ion.getIntensity(), intensityAverage, 0.001);
        }

    }
}
