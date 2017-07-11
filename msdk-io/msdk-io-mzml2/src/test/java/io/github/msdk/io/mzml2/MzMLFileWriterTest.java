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

package io.github.msdk.io.mzml2;

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
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.chromatograms.ChromatogramType;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.ActivationType;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzml2.data.MzMLCompressionType;
import io.github.msdk.util.MsSpectrumUtil;

public class MzMLFileWriterTest {

  private Path getResourcePath(String resource) throws MSDKException {
    final URL url = MzMLFileParserTest.class.getClassLoader().getResource(resource);
    try {
      return Paths.get(url.toURI()).toAbsolutePath();
    } catch (URISyntaxException e) {
      throw new MSDKException(e);
    }
  }

  @Test
  public void test5peptideFT() throws MSDKException, IOException {

    float intensityBuffer[];

    // Import the file
    String file = "5peptideFT.mzML";
    File inputFile = getResourcePath(file).toFile();
    Assert.assertTrue(inputFile.canRead());
    MzMLFileParser parser = new MzMLFileParser(inputFile);
    RawDataFile rawFile = parser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // Export the file to a new mzML
    File tempFile = File.createTempFile("msdk", ".mzML");
    tempFile.deleteOnExit();
    MzMLFileWriter exporter = new MzMLFileWriter(rawFile, tempFile,
        MzMLCompressionType.NUMPRESS_LINPRED_ZLIB, MzMLCompressionType.ZLIB);
    exporter.execute();
    Assert.assertEquals(1.0, exporter.getFinishedPercentage(), 0.0001);

    // Import the new mzML
    parser = new MzMLFileParser(tempFile);
    RawDataFile newMzMLFile = parser.execute();
    Assert.assertNotNull(newMzMLFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // Check number of scans
    List<MsScan> scans = newMzMLFile.getScans();
    Assert.assertNotNull(scans);
    Assert.assertEquals(rawFile.getScans().size(), scans.size());

    // 2nd scan, #2
    MsScan scan2 = scans.get(1);
    Assert.assertEquals(new Integer(2), scan2.getScanNumber());
    Assert.assertEquals(MsSpectrumType.PROFILE, scan2.getSpectrumType());
    Assert.assertEquals(new Integer(1), scan2.getMsLevel());
    Assert.assertEquals(0.474f, scan2.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.POSITIVE, scan2.getPolarity());
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

    // Import the file
    String file = "SRM.mzML";
    File inputFile = getResourcePath(file).toFile();
    Assert.assertTrue(inputFile.canRead());
    MzMLFileParser parser = new MzMLFileParser(inputFile);
    RawDataFile rawFile = parser.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, parser.getFinishedPercentage(), 0.0001);

    // Export the file to a new mzML
    File tempFile = File.createTempFile("msdk", ".mzML");
    tempFile.deleteOnExit();
    MzMLFileWriter exporter = new MzMLFileWriter(rawFile, tempFile,
        MzMLCompressionType.NUMPRESS_LINPRED_ZLIB, MzMLCompressionType.ZLIB);
    exporter.execute();
    Assert.assertEquals(1.0, exporter.getFinishedPercentage(), 0.0001);

    // Import the new mzML
    parser = new MzMLFileParser(tempFile);
    RawDataFile newMzMLFile = parser.execute();
    Assert.assertNotNull(newMzMLFile);
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

    // Check m/z values
    Assert.assertEquals(407.706, chromatograms.get(1).getMz(), 0.001);
    Assert.assertEquals(1084.486, chromatograms.get(18).getMz(), 0.001);
    Assert.assertEquals(1042.516, chromatograms.get(35).getMz(), 0.001);

    rawFile.dispose();

    // Cleanup
    rawFile.dispose();
    newMzMLFile.dispose();
  }

}
