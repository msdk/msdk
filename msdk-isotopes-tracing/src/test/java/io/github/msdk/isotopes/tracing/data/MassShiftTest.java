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

import java.util.InputMismatchException;

import junit.framework.TestCase;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class MassShiftTest extends TestCase {

  public void testToString() {
    MassShift massShift = new MassShift(1, 2, 1.0033);
    assertEquals("1.0033[1-2]", massShift.toString());
  }

  public void testFromString() {
    MassShift massShift = MassShift.fromString("1.0033[1-2]");
    MassShift expectedShift = new MassShift(1, 2, 1.0033);
    assertEquals(expectedShift, massShift);
  }

  public void testFailFromString() {
    try {
      MassShift.fromString("wrong pattern");
      fail("This should throw an InputMismatchException");
    } catch (InputMismatchException e) {
      // this exception was expected
    }
  }

}
