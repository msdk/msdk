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
import io.github.msdk.datamodel.IonType;
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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

/**
 * <p> SiriusIdentificationMethod class. </p>
 *
 * This class wraps the Sirius module and transforms its results into MSDK data structures
 * Transformation of IdentificationResult (Sirius) into IonAnnatation (MSDK)
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
  private final MsSpectrum ms1;
  private final MsSpectrum ms2;
  private final Double parentMass;
  private final IonType ion;
  private final int numberOfCandidates;
  private static boolean cancelled = false;
  private List<IonAnnotation> result;

  /**
   * <p> Constructor for SiriusIdentificationMethod class. </p>
   *
   * @param ms1 - can be null! MsSpectrum level 1
   * @param ms2 - MsSpectrum level 2
   * @param parentMass - Most intensive usually or specified
   * @param ion - Ionization
   * @param numberOfCandidates - amount of IdentificationResults to be returned from Sirius
   */
  public SiriusIdentificationMethod(@Nullable MsSpectrum ms1, @Nonnull MsSpectrum ms2, Double parentMass,
      IonType ion, int numberOfCandidates) {
    sirius = new Sirius();
    this.ms1 = ms1;
    this.ms2 = ms2;
    this.parentMass = parentMass;
    this.ion = ion;
    this.numberOfCandidates = numberOfCandidates;
  }

  public SiriusIdentificationMethod(@Nullable MsSpectrum ms1, @Nonnull MsSpectrum ms2, Double parentMass,
      IonType ion) { this(ms1, ms2, parentMass, ion, 5); }
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
    Scanner sc = new Scanner(file);
    ArrayList<String> strings = new ArrayList<>();
    while (sc.hasNext()) {
      if (cancelled)
        return null;
      strings.add(sc.nextLine());
    }
    sc.close();

    int size = strings.size();
    double mz[] = new double[size];
    float intensity[] = new float[size];

    int index = 0;
    for (String s : strings) {
      if (cancelled)
        return null;
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
  protected List<IdentificationResult> siriusProcessSpectra() throws MSDKException {
    Spectrum<Peak> siriusMs1 = null, siriusMs2;
    double mz[] = ms2.getMzValues();
    double intensity[] = LocalArrayUtil.convertToDoubles(ms2.getIntensityValues());
    siriusMs2 = sirius.wrapSpectrum(mz, intensity);

    if (ms1 != null) {
      mz = ms1.getMzValues();
      intensity = LocalArrayUtil.convertToDoubles(ms1.getIntensityValues());
      siriusMs1 = sirius.wrapSpectrum(mz, intensity);
    }

    String ionization = ion.getName();
    PrecursorIonType precursor = sirius.getPrecursorIonType(ionization);
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
