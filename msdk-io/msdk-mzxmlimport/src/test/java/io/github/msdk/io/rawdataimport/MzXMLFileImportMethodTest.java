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
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.rawdataimport.mzxml.MzXMLFileImportMethod;
import io.github.msdk.util.MsSpectrumUtil;

public class MzXMLFileImportMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @Test
    public void testA10A2() throws MSDKException {

        // Create the data structures
        MsSpectrumDataPointList dataPoints = MSDKObjectBuilder
                .getMsSpectrumDataPointList();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "A1-0_A2.mzXML");
        Assert.assertTrue(inputFile.canRead());
        MzXMLFileImportMethod importer = new MzXMLFileImportMethod(inputFile);
        RawDataFile rawFile = importer.execute();
        Assert.assertNotNull(rawFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // The file has 1 scan
        List<MsScan> scans = rawFile.getScans();
        Assert.assertNotNull(scans);
        Assert.assertEquals(1, scans.size());

        // 1st scan, #1
        MsScan scan1 = scans.get(0);
        Assert.assertEquals(new Integer(1), scan1.getScanNumber());
        Assert.assertEquals(new Integer(1), scan1.getMsFunction().getMsLevel());
        Assert.assertEquals(PolarityType.POSITIVE, scan1.getPolarity());
        scan1.getDataPoints(dataPoints);
        Assert.assertEquals(22431, dataPoints.getSize());
        Float scan1MaxInt = MsSpectrumUtil.getMaxIntensity(dataPoints);
        Assert.assertEquals(3E4f, scan1MaxInt, 1E3f);

        rawFile.dispose();

    }

    @SuppressWarnings("null")
    @Test
    public void testR1RG59B41() throws MSDKException {

        // Create the data structures
        MsSpectrumDataPointList dataPoints = MSDKObjectBuilder
                .getMsSpectrumDataPointList();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "R1_RG59_B4_1.mzXML");
        Assert.assertTrue(inputFile.canRead());
        MzXMLFileImportMethod importer = new MzXMLFileImportMethod(inputFile);
        RawDataFile rawFile = importer.execute();
        Assert.assertNotNull(rawFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // The file has 301 scans
        List<MsScan> scans = rawFile.getScans();
        Assert.assertNotNull(scans);
        Assert.assertEquals(301, scans.size());

        // 1st scan, #1000
        MsScan scan1 = scans.get(0);
        Assert.assertEquals(new Integer(1000), scan1.getScanNumber());
        Assert.assertEquals(new Integer(2), scan1.getMsFunction().getMsLevel());
        Assert.assertEquals(1596.72f,
                scan1.getChromatographyInfo().getRetentionTime(), 0.01f);
        Assert.assertEquals(PolarityType.POSITIVE, scan1.getPolarity());
        scan1.getDataPoints(dataPoints);
        Assert.assertEquals(210, dataPoints.getSize());
        Float scan1MaxInt = MsSpectrumUtil.getMaxIntensity(dataPoints);
        Assert.assertEquals(5.68E2f, scan1MaxInt, 1E1f);

        // 300th scan, #1299
        MsScan scan299 = scans.get(299);
        Assert.assertEquals(new Integer(1299), scan299.getScanNumber());
        Assert.assertEquals(new Integer(1),
                scan299.getMsFunction().getMsLevel());
        Assert.assertEquals(1765.578f,
                scan299.getChromatographyInfo().getRetentionTime(), 0.01f);
        Assert.assertEquals(PolarityType.POSITIVE, scan299.getPolarity());
        scan299.getDataPoints(dataPoints);
        Assert.assertEquals(1069, dataPoints.getSize());
        Float scan299MaxInt = MsSpectrumUtil.getMaxIntensity(dataPoints);
        Assert.assertEquals(1.24E6f, scan299MaxInt, 1E5f);

        rawFile.dispose();

    }

}
