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

package io.github.msdk.io.mzml;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.chromatograms.ChromatogramType;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.ActivationType;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.util.MsSpectrumUtil;

public class MzMLFileImportMethodTest {

  private static final String TEST_DATA_PATH = "src/test/resources/";


  @Test
  public void test5peptideFT() throws MSDKException {

    // Create the data structures
    double mzBuffer[] = new double[10000];
    float intensityBuffer[] = new float[10000];

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "5peptideFT.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // The file has 7 scans
    List<MsScan> scans = rawFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(7, scans.size());

    // 2nd scan, #2
    MsScan scan2 = scans.get(1);
    Assert.assertEquals(new Integer(2), scan2.getScanNumber());
    Assert.assertEquals(MsSpectrumType.PROFILE, scan2.getSpectrumType());
    Assert.assertEquals(new Integer(1), scan2.getMsFunction().getMsLevel());
    Assert.assertEquals(0.474f, scan2.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.POSITIVE, scan2.getPolarity());
    mzBuffer = scan2.getMzValues();
    intensityBuffer = scan2.getIntensityValues();
    Assert.assertEquals(19800, (int) scan2.getNumberOfDataPoints());
    Float scan2maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan2.getNumberOfDataPoints());
    Assert.assertEquals(1.8E5f, scan2maxInt, 1E4f);

    // 5th scan, #5
    MsScan scan5 = scans.get(4);
    Assert.assertEquals(new Integer(5), scan5.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan5.getSpectrumType());
    Assert.assertEquals(new Integer(2), scan5.getMsFunction().getMsLevel());
    Assert.assertEquals(2.094f, scan5.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.POSITIVE, scan5.getPolarity());
    mzBuffer = scan5.getMzValues();
    intensityBuffer = scan5.getIntensityValues();
    Assert.assertEquals(837, (int) scan5.getNumberOfDataPoints());
    Float scan5maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan5.getNumberOfDataPoints());
    Assert.assertEquals(8.6E3f, scan5maxInt, 1E2f);

    rawFile.dispose();

  }


  @Test
  public void testPwizTiny() throws MSDKException {

    // Create the data structures
    double mzBuffer[];
    float intensityBuffer[];

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "tiny.pwiz.idx.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // The file has 4 scans
    List<MsScan> scans = rawFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(4, scans.size());

    // 2nd scan, #20
    MsScan scan2 = scans.get(1);
    Assert.assertEquals(new Integer(20), scan2.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan2.getSpectrumType());
    Assert.assertEquals(new Integer(2), scan2.getMsFunction().getMsLevel());
    Assert.assertEquals(359.43f, scan2.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.POSITIVE, scan2.getPolarity());
    mzBuffer = scan2.getMzValues();
    intensityBuffer = scan2.getIntensityValues();
    Assert.assertEquals(10, (int) scan2.getNumberOfDataPoints());
    Float scan2maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan2.getNumberOfDataPoints());
    Assert.assertEquals(20f, scan2maxInt, 0.001f);

    List<IsolationInfo> scan2Isolations = scan2.getIsolations();
    Assert.assertNotNull(scan2Isolations);
    Assert.assertEquals(1, scan2Isolations.size());

    IsolationInfo scan2Isolation = scan2Isolations.get(0);
    Assert.assertEquals(445.34, scan2Isolation.getPrecursorMz(), 0.001);
    Assert.assertEquals(new Integer(2), scan2Isolation.getPrecursorCharge());

    rawFile.dispose();

  }


  @Test
  public void testParamGroup() throws MSDKException {

    // Create the data structures
    double mzBuffer[] = new double[10000];
    float intensityBuffer[] = new float[10000];

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "RawCentriodCidWithMsLevelInRefParamGroup.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // The file has 102 scans
    List<MsScan> scans = rawFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(102, scans.size());

    // 2nd scan, #1001
    MsScan scan2 = scans.get(1);
    Assert.assertEquals(new Integer(1001), scan2.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan2.getSpectrumType());
    Assert.assertEquals(new Integer(2), scan2.getMsFunction().getMsLevel());
    Assert.assertEquals(100.002f, scan2.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.POSITIVE, scan2.getPolarity());
    mzBuffer = scan2.getMzValues();
    intensityBuffer = scan2.getIntensityValues();
    Assert.assertEquals(33, (int) scan2.getNumberOfDataPoints());
    Float scan2maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan2.getNumberOfDataPoints());
    Assert.assertEquals(6.8E3f, scan2maxInt, 1E2f);

    // 101th scan, #1100
    MsScan scan101 = scans.get(100);
    Assert.assertEquals(new Integer(1100), scan101.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan101.getSpectrumType());
    Assert.assertEquals(new Integer(1), scan101.getMsFunction().getMsLevel());
    Assert.assertEquals(109.998f, scan101.getRetentionTime(), 0.01f);
    mzBuffer = scan101.getMzValues();
    intensityBuffer = scan101.getIntensityValues();
    Assert.assertEquals(21, (int) scan101.getNumberOfDataPoints());
    Float scan5maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan101.getNumberOfDataPoints());
    Assert.assertEquals(1.8E4f, scan5maxInt, 1E2f);

    rawFile.dispose();

  }


  @Test
  public void testCompressedAndUncompressed() throws MSDKException {

    // Import the compressed file
    File compressedFile = new File(TEST_DATA_PATH + "MzMLFile_7_compressed.mzML");
    Assert.assertTrue(compressedFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(compressedFile);
    RawDataFile compressedRaw = importer.execute();
    Assert.assertNotNull(compressedRaw);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // Import the uncompressed file
    File unCompressedFile = new File(TEST_DATA_PATH + "MzMLFile_7_uncompressed.mzML");
    Assert.assertTrue(unCompressedFile.canRead());
    importer = new MzMLFileImportMethod(unCompressedFile);
    RawDataFile uncompressedRaw = importer.execute();
    Assert.assertNotNull(uncompressedRaw);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // These files have 3 scans
    List<MsScan> compressedScans = compressedRaw.getScans();
    List<MsScan> unCompressedScans = uncompressedRaw.getScans();
    Assert.assertEquals(3, compressedScans.size());
    Assert.assertEquals(3, unCompressedScans.size());

    for (int i = 0; i < 3; i++) {
      MsScan compressedScan = compressedScans.get(i);
      MsScan unCompressedScan = unCompressedScans.get(i);

      double compressedMzBuffer[] = compressedScan.getMzValues();
      double uncompressedMzBuffer[] = unCompressedScan.getMzValues();
      float compressedIntensityBuffer[] = compressedScan.getIntensityValues();
      float uncompressedIntensityBuffer[] = unCompressedScan.getIntensityValues();

      Assert.assertTrue(Arrays.equals(compressedMzBuffer, uncompressedMzBuffer));
      Assert.assertTrue(Arrays.equals(compressedIntensityBuffer, uncompressedIntensityBuffer));

    }

    compressedRaw.dispose();
    uncompressedRaw.dispose();

  }


  @Test
  public void testSRM() throws MSDKException {

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "SRM.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // The file has 37 chromatograms
    List<Chromatogram> chromatograms = rawFile.getChromatograms();
    Assert.assertNotNull(chromatograms);
    Assert.assertEquals(37, chromatograms.size());

    // 4th chromatogram
    Chromatogram chromatogram = chromatograms.get(3);
    Assert.assertEquals(new Integer(4), chromatogram.getChromatogramNumber());
    Assert.assertEquals(ChromatogramType.MRM_SRM, chromatogram.getChromatogramType());
    Assert.assertEquals(new Integer(1608), chromatogram.getNumberOfDataPoints());
    Assert.assertEquals(new Integer(2), (Integer) chromatogram.getIsolations().size());
    Assert.assertEquals(new Double(440.706), chromatogram.getIsolations().get(1).getPrecursorMz());
    Assert.assertEquals(ActivationType.CID,
        chromatogram.getIsolations().get(0).getActivationInfo().getActivationType());

    // 1st chromatogram
    chromatogram = chromatograms.get(0);
    Assert.assertEquals(ChromatogramType.TIC, chromatogram.getChromatogramType());
    Assert.assertEquals(0, chromatogram.getIsolations().size());

    rawFile.dispose();
  }


  @Test
  public void testFileWithUV() throws MSDKException {

    // Create the data structures
    double mzBuffer[] = new double[10000];
    float intensityBuffer[] = new float[10000];

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "mzML_with_UV.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // The file has 27 MS scans, the rest are UV spectra
    List<MsScan> scans = rawFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(27, scans.size());

    // 2nd scan, #2101
    MsScan scan2 = scans.get(1);
    Assert.assertEquals(new Integer(2101), scan2.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan2.getSpectrumType());
    Assert.assertEquals(new Integer(1), scan2.getMsFunction().getMsLevel());
    Assert.assertEquals(1126.57f, scan2.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.NEGATIVE, scan2.getPolarity());
    mzBuffer = scan2.getMzValues();
    intensityBuffer = scan2.getIntensityValues();
    Assert.assertEquals(1315, (int) scan2.getNumberOfDataPoints());
    Float scan2maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan2.getNumberOfDataPoints());
    Assert.assertEquals(6457.04296f, scan2maxInt, 0.1f);

    rawFile.dispose();
  }


  @Test
  public void testEmptyScan() throws MSDKException {

    // Create the data structures
    double mzBuffer[] = new double[10000];
    float intensityBuffer[] = new float[10000];

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "emptyScan.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // The file has 1 scan, with no data points
    List<MsScan> scans = rawFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(1, scans.size());

    // 1st scan, #422
    MsScan scan2 = scans.get(0);
    Assert.assertEquals(new Integer(422), scan2.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan2.getSpectrumType());
    Assert.assertEquals(new Integer(2), scan2.getMsFunction().getMsLevel());
    Assert.assertEquals(309.1878f, scan2.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.POSITIVE, scan2.getPolarity());
    mzBuffer = scan2.getMzValues();
    intensityBuffer = scan2.getIntensityValues();
    Assert.assertEquals(0, (int) scan2.getNumberOfDataPoints());
    Float scan2maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan2.getNumberOfDataPoints());
    Assert.assertEquals(0f, scan2maxInt, 0.1f);

    // Test isolation data
    List<IsolationInfo> scan2isolations = scan2.getIsolations();
    Assert.assertEquals(1, scan2isolations.size());
    IsolationInfo scan2isolation = scan2isolations.get(0);
    Assert.assertEquals(574.144409179688, scan2isolation.getPrecursorMz(), 0.0000001);
    Assert.assertEquals(573.14, scan2isolation.getIsolationMzRange().lowerEndpoint(), 0.01);
    Assert.assertEquals(575.14, scan2isolation.getIsolationMzRange().upperEndpoint(), 0.01);

    rawFile.dispose();

  }

  @Test(expected = MSDKException.class)
  public void testTruncated() throws MSDKException {

    // Try importing an invalid (truncated file).
    // Import should throw an MSDKException.
    File inputFile = new File(TEST_DATA_PATH + "truncated.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    importer.execute();

  }

}
