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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.ActivationType;
import io.github.msdk.datamodel.Chromatogram;
import io.github.msdk.datamodel.ChromatogramType;
import io.github.msdk.datamodel.IsolationInfo;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.PolarityType;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.io.mzml.data.MzMLCompressionType;
import io.github.msdk.io.mzml.data.MzMLMsScan;
import io.github.msdk.io.mzxml.MzXMLFileParser;
import io.github.msdk.util.MsSpectrumUtil;

public class MzMLFileExportMethodTest {

  private Path getResourcePath(String resource) throws MSDKException {
    final URL url = MzMLFileImportMethodTest.class.getClassLoader().getResource(resource);
    try {
      return Paths.get(url.toURI()).toAbsolutePath();
    } catch (URISyntaxException e) {
      throw new MSDKException(e);
    }
  }

  @Test
  public void test5peptideFT() throws MSDKException, IOException {

    float intensityBuffer[];

    // Set up Predicate<MsScan>
    List<Integer> scansToParse = new ArrayList<>();
    scansToParse.addAll(Arrays.asList(2, 5));
    Predicate<MsScan> msScanPredicate = getMsScanPredicate(scansToParse);
    List<Integer> chromatogramsToParse = new ArrayList<>();
    Predicate<Chromatogram> chromatogramPredicate = getChromatogramPredicate(chromatogramsToParse);

    // Import the file
    String file = "5peptideFT.mzML";
    File inputFile = getResourcePath(file).toFile();
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod parser =
        new MzMLFileImportMethod(inputFile, msScanPredicate, chromatogramPredicate);
    RawDataFile rawFile = parser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // Export the file to a new mzML
    File tempFile = File.createTempFile("msdk", ".mzML");
    tempFile.deleteOnExit();
    MzMLFileExportMethod exporter = new MzMLFileExportMethod(rawFile, tempFile,
        MzMLCompressionType.NUMPRESS_LINPRED_ZLIB, MzMLCompressionType.ZLIB);
    exporter.execute();
    Assert.assertEquals(1.0, exporter.getFinishedPercentage(), 0.0001);

    // Import the new mzML
    parser = new MzMLFileImportMethod(tempFile, msScanPredicate, chromatogramPredicate);
    RawDataFile newMzMLFile = parser.execute();
    Assert.assertNotNull(newMzMLFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // The file has 7 scans, 2 pass the predicate
    List<MsScan> scans = newMzMLFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(7, scans.size());

    // 2nd scan, #2
    MsScan scan2 = scans.get(1);
    Assert.assertEquals(Integer.valueOf(2), scan2.getScanNumber());
    Assert.assertEquals(MsSpectrumType.PROFILE, scan2.getSpectrumType());
    Assert.assertEquals(Integer.valueOf(1), scan2.getMsLevel());
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
    Assert.assertEquals(Integer.valueOf(5), scan5.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan5.getSpectrumType());
    Assert.assertEquals(Integer.valueOf(2), scan5.getMsLevel());
    Assert.assertEquals(2.094f, scan5.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.POSITIVE, scan5.getPolarity());
    Assert.assertEquals(483.4679870605469, scan5.getMzValues()[200], 0.00001);
    intensityBuffer = scan5.getIntensityValues();
    Assert.assertEquals(837, (int) scan5.getNumberOfDataPoints());
    Float scan5maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan5.getNumberOfDataPoints());
    Assert.assertEquals(8.6E3f, scan5maxInt, 1E2f);

    // Cleanup
    rawFile.dispose();
    newMzMLFile.dispose();
  }

  @Test
  public void testSRM() throws MSDKException, IOException {

    // Set up Predicate<MsScan>
    List<Integer> scansToParse = new ArrayList<>();
    Predicate<MsScan> msScanPredicate = getMsScanPredicate(scansToParse);
    List<Integer> chromatogramsToParse = new ArrayList<>();
    chromatogramsToParse.addAll(Arrays.asList(1, 2, 4, 19, 36));
    Predicate<Chromatogram> chromatogramPredicate = getChromatogramPredicate(chromatogramsToParse);
    List<Integer> newChromatogramsToParse = new ArrayList<>();
    newChromatogramsToParse.addAll(Arrays.asList(1, 2, 3, 4, 5));
    Predicate<Chromatogram> newChromatogramPredicate =
        getChromatogramPredicate(newChromatogramsToParse);

    // Import the file
    String file = "SRM.mzML";
    File inputFile = getResourcePath(file).toFile();
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod parser =
        new MzMLFileImportMethod(inputFile, msScanPredicate, chromatogramPredicate);
    RawDataFile rawFile = parser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // Export the file to a new mzML
    File tempFile = File.createTempFile("msdk", ".mzML");
    tempFile.deleteOnExit();
    MzMLFileExportMethod exporter = new MzMLFileExportMethod(rawFile, tempFile,
        MzMLCompressionType.NUMPRESS_LINPRED_ZLIB, MzMLCompressionType.ZLIB);
    exporter.execute();
    Assert.assertEquals(1.0, exporter.getFinishedPercentage(), 0.0001);

    // Import the new mzML
    parser = new MzMLFileImportMethod(tempFile, msScanPredicate, newChromatogramPredicate);
    RawDataFile newMzMLFile = parser.execute();
    Assert.assertNotNull(newMzMLFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // The file has 37 chromatograms, 5 pass the predicate
    List<Chromatogram> chromatograms = newMzMLFile.getChromatograms();
    Assert.assertNotNull(chromatograms);
    Assert.assertEquals(37, chromatograms.size());

    // 4th chromatogram, #4
    Chromatogram chromatogram = chromatograms.get(3);
    Assert.assertEquals(Integer.valueOf(4), chromatogram.getChromatogramNumber());
    Assert.assertEquals(ChromatogramType.MRM_SRM, chromatogram.getChromatogramType());
    Assert.assertEquals(Integer.valueOf(1608), chromatogram.getNumberOfDataPoints());
    Assert.assertEquals(Integer.valueOf(2), (Integer) chromatogram.getIsolations().size());
    Assert.assertEquals(Double.valueOf(440.706),
        chromatogram.getIsolations().get(1).getPrecursorMz());
    Assert.assertEquals(ActivationType.CID,
        chromatogram.getIsolations().get(0).getActivationInfo().getActivationType());
    Assert.assertEquals(0.01095, chromatogram.getRetentionTimes()[0], 0.0001);
    Assert.assertEquals(38.500003814697266, chromatogram.getIntensityValues()[0], 0.0001);

    // 1st chromatogram, #1
    chromatogram = chromatograms.get(0);
    Assert.assertEquals(ChromatogramType.TIC, chromatogram.getChromatogramType());
    Assert.assertEquals(0, chromatogram.getIsolations().size());

    // Check m/z values
    Assert.assertEquals(407.706, chromatograms.get(1).getMz(), 0.001);
    Assert.assertEquals(1084.486, chromatograms.get(18).getMz(), 0.001);
    Assert.assertEquals(1042.516, chromatograms.get(35).getMz(), 0.001);

    // Cleanup
    rawFile.dispose();
    newMzMLFile.dispose();
  }

  @Test
  public void testFileWithUV() throws MSDKException, IOException {

    // Set up Predicate<MsScan>
    List<Integer> scansToParse = new ArrayList<>();
    scansToParse.addAll(Arrays.asList(2114, 2117));
    Predicate<MsScan> msScanPredicate = getMsScanPredicate(scansToParse);
    List<Integer> chromatogramsToParse = new ArrayList<>();
    Predicate<Chromatogram> chromatogramPredicate = getChromatogramPredicate(chromatogramsToParse);

    // Import the file
    String file = "mzML_with_UV.mzML";
    final Path path = getResourcePath(file);
    final File inputFile = path.toFile();
    MzMLFileImportMethod mzParser =
        new MzMLFileImportMethod(inputFile, msScanPredicate, chromatogramPredicate);
    RawDataFile rawFile = mzParser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, mzParser.getFinishedPercentage(), 0.0001);

    // Export the file to a new mzML
    File tempFile = File.createTempFile("msdk", ".mzML");
    tempFile.deleteOnExit();
    MzMLFileExportMethod exporter = new MzMLFileExportMethod(rawFile, tempFile,
        MzMLCompressionType.NUMPRESS_LINPRED_ZLIB, MzMLCompressionType.ZLIB);
    exporter.execute();
    Assert.assertEquals(1.0, exporter.getFinishedPercentage(), 0.0001);

    // Import the new mzML
    mzParser = new MzMLFileImportMethod(tempFile, msScanPredicate, chromatogramPredicate);
    RawDataFile newMzMLFile = mzParser.execute();
    Assert.assertNotNull(newMzMLFile);
    Assert.assertEquals(1.0, mzParser.getFinishedPercentage(), 0.0001);

    // The file has 27 scans, 2 scans pass the predicate
    Assert.assertEquals(27, newMzMLFile.getScans().size());

    // 15th Scan, #2114
    MzMLMsScan scan = (MzMLMsScan) newMzMLFile.getScans().get(14);
    Assert.assertNotNull(scan);
    Assert.assertNotNull(scan.getMzValues());
    Assert.assertNotNull(scan.getIntensityValues());
    Assert.assertEquals(Integer.valueOf(scan.getMzValues().length), scan.getNumberOfDataPoints());
    Assert.assertEquals(Integer.valueOf(scan.getIntensityValues().length),
        scan.getNumberOfDataPoints());
    Assert.assertEquals(Integer.valueOf(2114), scan.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan.getSpectrumType());
    Assert.assertEquals(9.939699e06f, scan.getTIC(), 1e01);
    Assert.assertEquals(100.175651550293, scan.getMzRange().lowerEndpoint(), 0.000001);
    Assert.assertEquals(999.832214355469, scan.getMzRange().upperEndpoint(), 0.000001);
    Assert.assertEquals(509.6600036621094, scan.getMzValues()[619], 0.0001);
    Assert.assertEquals("+ c ESI Q1MS [100.000-1000.000]", scan.getScanDefinition());
    Assert.assertEquals(Integer.valueOf(1), scan.getMsLevel());
    Assert.assertEquals(PolarityType.POSITIVE, scan.getPolarity());
    Assert.assertEquals(Float.valueOf((float) (18.89235 * 60)), scan.getRetentionTime());

    // Cleanup
    rawFile.dispose();
    newMzMLFile.dispose();

  }

  @Test
  public void testPwizTiny() throws MSDKException, IOException {

    float intensityBuffer[];

    // Set up Predicate<MsScan>
    List<Integer> scansToParse = new ArrayList<>();
    scansToParse.addAll(Arrays.asList(20));
    Predicate<MsScan> msScanPredicate = getMsScanPredicate(scansToParse);
    List<Integer> chromatogramsToParse = new ArrayList<>();
    Predicate<Chromatogram> chromatogramPredicate = getChromatogramPredicate(chromatogramsToParse);

    // Import the file
    String file = "tiny.pwiz.idx.mzML";
    File inputFile = getResourcePath(file).toFile();
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod parser =
        new MzMLFileImportMethod(inputFile, msScanPredicate, chromatogramPredicate);
    RawDataFile rawFile = parser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // Export the file to a new mzML
    File tempFile = File.createTempFile("msdk", ".mzML");
    tempFile.deleteOnExit();
    // XXX Zlib compression throws a java.lang.OutOfMemoryError exception
    MzMLFileExportMethod exporter = new MzMLFileExportMethod(rawFile, tempFile,
        MzMLCompressionType.NUMPRESS_LINPRED, MzMLCompressionType.NO_COMPRESSION);
    exporter.execute();
    Assert.assertEquals(1.0, exporter.getFinishedPercentage(), 0.0001);

    // Import the new mzML
    parser = new MzMLFileImportMethod(tempFile, msScanPredicate, chromatogramPredicate);
    RawDataFile newMzMLFile = parser.execute();
    Assert.assertNotNull(newMzMLFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // The file has 4 scans, 1 scan in RawFile
    List<MsScan> scans = newMzMLFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(4, scans.size());

    // 2nd scan, #20
    MsScan scan2 = scans.get(1);
    Assert.assertEquals(Integer.valueOf(20), scan2.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan2.getSpectrumType());
    Assert.assertEquals(Integer.valueOf(2), scan2.getMsLevel());
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
    Assert.assertEquals(Integer.valueOf(2), scan2Isolation.getPrecursorCharge());

    // Cleanup
    rawFile.dispose();
    newMzMLFile.dispose();

  }

  @Test
  public void testParamGroup() throws MSDKException, IOException {

    float intensityBuffer[];

    // Set up Predicate<MsScan>
    List<Integer> scansToParse = new ArrayList<>();
    scansToParse.addAll(Arrays.asList(1001, 1100));
    Predicate<MsScan> msScanPredicate = getMsScanPredicate(scansToParse);
    List<Integer> chromatogramsToParse = new ArrayList<>();
    Predicate<Chromatogram> chromatogramPredicate = getChromatogramPredicate(chromatogramsToParse);

    // Import the file
    String file = "RawCentriodCidWithMsLevelInRefParamGroup.mzML";
    File inputFile = getResourcePath(file).toFile();
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod parser =
        new MzMLFileImportMethod(inputFile, msScanPredicate, chromatogramPredicate);
    RawDataFile rawFile = parser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // Export the file to a new mzML
    File tempFile = File.createTempFile("msdk", ".mzML");
    tempFile.deleteOnExit();
    MzMLFileExportMethod exporter = new MzMLFileExportMethod(rawFile, tempFile,
        MzMLCompressionType.NUMPRESS_LINPRED_ZLIB, MzMLCompressionType.ZLIB);
    exporter.execute();
    Assert.assertEquals(1.0, exporter.getFinishedPercentage(), 0.0001);

    // Import the new mzML
    parser = new MzMLFileImportMethod(tempFile, msScanPredicate, chromatogramPredicate);
    RawDataFile newMzMLFile = parser.execute();
    Assert.assertNotNull(newMzMLFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // The file has 102 scans, 2 scans pass the predicate
    List<MsScan> scans = newMzMLFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(102, scans.size());

    // 2nd scan, #1001
    MsScan scan2 = scans.get(1);
    Assert.assertEquals(Integer.valueOf(1001), scan2.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan2.getSpectrumType());
    Assert.assertEquals(Integer.valueOf(2), scan2.getMsLevel());
    Assert.assertEquals(100.002f, scan2.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.POSITIVE, scan2.getPolarity());
    Assert.assertEquals(111.03714243896029, scan2.getMzValues()[10], 0.00001);
    intensityBuffer = scan2.getIntensityValues();
    Assert.assertEquals(33, (int) scan2.getNumberOfDataPoints());
    Float scan2maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan2.getNumberOfDataPoints());
    Assert.assertEquals(6.8E3f, scan2maxInt, 1E2f);

    // 101st scan, #1100
    MsScan scan101 = scans.get(100);
    Assert.assertEquals(Integer.valueOf(1100), scan101.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan101.getSpectrumType());
    Assert.assertEquals(Integer.valueOf(1), scan101.getMsLevel());
    Assert.assertEquals(109.998f, scan101.getRetentionTime(), 0.01f);
    Assert.assertEquals(174.10665617189798, scan101.getMzValues()[10], 0.00001);
    intensityBuffer = scan101.getIntensityValues();
    Assert.assertEquals(21, (int) scan101.getNumberOfDataPoints());
    Float scan5maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan101.getNumberOfDataPoints());
    Assert.assertEquals(1.8E4f, scan5maxInt, 1E2f);

    // Cleanup
    rawFile.dispose();
    newMzMLFile.dispose();
  }

  @Test
  public void testZlibAndNumpressCompression() throws MSDKException, IOException {

    // Set up Predicate<MsScan>
    List<Integer> scansToParse = new ArrayList<>();
    scansToParse.addAll(Arrays.asList(2103));
    Predicate<MsScan> msScanPredicate = getMsScanPredicate(scansToParse);
    List<Integer> chromatogramsToParse = new ArrayList<>();
    chromatogramsToParse.addAll(Arrays.asList(1));
    Predicate<Chromatogram> chromatogramPredicate = getChromatogramPredicate(chromatogramsToParse);

    // Import the file
    String file = "MzValues_Zlib+Numpress.mzML";
    File inputFile = getResourcePath(file).toFile();
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod parser =
        new MzMLFileImportMethod(inputFile, msScanPredicate, chromatogramPredicate);
    RawDataFile rawFile = parser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // Export the file to a new mzML
    File tempFile = File.createTempFile("msdk", ".mzML");
    tempFile.deleteOnExit();
    MzMLFileExportMethod exporter = new MzMLFileExportMethod(rawFile, tempFile,
        MzMLCompressionType.NUMPRESS_LINPRED_ZLIB, MzMLCompressionType.ZLIB);
    exporter.execute();
    Assert.assertEquals(1.0, exporter.getFinishedPercentage(), 0.0001);

    // Import the new mzML
    parser = new MzMLFileImportMethod(tempFile, msScanPredicate, chromatogramPredicate);
    RawDataFile newMzMLFile = parser.execute();
    Assert.assertNotNull(newMzMLFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // The file has 6 scans, 1 pass the predicate
    List<MsScan> scans = newMzMLFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(6, scans.size());

    // 4th scan, #2103
    MsScan scan4 = scans.get(3);
    Assert.assertEquals(Integer.valueOf(2103), scan4.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan4.getSpectrumType());
    Assert.assertEquals(Integer.valueOf(1), scan4.getMsLevel());
    Assert.assertEquals(1127.6449f, scan4.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.NEGATIVE, scan4.getPolarity());
    Assert.assertEquals(425.50030515961424, scan4.getMzValues()[510], 0.0001);
    Assert.assertEquals(1306, (int) scan4.getNumberOfDataPoints());
    Float scan2maxInt =
        MsSpectrumUtil.getMaxIntensity(scan4.getIntensityValues(), scan4.getNumberOfDataPoints());
    // Assert.assertEquals(8746.9599f, scan2maxInt, 0.1f);
    Assert.assertEquals(8746.96f, scan2maxInt, 0.1f);
    // Assert.assertEquals(58989.76953125f, scan4.getTIC(), 0.1);
    Assert.assertEquals(58989.77f, scan4.getTIC(), 0.1);
    Assert.assertEquals(100.317253112793, scan4.getMzRange().lowerEndpoint(), 0.000001);
    Assert.assertEquals(999.715515136719, scan4.getMzRange().upperEndpoint(), 0.000001);
    Assert.assertEquals("- c ESI Q1MS [100.000-1000.000]", scan4.getScanDefinition());

    // Test isolation data
    List<IsolationInfo> scan2isolations = scan4.getIsolations();
    Assert.assertEquals(0, scan2isolations.size());

    // The file has 2 chromatograms, 1 in RawFile
    List<Chromatogram> chromatograms = newMzMLFile.getChromatograms();
    Assert.assertNotNull(chromatograms);
    Assert.assertEquals(2, chromatograms.size());

    // 1st chromatogram
    Chromatogram chromatogram = chromatograms.get(0);
    Assert.assertEquals(1, (int) chromatogram.getChromatogramNumber());
    Assert.assertEquals(ChromatogramType.TIC, chromatogram.getChromatogramType());
    Assert.assertEquals(2126, (int) chromatogram.getNumberOfDataPoints());
    Assert.assertEquals(0, chromatogram.getIsolations().size());
    float[] rtValues = chromatogram.getRetentionTimes();
    Assert.assertEquals(2126, rtValues.length);
    Assert.assertEquals(12.60748291015625, rtValues[1410], 0.0001);

    // Cleanup
    rawFile.dispose();
    newMzMLFile.dispose();
  }

  @Test
  @Ignore("Temporarily ignored, but failing")
  public void testExportFromMzXML() throws Exception {

    float intensityBuffer[];

    // Import the file
    String file = "tiny.pwiz.mzXML";
    File inputFile = getResourcePath(file).toFile();
    Assert.assertTrue(inputFile.canRead());
    MzXMLFileParser parser = new MzXMLFileParser(inputFile);
    RawDataFile rawFile = parser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // Export the file to a new mzML
    File tempFile = File.createTempFile("msdk", ".mzML");
    tempFile.deleteOnExit();
    MzMLFileExportMethod exporter = new MzMLFileExportMethod(rawFile, tempFile,
        MzMLCompressionType.NO_COMPRESSION, MzMLCompressionType.NO_COMPRESSION);
    exporter.execute();
    Assert.assertEquals(1.0, exporter.getFinishedPercentage(), 0.0001);

    // Import the new mzML
    MzMLFileImportMethod parser2 = new MzMLFileImportMethod(tempFile);
    RawDataFile newMzMLFile = parser2.execute();
    Assert.assertNotNull(newMzMLFile);
    Assert.assertEquals(1.0, parser2.getFinishedPercentage(), 0.0001);

    // The file has 4 scans, 1 scan in RawFile
    List<MsScan> scans = newMzMLFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(4, scans.size());

    // 2nd scan, #20
    MsScan scan2 = scans.get(1);
    Assert.assertEquals(Integer.valueOf(20), scan2.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan2.getSpectrumType());
    Assert.assertEquals(Integer.valueOf(2), scan2.getMsLevel());
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
    Assert.assertEquals(Integer.valueOf(2), scan2Isolation.getPrecursorCharge());

    // Cleanup
    rawFile.dispose();
    newMzMLFile.dispose();

  }

  private Predicate<MsScan> getMsScanPredicate(List<Integer> scansToParse) {
    return s -> scansToParse.contains(s.getScanNumber());
  }

  private Predicate<Chromatogram> getChromatogramPredicate(List<Integer> chromatogramsToParse) {
    return c -> chromatogramsToParse.contains(c.getChromatogramNumber());
  }

}
