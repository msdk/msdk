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
    final String[] expectedResults = {"C13H10O4", "C11H8N3O3", "C9H13NO4P", "C7H11N4O3P", "C6H10N6O2S"};


    File inputFile = new File(ms2MspPath);
    MspSpectrum mspSpectrum = MspImportAlgorithm.parseMspFromFile(inputFile);
    double mz[] = mspSpectrum.getMzValues();
    double intensity[] = LocalArrayUtil.convertToDoubles(mspSpectrum.getIntensityValues());
    Pair<double[], double[]> ms2pair = new Pair<>(mz, intensity);
    SiriusIdentificationMethod siriusMethod = new SiriusIdentificationMethod();


    List<IdentificationResult> list = siriusMethod
        .identifyMs2Spectrum(null, ms2pair, parentMass, ionName, 5);


    String[] results = new String[5];
    int i = 0;


    for (IdentificationResult r : list) {
      results[i++] = r.getMolecularFormula().toString();
    }
    Assert.assertArrayEquals(expectedResults, results);
  }

  @Test
  public void testCreateExperimentMs1Ms2Custom() throws MSDKException, IOException {
    final double precursorMass = 315.123;
    final String ionName = "[M+H]+";
    final String[] expectedResults = {"C18H18O5", "C12H19N4O4P"};

    String ms1Path = "target/test-classes/flavokavainA_MS1.txt";
    String ms2Path = "target/test-classes/flavokavainA_MS2.txt";
    SiriusIdentificationMethod siriusMethod = new SiriusIdentificationMethod();

    Pair<double[], double[]> ms1Spectrum = siriusMethod.readCustomMsFile(ms1Path, "\t");
    Pair<double[], double[]> ms2Spectrum = siriusMethod.readCustomMsFile(ms2Path, "\t");

    List<IdentificationResult> list = siriusMethod.identifyMs2Spectrum(ms1Spectrum,
                                                                  ms2Spectrum,
                                                                  precursorMass,
                                                                  ionName,
                                                                  2);

    String[] results = new String[2];
    int i = 0;


// TODO: Fix the difference after second element
    for (IdentificationResult r : list) {
      results[i++] = r.getMolecularFormula().toString();
    }

    Assert.assertArrayEquals(expectedResults, results);

  }
}
