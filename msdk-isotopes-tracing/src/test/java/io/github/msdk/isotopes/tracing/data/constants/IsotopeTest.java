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

package io.github.msdk.isotopes.tracing.data.constants;

import java.util.InputMismatchException;

import io.github.msdk.isotopes.tracing.data.IsotopeList;
import junit.framework.TestCase;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class IsotopeTest extends TestCase {

  public void testByName() {
    assertEquals(Isotope.H_1, Isotope.byName("H_1"));
    assertEquals(Isotope.H_2, Isotope.byName("H_2"));
    assertEquals(Isotope.UNDEFINED, Isotope.byName("UNDEFINED"));
    assertEquals(Isotope.C_13, Isotope.byName("C_13"));
  }

  public void testFailByName() {
    try {
      assertEquals(Isotope.UNDEFINED, Isotope.byName("C13"));
      fail("This should shrow an InputMismatchException");
    } catch (InputMismatchException e) {
      // this exception was expected
    }
  }

  public void testApproximatelyByMassShift() {
    IsotopeList expectedIsotopes = new IsotopeList(Isotope.C_13, Isotope.C_13, Isotope.N_15,
        Isotope.Si_29, Isotope.Si_30, Isotope.UNDEFINED);
    IsotopeList actualList = new IsotopeList(Isotope.approximatelyByMassShiftValue(1.0033),
        Isotope.approximatelyByMassShiftValue(1.0034),
        Isotope.approximatelyByMassShiftValue(0.9970),
        Isotope.approximatelyByMassShiftValue(0.9996), Isotope.approximatelyByMassShiftValue(2.0),
        Isotope.approximatelyByMassShiftValue(0.5));
    assertEquals(expectedIsotopes, actualList);
  }

}
