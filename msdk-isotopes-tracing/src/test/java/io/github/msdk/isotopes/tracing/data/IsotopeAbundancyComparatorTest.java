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
public class IsotopeAbundancyComparatorTest extends TestCase {

  public void testCompare() {
    IsotopeAbundancyComparator comparator = new IsotopeAbundancyComparator();
    assertEquals(1, comparator.compare(Isotope.C_12, Isotope.C_13));
    assertEquals(-1, comparator.compare(Isotope.N_15, Isotope.N_14));
    assertEquals(0, comparator.compare(Isotope.P_31, Isotope.P_31));
  }

}
