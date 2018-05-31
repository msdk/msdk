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
import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.SimpleIonAnnotation;
import io.github.msdk.datamodel.SimpleMsSpectrum;
import io.github.msdk.spectra.centroidprofiledetection.SpectrumTypeDetectionAlgorithm;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.annotation.Nullable;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

/**
 * <p> SiriusIdentificationMethod class. </p>
 *
 *      This class wraps the Sirius module and transforms its results into MSDK data structures
 *      Transformation of IdentificationResult (Sirius) into IonAnnatation (MSDK)
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

  private final Sirius sirius;
  private MsSpectrum ms1;
  private MsSpectrum ms2;
  private double parentMass;
  private String ion;
  private int numberOfCandidates;
  private List<IonAnnotation> result;

  /**
   * <p> Constructor for SiriusIdentificationMethod class. </p>
   * @param ms1 - can be null! MsSpectrum level 1
   * @param ms2 - MsSpectrum level 2
   * @param parentMass - Most intensive usually or specified
   * @param ion - Ionization
   */
  public SiriusIdentificationMethod(@Nullable MsSpectrum ms1, MsSpectrum ms2, double parentMass,
      String ion) {
    sirius = new Sirius();
    this.ms1 = ms1;
    this.ms2 = ms2;
    this.parentMass = parentMass;
    this.ion = ion;
    numberOfCandidates = 5;
  }

  public double getParentMass() {
    return parentMass;
  }

  public void setParentMass(double mass) {
    parentMass = mass;
  }

  public String getIonization() {
    return ion;
  }

  public void setIonization(String ion) {
    this.ion = ion;
  }

  public MsSpectrum getMsSpectrum() {
    return ms1;
  }

  public void setMsSpectrum(MsSpectrum ms1) {
    this.ms1 = ms1;
  }

  public MsSpectrum getMs2Spectrum() {
    return ms2;
  }

  public void setMs2Spectrum(MsSpectrum ms2) {
    this.ms2 = ms2;
  }

  public int getNumberOfCandidates() {
    return numberOfCandidates;
  }

  public void setNumberOfCandidates(int number) {
    numberOfCandidates = number;
  }

  /**
   * This function is left here for custom spectrum files (just columns of mz and intensity values)
   * Does similar to mgf parser functionality
   */
  public static MsSpectrum readCustomMsFile(File file, String delimeter)
      throws IOException, MSDKRuntimeException {
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
    return new SimpleMsSpectrum(mz, intensity, size, type);
  }


  /**
   * Transformation of MSDK data structures into Sirius structures and processing by Sirius
   * Method is left to be protected for test coverage
   */
  protected List<IdentificationResult> siriusProcessSpectrums() throws MSDKException {
    Spectrum<Peak> siriusMs1 = null, siriusMs2;
    siriusMs2 = sirius
        .wrapSpectrum(ms2.getMzValues(), LocalArrayUtil.convertToDoubles(ms2.getIntensityValues()));
    if (ms1 != null) {
      siriusMs1 = sirius.wrapSpectrum(ms1.getMzValues(),
          LocalArrayUtil.convertToDoubles(ms1.getIntensityValues()));
    }

    PrecursorIonType precursor = sirius.getPrecursorIonType(ion);
    Ms2Experiment experiment = sirius.getMs2Experiment(parentMass, precursor, siriusMs1, siriusMs2);

//    TODO: think about IsotopePatternHandling type
    List<IdentificationResult> siriusResults = sirius
        .identify(experiment, numberOfCandidates, true, IsotopePatternHandling.omit);
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
    result = new ArrayList<>();
    List<IdentificationResult> siriusSpectrums = siriusProcessSpectrums();

    for (IdentificationResult r: siriusSpectrums) {
      SimpleIonAnnotation ionAnnotation = new SimpleIonAnnotation();
      IMolecularFormula formula = MolecularFormulaManipulator.getMolecularFormula(r.getMolecularFormula().toString(),
          SilentChemObjectBuilder.getInstance());
      ionAnnotation.setFormula(formula);
      result.add(ionAnnotation);
    }

    return result;
  }

  @Nullable
  @Override
  public List<IonAnnotation> getResult() {
    return result;
  }

  // TODO: implement
  @Override
  public void cancel() {

  }
}
