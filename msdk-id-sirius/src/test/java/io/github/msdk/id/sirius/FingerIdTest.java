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
import de.unijena.bioinf.ChemistryBase.ms.Ms2Experiment;
import io.github.msdk.MSDKException;
import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.IonAnnotation;
import io.github.msdk.datamodel.IonType;
import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.io.mgf.MgfFileImportMethod;
import io.github.msdk.io.mgf.MgfMsSpectrum;
import io.github.msdk.io.txt.TxtImportAlgorithm;
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
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.formula.MolecularFormulaRange;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FingerIdTest {
  private static final Logger logger = LoggerFactory.getLogger(FingerIdTest.class);

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
    final FormulaConstraints constraints = generateDefaultConstraints();

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
    try {
      FingerIdWebMethod fingerMethod = new FingerIdWebMethod(experiment, siriusAnnotation,
          fingerCandidates);
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
    } catch (MSDKRuntimeException exception) {
      logger.info("Connection with boecker-lab FingerId API failed");
    }
  }

  @Test
  public void testMultithreaded() throws MSDKException, IOException, InterruptedException {
    final double deviation = 10d;
    final double precursorMass = 233.1175;
    final IonType ion = IonTypeUtil.createIonType("[M+H]+");
    final String[] expectedSiriusResults = {"C14H16O3", "C12H14N3O2", "C10H19NO3P", "C8H17N4O2P"};
    final int siriusCandidates = 4;
    final int fingerCandidates = 3;
    final String ms1Path = "marindinin_MS1.txt";
    final String ms2Path = "marindinin_MS2.txt";
    final FormulaConstraints constraints = generateDefaultConstraints();

    File ms1File = getResourcePath(ms1Path).toFile();
    File ms2File = getResourcePath(ms2Path).toFile();
    MsSpectrum ms1Spectrum = TxtImportAlgorithm.parseMsSpectrum(new FileReader(ms1File));
    MsSpectrum ms2Spectrum = TxtImportAlgorithm.parseMsSpectrum(new FileReader(ms2File));

    List<MsSpectrum> ms1list = new ArrayList<>();
    List<MsSpectrum> ms2list = new ArrayList<>();
    ms2list.add(ms2Spectrum);
    ms1list.add(ms1Spectrum);

    final SiriusIdentificationMethod siriusMethod = new SiriusIdentificationMethod(ms1list,
        ms2list,
        precursorMass,
        ion,
        siriusCandidates,
        constraints,
        deviation);

    final List<IonAnnotation> siriusAnnotations = siriusMethod.execute();
    List<SiriusIonAnnotation> siriusAnnotationsList = new LinkedList<>();

    Set<String> testFormulas = new TreeSet<>();
    testFormulas.add(expectedSiriusResults[0]);
    testFormulas.add(expectedSiriusResults[1]);
    testFormulas.add(expectedSiriusResults[3]);

    int i = 0;
    for (IonAnnotation ann: siriusAnnotations) {
      String formula = MolecularFormulaManipulator.getString(ann.getFormula());
      Assert.assertEquals(formula, expectedSiriusResults[i++]);

      // Ignore C10H19NO3P, as it does not have FingerprintCandidates in future
      if (testFormulas.contains(formula)) {
        SiriusIonAnnotation t = (SiriusIonAnnotation) ann;
        siriusAnnotationsList.add(t);
      }
    }

    List<IonAnnotation>[] fingerResults = new List[3];
    final Ms2Experiment experiment = siriusMethod.getExperiment();

    CountDownLatch finishLatch = new CountDownLatch(3);
    CountDownLatch initLatch = new CountDownLatch(3);

    try {
      for (int k = 0; k < 3; k++) {
        FingerIdConcurrent thread = new FingerIdConcurrent(experiment, siriusAnnotationsList.get(k),
            fingerCandidates, initLatch, finishLatch);
        thread.run();
        fingerResults[k] = thread.getResults();
      }

      final String[] firstSMILES = {"COC1=CC(=O)OC(CCC2=CC=CC=C2)C1",
          "COC=C(CC=CC1=CC=CC=C1)C(=O)OC", "COC(=O)C=CCC(C=CC1=CC=CC=C1)O"};
      final String[] firstInchis = {"VOOYTQRREPYRIW", "CIKOXPINNQLUNR", "VUILPHNRZJUSCY"};
      final String[] firstNames = {"Marindinin", null,
          "methyl (2Z,5R,6E)-5-hydroxy-7-phenylhepta-2,6-dienoate"};

      final String[] secondSMILES = {"CC1=C(C(=NC2=CC=CC=C2)O)N([CH]N1C)O",
          "CCOC(=O)C([CH]NNC1=CC=CC=C1)[N+]#[C-]", "C1=C[CH]C(=C1)CNC(CC2=CN=CN2)C(=O)O"};
      final String[] secondInchis = {"WHKHVKOFMPKDPZ", "KFRMPWTUGNWZNC", "RIHSCMQGAIZEPT"};
      final String[] secondNames = {null, null, null};

      final String[] thirdSMILES = {"CCCNC1CCN(C(=O)C1(N)P=O)N"};
      final String[] thirdInchis = {"RIBGFYNNMUNNPJ"};
      final String[] thirdNames = {null};

      finishLatch.await();

      String[][] SMILES = new String[3][];
      String[][] INCHI = new String[3][];
      String[][] names = new String[3][];

      for (int k = 0; k < 3; k++) {
        SMILES[k] = new String[fingerResults[k].size()];
        names[k] = new String[fingerResults[k].size()];
        INCHI[k] = new String[fingerResults[k].size()];
        i = 0;
        for (IonAnnotation ann : fingerResults[k]) {
          SiriusIonAnnotation fingeridAnnotation = (SiriusIonAnnotation) ann;
          String smiles = fingeridAnnotation.getSMILES();
          String inchi = fingeridAnnotation.getInchiKey();
          String name = fingeridAnnotation.getDescription();

          SMILES[k][i] = smiles;
          INCHI[k][i] = inchi;
          names[k][i] = name;
          i++;
        }
      }

      Assert.assertArrayEquals(firstSMILES, SMILES[0]);
      Assert.assertArrayEquals(firstNames, names[0]);
      Assert.assertArrayEquals(firstInchis, INCHI[0]);

      Assert.assertArrayEquals(secondSMILES, SMILES[1]);
      Assert.assertArrayEquals(secondNames, names[1]);
      Assert.assertArrayEquals(secondInchis, INCHI[1]);

      Assert.assertArrayEquals(thirdSMILES, SMILES[2]);
      Assert.assertArrayEquals(thirdNames, names[2]);
      Assert.assertArrayEquals(thirdInchis, INCHI[2]);
    } catch (MSDKRuntimeException e) {
      logger.info("Connection with boecker-lab FingerId API failed");
    }
  }

  @Test
  public void testBigMgf() throws MSDKException, IOException {
    final String[] expectedInchis = {
      "SNICXCGAKADSCV",
      "AQCRXZYYMOXFAN",
      "DMRUVRQCQAKJTQ",
      "UEIZUEWXLJOVLD",
      "LSFQCPHPFYRFGQ"
    };
    final String[] expectedSmiles = {
      "CN1CCCC1C2=CC=CN=C2",
      "CN1CCCC1C2=NC=CC=C2",
      "CN1CCCC1C2=CC=NC=C2",
      "CN1CCC(C1)C2=CC=CN=C2",
      "CC1CC(C2=CC=CN=C2)N1C"
    };

    final String filename = "full_biglist.mgf";
    File file = getResourcePath(filename).toFile();
    MgfFileImportMethod mgfImporter = new MgfFileImportMethod(file);
    final List<MgfMsSpectrum> mgfSpectra = mgfImporter.execute();

    List<MsSpectrum> ms1 = new LinkedList<>();
    List<MsSpectrum> ms2 = new LinkedList<>();
    for (MgfMsSpectrum spectrum: mgfSpectra) {
      if (spectrum.getMsLevel().equals(2))
        ms2.add(spectrum);
      else
        ms1.add(spectrum);
    }

    final double parentMass = 163.1226;
    final IonType ion = IonTypeUtil.createIonType("[M+H]+");
    final double deviation = 10d;
    final FormulaConstraints constraints = generateDefaultConstraints();


    SiriusIdentificationMethod siriusMethod = new SiriusIdentificationMethod(ms1, ms2, parentMass,
        ion, 1, constraints, deviation);
    List<IonAnnotation> siriusSpectra = siriusMethod.execute();

    final Ms2Experiment experiment = siriusMethod.getExperiment();
    SiriusIonAnnotation sann = (SiriusIonAnnotation) siriusSpectra.get(0);
    FingerIdWebMethod fingerMethod = new FingerIdWebMethod(experiment, sann, 5);
    List<IonAnnotation> fingerResults = fingerMethod.execute();

    String[] smiles = new String[expectedSmiles.length];
    String[] inchis = new String[expectedInchis.length];
    for (int i = 0; i < fingerResults.size(); i++) {
      SiriusIonAnnotation fann = (SiriusIonAnnotation) fingerResults.get(i);
      smiles[i] = fann.getSMILES();
      inchis[i] = fann.getInchiKey();
    }
    Assert.assertArrayEquals(expectedInchis, inchis);
    Assert.assertArrayEquals(expectedSmiles, smiles);
  }

  private FormulaConstraints generateDefaultConstraints() throws IOException {
    final MolecularFormulaRange range = new MolecularFormulaRange();
    IsotopeFactory iFac = Isotopes.getInstance();
    range.addIsotope(iFac.getMajorIsotope("S"), 0, Integer.MAX_VALUE);
    range.addIsotope(iFac.getMajorIsotope("B"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Br"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Cl"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("F"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("I"), 0, 0);
    range.addIsotope(iFac.getMajorIsotope("Se"), 0, 0);

    return ConstraintsGenerator.generateConstraint(range);
  }



  public class FingerIdConcurrent implements Runnable {
    private FingerIdWebMethod method;
    private CountDownLatch initLatch;
    private CountDownLatch finishLatch;

    FingerIdConcurrent(Ms2Experiment experiment, SiriusIonAnnotation siriusIonAnnotation, int candidates, CountDownLatch initLatch, CountDownLatch finishLatch) throws MSDKException {
      method = new FingerIdWebMethod(experiment, siriusIonAnnotation, candidates);
      this.finishLatch = finishLatch;
      this.initLatch = initLatch;
    }

    public List<IonAnnotation> getResults() {
      return method.getResult();
    }

    @Override
    public void run() {
      initLatch.countDown();
      try {
        method.execute();
      } catch (Exception e) {
        e.printStackTrace();
      }
      finishLatch.countDown();
    }
  }
}
