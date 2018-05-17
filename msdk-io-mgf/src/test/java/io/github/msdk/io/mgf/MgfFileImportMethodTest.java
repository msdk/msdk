package io.github.msdk.io.mgf;

import io.github.msdk.MSDKException;
import java.io.File;
import java.io.IOException;
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
    File file = new File("target/test-classes/test_query.mgf");
    MgfFileImportMethod importMethod = new MgfFileImportMethod(file);
    importMethod.execute();

    Assert.assertEquals(true, true);
  }

}
