package io.github.msdk.io.mgf;

import static junit.framework.TestCase.fail;

import io.github.msdk.MSDKException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Created by evger on 17-May-18.
 */


public class MgfFileImportMethodTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void mgfImportTest() throws IOException, MSDKException {
    final int expectedSize = 1;
    final String expectedTitle = "example.9.9.2";
    final long expectedNumberDatapoints = 18;
    final int expectedCharge = 2;

    File file = new File("target/test-classes/test_query.mgf");
    MgfFileImportMethod importMethod = new MgfFileImportMethod(file);
    List<MgfMsSpectrum> spectrums = importMethod.execute();
    int size = spectrums.size();
    MgfMsSpectrum spectrum = (MgfMsSpectrum)spectrums.toArray()[0];

    Assert.assertEquals(expectedSize, size);
    Assert.assertEquals(expectedTitle, spectrum.getTitle());
    Assert.assertEquals(Long.valueOf(expectedNumberDatapoints), Long.valueOf(spectrum.getNumberOfDataPoints()));
    Assert.assertEquals(expectedCharge, spectrum.getPrecursorCharge());
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
    final long expectedNumberDatapoints[] = {53, 66, 13, 16, 9, 9, 9, 14, 18, 13};
    final int expectedCharges[] = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2};

    File file = new File("target/test-classes/F001257.mgf");
    MgfFileImportMethod importMethod = new MgfFileImportMethod(file);
    List<MgfMsSpectrum> spectrums = importMethod.execute();

    int size = spectrums.size();
    Assert.assertEquals(expectedSize, size);

    MgfMsSpectrum mgfSpectrum;
    Object[] objSpectrums = spectrums.toArray();
    String titles[] = new String[size];
    long dataPoints[] = new long[size];
    int charges[] = new int[size];
    for (int i = 0; i < size; i++) {
      mgfSpectrum = (MgfMsSpectrum)objSpectrums[i];
      titles[i] = mgfSpectrum.getTitle();
      dataPoints[i] = mgfSpectrum.getNumberOfDataPoints();
      charges[i] = mgfSpectrum.getPrecursorCharge();
    }

    Assert.assertArrayEquals(expectedTitles, titles);
    Assert.assertArrayEquals(expectedNumberDatapoints, dataPoints);
    Assert.assertArrayEquals(expectedCharges, charges);
  }

  @Test
  public void malformedInputRegexpTest() throws MSDKException {
    File file = new File("target/test-classes/malformed_query.mgf");
    MgfFileImportMethod importMethod = new MgfFileImportMethod(file);


    thrown.expect(MSDKException.class);
    thrown.expectMessage("Incorrect data format");
    List<MgfMsSpectrum> spectrums = importMethod.execute();
  }

}
