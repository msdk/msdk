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

import io.github.msdk.isotopes.tracing.data.constants.Isotope;
import junit.framework.TestCase;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class IsotopeMassComparatorTest extends TestCase {

  public void testCompare() {
    assertEquals(-1, new IsotopeMassComparator().compare(Isotope.H_1, Isotope.H_2));
    assertEquals(0, new IsotopeMassComparator().compare(Isotope.C_12, Isotope.C_12));
    assertEquals(1, new IsotopeMassComparator().compare(Isotope.N_15, Isotope.N_14));
  }

}
