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

import de.unijena.bioinf.ChemistryBase.chem.ChemicalAlphabet;
import de.unijena.bioinf.ChemistryBase.chem.Element;
import de.unijena.bioinf.ChemistryBase.chem.FormulaConstraints;
import de.unijena.bioinf.ChemistryBase.chem.PeriodicTable;
import de.unijena.bioinf.ChemistryBase.chem.PrecursorIonType;
import de.unijena.bioinf.ChemistryBase.ms.Deviation;
import de.unijena.bioinf.ChemistryBase.ms.Ms2Experiment;
import de.unijena.bioinf.ChemistryBase.ms.MutableMs2Experiment;
import de.unijena.bioinf.ChemistryBase.ms.Peak;
import de.unijena.bioinf.ChemistryBase.ms.Spectrum;
import de.unijena.bioinf.ChemistryBase.ms.utils.SimpleSpectrum;
import de.unijena.bioinf.sirius.IdentificationResult;
import de.unijena.bioinf.sirius.IsotopePatternHandling;
import de.unijena.bioinf.sirius.Sirius;
import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.IonAnnotation;
import io.github.msdk.datamodel.IonType;
import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.SimpleIonAnnotation;
import io.github.msdk.datamodel.SimpleMsSpectrum;
import io.github.msdk.spectra.centroidprofiledetection.SpectrumTypeDetectionAlgorithm;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openscience.cdk.formula.MolecularFormulaRange;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p> SiriusIdentificationMethod class. </p>
 *
 * This class wraps the Sirius module and transforms its results into MSDK data structures
 * Transformation of IdentificationResult (Sirius) into IonAnnatation (MSDK)
 * Containts sub-class for generating FormulaConstraints for Sirius from MolecularFormulaRange object
 */
public class SiriusIdentificationMethod implements MSDKMethod<List<IonAnnotation>> {

  /**
   * Dynamic loading of native libraries
   */
  static {
    try {
      System.load("glpk_4_60");
      System.load("glpk_4_60_java");
    } catch (UnsatisfiedLinkError e) {
      try {
        // GLPK requires two libraries
        String[] libs = {"glpk_4_60", "glpk_4_60_java"};
        NativeLibraryLoader.loadLibraryFromJar("glpk-4.60", libs);
      } catch (Exception e1) {
        throw new RuntimeException(e1);
      }
    }
  }




  private static final Logger logger = LoggerFactory.getLogger(SiriusIdentificationMethod.class);
  private final Sirius sirius;
  private final MsSpectrum ms1;
  private final MsSpectrum ms2;
  private final Double parentMass;
  private final IonType ion;
  private final int numberOfCandidates;
  private final FormulaConstraints constraints;
  private final Deviation deviation;
  private boolean cancelled = false;
  private List<IonAnnotation> result;

  /**
   * <p> Constructor for SiriusIdentificationMethod class. </p>
   *
   * @param ms1 - can be null! MsSpectrum level 1
   * @param ms2 - MsSpectrum level 2
   * @param parentMass - Most intensive usually or specified
   * @param ion - Ionization
   * @param numberOfCandidates - amount of IdentificationResults to be returned from Sirius
   * @param constraints - FormulaConstraints provided by the end user. Can be created using ConstraintsGenerator
   * @param deviation - float value of possible mass deviation
   */
  public SiriusIdentificationMethod(@Nullable MsSpectrum ms1, @Nonnull MsSpectrum ms2, Double parentMass,
      IonType ion, int numberOfCandidates, @Nullable FormulaConstraints constraints, Double deviation) {
    sirius = new Sirius();
    this.ms1 = ms1;
    this.ms2 = ms2;
    this.parentMass = parentMass;
    this.ion = ion;
    this.numberOfCandidates = numberOfCandidates;
    this.constraints = constraints;
    this.deviation = new Deviation(deviation);
  }

  /**
   * <p> Class ConstraintsGenerator. </p>
   * This class allows to construct a Sirius object FormulaConstraints using MolecularFormulaRange object
    */
  public static class ConstraintsGenerator {
    private final String[] defaultElementSymbols = new String[]{"C", "H", "N", "O", "P"};
    private final Element[] defaultElements;
    private final PeriodicTable periodicTable = PeriodicTable.getInstance();
    private final int maxNumberOfOneElements = 20;

    /**
     * <p>Constructor for ConstraintsGenerator class</p>
     * Initializes array of Elements `defaultElements`
     */
    public ConstraintsGenerator() {
      defaultElements = new Element[defaultElementSymbols.length];
      for (int i = 0; i < defaultElementSymbols.length; i++)
        defaultElements[i] = periodicTable.getByName(defaultElementSymbols[i]);
    }

    /**
     * <p> Method for generating FormulaConstraints from user-defined search space</p>
     * Parses isotopes from input parameter and transforms it into Element objects and sets their range value
     * @param range - User defined search space of possible elements
     * @return new Constraint to be used in Sirius
     */
    public FormulaConstraints generateConstraint(MolecularFormulaRange range) {
      logger.debug("ConstraintsGenerator started procesing");
      int size = range.getIsotopeCount();
      Element elements[] = Arrays.copyOf(defaultElements, defaultElements.length + size);
      int k = 0;

      // Add items from `range` into array with default elements
      for (IIsotope isotope: range.isotopes()) {
        int atomicNumber = isotope.getAtomicNumber();
        final Element element = periodicTable.get(atomicNumber);
        elements[defaultElements.length + k++] = element;
      }

      // Generate initial constraint w/o concrete Element range
      FormulaConstraints constraints = new FormulaConstraints(new ChemicalAlphabet(elements));

      // Specify each Element range
      for (IIsotope isotope: range.isotopes()) {
        int atomicNumber = isotope.getAtomicNumber();
        final Element element = periodicTable.get(atomicNumber);
        int min = range.getIsotopeCountMin(isotope);
        int max = range.getIsotopeCountMax(isotope);

        constraints.setLowerbound(element, min);
        if (max!= maxNumberOfOneElements) constraints.setUpperbound(element, max);
      }

      logger.debug("ConstraintsGenerator finished");
      return constraints;
    }

  }

  public double getParentMass() {
    return parentMass;
  }

  public IonType getIonization() {
    return ion;
  }

  public MsSpectrum getMsSpectrum() {
    return ms1;
  }

  public MsSpectrum getMs2Spectrum() {
    return ms2;
  }

  public int getNumberOfCandidates() {
    return numberOfCandidates;
  }

  /**
   * This function is left here for custom spectrum files (just columns of mz and intensity values)
   * Does similar to mgf parser functionality
   */
  public static MsSpectrum readCustomMsFile(File file, String delimeter)
      throws IOException, MSDKRuntimeException {
    logger.info("Started reading {}", file.getName());

    Scanner sc = new Scanner(file);
    ArrayList<String> strings = new ArrayList<>();
    while (sc.hasNext()) {
      strings.add(sc.nextLine());
    }
    sc.close();

    int size = strings.size();
    double mz[] = new double[size];
    float intensity[] = new float[size];

    int index = 0;
    for (String s : strings) {
      String[] splitted = s.split(delimeter);
      if (splitted.length == 2) {
        mz[index] = Double.parseDouble(splitted[0]);
        intensity[index++] = Float.parseFloat(splitted[1]);
      } else {
        throw new MSDKRuntimeException("Incorrect spectrum structure");
      }
    }

    MsSpectrumType type = SpectrumTypeDetectionAlgorithm.detectSpectrumType(mz, intensity, size);
    logger.info("Finished reading {}", file.getName());

    return new SimpleMsSpectrum(mz, intensity, size, type);
  }

  /**
   * Transformation of MSDK data structures into Sirius structures and processing by Sirius
   * Method is left to be protected for test coverage
   */
  protected List<IdentificationResult> siriusProcessSpectra() throws MSDKException {
    Spectrum<Peak> siriusMs1 = null, siriusMs2;
    double mz[] = ms2.getMzValues();
    double intensity[] = LocalArrayUtil.convertToDoubles(ms2.getIntensityValues());

    siriusMs2 = sirius.wrapSpectrum(mz, intensity);
    String ionization = ion.getName();
    PrecursorIonType precursor = sirius.getPrecursorIonType(ionization);

    /* MutableMs2Experiment allows to specify additional fields and it is exactly what comes from .getMs2Experiment */
    MutableMs2Experiment experiment = (MutableMs2Experiment) sirius.getMs2Experiment(parentMass, precursor, siriusMs1, siriusMs2);

    /* Method above does not use Ms1 spectrum anyway, I have to add it manually if exists */
    if (ms1 != null) {
      mz = ms1.getMzValues();
      intensity = LocalArrayUtil.convertToDoubles(ms1.getIntensityValues());
      siriusMs1 = sirius.wrapSpectrum(mz, intensity);
      /* MutableMs2Experiment does not accept Ms1 as a Spectrum<Peak>, so there is a new object */
      experiment.getMs1Spectra().add(new SimpleSpectrum(siriusMs1));
    }

    /* Manual setting up annotations, because sirius.identify does not use some of the fields */
    // TODO: think about deviation
    sirius.setAllowedMassDeviation(experiment, deviation);
    sirius.enableRecalibration(experiment, true);
    sirius.setIsotopeMode(experiment, IsotopePatternHandling.both);
    if (constraints != null)
      sirius.setFormulaConstraints(experiment, constraints);

    logger.debug("Sirius starts processing MsSpectrums");
    List<IdentificationResult> siriusResults = siriusResults = sirius.identify(experiment,
        numberOfCandidates, true, IsotopePatternHandling.both, constraints);
    logger.debug("Sirius finished processing and returned {} results", siriusResults.size());

    return siriusResults;
  }

  @Nullable
  @Override
  public Float getFinishedPercentage() {
    return null;
  }

  @Nullable
  @Override
  public List<IonAnnotation> execute() throws MSDKException {
    logger.info("Started execution of SiriusIdentificationMethod");

    result = new ArrayList<>();
    List<IdentificationResult> siriusSpectra = siriusProcessSpectra();

    for (IdentificationResult r : siriusSpectra) {
      if (cancelled)
        return null;
      SimpleIonAnnotation ionAnnotation = new SimpleIonAnnotation();
      IMolecularFormula formula = MolecularFormulaManipulator
          .getMolecularFormula(r.getMolecularFormula().toString(),
              SilentChemObjectBuilder.getInstance());
      ionAnnotation.setFormula(formula);
      result.add(ionAnnotation);
    }

    logger.info("Finished execution of SiriusIdentificationMethod");
    return result;
  }

  @Nullable
  @Override
  public List<IonAnnotation> getResult() {
    return result;
  }

  @Override
  public void cancel() {
    cancelled = true;
  }
}
