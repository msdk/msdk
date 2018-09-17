/*
 * (C) Copyright 2015-2018 by MSDK Development Team
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

package io.github.msdk.io.mgf;

import io.github.msdk.MSDKException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class MgfFileImportMethodTest {
  private Path getResourcePath(String resource) throws MSDKException {
    final URL url = MgfFileImportMethodTest.class.getClassLoader().getResource(resource);
    try {
      return Paths.get(url.toURI()).toAbsolutePath();
    } catch (URISyntaxException e) {
      throw new MSDKException(e);
    }
  }

  @Test
  public void mgfImportTest() throws IOException, MSDKException {
    final int expectedSize = 2;
    final String expectedTitle[] = {"example.9.9.2", "negative_charge"};
    final Integer expectedNumberDatapoints[] = {19, 16};
    final int[] expectedCharges = {2, -3};
    final String file = "test_query.mgf";

    File inputFile = getResourcePath(file).toFile();
    MgfFileImportMethod importMethod = new MgfFileImportMethod(inputFile);

    List<MgfMsSpectrum> spectrums = importMethod.execute();
    int size = spectrums.size();
    Assert.assertEquals(expectedSize, size);

    String titles[] = new String[expectedSize];
    int charges[] = new int[expectedSize];
    Integer dataPoints[] = new Integer[expectedSize];

    for (int i = 0; i < spectrums.size(); i++) {
      MgfMsSpectrum ms = spectrums.get(i);
      titles[i] = ms.getTitle();
      charges[i] = ms.getPrecursorCharge();
      dataPoints[i] = ms.getNumberOfDataPoints();
    }

    Assert.assertArrayEquals(expectedTitle, titles);
    Assert.assertArrayEquals(expectedNumberDatapoints, dataPoints);
    Assert.assertArrayEquals(expectedCharges, charges);
  }

  @Test
  public void multipleMgfImportTest() throws IOException, MSDKException {
    final int expectedSize = 10;
    final String expectedTitles[] = {
        "PRIDE_Exp_mzData_Ac_9266.xml_id_1",
        "PRIDE_Exp_mzData_Ac_9266.xml_id_2",
        "PRIDE_Exp_mzData_Ac_9266.xml_id_3",
        "PRIDE_Exp_mzData_Ac_9266.xml_id_4",
        "PRIDE_Exp_mzData_Ac_9266.xml_id_5",
        "PRIDE_Exp_mzData_Ac_9266.xml_id_6",
        "PRIDE_Exp_mzData_Ac_9266.xml_id_7",
        "PRIDE_Exp_mzData_Ac_9266.xml_id_8",
        "PRIDE_Exp_mzData_Ac_9266.xml_id_9",
        "PRIDE_Exp_mzData_Ac_9266.xml_id_10"
    };
    final long expectedNumberDatapoints[] = {54, 67, 14, 17, 10, 10, 10, 15, 19, 14};
    final int expectedCharges[] = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
    final String file = "F001257.mgf";

    File inputFile = getResourcePath(file).toFile();
    MgfFileImportMethod importMethod = new MgfFileImportMethod(inputFile);

    List<MgfMsSpectrum> spectrums = importMethod.execute();
    int size = spectrums.size();
    Assert.assertEquals(expectedSize, size);

    MgfMsSpectrum mgfSpectrum;
    String titles[] = new String[size];
    long dataPoints[] = new long[size];
    int charges[] = new int[size];

    for (int i = 0; i < size; i++) {
      mgfSpectrum = spectrums.get(i);
      titles[i] = mgfSpectrum.getTitle();
      dataPoints[i] = mgfSpectrum.getNumberOfDataPoints();
      charges[i] = mgfSpectrum.getPrecursorCharge();
    }

    Assert.assertArrayEquals(expectedTitles, titles);
    Assert.assertArrayEquals(expectedNumberDatapoints, dataPoints);
    Assert.assertArrayEquals(expectedCharges, charges);
  }

  @Test
  public void levelsTest() throws MSDKException {
    final String fname = "levels.mgf";
    final File inputFile = getResourcePath(fname).toFile();
    final Integer[] expectedLevels = {1, 2, null};

    MgfFileImportMethod importMethod = new MgfFileImportMethod(inputFile);
    List<MgfMsSpectrum> spectrums = importMethod.execute();

    Integer[] levels = new Integer[expectedLevels.length];
    for (int i = 0; i < spectrums.size(); i++) {
      MgfMsSpectrum ms = spectrums.get(i);
      levels[i] = ms.getMsLevel();
    }

    Assert.assertArrayEquals(expectedLevels, levels);
  }

  @Test(expected = MSDKException.class)
  public void malformedInputRegexpTest() throws MSDKException {
    final String file = "malformed_query.mgf";
    File inputFile = getResourcePath(file).toFile();
    MgfFileImportMethod importMethod = new MgfFileImportMethod(inputFile);

    importMethod.execute();
  }

}
