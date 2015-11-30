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

package io.github.msdk.centroiding;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.datapointstore.DataPointStoreFactory;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import io.github.msdk.util.MsSpectrumUtil;

public class ExactMassCentroidingMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @SuppressWarnings("null")
    @Test
    public void testOrbitrap() throws MSDKException {

        // Create the data structures
        DataPointStore dataStore = DataPointStoreFactory.getMemoryDataStore();
        MsSpectrumDataPointList dataPoints = MSDKObjectBuilder
                .getMsSpectrumDataPointList();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "profile_orbitrap.mzML");
        Assert.assertTrue(inputFile.canRead());
        MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
        RawDataFile rawFile = importer.execute();
        Assert.assertNotNull(rawFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);
        List<MsScan> scans = rawFile.getScans();
        Assert.assertNotNull(scans);

        MsScan lastScan = scans.get(scans.size() - 1);

        final float noiseLevel = 1E3f;
        ExactMassCentroidingMethod centroider = new ExactMassCentroidingMethod(
                lastScan, dataStore, noiseLevel);
        final MsScan centroidedScan = centroider.execute();
        Assert.assertEquals(1.0, centroider.getFinishedPercentage(), 0.0001);

        centroidedScan.getDataPoints(dataPoints);
        double mzBuffer[] = dataPoints.getMzBuffer();
        float intensityBuffer[] = dataPoints.getIntensityBuffer();

        Assert.assertTrue(dataPoints.getSize() > 50);

        Integer basePeak = MsSpectrumUtil.getBasePeakIndex(dataPoints);

        Assert.assertEquals(3.537E7f, intensityBuffer[basePeak], 1E5);
        Assert.assertEquals(281.24774060, mzBuffer[basePeak], 0.0000001);

        rawFile.dispose();

    }

}
