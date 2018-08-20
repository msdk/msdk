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

package io.github.msdk.io.mzml;

import io.github.msdk.io.mzml.data.MzMLRawDataFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Assert;
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
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import io.github.msdk.io.mzml.data.MzMLMsScan;
import io.github.msdk.util.MsSpectrumUtil;

public class MzMLFileImportMethodTest {

  private Path getResourcePath(String resource) throws MSDKException {
    final URL url = MzMLFileImportMethodTest.class.getClassLoader().getResource(resource);
    try {
      return Paths.get(url.toURI()).toAbsolutePath();
    } catch (URISyntaxException e) {
      throw new MSDKException(e);
    }
  }

  @Test
  public void testFileWithUV() throws MSDKException {

    // Import the file
    String file = "mzML_with_UV.mzML";
    final Path path = getResourcePath(file);
    final File inputFile = path.toFile();
    MzMLFileImportMethod mzParser = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = mzParser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, mzParser.getFinishedPercentage(), 0.0001);

    // The file has 27 scans
    Assert.assertEquals(27, rawFile.getScans().size());

    // 15th Scan, #2114
    MzMLMsScan scan = (MzMLMsScan) rawFile.getScans().get(14);
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

    rawFile.dispose();

    // Import the file using the alternate constructor
    Assert.assertNotNull(new File(path.toString()));
    MzMLFileImportMethod mzParser2 = new MzMLFileImportMethod(path);
    RawDataFile rawFile2 = mzParser2.execute();
    Assert.assertNotNull(rawFile2);
    Assert.assertEquals(1.0, mzParser2.getFinishedPercentage(), 0.0001);

    // The file has 27 scans
    Assert.assertEquals(27, rawFile.getScans().size());

    // 18th Scan, #2117
    MzMLMsScan scan2 = (MzMLMsScan) rawFile2.getScans().get(17);
    Assert.assertNotNull(scan2);
    Assert.assertNotNull(scan2.getMzValues());
    Assert.assertNotNull(scan2.getIntensityValues());
    Assert.assertEquals(Integer.valueOf(scan2.getMzValues().length), scan2.getNumberOfDataPoints());
    Assert.assertEquals(Integer.valueOf(scan2.getIntensityValues().length),
        scan2.getNumberOfDataPoints());
    Assert.assertEquals(Integer.valueOf(2117), scan2.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan2.getSpectrumType());
    Assert.assertEquals(Float.valueOf(43900.855f), scan2.getTIC(), 10);
    Assert.assertEquals(100.300285339355, scan2.getMzRange().lowerEndpoint(), 0.000001);
    Assert.assertEquals(999.323547363281, scan2.getMzRange().upperEndpoint(), 0.000001);
    Assert.assertEquals("- c ESI Q1MS [100.000-1000.000]", scan2.getScanDefinition());
    Assert.assertEquals(Integer.valueOf(1), scan2.getMsLevel());
    Assert.assertEquals(PolarityType.NEGATIVE, scan2.getPolarity());
    Assert.assertEquals(Float.valueOf((float) (18.919083333333 * 60)), scan2.getRetentionTime());

    rawFile2.dispose();
  }

  /**
   * Used for debugging to manually iterate over chromatograms and every 100th scan.
   * @throws MSDKException
   */
  //@Test
  public void testCustomFile() throws MSDKException {
    //String file = ... "QC_Shew_12_02_SPE_Run-01_10Dec12_Eagle_12-12-10.mzml";
    String file = "";
    File f = new File(file);
    MzMLFileImportMethod p = new MzMLFileImportMethod(f);
    MzMLRawDataFile data = p.execute();
    double mzSum0 = 0;
    double abSum0 = 0;
    System.out.println("Iterating over scans");

    for (Chromatogram c : data.getChromatograms()) {
      System.out.println("Chromatogram #" + c.getChromatogramNumber() + " type " + c.getChromatogramType());
      System.out.printf("It has %d intensity points, %d retention time points", c.getIntensityValues().length, c.getRetentionTimes().length);
    }

    int count = 0;
    int total = data.getScans().size();
    for (MsScan scan : data.getScans()) {
      if (count % 100 != 0) continue;
      double[] mz = scan.getMzValues();
      float[] ab = scan.getIntensityValues();
      if (mz.length != ab.length)
        throw new MSDKException("mz and intensity arrays of different lengths, scan #" + scan.getScanNumber());
      if (mz.length > 0) {
        // side effects so that the loop didn't get cut out by the compiler
        mzSum0 += mz[0];
        abSum0 += ab[0];
      }
      count++;
    }
    System.out.println("  Done");
    // side effects so that the loop didn't get cut out by the compiler
    System.out.printf("Total mz sum is %.4f, ab sum is %.4ff\n", mzSum0, abSum0);

  }

  @Test
  public void test5peptideFT() throws MSDKException {

    float intensityBuffer[];

    // Set up Predicate<MsScan>
    List<Integer> scansToParse = new ArrayList<>();
    scansToParse.addAll(Arrays.asList(2, 3, 5));
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

    // The file has 7 scans, 3 pass the predicate
    List<MsScan> scans = rawFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(7, scans.size());

    // 2nd scan, #2
    MsScan scan2 = scans.get(1);
    Assert.assertEquals(Integer.valueOf(2), scan2.getScanNumber());
    Assert.assertEquals(MsSpectrumType.PROFILE, scan2.getSpectrumType());
    Assert.assertEquals(Integer.valueOf(1), scan2.getMsLevel());
    Assert.assertEquals(0.474f, scan2.getRetentionTime(), 0.01f);
    Assert.assertEquals(0, scan2.getIsolations().size());
    Assert.assertEquals(PolarityType.POSITIVE, scan2.getPolarity());
    Assert.assertEquals(209.1818184554577, scan2.getMzValues()[100], 0.00001);
    scan2.getMzValues();
    intensityBuffer = scan2.getIntensityValues();
    Assert.assertEquals(19800, (int) scan2.getNumberOfDataPoints());
    Float scan2maxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan2.getNumberOfDataPoints());
    Assert.assertEquals(1.8E5f, scan2maxInt, 1E4f);

    // 3rd scan, #3
    MsScan scan3 = scans.get(2);
    Assert.assertEquals(Integer.valueOf(3), scan3.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan3.getSpectrumType());
    Assert.assertEquals(Integer.valueOf(2), scan3.getMsLevel());
    Assert.assertEquals(1, scan3.getIsolations().size());
    Assert.assertEquals(PolarityType.POSITIVE, scan3.getPolarity());

    // 5th scan, #5
    MsScan scan5 = scans.get(4);
    Assert.assertEquals(Integer.valueOf(5), scan5.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan5.getSpectrumType());
    Assert.assertEquals(Integer.valueOf(2), scan5.getMsLevel());
    Assert.assertEquals(1, scan5.getIsolations().size());
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
  public void testPwizTiny() throws MSDKException, FileNotFoundException {

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
    // InputStream constructor
    FileInputStream fis = new FileInputStream(inputFile);
    Assert.assertNotNull(fis);
    MzMLFileImportMethod parser =
        new MzMLFileImportMethod(fis, msScanPredicate, chromatogramPredicate);
    RawDataFile rawFile = parser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // The file has 4 scans, 1 scan in RawFile
    List<MsScan> scans = rawFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(scansToParse.size(), scans.size());

    // 1st scan, #20
    MsScan scan2 = scans.get(0);
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

    rawFile.dispose();

  }


  @Test
  public void testParamGroup() throws MSDKException {

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

    // The file has 102 scans, 2 pass the predicate
    List<MsScan> scans = rawFile.getScans();
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

    rawFile.dispose();

  }


  @Test
  public void testCompressedAndUncompressed() throws MSDKException {

    // Set up Predicate<MsScan>
    List<Integer> scansToParse = new ArrayList<>();
    scansToParse.addAll(Arrays.asList(1, 2, 3));
    Predicate<MsScan> msScanPredicate = getMsScanPredicate(scansToParse);
    List<Integer> chromatogramsToParse = new ArrayList<>();
    Predicate<Chromatogram> chromatogramPredicate = getChromatogramPredicate(chromatogramsToParse);

    // Import the compressed file
    String fileCompressed = "MzMLFile_7_compressed.mzML";
    File compressedFile = getResourcePath(fileCompressed).toFile();
    Assert.assertTrue(compressedFile.canRead());
    MzMLFileImportMethod parser =
        new MzMLFileImportMethod(compressedFile, msScanPredicate, chromatogramPredicate);
    RawDataFile compressedRaw = parser.execute();
    Assert.assertNotNull(compressedRaw);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // Import the uncompressed file
    String fileUncompressed = "MzMLFile_7_uncompressed.mzML";
    File unCompressedFile = getResourcePath(fileUncompressed).toFile();
    Assert.assertTrue(unCompressedFile.canRead());
    parser = new MzMLFileImportMethod(unCompressedFile, msScanPredicate, chromatogramPredicate);
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

    // Set up Predicate<MsScan>
    List<Integer> scansToParse = new ArrayList<>();
    Predicate<MsScan> msScanPredicate = getMsScanPredicate(scansToParse);
    List<Integer> chromatogramsToParse = new ArrayList<>();
    chromatogramsToParse.addAll(Arrays.asList(1, 2, 4, 19, 36));
    Predicate<Chromatogram> chromatogramPredicate = getChromatogramPredicate(chromatogramsToParse);

    // Import the file
    String file = "SRM.mzML";
    File inputFile = getResourcePath(file).toFile();
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod parser =
        new MzMLFileImportMethod(inputFile, msScanPredicate, chromatogramPredicate);
    RawDataFile rawFile = parser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // The file has 37 chromatograms, 5 pass the predicate
    List<Chromatogram> chromatograms = rawFile.getChromatograms();
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

    rawFile.dispose();
  }



  @Test
  public void testEmptyScan() throws MSDKException {

    float intensityBuffer[];

    // Set up Predicate<MsScan>
    List<Integer> scansToParse = new ArrayList<>();
    scansToParse.addAll(Arrays.asList(422));
    Predicate<MsScan> msScanPredicate = getMsScanPredicate(scansToParse);
    List<Integer> chromatogramsToParse = new ArrayList<>();
    Predicate<Chromatogram> chromatogramPredicate = getChromatogramPredicate(chromatogramsToParse);

    // Import the file
    String file = "emptyScan.mzML";
    File inputFile = getResourcePath(file).toFile();
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod parser =
        new MzMLFileImportMethod(inputFile, msScanPredicate, chromatogramPredicate);
    RawDataFile rawFile = parser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // The file has 1 scan, with no data points
    List<MsScan> scans = rawFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(1, scans.size());

    // 1st scan, #422
    MsScan scan2 = scans.get(0);
    Assert.assertEquals(Integer.valueOf(422), scan2.getScanNumber());
    Assert.assertEquals(MsSpectrumType.CENTROIDED, scan2.getSpectrumType());
    Assert.assertEquals(Integer.valueOf(2), scan2.getMsLevel());
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

    // Set up Predicate<MsScan>
    List<Integer> scansToParse = new ArrayList<>();
    Predicate<MsScan> msScanPredicate = getMsScanPredicate(scansToParse);
    List<Integer> chromatogramsToParse = new ArrayList<>();
    Predicate<Chromatogram> chromatogramPredicate = getChromatogramPredicate(chromatogramsToParse);

    // Try importing an invalid (truncated file).
    // Import should throw an MSDKException.
    String file = "truncated.mzML";
    File inputFile = getResourcePath(file).toFile();
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod parser =
        new MzMLFileImportMethod(inputFile, msScanPredicate, chromatogramPredicate);
    parser.execute();

  }

  @Test
  public void testZlibAndNumpressCompression() throws MSDKException {

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

    // The file has 6 scans, 1 pass the predicate
    List<MsScan> scans = rawFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(6, scans.size());

    // 4th, #2103
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
    Assert.assertEquals(8746.9599f, scan2maxInt, 0.1f);
    Assert.assertEquals(8746.96f, scan2maxInt, 0.1f);
    Assert.assertEquals(58989.76953125f, scan4.getTIC(), 0.1);
    Assert.assertEquals(58989.77f, scan4.getTIC(), 0.1);
    Assert.assertEquals(100.317253112793, scan4.getMzRange().lowerEndpoint(), 0.000001);
    Assert.assertEquals(999.715515136719, scan4.getMzRange().upperEndpoint(), 0.000001);
    Assert.assertEquals("- c ESI Q1MS [100.000-1000.000]", scan4.getScanDefinition());

    // Test isolation data
    List<IsolationInfo> scan2isolations = scan4.getIsolations();
    Assert.assertEquals(0, scan2isolations.size());

    // The file has 2 chromatograms, 1 passed the predicate
    List<Chromatogram> chromatograms = rawFile.getChromatograms();
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

    rawFile.dispose();

  }

  @Test
  public void testPredicate() throws Exception {

    // Import the file
    String file = "5peptideFT.mzML";
    Path inputFile = getResourcePath(file);
    MzMLFileImportMethod parser = new MzMLFileImportMethod(inputFile, s -> false, c -> false);
    RawDataFile rawFile = parser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // The file has 7 scans, 2 pass the predicate
    List<MsScan> scans = rawFile.getScans();
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
    float[] intensityBuffer = scan2.getIntensityValues();
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
  }

  private Predicate<MsScan> getMsScanPredicate(List<Integer> scansToParse) {
    return s -> scansToParse.contains(s.getScanNumber());
  }

  private Predicate<Chromatogram> getChromatogramPredicate(List<Integer> chromatogramsToParse) {
    return c -> chromatogramsToParse.contains(c.getChromatogramNumber());
  }
}
