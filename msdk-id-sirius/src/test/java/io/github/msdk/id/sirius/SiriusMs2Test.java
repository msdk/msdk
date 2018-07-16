/*
 * (C) Copyright 2015-2018 by MSDK Development Team
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

package io.github.msdk.id.sirius;

import de.unijena.bioinf.ChemistryBase.chem.FormulaConstraints;
import de.unijena.bioinf.sirius.IdentificationResult;
import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.IonAnnotation;
import io.github.msdk.datamodel.IonType;
import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.SimpleMsSpectrum;
import io.github.msdk.io.msp.MspImportAlgorithm;
import io.github.msdk.io.msp.MspSpectrum;
import io.github.msdk.spectra.centroidprofiledetection.SpectrumTypeDetectionAlgorithm;
import io.github.msdk.util.IonTypeUtil;
import io.github.msdk.io.txt.TxtImportAlgorithm;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.formula.MolecularFormulaRange;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

public class SiriusMs2Test {
  public SiriusMs2Test() throws MSDKException {
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
  public void testCreateMs2ExperimentMsp() throws MSDKException, IOException {
    final String ms2Msp = "sample.msp";
    final FormulaConstraints constraints = null;
    final double parentMass = 231.065;
    final IonType ion = IonTypeUtil.createIonType("[M+H]+");
    final double deviation = 10d;
    final String[] expectedResults = {"C13H10O4", "C11H8N3O3", "C9H13NO4P", "C7H11N4O3P",
        "C6H10N6O2S"};
    final int amount = 5;



    File inputFile = getResourcePath(ms2Msp).toFile();

    MspSpectrum mspSpectrum = MspImportAlgorithm.parseMspFromFile(inputFile);
    double mz[] = mspSpectrum.getMzValues();
    float intensity[] = mspSpectrum.getIntensityValues();
    int size = mz.length;

    MsSpectrumType type = SpectrumTypeDetectionAlgorithm.detectSpectrumType(mz, intensity, size);
    MsSpectrum ms2 = new SimpleMsSpectrum(mz, intensity, size, type);

    LinkedList<MsSpectrum> ms2list = new LinkedList<>();
    ms2list.add(ms2);
    SiriusIdentificationMethod siriusMethod = new SiriusIdentificationMethod(null,
        ms2list,
        parentMass,
        ion,
        amount,
        constraints,
        deviation);

    List<IdentificationResult> list = siriusMethod.siriusProcessSpectra();

    String[] results = new String[amount];
    int i = 0;

    for (IdentificationResult r : list) {
      results[i++] = r.getMolecularFormula().toString();
    }
    Assert.assertArrayEquals(expectedResults, results);
  }

  @Test
  public void testCreateExperimentMs1Ms2Custom() throws MSDKException, IOException {
    final double deviation = 10d;
    final double precursorMass = 315.1230;
    final IonType ion = IonTypeUtil.createIonType("[M+H]+");
    final String[] expectedResults = {"C18H18O5",
        "C12H19N4O4P",
        "C14H21NO5P",
        "C16H16N3O4",
        "C10H24N2O5P2",
        "C14H14N6O3",
        "C11H23O8P",
        "C19H14N4O",
        "C10H17N7O3P",
        "C13H15N8P"
    };
    final String ms1Path = "flavokavainA_MS1.txt";
    final String ms2Path = "flavokavainA_MS2.txt";
    final int candidatesAmount = 10;

    final MolecularFormulaRange range = new MolecularFormulaRange();
    IsotopeFactory iFac = Isotopes.getInstance();
    range.addIsotope(iFac.getMajorIsotope("S"), 0, Integer.MAX_VALUE);
    range.addIsotope(iFac.getMajorIsotope("B"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Br"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Cl"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("F"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("I"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Se"), 0, 0);

    final FormulaConstraints constraints = ConstraintsGenerator.generateConstraint(range);


    File ms1File = getResourcePath(ms1Path).toFile();
    File ms2File = getResourcePath(ms2Path).toFile();
    MsSpectrum ms1Spectrum = TxtImportAlgorithm.parseMsSpectrum(new FileReader(ms1File));
    MsSpectrum ms2Spectrum = TxtImportAlgorithm.parseMsSpectrum(new FileReader(ms2File));

    LinkedList<MsSpectrum> ms1list = new LinkedList<>();
    LinkedList<MsSpectrum> ms2list = new LinkedList<>();
    ms1list.add(ms1Spectrum);
    ms2list.add(ms2Spectrum);

    SiriusIdentificationMethod siriusMethod = new SiriusIdentificationMethod(ms1list,
        ms2list,
        precursorMass,
        ion,
        candidatesAmount,
        constraints,
        deviation);

    List<IdentificationResult> list = siriusMethod.siriusProcessSpectra();

    String[] results = new String[candidatesAmount];
    int i = 0;

    for (IdentificationResult r : list) {
      results[i++] = r.getMolecularFormula().toString();
    }

    Assert.assertArrayEquals(expectedResults, results);
  }


  @Test
  public void testBisnoryagoninDataSet() throws MSDKException, IOException {
    final double deviation = 14d;
    final double precursorMass = 231.0647;
    final IonType ion = IonTypeUtil.createIonType("[M+H]+");
    final String[] expectedResults = {
        "C13H10O4",
        "C10H11FO5",
        "C8H9FN3O4",
        "C11H8N3O3",
        "C9H13NO4P",
        "C6H7FN6O3",
        "C11H9F3O2"
    };
    final String ms1Path = "bisnoryangonin_MS1.txt";
    final String ms2Path = "bisnoryangonin_MS2.txt";
    final int candidatesAmount = 7;

    final MolecularFormulaRange range = new MolecularFormulaRange();
    IsotopeFactory iFac = Isotopes.getInstance();
    range.addIsotope(iFac.getMajorIsotope("S"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("B"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Br"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Cl"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("F"), 0, Integer.MAX_VALUE);
    range.addIsotope(iFac.getMajorIsotope("I"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Se"), 0, 0);

    final FormulaConstraints constraints = ConstraintsGenerator.generateConstraint(range);


    File ms1File = getResourcePath(ms1Path).toFile();
    File ms2File = getResourcePath(ms2Path).toFile();

    MsSpectrum ms1Spectrum = TxtImportAlgorithm.parseMsSpectrum(new FileReader(ms1File));
    MsSpectrum ms2Spectrum = TxtImportAlgorithm.parseMsSpectrum(new FileReader(ms2File));

    LinkedList<MsSpectrum> ms1list = new LinkedList<>();
    LinkedList<MsSpectrum> ms2list = new LinkedList<>();
    ms1list.add(ms1Spectrum);
    ms2list.add(ms2Spectrum);

    SiriusIdentificationMethod siriusMethod = new SiriusIdentificationMethod(ms1list,
        ms2list,
        precursorMass,
        ion,
        candidatesAmount,
        constraints,
        deviation);

    List<IdentificationResult> list = siriusMethod.siriusProcessSpectra();

    String[] results = new String[candidatesAmount];
    int i = 0;

    for (IdentificationResult r : list) {
      results[i++] = r.getMolecularFormula().toString();
    }

    Assert.assertArrayEquals(expectedResults, results);
  }

  @Test
  public void testMultipleMs2Spectra() throws MSDKException, IOException {
    final double deviation = 13d;
    final double precursorMass = 233.1175;
    final IonType ion = IonTypeUtil.createIonType("[M+H]+");
    final String[] expectedResults = {
        "C14H16O3",
        "C11H17FO4",
        "C9H15FN3O3",
        "C12H14N3O2",
        "C6H16F2N3O4"
    };
    final String ms1Path = "marindinin_MS1.txt";
    final String ms2Path = "marindinin_MS2.txt";
    final int candidatesAmount = 5;

    final MolecularFormulaRange range = new MolecularFormulaRange();
    IsotopeFactory iFac = Isotopes.getInstance();
    range.addIsotope(iFac.getMajorIsotope("S"), 0, Integer.MAX_VALUE);
    range.addIsotope(iFac.getMajorIsotope("B"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Br"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Cl"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("F"), 0, Integer.MAX_VALUE);
    range.addIsotope(iFac.getMajorIsotope("I"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Se"), 0, 0);

    final FormulaConstraints constraints = ConstraintsGenerator.generateConstraint(range);


    File ms1File = getResourcePath(ms1Path).toFile();
    File ms2File = getResourcePath(ms2Path).toFile();

    MsSpectrum ms2Spectrum1 = TxtImportAlgorithm.parseMsSpectrum(new FileReader(ms1File));
    MsSpectrum ms2Spectrum2 = TxtImportAlgorithm.parseMsSpectrum(new FileReader(ms2File));


    LinkedList<MsSpectrum> ms2list = new LinkedList<>();
    ms2list.add(ms2Spectrum1);
    ms2list.add(ms2Spectrum2);

    SiriusIdentificationMethod siriusMethod = new SiriusIdentificationMethod(null,
        ms2list,
        precursorMass,
        ion,
        candidatesAmount,
        constraints,
        deviation);

    List<IdentificationResult> list = siriusMethod.siriusProcessSpectra();

    String[] results = new String[candidatesAmount];
    int i = 0;

    for (IdentificationResult r : list) {
      results[i++] = r.getMolecularFormula().toString();
    }

    Assert.assertArrayEquals(expectedResults, results);
  }

  @Test
  public void testOnlyMs1Spectra() throws MSDKException, IOException {
    final double deviation = 10d;
    final double precursorMass = 233.1175;
    final IonType ion = IonTypeUtil.createIonType("[M+H]+");
    final String[] expectedResults = {
        "C14H16O3",
        "C10H12N6O",
        "C8H17N4O2P",
        "C12H14N3O2",
        "C10H19NO3P",
        "C6H15N7OP"
    };
    final String ms1Path = "marindinin_MS1.txt";
    final int candidatesAmount = 6;

    final MolecularFormulaRange range = new MolecularFormulaRange();
    IsotopeFactory iFac = Isotopes.getInstance();
    range.addIsotope(iFac.getMajorIsotope("S"), 0, Integer.MAX_VALUE);
    range.addIsotope(iFac.getMajorIsotope("B"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Br"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Cl"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("F"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("I"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Se"), 0, 0);

    final FormulaConstraints constraints = ConstraintsGenerator.generateConstraint(range);


    File ms1File = getResourcePath(ms1Path).toFile();

    MsSpectrum ms2Spectrum = TxtImportAlgorithm.parseMsSpectrum(new FileReader(ms1File));

    LinkedList<MsSpectrum> ms1list = new LinkedList<>();
    ms1list.add(ms2Spectrum);

    SiriusIdentificationMethod siriusMethod = new SiriusIdentificationMethod(ms1list,
        null,
        precursorMass,
        ion,
        candidatesAmount,
        constraints,
        deviation);

    List<IdentificationResult> list = siriusMethod.siriusProcessSpectra();

    String[] results = new String[candidatesAmount];
    int i = 0;

    for (IdentificationResult r : list) {
      results[i++] = r.getMolecularFormula().toString();
    }

    Assert.assertArrayEquals(expectedResults, results);
  }


  @Test
  public void testIonAnnotation() throws IOException, MSDKException {
    final double deviation = 10d;
    final double precursorMass = 233.1175;
    final IonType ion = IonTypeUtil.createIonType("[M+H]+");
    final String[] expectedResults = {
        "C14H16O3",
        "C12H14N3O2",
        "C10H19NO3P",
        "C9H18N3O2S",
        "C8H17N4O2P"
    };
    final int expectedCharge = 1;
    final String ms2Path = "marindinin_MS2.txt";
    final int candidatesAmount = 5;

    final MolecularFormulaRange range = new MolecularFormulaRange();
    IsotopeFactory iFac = Isotopes.getInstance();
    range.addIsotope(iFac.getMajorIsotope("S"), 0, Integer.MAX_VALUE);
    range.addIsotope(iFac.getMajorIsotope("B"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Br"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Cl"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("F"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("I"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Se"), 0, 0);

    final FormulaConstraints constraints = ConstraintsGenerator.generateConstraint(range);
    File ms2File = getResourcePath(ms2Path).toFile();

    MsSpectrum ms2Spectrum2 = TxtImportAlgorithm.parseMsSpectrum(new FileReader(ms2File));

    LinkedList<MsSpectrum> ms2list = new LinkedList<>();
    ms2list.add(ms2Spectrum2);

    SiriusIdentificationMethod siriusMethod = new SiriusIdentificationMethod(null,
        ms2list,
        precursorMass,
        ion,
        candidatesAmount,
        constraints,
        deviation);

    List<IonAnnotation> list = siriusMethod.execute();
    int i = 0;
    for (IonAnnotation annotation: list) {
      int charge = annotation.getFormula().getCharge();
      String formula = MolecularFormulaManipulator.getString(annotation.getFormula());

      Assert.assertEquals(expectedCharge, charge);
      Assert.assertEquals(expectedResults[i++], formula);
    }
  }
}
