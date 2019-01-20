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

package io.github.msdk.isotopes.tracing.simulation;

import java.util.ArrayList;
import java.util.Map.Entry;

import io.github.msdk.isotopes.tracing.data.ElementFormula;
import io.github.msdk.isotopes.tracing.data.Fragment;
import io.github.msdk.isotopes.tracing.data.FragmentList;
import io.github.msdk.isotopes.tracing.data.IsotopeFormula;
import io.github.msdk.isotopes.tracing.data.IsotopeListList;
import io.github.msdk.isotopes.tracing.data.IsotopePattern;
import io.github.msdk.isotopes.tracing.data.MSDatabase;
import io.github.msdk.isotopes.tracing.data.MSDatabaseList;
import io.github.msdk.isotopes.tracing.data.MSShiftDatabase;
import io.github.msdk.isotopes.tracing.data.MassShiftDataSet;
import io.github.msdk.isotopes.tracing.data.MassShiftList;
import io.github.msdk.isotopes.tracing.data.MassSpectrum;
import io.github.msdk.isotopes.tracing.data.constants.Element;
import io.github.msdk.isotopes.tracing.data.constants.Isotope;

/**
 * Includes a list of {@link MSDatabase}s corresponding to the requested fragments and options from
 * a {@link IsotopePatternSimulatorRequest}.
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class IsotopePatternSimulatorResponse {
  // TODO: optimize the IsotopePatternSimulatorResponse and the IsotopePatternSimulator simulation
  // methods to include a (labeled) MassSpectrum as response, where the labels include the
  // information on the isotopes that induced the peaks.
  private MSDatabaseList msDatabaseList;

  /**
   * @return the msDatabaseList
   */
  public MSDatabaseList getMsDatabaseList() {
    return msDatabaseList;
  }

  /**
   * @param msDatabaseList the msDatabaseList to set
   */
  public void setMsDatabaseList(MSDatabaseList msDatabaseList) {
    this.msDatabaseList = msDatabaseList;
  }
  
    /**
     * 
     * @param index
     *            index of the {@link Fragment} in the {@link FragmentList} of
     *            the {@link IsotopePatternSimulatorRequest} that corresponds to
     *            the spectrum you want to get.
     * @return Simulated spectrum with corresponding formulas. Attention! This
     *         only works if analyzeMassShifts was set to true in the request.
     */
  public IsotopePattern getIsotopePattern(int index) {
    // TODO: Ensure that MassShifts have been analyzed.
    MSShiftDatabase database = ((MSShiftDatabase) getMsDatabaseList().get(index));
    MassSpectrum mixedSpectrum = database.getMixedSpectrum();
    MassShiftDataSet mixedShifts = database.getMixedMassShifts();
    ElementFormula compoundFormula = ElementFormula.fromString(database.getCompoundFormula());
    ArrayList<IsotopeFormula> isotopeFormulas = new ArrayList<IsotopeFormula>();
    for (Entry<MassShiftList, IsotopeListList> shiftEntry : mixedShifts.entrySet()) {
      IsotopeFormula shiftInducingIsotopes = shiftEntry.getValue().toIsotopeFormula();
      IsotopeFormula completeIsotopeFormula = new IsotopeFormula();
      for (Entry<Element, Integer> compoundFormulaEntry : compoundFormula.entrySet()) {
        Element element = compoundFormulaEntry.getKey();
        for (Isotope isotope : element.getIsotopes()) {
          int totalElementNumber = compoundFormula.get(element);
          if (shiftInducingIsotopes.get(isotope) != null) {
            int numberOfHeavyIsotopes = shiftInducingIsotopes.get(isotope);
            completeIsotopeFormula.put(element.lightestIsotope(),
                totalElementNumber - numberOfHeavyIsotopes);
            completeIsotopeFormula.put(isotope, numberOfHeavyIsotopes);
          } else {
            completeIsotopeFormula.put(element.lightestIsotope(), totalElementNumber);
          }
        }
      }
      isotopeFormulas.add(completeIsotopeFormula);
    }
    IsotopePattern pattern = new IsotopePattern(mixedSpectrum.getIntensityType(), isotopeFormulas);
    for (Entry<Double, Double> entry : mixedSpectrum.entrySet()) {
      pattern.put(entry.getKey(), entry.getValue());
    }
    return pattern;
  }

    /**
     * 
     * @param index
     *            index of the {@link Fragment} in the {@link FragmentList} of
     *            the {@link IsotopePatternSimulatorRequest} that corresponds to
     *            the spectrum you want to get.
     * @return Simulated spectrum with corresponding formulas. Formulas only
     *         represent the heavy isotopes that induced this peak. Attention!
     *         This only works if analyzeMassShifts was set to true in the
     *         request.
     */
  public IsotopePattern getIsotopePatternWithReducedFormulas(int index) {
    // TODO: Ensure that MassShifts have been analyzed.
    MSShiftDatabase database = ((MSShiftDatabase) getMsDatabaseList().get(index));
    MassSpectrum mixedSpectrum = database.getMixedSpectrum();
    MassShiftDataSet mixedShifts = database.getMixedMassShifts();
    ArrayList<IsotopeFormula> isotopeFormulas = new ArrayList<IsotopeFormula>();
    for (Entry<MassShiftList, IsotopeListList> shiftEntry : mixedShifts.entrySet()) {
      isotopeFormulas.add(shiftEntry.getValue().toIsotopeFormula());
    }
    IsotopePattern pattern = new IsotopePattern(mixedSpectrum.getIntensityType(), isotopeFormulas);
    for (Entry<Double, Double> entry : mixedSpectrum.entrySet()) {
      pattern.put(entry.getKey(), entry.getValue());
    }
    return pattern;
  }

    /**
     * 
     * @param index
     *            index of the {@link Fragment} in the {@link FragmentList} of
     *            the {@link IsotopePatternSimulatorRequest} that corresponds to
     *            the spectrum you want to get.
     * @return Simulated spectrum.
     */
    public MassSpectrum getSpectrum(int index) {
        // TODO: Ensure that MassShifts have been analyzed.
        MSShiftDatabase database = ((MSShiftDatabase) getMsDatabaseList()
                .get(index));
        MassSpectrum mixedSpectrum = database.getMixedSpectrum();
        return mixedSpectrum;
    }

}
