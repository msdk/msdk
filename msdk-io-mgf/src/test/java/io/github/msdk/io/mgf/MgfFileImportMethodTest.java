package io.github.msdk.io.mgf;

import io.github.msdk.MSDKException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by evger on 17-May-18.
 */
public class MgfFileImportMethodTest {
  @Test
  public void testMgfImport() throws IOException, MSDKException {
    final int expectedSize = 1;
    final String expectedTitle = "example.9.9.2";
    final long expectedNumberDatapoints = 18;
    final int expectedCharge = 2;

    File file = new File("target/test-classes/test_query.mgf");
    MgfFileImportMethod importMethod = new MgfFileImportMethod(file);
    Collection<MgfMsSpectrum> spectrums = importMethod.execute();
    int size = spectrums.size();
    MgfMsSpectrum spectrum = (MgfMsSpectrum)spectrums.toArray()[0];

    Assert.assertEquals(expectedSize, size);
    Assert.assertEquals(expectedTitle, spectrum.getTitle());
    Assert.assertEquals(Long.valueOf(expectedNumberDatapoints), Long.valueOf(spectrum.getNumberOfDataPoints()));
    Assert.assertEquals(expectedCharge, spectrum.getPrecursorCharge());
  }

}
