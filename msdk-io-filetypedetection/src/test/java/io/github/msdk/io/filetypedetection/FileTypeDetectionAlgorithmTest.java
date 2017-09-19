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

package io.github.msdk.io.filetypedetection;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.FileType;

public class FileTypeDetectionAlgorithmTest {

  private static final String TEST_DATA_PATH = "src/test/resources/";

  @Test
  public void testNetCDF() throws MSDKException, IOException {
    File fileName = new File(TEST_DATA_PATH + "wt15.CDF");
    FileType fileType = FileTypeDetectionAlgorithm.detectDataFileType(fileName);
    Assert.assertEquals(FileType.NETCDF, fileType);
  }

  @Test
  public void testMzXML() throws MSDKException, IOException {
    File fileName = new File(TEST_DATA_PATH + "A1-0_A2.mzXML");
    FileType fileType = FileTypeDetectionAlgorithm.detectDataFileType(fileName);
    Assert.assertEquals(FileType.MZXML, fileType);
  }

  @Test
  public void testMzML() throws MSDKException, IOException {
    File fileName = new File(TEST_DATA_PATH + "RawCentriodCidWithMsLevelInRefParamGroup.mzML");
    FileType fileType = FileTypeDetectionAlgorithm.detectDataFileType(fileName);
    Assert.assertEquals(FileType.MZML, fileType);
  }

  @Test
  public void testMzData() throws MSDKException, IOException {
    File fileName = new File(TEST_DATA_PATH + "test.mzData");
    FileType fileType = FileTypeDetectionAlgorithm.detectDataFileType(fileName);
    Assert.assertEquals(FileType.MZDATA, fileType);
  }

  @Test
  public void testThermoRaw() throws MSDKException, IOException {
    File fileName = new File(TEST_DATA_PATH + "RP240K_01.raw");
    FileType fileType = FileTypeDetectionAlgorithm.detectDataFileType(fileName);
    Assert.assertEquals(FileType.THERMO_RAW, fileType);
  }

  @Test
  public void testWatersRaw() throws MSDKException, IOException {
    File fileName = new File(TEST_DATA_PATH + "20150813-63.raw");
    FileType fileType = FileTypeDetectionAlgorithm.detectDataFileType(fileName);
    Assert.assertEquals(FileType.WATERS_RAW, fileType);
  }

  @Test
  public void testMzTAB() throws MSDKException, IOException {
    File fileName = new File(TEST_DATA_PATH + "lipidomics-HFD-LD-study-PL-DG-SM.mzTab");
    FileType fileType = FileTypeDetectionAlgorithm.detectDataFileType(fileName);
    Assert.assertEquals(FileType.MZTAB, fileType);
  }

  @Test
  public void testMzDB() throws MSDKException, IOException {
    File fileName = new File(TEST_DATA_PATH + "OVEMB150205_12.raw.0.9.8_truncated.mzDB");
    FileType fileType = FileTypeDetectionAlgorithm.detectDataFileType(fileName);
    Assert.assertEquals(FileType.MZDB, fileType);
  }

}
