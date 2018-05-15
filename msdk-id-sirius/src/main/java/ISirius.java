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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * Created by evger on 14-May-18.
 */

public class ISirius {

  public List<IdentificationResult> identifyMs2Spectrum(String path) throws MSDKException {
    final Sirius sirius = new Sirius();
    MspSpectrum mspSpectrum = MspImportAlgorithm.parseMspFromString(path);
    double mz[] = mspSpectrum.getMzValues();
    double intensive[] = mspSpectrum.getIntensityValues();

    Spectrum<Peak> ms1 = null;
    Spectrum<Peak> ms2 = sirius.wrapSpectrum(mz, intensive);

    /* TODO: explore non-deprecated methods */
    Ionization ion = sirius.getIonization("[M+H]+");
    Ms2Experiment experiment = sirius.getMs2Experiment(231.07d, ion, ms1, ms2);
//        Not tested
//        Compilation failures as no ms1 spectrum, right now do not understand how not to set it.
//        Error on request for GurobiJni60 library

    List<IdentificationResult> results = sirius.identify(experiment);
    return results;
  }
}
