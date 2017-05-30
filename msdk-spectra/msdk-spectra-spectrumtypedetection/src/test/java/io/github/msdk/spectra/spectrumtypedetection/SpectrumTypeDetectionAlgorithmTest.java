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

package io.github.msdk.spectra.spectrumtypedetection;

import java.io.File;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;

public class SpectrumTypeDetectionAlgorithmTest {

  private static final String TEST_DATA_PATH = "src/test/resources/";


  @Test
  public void testCentroided1() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "centroided1.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.CENTROIDED;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testCentroided2() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "centroided2.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.CENTROIDED;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testCentroided3() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "centroided3.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.CENTROIDED;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testCentroided4() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "centroided4.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.CENTROIDED;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testCentroided5() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "centroided5.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.CENTROIDED;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testCentroided6() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "centroided6.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.CENTROIDED;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testCentroided7() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "centroided7.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.CENTROIDED;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testCentroided8() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "centroided8.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.CENTROIDED;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testCentroided9() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "centroided9.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.CENTROIDED;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testCentroided10() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "centroided10.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.CENTROIDED;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testCentroided11() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "centroided11.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.CENTROIDED;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testCentroided12() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "centroided12.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.CENTROIDED;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testCentroided13() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "centroided13.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.CENTROIDED;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testCentroided14() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "centroided14.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.CENTROIDED;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testCentroided15() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "centroided15.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.CENTROIDED;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  @Ignore("Algorithm fails on this test, need a better algorithm")
  public void testCentroided16() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "centroided16.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.CENTROIDED;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testProfile1() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "profile1.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.PROFILE;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testProfile2() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "profile2.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.PROFILE;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testProfile3() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "profile3.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.PROFILE;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testProfile4() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "profile4.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.PROFILE;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testProfile5() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "profile5.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.PROFILE;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testProfile6() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "profile6.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.PROFILE;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testProfile7() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "profile7.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.PROFILE;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testProfile8() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "profile8.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.PROFILE;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testProfile9() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "profile9.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.PROFILE;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  @Ignore("Algorithm fails on this test, need a better algorithm")
  public void testProfile10() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "profile10.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.PROFILE;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testThresholded1() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "thresholded1.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.THRESHOLDED;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  public void testThresholded2() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "thresholded2.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.THRESHOLDED;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  @Ignore("Algorithm fails on this test, need a better algorithm")
  public void testThresholded3() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "thresholded3.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.THRESHOLDED;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }


  @Test
  @Ignore("Algorithm fails on this test, need a better algorithm")
  public void testThresholded4() throws Exception {

    File inputFile = new File(TEST_DATA_PATH + "thresholded4.mzML");
    Assert.assertTrue(inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    for (MsScan scan : rawFile.getScans()) {
      final MsSpectrumType expectedType = MsSpectrumType.THRESHOLDED;
      final MsSpectrumType detectedType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(scan);
      Assert.assertEquals("Scan type wrongly detected for scan " + scan.getScanNumber() + " in "
          + rawFile.getName(), expectedType, detectedType);
    }

    rawFile.dispose();

  }

}
