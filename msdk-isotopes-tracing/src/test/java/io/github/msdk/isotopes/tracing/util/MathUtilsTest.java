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

package io.github.msdk.isotopes.tracing.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.isotopes.tracing.data.IsotopeFormula;
import io.github.msdk.isotopes.tracing.data.IsotopeList;
import io.github.msdk.isotopes.tracing.data.constants.Element;
import junit.framework.TestCase;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class MathUtilsTest extends TestCase {

  private static final Logger LOG = LoggerFactory.getLogger(MathUtilsTest.class);

  /*
   * test the MathUtils.naturalAbundance method for isotopologues consisting of isotopes that
   * correspond all to the same kind of element, where only elements with two existing isotopes are
   * considered
   */
  public void testNaturalAbundance() {
    int totalElements = 10;
    for (Element element : Element.values()) {
      IsotopeList isotopes = element.getIsotopes();
      double firstIsotopeAbundance = isotopes.get(0).getAbundance();
      if (isotopes.size() == 2) {
        for (int firstIsotopeNumber =
            0; firstIsotopeNumber <= totalElements; firstIsotopeNumber++) {
          IsotopeFormula isotopologue = new IsotopeFormula();
          isotopologue.put(isotopes.get(0), firstIsotopeNumber);
          isotopologue.put(isotopes.get(1), totalElements - firstIsotopeNumber);
          Double expectedAbundance = MathUtils.binom(totalElements, firstIsotopeNumber)
              * Math.pow(1 - firstIsotopeAbundance, totalElements - firstIsotopeNumber)
              * Math.pow(firstIsotopeAbundance, firstIsotopeNumber);
          LOG.info("expectedAbundance" + expectedAbundance);
          Double actualAbundance = MathUtils.naturalAbundance(isotopologue);
          LOG.info("actualAbundance" + actualAbundance);
          assertEquals(MathUtils.round(expectedAbundance, 6), MathUtils.round(actualAbundance, 6));
        }
      }
    }
  }

}
