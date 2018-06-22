package io.github.msdk.id.sirius;/*
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

import de.unijena.bioinf.ChemistryBase.chem.FormulaConstraints;
import de.unijena.bioinf.sirius.IdentificationResult;
import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.IonType;
import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.SimpleMsSpectrum;
import io.github.msdk.io.msp.MspImportAlgorithm;
import io.github.msdk.io.msp.MspSpectrum;
import io.github.msdk.io.txt.TxtImportAlgorithm;
import io.github.msdk.spectra.centroidprofiledetection.SpectrumTypeDetectionAlgorithm;
import io.github.msdk.util.IonTypeUtil;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.formula.MolecularFormulaRange;

public class FingerIdTest {

  public FingerIdTest() throws MSDKException {
    String loggingProperties = getResourcePath("logging.properties").toString();
    System.setProperty("java.util.logging.config.file", loggingProperties);
  }

  private Path getResourcePath(String resource) throws MSDKException {
    final URL url = SiriusMs2Test.class.getClassLoader().getResource(resource);
    try {
      return Paths.get(url.toURI()).toAbsolutePath();
    } catch (URISyntaxException e) {
      throw new MSDKException(e);
    }
  }

  @Test
  public void testPredictedFingerprint() throws MSDKException, IOException {
    final double deviation = 10d;
    final double precursorMass = 233.1175;
    final IonType ion = IonTypeUtil.createIonType("[M+H]+");
//    final String[] expectedResults = {"C13H10O4", "C11H8N3O3", "C9H13NO4P", "C7H11N4O3P",
//        "C6H10N6O2S"};
    final int candidatesAmount = 5;



    final ConstraintsGenerator generator = new ConstraintsGenerator();

    final MolecularFormulaRange range = new MolecularFormulaRange();
    IsotopeFactory iFac = Isotopes.getInstance();
    range.addIsotope(iFac.getMajorIsotope("S"), 0, Integer.MAX_VALUE);
    range.addIsotope(iFac.getMajorIsotope("B"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Br"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Cl"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("F"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("I"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Se"), 0, 0);

    final FormulaConstraints constraints = generator.generateConstraint(range);
    final String ms2Path = "flavokavainA_MS2.txt";

    File ms2File = getResourcePath(ms2Path).toFile();
    MsSpectrum ms2Spectrum = TxtImportAlgorithm.parseMsSpectrum(new FileReader(ms2File));

    List<MsSpectrum> ms2list = new ArrayList<>();
    ms2list.add(ms2Spectrum);

    SiriusIdentificationMethod siriusMethod = new SiriusIdentificationMethod(null,
        ms2list,
        precursorMass,
        ion,
        candidatesAmount,
        constraints,
        deviation);

    List<IdentificationResult> list = siriusMethod.siriusProcessSpectra();
    System.out.println("TADA");
  }

}
