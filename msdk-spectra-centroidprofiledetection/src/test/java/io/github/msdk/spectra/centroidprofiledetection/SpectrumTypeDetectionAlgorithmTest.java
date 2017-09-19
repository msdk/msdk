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

package io.github.msdk.spectra.centroidprofiledetection;

import java.io.File;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.spectra.centroidprofiledetection.SpectrumTypeDetectionAlgorithm;

public class SpectrumTypeDetectionAlgorithmTest {

  @Test
  public void testCentroided1() throws Exception {

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("centroided1.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("centroided2.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("centroided3.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("centroided4.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("centroided5.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("centroided6.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("centroided7.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("centroided8.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("centroided9.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("centroided10.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("centroided11.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("centroided12.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("centroided13.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("centroided14.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("centroided15.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("centroided16.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("profile1.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("profile2.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("profile3.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("profile4.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("profile5.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("profile6.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("profile7.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("profile8.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("profile9.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("profile10.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("thresholded1.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("thresholded2.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("thresholded3.mzML").toURI());
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

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("thresholded4.mzML").toURI());
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
