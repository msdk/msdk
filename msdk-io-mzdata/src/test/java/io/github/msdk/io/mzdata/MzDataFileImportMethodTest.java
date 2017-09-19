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

package io.github.msdk.io.mzdata;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.PolarityType;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.util.MsSpectrumUtil;

public class MzDataFileImportMethodTest {

  @Test
  public void testMzDataFile() throws Exception {

    // Import the file
    File inputFile = new File(this.getClass().getClassLoader().getResource("test.mzData").toURI());
    Assert.assertTrue(inputFile.canRead());
    MzDataFileImportMethod importer = new MzDataFileImportMethod(inputFile);
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
    Assert.assertEquals(new Integer(1), scan1.getMsLevel());
    Float rt = scan1.getRetentionTime();
    Assert.assertNull(rt);
    Assert.assertEquals(PolarityType.UNKNOWN, scan1.getPolarity());
    Assert.assertEquals(0, (int) scan1.getNumberOfDataPoints());

    rawFile.dispose();

  }


  @Test
  public void testMM14() throws Exception {

    // Create the data structures
    double mzBuffer[] = new double[10000];
    float intensityBuffer[] = new float[10000];

    // Import the file
    File inputFile = new File(this.getClass().getClassLoader().getResource("MM14.mzdata").toURI());
    Assert.assertTrue(inputFile.canRead());
    MzDataFileImportMethod importer = new MzDataFileImportMethod(inputFile);
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
    Assert.assertEquals(new Integer(1), scan1.getMsLevel());
    Assert.assertEquals(270.336f, scan1.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.POSITIVE, scan1.getPolarity());
    mzBuffer = scan1.getMzValues();
    intensityBuffer = scan1.getIntensityValues();
    Assert.assertEquals(1378, (int) scan1.getNumberOfDataPoints());
    Float scan1MaxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan1.getNumberOfDataPoints());
    Assert.assertEquals(1.7E3f, scan1MaxInt, 1E2f);

    // 100th scan, #100
    MsScan scan100 = scans.get(99);
    Assert.assertEquals(new Integer(100), scan100.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan100.getSpectrumType());
    Assert.assertEquals(new Integer(1), scan100.getMsLevel());
    Assert.assertEquals(303.642f, scan100.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.POSITIVE, scan100.getPolarity());
    mzBuffer = scan100.getMzValues();
    intensityBuffer = scan100.getIntensityValues();
    Assert.assertEquals(1375, (int) scan100.getNumberOfDataPoints());
    Float scan100MaxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan100.getNumberOfDataPoints());
    Assert.assertEquals(7.95E2f, scan100MaxInt, 1E1f);

    rawFile.dispose();

  }


  @Test
  public void testMSMSposChallenge0() throws Exception {

    // Create the data structures
    double mzBuffer[] = new double[10000];
    float intensityBuffer[] = new float[10000];

    // Import the file
    File inputFile = new File(this.getClass().getClassLoader().getResource("MSMSpos_Challenge0.mzData").toURI());
    Assert.assertTrue(inputFile.canRead());
    MzDataFileImportMethod importer = new MzDataFileImportMethod(inputFile);
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
    Assert.assertEquals(new Integer(2), scan1.getMsLevel());
    Assert.assertEquals(309.350f, scan1.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.POSITIVE, scan1.getPolarity());
    mzBuffer = scan1.getMzValues();
    intensityBuffer = scan1.getIntensityValues();
    Assert.assertEquals(41, (int) scan1.getNumberOfDataPoints());
    Float scan1MaxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan1.getNumberOfDataPoints());
    Assert.assertEquals(2.36E3f, scan1MaxInt, 1E2f);

    rawFile.dispose();

  }

}
