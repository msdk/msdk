
import de.unijena.bioinf.sirius.IdentificationResult;
import io.github.msdk.MSDKException;
import io.github.msdk.io.msp.MspImportAlgorithm;
import io.github.msdk.io.msp.MspSpectrum;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

/**
 * Created by evger on 15-May-18.
 */
public class SiriusMs2Test {

  @Test
  public void testCreateMs2Experiment() throws MSDKException, IOException {
    String ms2Path = "target/test-classes/sample.msp";
    final double parentMass = 231.065;


    File inputFile = new File(ms2Path);
    MspSpectrum mspSpectrum = MspImportAlgorithm.parseMspFromFile(inputFile);
    double mz[] = mspSpectrum.getMzValues();
    double intensive[] = LocalArrayUtil.convertToDoubles(mspSpectrum.getIntensityValues());

    List<IdentificationResult> list = ISirius.identifyMs2Spectrum(mz, intensive, parentMass);
    for (IdentificationResult r : list) {
      System.out.println(r.toString());
    }
  }
}
