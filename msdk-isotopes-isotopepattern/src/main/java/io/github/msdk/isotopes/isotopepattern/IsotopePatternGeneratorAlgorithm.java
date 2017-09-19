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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import io.github.msdk.util.MsSpectrumUtil;

/**
 * Generates isotope patterns for chemical formulas
 */
public class IsotopePatternGeneratorAlgorithm {

  private static final double ELECTRON_MASS = 5.4857990943E-4;

  private static final Pattern formulaPattern =
      Pattern.compile("^[\\[\\(]?(([A-Z][a-z]?[0-9]*)+)[\\]\\)]?(([0-9]*)([-+]))?$");

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
      @Nonnull Double minAbundance, @Nonnull Float intensityScale, @Nonnull Double mzTolerance) {

    Matcher m = formulaPattern.matcher(chemicalFormula);
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

    return generateIsotopes(cdkFormula, charge, minAbundance, intensityScale, mzTolerance);

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
      @Nonnull Double mzTolerance) {

    IsotopePatternGenerator generator = new IsotopePatternGenerator(minAbundance);

    IsotopePattern pattern = generator.getIsotopes(cdkFormula);

    final int numOfIsotopes = pattern.getNumberOfIsotopes();
    final double mzValues[] = new double[numOfIsotopes];
    final float intensityValues[] = new float[numOfIsotopes];

    for (int i = 0; i < numOfIsotopes; i++) {
      IsotopeContainer isotope = pattern.getIsotope(i);

      // For each unit of charge, we have to add or remove a mass of a
      // single electron. If the charge is positive, we remove electron
      // mass. If the charge is negative, we add it.
      mzValues[i] = isotope.getMass() - (charge * ELECTRON_MASS);

      if (charge != 0)
        mzValues[i] /= Math.abs(charge);

      intensityValues[i] = (float) isotope.getIntensity();

    }

    final int newSize = mergeIsotopes(mzValues, intensityValues, numOfIsotopes, mzTolerance);

    MsSpectrumUtil.normalizeIntensity(intensityValues, newSize, intensityScale);

    MsSpectrum result = new SimpleMsSpectrum(mzValues, intensityValues, newSize,
        MsSpectrumType.CENTROIDED);

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
