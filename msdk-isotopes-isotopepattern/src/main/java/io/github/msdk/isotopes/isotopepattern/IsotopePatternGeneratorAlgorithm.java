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

package io.github.msdk.isotopes.isotopepattern;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;

import javax.annotation.Nonnull;

import org.openscience.cdk.formula.IsotopeContainer;
import org.openscience.cdk.formula.IsotopePattern;
import org.openscience.cdk.formula.IsotopePatternGenerator;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import com.google.common.base.Strings;

import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.SimpleMsSpectrum;
import io.github.msdk.isotopes.isotopepattern.impl.ExtendedIsotopePattern;
import io.github.msdk.util.MsSpectrumUtil;

/**
 * Generates isotope patterns for chemical formulas
 */
public class IsotopePatternGeneratorAlgorithm {

  private static final double ELECTRON_MASS = 5.4857990943E-4;

  public static void main(String[] args) throws IOException {
    String chemicalFormula = "C7H14NOSi+";
    String capacityFormula = "C4N";
    String tracer1 = "13C";
    String tracer2 = "15N";
    double tracer1Inc = 0.2;
    double tracer2Inc = 0.2;
    double tracerAllInc = 0.2;
    double minAbundance = 0.0001;
    float intensityScale = 1000f;
    double mzTolerance = 0.0001;
    boolean storeFormula = true;
    MsSpectrum spectrum =
        simulateMultiTracedPattern(chemicalFormula, capacityFormula, tracer1, tracer2, tracer1Inc,
            tracer2Inc, tracerAllInc, minAbundance, intensityScale, mzTolerance, storeFormula);
    System.out.println(Arrays.toString(spectrum.getMzValues()));
    System.out.println(Arrays.toString(spectrum.getIntensityValues()));
    System.out
        .println(Arrays.toString(((ExtendedIsotopePattern) spectrum).getIsotopeComposition()));
  }

  public static @Nonnull MsSpectrum simulateMultiTracedPattern(@Nonnull String chemicalFormula,
      @Nonnull String capacityFormula, @Nonnull String tracer1, @Nonnull String tracer2,
      @Nonnull double tracer1Inc, @Nonnull double tracer2Inc, double tracerAllInc,
      @Nonnull Double minAbundance, @Nonnull Float intensityScale, @Nonnull Double mzTolerance,
      @Nonnull Boolean storeFormula) throws IOException {
    IsotopePatternGeneratorUtils.multiTracerQualityCheck(chemicalFormula, capacityFormula, tracer1,
        tracer2, tracer1Inc, tracer2Inc, tracerAllInc);
    Matcher m = IsotopePatternGeneratorUtils.formulaPattern.matcher(chemicalFormula);
    m.matches();
    String formulaNoCharge = m.group(1);
    String chargeCount = m.group(4);
    String chargeSign = m.group(5);
    String suffix = chargeCount + chargeSign;
    String tracer1ReducedFormula = "[" + IsotopePatternGeneratorUtils.reduceFormula(formulaNoCharge,
        capacityFormula, tracer1, null) + "]" + suffix;
    String tracer2ReducedFormula = "[" + IsotopePatternGeneratorUtils.reduceFormula(formulaNoCharge,
        capacityFormula, null, tracer2) + "]" + suffix;
    String tracerBothReducedFormula = "[" + IsotopePatternGeneratorUtils
        .reduceFormula(formulaNoCharge, capacityFormula, tracer1, tracer2) + "]" + suffix;
    double totalInc = tracer1Inc + tracer2Inc + tracerAllInc;
    MsSpectrum naturalPattern = generateIsotopes(chemicalFormula, minAbundance,
        (float) (1.0f - totalInc), mzTolerance, storeFormula);
    MsSpectrum tracer1ReducedPattern = generateIsotopes(tracer1ReducedFormula, minAbundance,
        (float) tracer1Inc, mzTolerance, storeFormula);
    MsSpectrum tracer2ReducedPattern = generateIsotopes(tracer2ReducedFormula, minAbundance,
        (float) tracer2Inc, mzTolerance, storeFormula);
    MsSpectrum tracerBothReducedPattern = generateIsotopes(tracerBothReducedFormula, minAbundance,
        (float) tracerAllInc, mzTolerance, storeFormula);
    tracer1ReducedPattern = IsotopePatternGeneratorUtils.addTracerMass(tracer1ReducedPattern,
        capacityFormula, tracer1, null);
    tracer2ReducedPattern = IsotopePatternGeneratorUtils.addTracerMass(tracer2ReducedPattern,
        capacityFormula, null, tracer2);
    tracerBothReducedPattern = IsotopePatternGeneratorUtils.addTracerMass(tracerBothReducedPattern,
        capacityFormula, tracer1, tracer2);
    if (storeFormula) {
      tracer1ReducedPattern = IsotopePatternGeneratorUtils
          .addTracerComposition(tracer1ReducedPattern, capacityFormula, tracer1, null);
      tracer2ReducedPattern = IsotopePatternGeneratorUtils
          .addTracerComposition(tracer2ReducedPattern, capacityFormula, null, tracer2);
      tracerBothReducedPattern = IsotopePatternGeneratorUtils
          .addTracerComposition(tracerBothReducedPattern, capacityFormula, tracer1, tracer2);

    }
    MsSpectrum mergedSpectrum =
        IsotopePatternGeneratorUtils.merge(naturalPattern, tracer1ReducedPattern);
    mergedSpectrum = IsotopePatternGeneratorUtils.merge(mergedSpectrum, tracer2ReducedPattern);
    mergedSpectrum = IsotopePatternGeneratorUtils.merge(mergedSpectrum, tracerBothReducedPattern);
    mergedSpectrum = IsotopePatternGeneratorUtils.normalize(mergedSpectrum, intensityScale);
    return mergedSpectrum;
  }

  public static @Nonnull MsSpectrum generateIsotopes(@Nonnull String chemicalFormula,
      @Nonnull Double minAbundance, @Nonnull Float intensityScale, @Nonnull Double mzTolerance) {
    return generateIsotopes(chemicalFormula, minAbundance, intensityScale, mzTolerance, false);
  }

  /**
   * <p>
   * generateIsotopes.
   * </p>
   *
   * @param chemicalFormula a {@link java.lang.String} object.
   * @param minAbundance a {@link java.lang.Double} object.
   * @param intensityScale a {@link java.lang.Float} object.
   * @param mzTolerance a {@link java.lang.Double} object.
   * @return a {@link io.github.msdk.datamodel.MsSpectrum} object.
   */
  public static @Nonnull MsSpectrum generateIsotopes(@Nonnull String chemicalFormula,
      @Nonnull Double minAbundance, @Nonnull Float intensityScale, @Nonnull Double mzTolerance,
      @Nonnull Boolean storeFormula) {

    Matcher m = IsotopePatternGeneratorUtils.formulaPattern.matcher(chemicalFormula);
    if (!m.matches())
      throw new IllegalArgumentException("Invalid chemical formula: " + chemicalFormula);
    String formulaNoCharge = m.group(1);
    String chargeCount = m.group(4);
    String chargeSign = m.group(5);

    // Parse charge, 0 is default
    int charge = 0;
    if (!Strings.isNullOrEmpty(chargeCount))
      charge = Integer.parseInt(chargeCount);

    // Simple + or - indicates charge 1
    if ((charge == 0) && (!Strings.isNullOrEmpty(chargeSign)))
      charge = 1;

    // Check for negative charge
    if ("-".equals(chargeSign))
      charge *= -1;

    IMolecularFormula cdkFormula = MolecularFormulaManipulator.getMolecularFormula(formulaNoCharge,
        SilentChemObjectBuilder.getInstance());

    if (cdkFormula == null)
      throw new MSDKRuntimeException(
          "Could not generate CDK chemical formula for " + formulaNoCharge);

    return generateIsotopes(cdkFormula, charge, minAbundance, intensityScale, mzTolerance,
        storeFormula);

  }

  public static @Nonnull MsSpectrum generateIsotopes(@Nonnull IMolecularFormula cdkFormula,
      @Nonnull Integer charge, @Nonnull Double minAbundance, @Nonnull Float intensityScale,
      @Nonnull Double mzTolerance) {
    return generateIsotopes(cdkFormula, charge, minAbundance, intensityScale, mzTolerance, false);
  }

  /**
   * <p>
   * generateIsotopes.
   * </p>
   *
   * @param cdkFormula a {@link org.openscience.cdk.interfaces.IMolecularFormula} object.
   * @param charge a {@link java.lang.Integer} object.
   * @param minAbundance a {@link java.lang.Double} object.
   * @param intensityScale a {@link java.lang.Float} object.
   * @param mzTolerance a {@link java.lang.Double} object.
   * @return a {@link io.github.msdk.datamodel.MsSpectrum} object.
   */
  public static @Nonnull MsSpectrum generateIsotopes(@Nonnull IMolecularFormula cdkFormula,
      @Nonnull Integer charge, @Nonnull Double minAbundance, @Nonnull Float intensityScale,
      @Nonnull Double mzTolerance, @Nonnull Boolean storeFormula) {

    IsotopePatternGenerator generator = new IsotopePatternGenerator(minAbundance);
    generator.setStoreFormulas(storeFormula);

    IsotopePattern pattern = generator.getIsotopes(cdkFormula);

    final int numOfIsotopes = pattern.getNumberOfIsotopes();
    final double mzValues[] = new double[numOfIsotopes];
    final float intensityValues[] = new float[numOfIsotopes];
    String isotopeComposition[] = new String[numOfIsotopes];

    for (int i = 0; i < numOfIsotopes; i++) {
      IsotopeContainer isotope = pattern.getIsotope(i);

      // For each unit of charge, we have to add or remove a mass of a
      // single electron. If the charge is positive, we remove electron
      // mass. If the charge is negative, we add it.
      mzValues[i] = isotope.getMass() - (charge * ELECTRON_MASS);

      if (charge != 0)
        mzValues[i] /= Math.abs(charge);

      intensityValues[i] = (float) isotope.getIntensity();

      if (storeFormula) {
        isotopeComposition[i] = IsotopePatternGeneratorUtils.formatCDKString(isotope.toString());
      }

    }

    final int newSize = mergeIsotopes(mzValues, intensityValues, numOfIsotopes, mzTolerance);

    MsSpectrumUtil.normalizeIntensity(intensityValues, newSize, intensityScale);

    MsSpectrum result;
    if (storeFormula) {
      String formulaString = MolecularFormulaManipulator.getString(cdkFormula);
      result = new ExtendedIsotopePattern(mzValues, intensityValues, newSize,
          MsSpectrumType.CENTROIDED, formulaString, isotopeComposition);
    } else {
      result = new SimpleMsSpectrum(mzValues, intensityValues, newSize, MsSpectrumType.CENTROIDED);
    }

    return result;

  }

  /**
   * Merges the isotopes falling within the given m/z tolerance. If the m/z difference between the
   * isotopes is smaller than mzTolerance, their intensity is added together and new m/z value is
   * calculated as a weighted average.
   */
  private static int mergeIsotopes(double mzValues[], float intensityValues[], int size,
      double mzTolerance) {
    if (mzValues.length != intensityValues.length)
      throw new IllegalArgumentException("Mass and intensity arrays must be of the same size");
    if (mzValues.length < 2)
      return mzValues.length;

    int ptrCur = 0;
    int ptrNex = 1;
    for (; ptrNex < mzValues.length; ptrNex++) {
      double mzCur = mzValues[ptrCur];
      double mzNex = mzValues[ptrNex];
      if (Math.abs(mzCur - mzNex) < mzTolerance) {
        // merge, only next pointer moves
        float abCur = intensityValues[ptrCur];
        float abNex = intensityValues[ptrNex];
        float abNew = abCur + abNex;
        double mzNew = (mzCur * abCur + mzNex * abNex) / abNew;
        mzValues[ptrCur] = mzNew;
        intensityValues[ptrCur] = abNew;

      } else {
        // don't merge, move current pointer and copy the value there
        ptrCur++;

        mzValues[ptrCur] = mzValues[ptrNex];
        intensityValues[ptrCur] = intensityValues[ptrNex];
      }
    }

    return ptrCur + 1;

  }

}
