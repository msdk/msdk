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
package io.github.msdk.isotopes.tracing.data;

import java.util.ArrayList;
import java.util.Map.Entry;

import io.github.msdk.isotopes.tracing.data.constants.IntensityType;

/**
 * A {@link MassSpectrum} where each peak corresponds to an isotopologue of the parent peak
 * molecule. This class also provides a member to store the isotope compositions.
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class IsotopePattern extends MassSpectrum {

  /*
   * ordered as the entries in the MassSpectrum
   */
  private ArrayList<IsotopeFormula> formulas;

  public IsotopePattern(IntensityType intensityType, ArrayList<IsotopeFormula> formulas) {
    super(intensityType);
    this.formulas = formulas;
  }

  public ArrayList<IsotopeFormula> getFormulas() {
    return formulas;
  }

  public void setFormulas(ArrayList<IsotopeFormula> formulas) {
    this.formulas = formulas;
  }

  public IsotopeFormula getFormula(int index) {
    return formulas.get(index);
  }

  /**
   * 
   * @param mass
   * @return The {@link IsotopeFormula} corresponding to this mass in the spectrum. An empty
   *         formula, if the mass is not part of the spectrum.
   * 
   */
  public IsotopeFormula getFormula(Double mass) {
    int entryCount = 0;
    for (Entry<Double, Double> entry : this.entrySet()) {
      if (entry.getKey().equals(mass)) {
        return formulas.get(entryCount);
      }
      entryCount++;
    }
    return new IsotopeFormula();
  }

  @Override
  public String toString() {
    DataTable dataTable = new DataTable("Mass", "Frequency", "Formula");
    dataTable.addColumn(this);
    ArrayList<String> formulaStr = new ArrayList();
    for (IsotopeFormula formula : formulas) {
      formulaStr.add(formula.toSimpleString());
    }
    dataTable.addColumn(formulaStr);
    return dataTable.toString("NA", true);
  }
}
