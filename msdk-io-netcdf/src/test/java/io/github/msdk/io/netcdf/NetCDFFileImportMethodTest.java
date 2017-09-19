/*
 * (C) Copyright 2015-2017 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1 as published by the Free
 * Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation.
 */

package io.github.msdk.io.netcdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.util.MsSpectrumUtil;

public class NetCDFFileImportMethodTest {

  private static final String TEST_DATA_PATH = "src/test/resources/";


  @Test
  public void testWT15() throws MSDKException, IOException {

    float intensityBuffer[];

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "wt15.CDF");
    Assert.assertTrue(inputFile.canRead());
    NetCDFFileImportMethod importer = new NetCDFFileImportMethod(inputFile);
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
    Assert.assertEquals(new Integer(1), scan3.getMsLevel());
    Assert.assertEquals(2504.508f, scan3.getRetentionTime(), 0.01f);
    scan3.getMzValues();
    intensityBuffer = scan3.getIntensityValues();
    Assert.assertEquals(420, (int) scan3.getNumberOfDataPoints());
    Float scan3maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan3.getNumberOfDataPoints());
    Assert.assertEquals(4.5E4f, scan3maxInt, 1E3f);

    // 1278th scan, #1278
    MsScan scan1278 = scans.get(1277);
    Assert.assertEquals(new Integer(1278), scan1278.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan1278.getSpectrumType());
    Assert.assertEquals(new Integer(1), scan1278.getMsLevel());
    Assert.assertEquals(4499.826f, scan1278.getRetentionTime(), 0.01f);
    scan1278.getMzValues();
    intensityBuffer = scan1278.getIntensityValues();
    Assert.assertEquals(61, (int) scan1278.getNumberOfDataPoints());
    Float scan1278maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan1278.getNumberOfDataPoints());
    Assert.assertEquals(4.0E3f, scan1278maxInt, 1E2f);

    rawFile.dispose();

  }

  @Test
  public void testWT15Random() throws MSDKException, IOException {

    // Set up Predicate<MsScan>
    List<Integer> scansToParse = new ArrayList<>();
    scansToParse.addAll(Arrays.asList(3, 1278));
    Predicate<MsScan> msScanPredicate = getMsScanPredicate(scansToParse);

    float intensityBuffer[];

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "wt15.CDF");
    Assert.assertTrue(inputFile.canRead());
    NetCDFFileImportMethod importer = new NetCDFFileImportMethod(inputFile, msScanPredicate);
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
    Assert.assertEquals(new Integer(1), scan3.getMsLevel());
    Assert.assertEquals(2504.508f, scan3.getRetentionTime(), 0.01f);
    scan3.getMzValues();
    intensityBuffer = scan3.getIntensityValues();
    Assert.assertEquals(420, (int) scan3.getNumberOfDataPoints());
    Float scan3maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan3.getNumberOfDataPoints());
    Assert.assertEquals(4.5E4f, scan3maxInt, 1E3f);

    // 1278th scan, #1278
    MsScan scan1278 = scans.get(1277);
    Assert.assertEquals(new Integer(1278), scan1278.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan1278.getSpectrumType());
    Assert.assertEquals(new Integer(1), scan1278.getMsLevel());
    Assert.assertEquals(4499.826f, scan1278.getRetentionTime(), 0.01f);
    scan1278.getMzValues();
    intensityBuffer = scan1278.getIntensityValues();
    Assert.assertEquals(61, (int) scan1278.getNumberOfDataPoints());
    Float scan1278maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan1278.getNumberOfDataPoints());
    Assert.assertEquals(4.0E3f, scan1278maxInt, 1E2f);

    rawFile.dispose();

  }

  @Test
  public void testWT15UnparsedScan() throws MSDKException, IOException {

    float intensityBuffer[];

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "wt15.CDF");
    Assert.assertTrue(inputFile.canRead());
    NetCDFFileImportMethod importer = new NetCDFFileImportMethod(inputFile, s -> false);
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
    Assert.assertEquals(new Integer(1), scan3.getMsLevel());
    Assert.assertEquals(2504.508f, scan3.getRetentionTime(), 0.01f);
    scan3.getMzValues();
    intensityBuffer = scan3.getIntensityValues();
    Assert.assertEquals(420, (int) scan3.getNumberOfDataPoints());
    Float scan3maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan3.getNumberOfDataPoints());
    Assert.assertEquals(4.5E4f, scan3maxInt, 1E3f);

    rawFile.dispose();

  }

  private Predicate<MsScan> getMsScanPredicate(List<Integer> scansToParse) {
    return s -> scansToParse.contains(s.getScanNumber());
  }

}
