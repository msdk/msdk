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
import de.unijena.bioinf.ChemistryBase.ms.Ms2Experiment;
import de.unijena.bioinf.sirius.IdentificationResult;
import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.IonAnnotation;
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
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

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
    final double precursorMass = 315.1230;
    final IonType ion = IonTypeUtil.createIonType("[M+H]+");
    final String expectedSiriusResult = "C18H18O5";
    final int siriusCandidates = 1;
    final int fingerCandidates = 3;
    final String ms1Path = "flavokavainA_MS1.txt";
    final String ms2Path = "flavokavainA_MS2.txt";

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

    File ms1File = getResourcePath(ms1Path).toFile();
    File ms2File = getResourcePath(ms2Path).toFile();
    MsSpectrum ms1Spectrum = TxtImportAlgorithm.parseMsSpectrum(new FileReader(ms1File));
    MsSpectrum ms2Spectrum = TxtImportAlgorithm.parseMsSpectrum(new FileReader(ms2File));

    List<MsSpectrum> ms1list = new ArrayList<>();
    List<MsSpectrum> ms2list = new ArrayList<>();
    ms2list.add(ms2Spectrum);
    ms1list.add(ms1Spectrum);

    SiriusIdentificationMethod siriusMethod = new SiriusIdentificationMethod(ms1list,
        ms2list,
        precursorMass,
        ion,
        siriusCandidates,
        constraints,
        deviation);

    List<IonAnnotation> siriusAnnotations = siriusMethod.execute();
    SiriusIonAnnotation siriusAnnotation = (SiriusIonAnnotation) siriusAnnotations.get(0);
    String formula = MolecularFormulaManipulator.getString(siriusAnnotation.getFormula());
    Assert.assertEquals(formula, expectedSiriusResult);

    final String[] candidateNames = {"Flavokavain A", "4''-Hydroxy-2'',4,6''-trimethoxychalcone", null};
    final String[] inchis = {"CGIBCVBDFUTMPT", "FQZORWOCAANBNC", "JGYYVILPBDGMNE"};
    final String[] SMILES = {"COC1=CC=C(C=C1)C=CC(=O)C2=C(C=C(C=C2O)OC)OC", "COC1=CC=C(C=C1)C=CC(=O)C2=C(C=C(C=C2OC)O)OC", "COC1=CC=C(C=C1)C=CC(=O)C2=CC=C(C(=C2O)OC)OC"};

    Ms2Experiment experiment = siriusMethod.getExperiment();
    FingerIdWebMethod fingerMethod = new FingerIdWebMethod(experiment, siriusAnnotation, fingerCandidates);
    List<IonAnnotation> annotations = fingerMethod.execute();
    for (int i = 0; i < annotations.size(); i++) {
      SiriusIonAnnotation fingeridAnnotation = (SiriusIonAnnotation) annotations.get(i);
      String smiles = fingeridAnnotation.getSMILES();
      String inchi = fingeridAnnotation.getInchiKey();
      String name = fingeridAnnotation.getDescription();

      Assert.assertEquals(smiles, SMILES[i]);
      Assert.assertEquals(inchi, inchis[i]);
      Assert.assertEquals(name, candidateNames[i]);
    }
  }

}
