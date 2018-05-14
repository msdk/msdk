import de.unijena.bioinf.ChemistryBase.chem.Ionization;
import de.unijena.bioinf.ChemistryBase.ms.Ms2Experiment;
import de.unijena.bioinf.ChemistryBase.ms.Peak;
import de.unijena.bioinf.ChemistryBase.ms.Spectrum;
import de.unijena.bioinf.sirius.IdentificationResult;
import de.unijena.bioinf.sirius.Sirius;
import javafx.util.Pair;


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

    public static Pair<double[], double[]> readMsFile(String path) throws FileNotFoundException {
        List<Double> mz, intensive;
        mz = new ArrayList<>();
        intensive = new ArrayList<>();
        Scanner sc = new Scanner(new File(path));
        String words[];

        while (sc.hasNext()) {
//            words = sc.nextLine().split(" ")
            mz.add(sc.nextDouble());
            intensive.add(sc.nextDouble());
        }

        sc.close();

        Pair<double[], double[]> pair;
        Double mzArray[] = mz.toArray(new Double[mz.size()]);
        Double intensiveArray[] = intensive.toArray(new Double[intensive.size()]);

        double[] mzPrimitive = Stream.of(mzArray).mapToDouble(Double::doubleValue).toArray();
        double[] intensivePrimitive = Stream.of(intensiveArray).mapToDouble(Double::doubleValue).toArray();

        return new Pair<>(mzPrimitive, intensivePrimitive);
    }

    public static void main(String[] args) {
        final Sirius sirius = new Sirius();
        Pair<double[], double[]> fileContent = null;
        try {
            fileContent = readMsFile("query.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        double mz[] = fileContent.getKey();
        double intensive[] = fileContent.getValue();

        Spectrum<Peak> ms1 = null;
        Spectrum<Peak> ms2 = sirius.wrapSpectrum(mz, intensive);

        /* TODO: explore non-deprecated methods */
        Ionization ion = sirius.getIonization("[M+H]+");
        Ms2Experiment experiment = sirius.getMs2Experiment(231.07d, ion, ms1, ms2);
//        Not tested
//        Compilation failures as no ms1 spectrum, right now do not understand how not to set it.
//        Error on request for GurobiJni60 library


        List<IdentificationResult> results = sirius.identify(experiment);
        System.out.println(results.toString());
    }
}
