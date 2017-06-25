
/*
 * (C) Copyright 2015-2016 by MSDK Development Team
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

package io.github.msdk.io.mzml2;

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

public class MzMLFileParserTest {

  private static final String TEST_DATA_PATH = "src/test/resources/";

  @Test
  public void testFileWithUV() throws MSDKException {

    // Import the file
    final String inputFileName = TEST_DATA_PATH + "mzML_with_UV.mzML";
    final File inputFile = new File(inputFileName);
    MzMLFileParser mzParser = new MzMLFileParser(inputFile);
    RawDataFile rawFile = mzParser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, mzParser.getFinishedPercentage(), 0.0001);

    // The file has 27 scans
    Assert.assertEquals(27, rawFile.getScans().size());

    // 14th Scan, #2114
    MzMLSpectrum spectrum = (MzMLSpectrum) rawFile.getScans().get(14);
    Assert.assertNotNull(spectrum);
    Assert.assertNotNull(spectrum.getMzValues());
    Assert.assertNotNull(spectrum.getIntensityValues());
    Assert.assertEquals(new Integer(spectrum.getMzValues().length),
        spectrum.getNumberOfDataPoints());
    Assert.assertEquals(new Integer(spectrum.getIntensityValues().length),
        spectrum.getNumberOfDataPoints());
    Assert.assertEquals(new Integer(2114), spectrum.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, spectrum.getSpectrumType());
    Assert.assertEquals(new Float(9.939699e06), spectrum.getTIC(), 10);
    Assert.assertEquals(new Double(100.175651550293), spectrum.getMzRange().lowerEndpoint(),
        0.000001);
    Assert.assertEquals(new Double(999.832214355469), spectrum.getMzRange().upperEndpoint(),
        0.000001);
    Assert.assertEquals(509.6600036621094, spectrum.getMzValues()[619], 0.0001);
    Assert.assertEquals("+ c ESI Q1MS [100.000-1000.000]", spectrum.getScanDefinition());
    Assert.assertEquals(new Integer(1), spectrum.getMsLevel());
    Assert.assertEquals(PolarityType.POSITIVE, spectrum.getPolarity());
    Assert.assertEquals(new Float(18.89235 * 60), spectrum.getRetentionTime());

    rawFile.dispose();

    // Import the file using the alternate constructor
    Assert.assertNotNull(new File(inputFileName));
    MzMLFileParser mzParser2 = new MzMLFileParser(inputFileName);
    RawDataFile rawFile2 = mzParser2.execute();
    Assert.assertNotNull(rawFile2);
    Assert.assertEquals(1.0, mzParser2.getFinishedPercentage(), 0.0001);

    // The file has 27 scans
    Assert.assertEquals(27, rawFile2.getScans().size());

    // Scan 17, #2117
    MzMLSpectrum spectrum2 = (MzMLSpectrum) rawFile2.getScans().get(17);
    Assert.assertNotNull(spectrum2);
    Assert.assertNotNull(spectrum2.getMzValues());
    Assert.assertNotNull(spectrum2.getIntensityValues());
    Assert.assertEquals(new Integer(spectrum2.getMzValues().length),
        spectrum2.getNumberOfDataPoints());
    Assert.assertEquals(new Integer(spectrum2.getIntensityValues().length),
        spectrum2.getNumberOfDataPoints());
    Assert.assertEquals(new Integer(2117), spectrum2.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, spectrum2.getSpectrumType());
    Assert.assertEquals(new Float(43900.85546875), spectrum2.getTIC(), 10);
    Assert.assertEquals(new Double(100.300285339355), spectrum2.getMzRange().lowerEndpoint(),
        0.000001);
    Assert.assertEquals(new Double(999.323547363281), spectrum2.getMzRange().upperEndpoint(),
        0.000001);
    Assert.assertEquals("- c ESI Q1MS [100.000-1000.000]", spectrum2.getScanDefinition());
    Assert.assertEquals(new Integer(1), spectrum2.getMsLevel());
    Assert.assertEquals(PolarityType.NEGATIVE, spectrum2.getPolarity());
    Assert.assertEquals(new Float(18.919083333333 * 60), spectrum2.getRetentionTime());

    rawFile2.dispose();
  }

  @Test
  public void test5peptideFT() throws MSDKException {

    float intensityBuffer[] = new float[10000];

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "5peptideFT.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileParser parser = new MzMLFileParser(inputFile);
    RawDataFile rawFile = parser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // The file has 7 scans
    List<MsScan> scans = rawFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(7, scans.size());

    // 2nd scan, #2
    MsScan scan2 = scans.get(1);
    Assert.assertEquals(new Integer(2), scan2.getScanNumber());
    Assert.assertEquals(MsSpectrumType.PROFILE, scan2.getSpectrumType());
    Assert.assertEquals(new Integer(1), scan2.getMsLevel());
    Assert.assertEquals(0.474f, scan2.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.POSITIVE, scan2.getPolarity());
    Assert.assertEquals(209.1818184554577, scan2.getMzValues()[100], 0.00001);
    scan2.getMzValues();
    intensityBuffer = scan2.getIntensityValues();
    Assert.assertEquals(19800, (int) scan2.getNumberOfDataPoints());
    Float scan2maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan2.getNumberOfDataPoints());
    Assert.assertEquals(1.8E5f, scan2maxInt, 1E4f);

    // 5th scan, #5
    MsScan scan5 = scans.get(4);
    Assert.assertEquals(new Integer(5), scan5.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan5.getSpectrumType());
    Assert.assertEquals(new Integer(2), scan5.getMsLevel());
    Assert.assertEquals(2.094f, scan5.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.POSITIVE, scan5.getPolarity());
    Assert.assertEquals(483.4679870605469, scan5.getMzValues()[200], 0.00001);
    intensityBuffer = scan5.getIntensityValues();
    Assert.assertEquals(837, (int) scan5.getNumberOfDataPoints());
    Float scan5maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan5.getNumberOfDataPoints());
    Assert.assertEquals(8.6E3f, scan5maxInt, 1E2f);

    rawFile.dispose();

  }


  @Test
  public void testPwizTiny() throws MSDKException {

    float intensityBuffer[];

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "tiny.pwiz.idx.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileParser parser = new MzMLFileParser(inputFile);
    RawDataFile rawFile = parser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // The file has 4 scans
    List<MsScan> scans = rawFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(4, scans.size());

    // 2nd scan, #20
    MsScan scan2 = scans.get(1);
    Assert.assertEquals(new Integer(20), scan2.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan2.getSpectrumType());
    Assert.assertEquals(new Integer(2), scan2.getMsLevel());
    Assert.assertEquals(359.43f, scan2.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.POSITIVE, scan2.getPolarity());
    Assert.assertEquals(16.0, scan2.getMzValues()[8], 0.00001);
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

    float intensityBuffer[] = new float[10000];

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "RawCentriodCidWithMsLevelInRefParamGroup.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileParser parser = new MzMLFileParser(inputFile);
    RawDataFile rawFile = parser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // The file has 102 scans
    List<MsScan> scans = rawFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(102, scans.size());

    // 2nd scan, #1001
    MsScan scan2 = scans.get(1);
    Assert.assertEquals(new Integer(1001), scan2.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan2.getSpectrumType());
    Assert.assertEquals(new Integer(2), scan2.getMsLevel());
    Assert.assertEquals(100.002f, scan2.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.POSITIVE, scan2.getPolarity());
    Assert.assertEquals(111.03714243896029, scan2.getMzValues()[10], 0.00001);
    intensityBuffer = scan2.getIntensityValues();
    Assert.assertEquals(33, (int) scan2.getNumberOfDataPoints());
    Float scan2maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan2.getNumberOfDataPoints());
    Assert.assertEquals(6.8E3f, scan2maxInt, 1E2f);

    // 101th scan, #1100
    MsScan scan101 = scans.get(100);
    Assert.assertEquals(new Integer(1100), scan101.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan101.getSpectrumType());
    Assert.assertEquals(new Integer(1), scan101.getMsLevel());
    Assert.assertEquals(109.998f, scan101.getRetentionTime(), 0.01f);
    Assert.assertEquals(174.10665617189798, scan101.getMzValues()[10], 0.00001);
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
    MzMLFileParser parser = new MzMLFileParser(compressedFile);
    RawDataFile compressedRaw = parser.execute();
    Assert.assertNotNull(compressedRaw);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // Import the uncompressed file
    File unCompressedFile = new File(TEST_DATA_PATH + "MzMLFile_7_uncompressed.mzML");
    Assert.assertTrue(unCompressedFile.canRead());
    parser = new MzMLFileParser(unCompressedFile);
    RawDataFile uncompressedRaw = parser.execute();
    Assert.assertNotNull(uncompressedRaw);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

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
    MzMLFileParser parser = new MzMLFileParser(inputFile);
    RawDataFile rawFile = parser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

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
    Assert.assertEquals(0.01095, chromatogram.getRetentionTimes()[0], 0.0001);
    Assert.assertEquals(38.500003814697266, chromatogram.getIntensityValues()[0], 0.0001);

    // 1st chromatogram
    chromatogram = chromatograms.get(0);
    Assert.assertEquals(ChromatogramType.TIC, chromatogram.getChromatogramType());
    Assert.assertEquals(0, chromatogram.getIsolations().size());

    rawFile.dispose();
  }



  @Test
  public void testEmptyScan() throws MSDKException {

    float intensityBuffer[] = new float[10000];

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "emptyScan.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileParser parser = new MzMLFileParser(inputFile);
    RawDataFile rawFile = parser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // The file has 1 scan, with no data points
    List<MsScan> scans = rawFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(1, scans.size());

    // 1st scan, #422
    MsScan scan2 = scans.get(0);
    Assert.assertEquals(new Integer(422), scan2.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan2.getSpectrumType());
    Assert.assertEquals(new Integer(2), scan2.getMsLevel());
    Assert.assertEquals(309.1878f, scan2.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.POSITIVE, scan2.getPolarity());
    scan2.getMzValues();
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
    MzMLFileParser parser = new MzMLFileParser(inputFile);
    parser.execute();

  }

  @Test
  public void testZlibAndNumpressCompression() throws MSDKException {

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "MzValues_Zlib+Numpress.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileParser parser = new MzMLFileParser(inputFile);
    RawDataFile rawFile = parser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // The file has 6 scans
    List<MsScan> scans = rawFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(6, scans.size());

    // 4th scan, #2103
    MsScan scan4 = scans.get(3);
    Assert.assertEquals(new Integer(2103), scan4.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan4.getSpectrumType());
    Assert.assertEquals(new Integer(1), scan4.getMsLevel());
    Assert.assertEquals(1127.6449f, scan4.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.NEGATIVE, scan4.getPolarity());
    Assert.assertEquals(425.50030515961424, scan4.getMzValues()[510], 0.0001);
    Assert.assertEquals(1306, (int) scan4.getNumberOfDataPoints());
    Float scan2maxInt =
        MsSpectrumUtil.getMaxIntensity(scan4.getIntensityValues(), scan4.getNumberOfDataPoints());
    Assert.assertEquals(8746.9599f, scan2maxInt, 0.1f);
    Assert.assertEquals(new Float(58989.76953125), scan4.getTIC(), 10);
    Assert.assertEquals(new Double(100.317253112793), scan4.getMzRange().lowerEndpoint(), 0.000001);
    Assert.assertEquals(new Double(999.715515136719), scan4.getMzRange().upperEndpoint(), 0.000001);
    Assert.assertEquals("- c ESI Q1MS [100.000-1000.000]", scan4.getScanDefinition());

    // Test isolation data
    List<IsolationInfo> scan2isolations = scan4.getIsolations();
    Assert.assertEquals(0, scan2isolations.size());

    // The file has 2 chromatograms
    List<Chromatogram> chromatograms = rawFile.getChromatograms();
    Assert.assertNotNull(chromatograms);
    Assert.assertEquals(2, chromatograms.size());

    // 1st chromatogram
    Chromatogram chromatogram = chromatograms.get(0);
    Assert.assertEquals(new Integer(1), chromatogram.getChromatogramNumber());
    Assert.assertEquals(ChromatogramType.TIC, chromatogram.getChromatogramType());
    Assert.assertEquals(new Integer(2126), chromatogram.getNumberOfDataPoints());
    Assert.assertEquals(new Integer(0), (Integer) chromatogram.getIsolations().size());
    float[] rtValues = chromatogram.getRetentionTimes();
    Assert.assertEquals(2126, rtValues.length);
    Assert.assertEquals(12.60748291015625, rtValues[1410], 0.0001);

    rawFile.dispose();

  }
}
