import de.unijena.bioinf.ChemistryBase.chem.PrecursorIonType;
import de.unijena.bioinf.ChemistryBase.ms.Ms2Experiment;
import de.unijena.bioinf.ChemistryBase.ms.Peak;
import de.unijena.bioinf.ChemistryBase.ms.Spectrum;
import de.unijena.bioinf.sirius.IdentificationResult;
import de.unijena.bioinf.sirius.Sirius;
import io.github.msdk.MSDKException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.annotation.Nullable;

/**
 * Created by evger on 14-May-18.
 */

public class ISirius {
  /* This function is left here for non-msp files */
  public static Pair<double[], double[]> readCustomMsFile(String path) throws IOException {
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

  public static List<IdentificationResult> identifyMs2Spectrum(
      @Nullable Pair<double[], double[]> ms1pair, Pair<double[], double[]> ms2pair,
      double parentMass, String ion) throws MSDKException, IOException {

    final Sirius sirius = new Sirius();
    Spectrum<Peak> ms1 = null, ms2 = null;
    /**
     *
     * TODO: Temporary added dependency back to the 3.1.3
     * sirius_api:4.0 (as well as sirius:4.0) does not have those classes
     *
     **/


    ms2 = sirius.wrapSpectrum(ms2pair.getKey(), ms2pair.getVal());
    if (ms1pair != null) {
      ms1 = sirius.wrapSpectrum(ms1pair.getKey(), ms1pair.getVal());
    }

    PrecursorIonType precursor = sirius.getPrecursorIonType(ion);
    Ms2Experiment experiment = sirius.getMs2Experiment(parentMass, precursor, ms1, ms2);

//        Compilation failures as no ms1 spectrum, right now do not understand how not to set it.
//        Error on request for GurobiJni60 library
    /* Runtime failure on fragmentation tree construction (NullPointer) - if used on MSMS provided by Tomas earlier */
    /* Runtime failure on fragmentation tree construction (assertion error) - if used on data from .msp */

    List<IdentificationResult> results = sirius.identify(experiment, 5, true, null);
    return results;
  }
}
