
import de.unijena.bioinf.sirius.IdentificationResult;
import io.github.msdk.MSDKException;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

/**
 * Created by evger on 15-May-18.
 */
public class SiriusMs2Test {

  @Test
  public void testCreateMs2Experiment() throws MSDKException, IOException {
    String ms2Path = "target/test-classes/query.txt";
    List<IdentificationResult> list = ISirius.identifyMs2Spectrum(ms2Path);
    for (IdentificationResult r : list) {
      System.out.println(r.toString());
    }
  }
}
