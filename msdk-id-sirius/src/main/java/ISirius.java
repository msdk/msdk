import de.unijena.bioinf.ChemistryBase.chem.Ionization;
import de.unijena.bioinf.ChemistryBase.ms.Ms2Experiment;
import de.unijena.bioinf.ChemistryBase.ms.Peak;
import de.unijena.bioinf.ChemistryBase.ms.Spectrum;
import de.unijena.bioinf.sirius.IdentificationResult;
import de.unijena.bioinf.sirius.Sirius;
import io.github.msdk.MSDKException;
import io.github.msdk.io.msp.MspImportAlgorithm;
import io.github.msdk.io.msp.MspSpectrum;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.javatuples.Pair;

/**
 * Created by evger on 14-May-18.
 */

public class ISirius {

  private static Pair<double[], double[]> readCustomMsFile(String path) throws IOException {
    Scanner sc = new Scanner(new File(path));
    ArrayList<String> strings = new ArrayList<>();
    while (sc.hasNext()) {
      strings.add(sc.nextLine());
    }
    sc.close();

    double mz[] = new double[strings.size()];
    double intensive[] = new double[strings.size()];

    int index = 0;
    for (String s : strings) {
      String[] splitted = s.split("\t");
      mz[index] = Double.parseDouble(splitted[0]);
      intensive[index++] = Double.parseDouble(splitted[1]);
    }

    return new Pair<>(mz, intensive);
  }

  public static List<IdentificationResult> identifyMs2Spectrum(String path)
      throws MSDKException, IOException {
    final Sirius sirius = new Sirius();

    /**
     *
     * TODO: Temporary added dependency back to the 3.1.3
     * sirius_api:4.0 (as well as sirius:4.0) does not have those classes
     *
     **/
    Ms2Experiment ms2Experiment;

    double mz[] = null, intensive[] = null;
//    Pair<double[], double[]> content = readCustomMsFile(path);
//    mz = content.getValue0();
//    intensive = content.getValue1();

    MspSpectrum mspSpectrum = MspImportAlgorithm.parseMspFromString(path);
    mz = mspSpectrum.getMzValues();
    intensive = ArrayUtil.convertToDoubles(mspSpectrum.getIntensityValues());
    Spectrum<Peak> ms2 = sirius.wrapSpectrum(mz, intensive);

    /* TODO: explore non-deprecated methods */
    Ionization ion = sirius.getIonization("[M+H]+");
    Ms2Experiment experiment = sirius.getMs2Experiment(231.07d, ion, ms2, ms2);
//        Compilation failures as no ms1 spectrum, right now do not understand how not to set it.
//        Error on request for GurobiJni60 library

    List<IdentificationResult> results = sirius.identify(experiment);
    return results;
  }
}
