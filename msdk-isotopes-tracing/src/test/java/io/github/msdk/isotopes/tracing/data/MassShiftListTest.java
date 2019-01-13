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

import junit.framework.TestCase;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class MassShiftListTest extends TestCase {

  public void testConstructor() {
    MassShiftList list = new MassShiftList(new MassShift(0, 0, 0.0), new MassShift(0, 1, 1.0033),
        new MassShift(1, 2, 2.0), new MassShift(2, 3, 0.9996));
    assertEquals(4, list.size());
    assertEquals(0.0, list.get(0).getShiftValue());
    assertEquals(1.0033, list.get(1).getShiftValue());
    assertEquals(2.0, list.get(2).getShiftValue());
    assertEquals(0.9996, list.get(3).getShiftValue());
  }

  public void testToString() {
    MassShiftList massShiftList = new MassShiftList(new MassShift(0, 0, 0.0),
        new MassShift(0, 1, 1.0033), new MassShift(1, 2, 2.0), new MassShift(2, 3, 0.9996));
    String expectedString = "0.0[0-0]_1.0033[0-1]_2.0[1-2]_0.9996[2-3]";
    String actualString = massShiftList.toString();
    assertEquals(expectedString, actualString);
  }

  public void testFromString() {
    MassShiftList expected = new MassShiftList(new MassShift(0, 0, 0.0),
        new MassShift(0, 1, 1.0033), new MassShift(1, 2, 2.0), new MassShift(2, 3, 0.9996));
    MassShiftList actual = MassShiftList.fromString("0.0[0-0]_1.0033[0-1]_2.0[1-2]_0.9996[2-3]");
    assertEquals(expected, actual);
  }

  public void testEqualsUpToPermutationOfIsotopes() {
    MassShiftList first =
        MassShiftList.fromString("2.0067[0-1]_1.0033[1-2]_1.0034[2-4]_0.9996[4-5]");
    MassShiftList second =
        MassShiftList.fromString("2.0067[0-1]_1.0033[1-2]_0.9996[2-3]_1.0034[3-5]");
    assertTrue(first.equalsUpToPermutationOfIsotopes(second));

    first = MassShiftList.fromString("2.0[0-3]_0.9996[3-4]");
    second = MassShiftList.fromString("0.9996[0-1]_2.0[1-4]");
    assertTrue(first.equalsUpToPermutationOfIsotopes(second));

    first = MassShiftList.fromString("1.0033[0-1]_1.0034[1-3]_0.9996[3-5]");
    second = MassShiftList.fromString("1.0033[0-1]_0.9996[1-2]_1.0034[2-5]");
    assertTrue(first.equalsUpToPermutationOfIsotopes(second));

    first = MassShiftList.fromString("3.0038[0-1]_1.0033[1-3]_0.9996[3-4]");
    second = MassShiftList.fromString("3.0038[0-1]_0.9995[1-2]_1.0034[2-4]");
    assertTrue(first.equalsUpToPermutationOfIsotopes(second));

    first = MassShiftList.fromString("4.0071[0-1]_1.0033[1-2]_1.0034[2-4]_0.9996[4-5]");
    second = MassShiftList.fromString("4.0071[0-1]_1.0033[1-2]_0.9996[2-3]_1.0034[3-5]");
    assertTrue(first.equalsUpToPermutationOfIsotopes(second));


  }

}
