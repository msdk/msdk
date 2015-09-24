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

package io.github.msdk.io.rawdataimport;

import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.datapointstore.DataPointStoreFactory;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.rawdataimport.nativeformats.WatersRawImportMethod;
import io.github.msdk.util.MsSpectrumUtil;

public class WatersRawImportMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @SuppressWarnings("null")
    @Test
    public void test20150813() throws Exception {

        // Run this test only on Windows
        assumeTrue(System.getProperty("os.name").startsWith("Windows"));

        // Create the data structures
        DataPointStore dataStore = DataPointStoreFactory
                .getMemoryDataStore();
        MsSpectrumDataPointList dataPoints = MSDKObjectBuilder
                .getMsSpectrumDataPointList();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "20150813-63.raw");
        Assert.assertTrue(inputFile.canRead());
        WatersRawImportMethod importer = new WatersRawImportMethod(inputFile,
                dataStore);
        RawDataFile rawFile = importer.execute();
        Assert.assertNotNull(rawFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // The file has 3185 scans
        List<MsScan> scans = rawFile.getScans();
        Assert.assertNotNull(scans);
        Assert.assertEquals(3185, scans.size());

        // 1st scan, #1
        MsScan scan1 = scans.get(0);
        Assert.assertEquals(new Integer(1), scan1.getScanNumber());
        Assert.assertEquals(MsSpectrumType.CENTROIDED, scan1.getSpectrumType());
        Assert.assertEquals(new Integer(1), scan1.getMsFunction().getMsLevel());
        Assert.assertEquals(0.226f,
                scan1.getChromatographyInfo().getRetentionTime(), 0.01f);
        Assert.assertEquals(PolarityType.POSITIVE, scan1.getPolarity());
        scan1.getDataPoints(dataPoints);
        Assert.assertEquals(248, dataPoints.getSize());
        Float scan1maxInt = MsSpectrumUtil.getMaxIntensity(dataPoints);
        Assert.assertEquals(9.55E5f, scan1maxInt, 1E4f);

        // 3000th scan, #3
        MsScan scan3000 = scans.get(2999);
        Assert.assertEquals(new Integer(3000), scan3000.getScanNumber());
        Assert.assertEquals(MsSpectrumType.CENTROIDED,
                scan3000.getSpectrumType());
        Assert.assertEquals(new Integer(1),
                scan3000.getMsFunction().getMsLevel());
        Assert.assertEquals(636.228f,
                scan3000.getChromatographyInfo().getRetentionTime(), 0.01f);
        Assert.assertEquals(PolarityType.NEGATIVE, scan3000.getPolarity());
        scan3000.getDataPoints(dataPoints);
        Assert.assertEquals(224, dataPoints.getSize());
        Float scan3000maxInt = MsSpectrumUtil.getMaxIntensity(dataPoints);
        Assert.assertEquals(4.23E5f, scan3000maxInt, 1E4f);
    }

}
