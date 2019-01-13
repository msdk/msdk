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

import java.util.HashMap;
import java.util.InputMismatchException;

import io.github.msdk.isotopes.tracing.data.constants.Element;
import io.github.msdk.isotopes.tracing.data.constants.ErrorMessage;
import io.github.msdk.isotopes.tracing.util.MathUtils;
import junit.framework.TestCase;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class FragmentTest extends TestCase {

  public void testFragment() {
    String formula = "C5N3H14";
    String capacityFormula = "C2N";
    Fragment fragment = new Fragment(formula, capacityFormula);
    HashMap<Element, Integer> expectedFormulaMap = new HashMap<>();
    expectedFormulaMap.put(Element.C, 5);
    expectedFormulaMap.put(Element.N, 3);
    expectedFormulaMap.put(Element.H, 14);
    HashMap<Element, Integer> expectedCapacity = new HashMap<>();
    expectedCapacity.put(Element.C, 2);
    expectedCapacity.put(Element.N, 1);
    assertEquals(expectedFormulaMap, fragment.getFormula());
    assertEquals(expectedCapacity, fragment.getTracerCapacity());
  }

  public void testFragmentFail() {
    String formula = "C5N3(H14)2";
    String capacityFormula = "C2N";
    try {
      new Fragment(formula, capacityFormula);
      fail("Should throw an exception because of the brackets in " + formula);
    } catch (Exception e) {
      assertTrue(e instanceof InputMismatchException);
      assertTrue(e.getMessage().contains(ErrorMessage.INVALID_FORMULA.getMessage()));
    }
  }

  public void testRelativeMass() {
    Fragment fragment = new Fragment("C5NH12", "CN");
    assertEquals(86.1565, fragment.relativeMass());
  }

  public void testLowestMass() {
    Fragment fragment = new Fragment("C5NH12", "CN");
    assertEquals(86.096974, fragment.lowestMass());
  }

  public void testHighestMass() {
    Fragment fragment = new Fragment("C5NH12", "CN");
    double roundedMass = (double) (MathUtils.round(fragment.highestMass(), 6));
    assertEquals(104.186108, roundedMass);
  }

  public void testlowestFullIncorporatedMass() {
    Fragment fragment = new Fragment("C5NH12", "CN");
    assertEquals(88.097364, MathUtils.round(fragment.lowestFullIncorporatedMass(), 6));
  }

  public void testChangeCapacity() {
    String formula = "C5N3H14";
    String capacityFormula = "C2N";
    Fragment fragment = new Fragment(formula, capacityFormula);
    HashMap<Element, Integer> expectedCapacity = new HashMap<>();
    expectedCapacity.put(Element.C, 2);
    expectedCapacity.put(Element.N, 1);
    assertEquals(expectedCapacity, fragment.getTracerCapacity());
    fragment.setCapacity("CH");
    expectedCapacity.clear();
    expectedCapacity.put(Element.C, 1);
    expectedCapacity.put(Element.H, 1);
    assertEquals(expectedCapacity, fragment.getTracerCapacity());
  }

}
