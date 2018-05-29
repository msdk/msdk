/*
 * (C) Copyright 2015-2017 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1 as published by the Free
 * Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation.
 */

import de.unijena.bioinf.sirius.IdentificationResult;
import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.SimpleMsSpectrum;
import io.github.msdk.io.msp.MspImportAlgorithm;
import io.github.msdk.io.msp.MspSpectrum;
import io.github.msdk.spectra.centroidprofiledetection.SpectrumTypeDetectionAlgorithm;
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
    final String ms2MspPath = "target/test-classes/sample.msp";
    final double parentMass = 231.065;
    final String ionName = "[M+H]+";
    final String[] expectedResults = {"C13H10O4", "C11H8N3O3", "C9H13NO4P", "C7H11N4O3P",
        "C6H10N6O2S"};

    File inputFile = new File(ms2MspPath);
    MspSpectrum mspSpectrum = MspImportAlgorithm.parseMspFromFile(inputFile);
    double mz[] = mspSpectrum.getMzValues();
    float intensity[] = mspSpectrum.getIntensityValues();
    int size = mz.length;

    MsSpectrumType type = SpectrumTypeDetectionAlgorithm.detectSpectrumType(mz, intensity, size);
    MsSpectrum ms2 = new SimpleMsSpectrum(mz, intensity, size, type);
    SiriusIdentificationMethod siriusMethod = new SiriusIdentificationMethod(null, ms2, parentMass,
        ionName);

    List<IdentificationResult> list = siriusMethod.siriusProcessSpectrums();

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

    MsSpectrum ms1Spectrum = SiriusIdentificationMethod.readCustomMsFile(ms1Path, "\t");
    MsSpectrum ms2Spectrum = SiriusIdentificationMethod.readCustomMsFile(ms2Path, "\t");
    SiriusIdentificationMethod siriusMethod = new SiriusIdentificationMethod(ms1Spectrum,
        ms2Spectrum,
        precursorMass,
        ionName);
    siriusMethod.setNumberOfCandidates(2);

    List<IdentificationResult> list = siriusMethod.siriusProcessSpectrums();

    String[] results = new String[2];
    int i = 0;

// TODO: Fix the difference after second element
    for (IdentificationResult r : list) {
      results[i++] = r.getMolecularFormula().toString();
    }

    Assert.assertArrayEquals(expectedResults, results);
  }
}
