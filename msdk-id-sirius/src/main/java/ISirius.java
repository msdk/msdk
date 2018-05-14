import de.unijena.bioinf.ChemistryBase.ms.Ms2Experiment;
import de.unijena.bioinf.sirius.IdentificationResult;
import de.unijena.bioinf.sirius.Sirius;

import java.util.List;

/**
 * Created by evger on 14-May-18.
 */

public class ISirius {
    public static void main(String[] args) {
        final Sirius sirius = new Sirius();
        Ms2Experiment experiment = sirius.getMs2Experiment();

        List<IdentificationResult> results = sirius.identify(experiment);
    }
}
