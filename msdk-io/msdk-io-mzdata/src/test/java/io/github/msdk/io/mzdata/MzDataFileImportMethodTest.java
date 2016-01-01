/* 
 * (C) Copyright 2015-2016 by MSDK Development Team
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

package io.github.msdk.io.mzdata;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzdata.MzDataFileImportMethod;
import io.github.msdk.util.MsSpectrumUtil;

public class MzDataFileImportMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @SuppressWarnings("null")
    @Test
    public void testMzDataFile() throws MSDKException {

        // Create the data structures
        DataPointStore dataStore = DataPointStoreFactory.getMemoryDataStore();
        double mzBuffer[] = new double[10000];
        float intensityBuffer[] = new float[10000];

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "test.mzData");
        Assert.assertTrue(inputFile.canRead());
        MzDataFileImportMethod importer = new MzDataFileImportMethod(inputFile,
                dataStore);
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
        ChromatographyInfo rt = scan1.getChromatographyInfo();
        Assert.assertNull(rt);
        Assert.assertEquals(PolarityType.UNKNOWN, scan1.getPolarity());
        mzBuffer = scan1.getMzValues(mzBuffer);
        intensityBuffer = scan1.getIntensityValues(intensityBuffer);
        Assert.assertEquals(0, (int) scan1.getNumberOfDataPoints());

        rawFile.dispose();

    }

    @SuppressWarnings("null")
    @Test
    public void testMM14() throws MSDKException {

        // Create the data structures
        DataPointStore dataStore = DataPointStoreFactory.getMemoryDataStore();
        double mzBuffer[] = new double[10000];
        float intensityBuffer[] = new float[10000];

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "MM14.mzdata");
        Assert.assertTrue(inputFile.canRead());
        MzDataFileImportMethod importer = new MzDataFileImportMethod(inputFile,
                dataStore);
        RawDataFile rawFile = importer.execute();
        Assert.assertNotNull(rawFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // The file has 112 scans
        List<MsScan> scans = rawFile.getScans();
        Assert.assertNotNull(scans);
        Assert.assertEquals(112, scans.size());

        // 1st scan, #1
        MsScan scan1 = scans.get(0);
        Assert.assertEquals(new Integer(1), scan1.getScanNumber());
        Assert.assertEquals(MsSpectrumType.CENTROIDED, scan1.getSpectrumType());
        Assert.assertEquals(new Integer(1), scan1.getMsFunction().getMsLevel());
        ChromatographyInfo rt = scan1.getChromatographyInfo();
        Assert.assertNotNull(rt);
        Assert.assertEquals(270.336f, rt.getRetentionTime(), 0.01f);
        Assert.assertEquals(PolarityType.POSITIVE, scan1.getPolarity());
        mzBuffer = scan1.getMzValues(mzBuffer);
        intensityBuffer = scan1.getIntensityValues(intensityBuffer);
        Assert.assertEquals(1378, (int) scan1.getNumberOfDataPoints());
        Float scan1MaxInt = MsSpectrumUtil.getMaxIntensity(intensityBuffer,
                scan1.getNumberOfDataPoints());
        Assert.assertEquals(1.7E3f, scan1MaxInt, 1E2f);

        // 100th scan, #100
        MsScan scan100 = scans.get(99);
        Assert.assertEquals(new Integer(100), scan100.getScanNumber());
        Assert.assertEquals(MsSpectrumType.CENTROIDED,
                scan100.getSpectrumType());
        Assert.assertEquals(new Integer(1),
                scan100.getMsFunction().getMsLevel());
        rt = scan100.getChromatographyInfo();
        Assert.assertNotNull(rt);
        Assert.assertEquals(303.642f, rt.getRetentionTime(), 0.01f);
        Assert.assertEquals(PolarityType.POSITIVE, scan100.getPolarity());
        mzBuffer = scan100.getMzValues(mzBuffer);
        intensityBuffer = scan100.getIntensityValues(intensityBuffer);
        Assert.assertEquals(1375, (int) scan100.getNumberOfDataPoints());
        Float scan100MaxInt = MsSpectrumUtil.getMaxIntensity(intensityBuffer,
                scan100.getNumberOfDataPoints());
        Assert.assertEquals(7.95E2f, scan100MaxInt, 1E1f);

        rawFile.dispose();

    }

    @SuppressWarnings("null")
    @Test
    public void testMSMSposChallenge0() throws MSDKException {

        // Create the data structures
        DataPointStore dataStore = DataPointStoreFactory.getMemoryDataStore();
        double mzBuffer[] = new double[10000];
        float intensityBuffer[] = new float[10000];

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "MSMSpos_Challenge0.mzData");
        Assert.assertTrue(inputFile.canRead());
        MzDataFileImportMethod importer = new MzDataFileImportMethod(inputFile,
                dataStore);
        RawDataFile rawFile = importer.execute();
        Assert.assertNotNull(rawFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // The file has 112 scans
        List<MsScan> scans = rawFile.getScans();
        Assert.assertNotNull(scans);
        Assert.assertEquals(104, scans.size());

        // 1st scan, #918
        MsScan scan1 = scans.get(0);
        Assert.assertEquals(new Integer(918), scan1.getScanNumber());
        Assert.assertEquals(MsSpectrumType.CENTROIDED, scan1.getSpectrumType());
        Assert.assertEquals(new Integer(2), scan1.getMsFunction().getMsLevel());
        ChromatographyInfo rt = scan1.getChromatographyInfo();
        Assert.assertNotNull(rt);
        Assert.assertEquals(309.350f, rt.getRetentionTime(), 0.01f);
        Assert.assertEquals(PolarityType.POSITIVE, scan1.getPolarity());
        mzBuffer = scan1.getMzValues(mzBuffer);
        intensityBuffer = scan1.getIntensityValues(intensityBuffer);
        Assert.assertEquals(41, (int) scan1.getNumberOfDataPoints());
        Float scan1MaxInt = MsSpectrumUtil.getMaxIntensity(intensityBuffer,
                scan1.getNumberOfDataPoints());
        Assert.assertEquals(2.36E3f, scan1MaxInt, 1E2f);

        rawFile.dispose();

    }

}
