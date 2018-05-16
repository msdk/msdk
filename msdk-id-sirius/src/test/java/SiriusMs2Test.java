import de.unijena.bioinf.sirius.IdentificationResult;
import io.github.msdk.MSDKException;
import io.github.msdk.io.msp.MspImportAlgorithm;
import io.github.msdk.io.msp.MspSpectrum;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by evger on 15-May-18.
 */
public class SiriusMs2Test {

  @Test
  public void testCreateMs2ExperimentMsp() throws MSDKException, IOException {
    String ms2MspPath = "target/test-classes/sample.msp";
    final double parentMass = 231.065;
    final String ionName = "[M+H]+";


    File inputFile = new File(ms2MspPath);
    MspSpectrum mspSpectrum = MspImportAlgorithm.parseMspFromFile(inputFile);
    double mz[] = mspSpectrum.getMzValues();
    double intensive[] = LocalArrayUtil.convertToDoubles(mspSpectrum.getIntensityValues());
    Pair<double[], double[]> ms2pair = new Pair<>(mz, intensive);

    List<IdentificationResult> list = ISirius.identifyMs2Spectrum(null, ms2pair, parentMass, ionName);
    for (IdentificationResult r : list) {
      System.out.println(r.toString());
    }

    /* Temporary solution  */
    Assert.assertEquals(true, true);
  }

  @Test
  public void testCreateExperimentMs1Ms2Custom() throws MSDKException, IOException {
    String ms1Path = "target/test-classes/bisnoryangonin_MS1.txt";
    String ms2Path = "target/test-classes/bisnoryangonin_MS1.txt";
    final double precursorMass = 231.0647;
    final String ionName = "[M+H]+";

    Pair<double[], double[]> ms1Spectrum = ISirius.readCustomMsFile(ms1Path);
    Pair<double[], double[]> ms2Spectrum = ISirius.readCustomMsFile(ms2Path);

    List<IdentificationResult> list = ISirius.identifyMs2Spectrum(ms1Spectrum,
                                                                  ms2Spectrum,
                                                                  precursorMass,
                                                                  ionName);

    File temp = new File("temp.txt");
    for (IdentificationResult r : list) {
      String s = String.format("%s :: %f", r.getMolecularFormula().toString(), r.getIsotopeScore());
      System.out.println(s);
      r.writeTreeToFile(temp);
    }
//    System.out.println(list.get(0).getJSONTree());
    /* Temporary solution  */
    Assert.assertEquals(true, true);
  }
}
