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

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.datapointstore.DataPointStoreFactory;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.rawdataimport.netcdf.NetCDFFileImportMethod;
import io.github.msdk.util.MsSpectrumUtil;

public class NetCDFFileImportMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @SuppressWarnings("null")
    @Test
    public void testWT15() throws MSDKException {

        // Create the data structures
        DataPointStore dataStore = DataPointStoreFactory
                .getTmpFileDataPointStore();
        MsSpectrumDataPointList dataPoints = MSDKObjectBuilder
                .getMsSpectrumDataPointList();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "wt15.CDF");
        Assert.assertTrue(inputFile.canRead());
        NetCDFFileImportMethod importer = new NetCDFFileImportMethod(inputFile,
                dataStore);
        RawDataFile rawFile = importer.execute();
        Assert.assertNotNull(rawFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // The file has 1278 scans
        List<MsScan> scans = rawFile.getScans();
        Assert.assertNotNull(scans);
        Assert.assertEquals(1278, scans.size());

        // 3rd scan, #3
        MsScan scan3 = scans.get(2);
        Assert.assertEquals(new Integer(3), scan3.getScanNumber());
        Assert.assertEquals(MsSpectrumType.CENTROIDED, scan3.getSpectrumType());
        Assert.assertEquals(new Integer(1), scan3.getMsFunction().getMsLevel());
        Assert.assertEquals(2504.508f,
                scan3.getChromatographyInfo().getRetentionTime(), 0.01f);
        scan3.getDataPoints(dataPoints);
        Assert.assertEquals(420, dataPoints.getSize());
        Float scan3maxInt = MsSpectrumUtil.getMaxIntensity(dataPoints);
        Assert.assertEquals(4.5E4f, scan3maxInt, 1E3f);

        // 1278th scan, #1278
        MsScan scan1278 = scans.get(1277);
        Assert.assertEquals(new Integer(1278), scan1278.getScanNumber());
        Assert.assertEquals(MsSpectrumType.CENTROIDED,
                scan1278.getSpectrumType());
        Assert.assertEquals(new Integer(1),
                scan1278.getMsFunction().getMsLevel());
        Assert.assertEquals(4499.826f,
                scan1278.getChromatographyInfo().getRetentionTime(), 0.01f);
        scan1278.getDataPoints(dataPoints);
        Assert.assertEquals(61, dataPoints.getSize());
        Float scan1278maxInt = MsSpectrumUtil.getMaxIntensity(dataPoints);
        Assert.assertEquals(4.0E3f, scan1278maxInt, 1E2f);

        rawFile.dispose();

    }

}
