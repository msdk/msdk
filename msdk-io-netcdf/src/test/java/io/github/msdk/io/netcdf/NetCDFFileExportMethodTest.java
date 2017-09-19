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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import io.github.msdk.util.MsSpectrumUtil;

public class NetCDFFileExportMethodTest {

  private Path getResourcePath(String resource) throws MSDKException {
    final URL url = NetCDFFileExportMethod.class.getClassLoader().getResource(resource);
    try {
      return Paths.get(url.toURI()).toAbsolutePath();
    } catch (URISyntaxException e) {
      throw new MSDKException(e);
    }
  }

  @Test
  public void testWT15() throws MSDKException, IOException {

    float intensityBuffer[];

    // Import the file
    File inputFile = getResourcePath("wt15.CDF").toFile();
    Assert.assertTrue(inputFile.canRead());
    NetCDFFileImportMethod importer = new NetCDFFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // Export the file to a new CDF file
    File tempFile = File.createTempFile("msdk", ".CDF");
    tempFile.deleteOnExit();
    NetCDFFileExportMethod exporter = new NetCDFFileExportMethod(rawFile, tempFile);
    exporter.execute();
    Assert.assertEquals(1.0, exporter.getFinishedPercentage(), 0.0001);

    // Import the new file
    importer = new NetCDFFileImportMethod(tempFile);
    RawDataFile newRawFile = importer.execute();
    Assert.assertNotNull(newRawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // The file has 1278 scans
    List<MsScan> scans = newRawFile.getScans();
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

    newRawFile.dispose();
    rawFile.dispose();

  }

  @Test
  public void test5peptideFT() throws MSDKException, IOException {

    float intensityBuffer[];

    // Import the file
    File inputFile = getResourcePath("5peptideFT.mzML").toFile();
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod parser = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = parser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // Export the file to a new mzML
    File tempFile = File.createTempFile("msdk", ".cdf");
    tempFile.deleteOnExit();
    NetCDFFileExportMethod exporter = new NetCDFFileExportMethod(rawFile, tempFile);
    exporter.execute();
    Assert.assertEquals(1.0, exporter.getFinishedPercentage(), 0.0001);

    // Import the new cdf file
    NetCDFFileImportMethod importer = new NetCDFFileImportMethod(tempFile);
    RawDataFile newRawFile = importer.execute();
    Assert.assertNotNull(newRawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // The file has 7 scans, 2 pass the predicate
    List<MsScan> scans = newRawFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(7, scans.size());

    // 2nd scan, #2
    MsScan scan2 = scans.get(1);
    Assert.assertEquals(Integer.valueOf(2), scan2.getScanNumber());
    Assert.assertEquals(MsSpectrumType.PROFILE, scan2.getSpectrumType());
    Assert.assertEquals(Integer.valueOf(1), scan2.getMsLevel());
    Assert.assertEquals(0.474f, scan2.getRetentionTime(), 0.01f);
    // TODO Should find the right place to store polarity in the cdf file
    // Assert.assertEquals(PolarityType.POSITIVE, scan2.getPolarity());
    Assert.assertEquals(209.1818184554577, scan2.getMzValues()[100], 0.00001);
    scan2.getMzValues();
    intensityBuffer = scan2.getIntensityValues();
    Assert.assertEquals(19800, (int) scan2.getNumberOfDataPoints());
    Float scan2maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan2.getNumberOfDataPoints());
    Assert.assertEquals(1.8E5f, scan2maxInt, 1E4f);

    // 5th scan, #5
    MsScan scan5 = scans.get(4);
    Assert.assertEquals(Integer.valueOf(5), scan5.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan5.getSpectrumType());
    // TODO Should find the right place to store ms level in the cdf file
    // Assert.assertEquals(Integer.valueOf(2), scan5.getMsLevel());
    Assert.assertEquals(2.094f, scan5.getRetentionTime(), 0.01f);
    // TODO Should find the right place to store polarity in the cdf file
    // Assert.assertEquals(PolarityType.POSITIVE, scan5.getPolarity());
    Assert.assertEquals(483.4679870605469, scan5.getMzValues()[200], 0.00001);
    intensityBuffer = scan5.getIntensityValues();
    Assert.assertEquals(837, (int) scan5.getNumberOfDataPoints());
    Float scan5maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan5.getNumberOfDataPoints());
    Assert.assertEquals(8.6E3f, scan5maxInt, 1E2f);

    // Cleanup
    rawFile.dispose();
    newRawFile.dispose();
  }

}
