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

import io.github.msdk.isotopes.tracing.data.constants.Isotope;
import junit.framework.TestCase;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class IsotopeListListTest extends TestCase {

  private static final Logger LOG = LoggerFactory.getLogger(IsotopeListListTest.class);

  public void testToString() {
    IsotopeListList isotopeListList = new IsotopeListList(
        new IsotopeList(Isotope.B_10, Isotope.B_11), new IsotopeList(Isotope.C_12, Isotope.C_13));
    LOG.info("isotopeListList" + isotopeListList);
    assertEquals("[B_10|B_11][C_12|C_13]", isotopeListList.toString());
  }

  public void testFromString() {
    IsotopeListList expectedList = new IsotopeListList(new IsotopeList(Isotope.B_10, Isotope.B_11),
        new IsotopeList(Isotope.C_12, Isotope.C_13));
    LOG.info("actualList" + IsotopeListList.fromString("[B_10|B_11][C_12|C_13]"));
    assertEquals(expectedList, IsotopeListList.fromString("[B_10|B_11][C_12|C_13]"));
  }

  public void testToCommaSeparatedCountString() {
    IsotopeListList isotopeListList =
        new IsotopeListList(new IsotopeList(Isotope.B_10, Isotope.B_11, Isotope.C_13),
            new IsotopeList(Isotope.C_12, Isotope.C_13, Isotope.B_10));
    LOG.info("actualCountString" + isotopeListList.toCommaSeparatedCountString());
    assertEquals("B_10: 2, B_11: 1, C_13: 2, C_12: 1",
        isotopeListList.toCommaSeparatedCountString());
  }

}
