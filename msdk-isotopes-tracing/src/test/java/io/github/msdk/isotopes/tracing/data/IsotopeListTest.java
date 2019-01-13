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
public class IsotopeListTest extends TestCase {
  private static final Logger LOGGER = LoggerFactory.getLogger(IsotopeListTest.class);

  public void testConstructor() {
    IsotopeList list = new IsotopeList(Isotope.C_12, Isotope.C_13, Isotope.C_12);
    assertEquals(3, list.size());
    assertEquals(Isotope.C_12, list.get(0));
    assertEquals(Isotope.C_13, list.get(1));
    assertEquals(Isotope.C_12, list.get(2));
  }

  public void testToString() {
    IsotopeList list = new IsotopeList(Isotope.C_12, Isotope.C_13, Isotope.C_12);
    assertEquals("C_12|C_13|C_12", list.toString());
  }

  public void testFromString() {
    IsotopeList expected = new IsotopeList(Isotope.C_12, Isotope.C_13, Isotope.C_12);
    IsotopeList actual = IsotopeList.fromString("C_12|C_13|C_12");
    assertEquals(expected, actual);
  }

  public void testToVerticalCountString() {
    IsotopeList list =
        new IsotopeList(Isotope.C_12, Isotope.C_12, Isotope.C_12, Isotope.N_15, Isotope.H_1);
    assertTrue(list.toVerticalCountString().contains("C_12: 3"));
    assertTrue(list.toVerticalCountString().contains("H_1: 1"));
    assertTrue(list.toVerticalCountString().contains("N_15: 1"));
    LOGGER.info("\n" + list.toVerticalCountString());
  }

  public void testToCommaSeparatedCountString() {
    IsotopeList list =
        new IsotopeList(Isotope.C_12, Isotope.C_13, Isotope.C_12, Isotope.N_15, Isotope.H_1);
    assertEquals("C_12: 2, C_13: 1, N_15: 1, H_1: 1", list.toCommaSeparatedCountString());
    LOGGER.info(list.toCommaSeparatedCountString());
  }

}
