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
import java.util.ArrayList;
import java.util.Iterator;
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
        List<MsScan> scans = rawFile.getScans();
        DataPointStore store = DataPointStoreFactory.getMemoryDataStore();

        // Testing the filter with normal values
        for (MsScan scan : scans) {
            MeanFilterMethod meanFilter = new MeanFilterMethod(scan, 0.5, store);
            MsScan newScan = meanFilter.execute();
            Assert.assertEquals(1.0, meanFilter.getFinishedPercentage(), 0.0001);

            // The result of the method can't be Null
            Assert.assertNotNull(newScan);
        }

        // Test windowLength == 0 -> the resulting scan should be the equal to the input scan
        MsScan scanTest = scans.get(0);
        MeanFilterMethod meanFilter = new MeanFilterMethod(scanTest, 0, store);
        MsScan newScan = meanFilter.execute();
        Assert.assertEquals(1.0, meanFilter.getFinishedPercentage(), 0.0001);

        // The result of the method can't be Null
        Assert.assertNotNull(newScan);

        // The resultint scan should be equal to the input scan
        MsSpectrumDataPointList dataPoints = MSDKObjectBuilder.getMsSpectrumDataPointList();
        scanTest.getDataPoints(dataPoints);
        List<Ion> ions = new ArrayList();
        List<Ion> newIons = new ArrayList();
        float intensityAverage = 0;

        for (MsIon ion : dataPoints) {
            ions.add(new Ion(ion.getMz(), ion.getIntensity()));
            intensityAverage += ion.getIntensity();
        }
        
        intensityAverage/=ions.size();

        newScan.getDataPoints(dataPoints);
        for (MsIon ion : dataPoints) {
            newIons.add(new Ion(ion.getMz(), ion.getIntensity()));
        }

        Assert.assertEquals(ions.size(), newIons.size());

        for (int i = 0; i < newIons.size(); i++) {
            Assert.assertEquals(ions.get(i).mz(), newIons.get(i).mz(), 0.0001);
            Assert.assertEquals(ions.get(i).intensity(), newIons.get(i).intensity(), 0.0001);
        }

        // Test windowLength == 100000 -> all the dataPoints should have the same intensity
        meanFilter = new MeanFilterMethod(scanTest, 100000, store);
        newScan = meanFilter.execute();
        Assert.assertEquals(1.0, meanFilter.getFinishedPercentage(), 0.0001);

        newScan.getDataPoints(dataPoints);
        for (MsIon ion : dataPoints) {       
            Assert.assertEquals(ion.getIntensity(), intensityAverage, 0.0001);
        }

    }
}

class Ion {

    private final Double mz;
    private final Float intensity;

    public Ion(Double mz, Float intensity) {
        this.mz = mz;
        this.intensity = intensity;
    }

    public Double mz() {
        return mz;
    }

    public Float intensity() {
        return intensity;
    }
}
