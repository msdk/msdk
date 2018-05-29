import de.unijena.bioinf.ChemistryBase.chem.PrecursorIonType;
import de.unijena.bioinf.ChemistryBase.ms.Ms2Experiment;
import de.unijena.bioinf.ChemistryBase.ms.Peak;
import de.unijena.bioinf.ChemistryBase.ms.Spectrum;
import de.unijena.bioinf.sirius.IdentificationResult;
import de.unijena.bioinf.sirius.IsotopePatternHandling;
import de.unijena.bioinf.sirius.Sirius;
import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.IonAnnotation;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.annotation.Nullable;

/**
 * Created by evger on 14-May-18.
 */

public class SiriusIdentificationMethod implements MSDKMethod<IonAnnotation> {
  Sirius sirius;

  SiriusIdentificationMethod() {
    sirius = new Sirius();
  }

  /* This function is left here for non-msp files */
  public Pair<double[], double[]> readCustomMsFile(String path, String delimeter) throws IOException, MSDKRuntimeException {
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
      String[] splitted = s.split(delimeter);
      if (splitted.length == 2) {
        mz[index] = Double.parseDouble(splitted[0]);
        intensive[index++] = Double.parseDouble(splitted[1]);
      } else throw new MSDKRuntimeException("Incorrect spectrum structure");
    }

    return new Pair<>(mz, intensive);
  }

  public List<IdentificationResult> identifyMs2Spectrum(
      @Nullable Pair<double[], double[]> ms1pair, Pair<double[], double[]> ms2pair,
      double parentMass, String ion, int numberOfCanditates) throws MSDKException, IOException {
    Spectrum<Peak> ms1 = null, ms2 = null;
    ms2 = sirius.wrapSpectrum(ms2pair.getKey(), ms2pair.getVal());
    if (ms1pair != null) {
      ms1 = sirius.wrapSpectrum(ms1pair.getKey(), ms1pair.getVal());
    }

    PrecursorIonType precursor = sirius.getPrecursorIonType(ion);
    Ms2Experiment experiment = sirius.getMs2Experiment(parentMass, precursor, ms1, ms2);

//        Error on request for GurobiJni60 library, Cplex .dll missed java path.

    List<IdentificationResult> results = sirius.identify(experiment, numberOfCanditates, true, IsotopePatternHandling.omit);
    return results;
  }

  @Nullable
  @Override
  public Float getFinishedPercentage() {
    return null;
  }

  @Nullable
  @Override
  public IonAnnotation execute() throws MSDKException {
    return null;
  }

  @Nullable
  @Override
  public IonAnnotation getResult() {
    return null;
  }

  // TODO: implement
  @Override
  public void cancel() {

  }
}
