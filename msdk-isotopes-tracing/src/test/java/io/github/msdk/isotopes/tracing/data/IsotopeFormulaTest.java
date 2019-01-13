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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.isotopes.tracing.data.constants.Element;
import io.github.msdk.isotopes.tracing.data.constants.Isotope;
import junit.framework.TestCase;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class IsotopeFormulaTest extends TestCase {
  private static final Logger LOGGER = LoggerFactory.getLogger(IsotopeFormulaTest.class);

  public void testToElementFormula() {
    IsotopeFormula isotopeFormula = new IsotopeFormula();
    isotopeFormula.put(Isotope.C_12, 2);
    isotopeFormula.put(Isotope.C_13, 3);
    isotopeFormula.put(Isotope.H_1, 2);
    isotopeFormula.put(Isotope.H_2, 5);
    isotopeFormula.put(Isotope.N_15, 2);
    ElementFormula elementFormula = new ElementFormula();
    elementFormula.put(Element.C, 5);
    elementFormula.put(Element.H, 7);
    elementFormula.put(Element.N, 2);
    LOGGER.info("expected element Formula" + elementFormula);
    LOGGER.info("actual element Formula" + isotopeFormula.toElementFormula());
    assertEquals(elementFormula, isotopeFormula.toElementFormula());
  }

  public void testToIsotopeList() {
    IsotopeFormula isotopeFormula = new IsotopeFormula();
    isotopeFormula.put(Isotope.C_12, 2);
    isotopeFormula.put(Isotope.C_13, 3);
    isotopeFormula.put(Isotope.H_1, 2);
    isotopeFormula.put(Isotope.H_2, 5);
    isotopeFormula.put(Isotope.N_15, 2);
    IsotopeList isotopeList = new IsotopeList();
    isotopeList.add(Isotope.C_12);
    isotopeList.add(Isotope.C_13);
    isotopeList.add(Isotope.H_1);
    isotopeList.add(Isotope.H_2);
    isotopeList.add(Isotope.N_15);
    LOGGER.info("expected isotopeList" + isotopeList);
    LOGGER.info("actual isotopeList" + isotopeFormula.toIsotopeList());
    assertEquals(isotopeList, isotopeFormula.toIsotopeList());
  }

  public void testToNiceFormattedFormula() {
    IsotopeFormula isotopeFormula = new IsotopeFormula();
    isotopeFormula.put(Isotope.C_12, 2);
    isotopeFormula.put(Isotope.C_13, 3);
    isotopeFormula.put(Isotope.H_1, 2);
    isotopeFormula.put(Isotope.H_2, 5);
    isotopeFormula.put(Isotope.N_15, 2);
    LOGGER.info("actual niceFormula" + isotopeFormula.toNiceFormattedFormula());
  }

}
