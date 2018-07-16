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

import de.unijena.bioinf.ChemistryBase.chem.ChemicalAlphabet;
import de.unijena.bioinf.ChemistryBase.chem.Element;
import de.unijena.bioinf.ChemistryBase.chem.FormulaConstraints;
import de.unijena.bioinf.ChemistryBase.chem.PeriodicTable;

import java.util.Arrays;

import org.openscience.cdk.formula.MolecularFormulaRange;
import org.openscience.cdk.interfaces.IIsotope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p> Class ConstraintsGenerator. </p>
 * This class allows to construct a Sirius object FormulaConstraints using MolecularFormulaRange object
 */
public class ConstraintsGenerator {
  private static final Logger logger = LoggerFactory.getLogger(ConstraintsGenerator.class);
  private static final String[] defaultElementSymbols = new String[]{"C", "H", "N", "O", "P"};
  private static final Element[] defaultElements;
  private static final PeriodicTable periodicTable = PeriodicTable.getInstance();
  private static final int maxNumberOfOneElements = 20;

  static {
    defaultElements = new Element[defaultElementSymbols.length];
    for (int i = 0; i < defaultElementSymbols.length; i++)
      defaultElements[i] = periodicTable.getByName(defaultElementSymbols[i]);
  }

  private ConstraintsGenerator() {}

  /**
   * <p> Method for generating FormulaConstraints from user-defined search space</p>
   * Parses isotopes from input parameter and transforms it into Element objects and sets their range value
   * @param range - User defined search space of possible elements
   * @return new Constraint to be used in Sirius
   */
  public static FormulaConstraints generateConstraint(MolecularFormulaRange range) {
    logger.debug("ConstraintsGenerator started processing");
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

    synchronized (periodicTable) {
      // Specify each Element range
      for (IIsotope isotope : range.isotopes()) {
        int atomicNumber = isotope.getAtomicNumber();
        final Element element = periodicTable.get(atomicNumber);
        int min = range.getIsotopeCountMin(isotope);
        int max = range.getIsotopeCountMax(isotope);

        constraints.setLowerbound(element, min);
        if (max != maxNumberOfOneElements)
          constraints.setUpperbound(element, max);
      }
    }

    logger.debug("ConstraintsGenerator finished");
    return constraints;
  }
}