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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.isotopes.tracing.data.Fragment;
import io.github.msdk.isotopes.tracing.data.IsotopeList;
import io.github.msdk.isotopes.tracing.data.MassSpectrum;
import junit.framework.TestCase;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class ElementTest extends TestCase {

  private static final Logger LOGGER = LoggerFactory.getLogger(ElementTest.class);

  public void testGetIsotopes() {
    IsotopeList expectedIsotopes = new IsotopeList();
    expectedIsotopes.add(Isotope.C_12);
    expectedIsotopes.add(Isotope.C_13);
    testIsotopes(Element.C, expectedIsotopes);

    expectedIsotopes.clear();
    expectedIsotopes.add(Isotope.H_1);
    expectedIsotopes.add(Isotope.H_2);
    testIsotopes(Element.H, expectedIsotopes);

    expectedIsotopes.clear();
    expectedIsotopes.add(Isotope.N_14);
    expectedIsotopes.add(Isotope.N_15);
    testIsotopes(Element.N, expectedIsotopes);

    expectedIsotopes.clear();
    expectedIsotopes.add(Isotope.O_16);
    expectedIsotopes.add(Isotope.O_17);
    expectedIsotopes.add(Isotope.O_18);
    testIsotopes(Element.O, expectedIsotopes);

    expectedIsotopes.clear();
    expectedIsotopes.add(Isotope.Si_28);
    expectedIsotopes.add(Isotope.Si_29);
    expectedIsotopes.add(Isotope.Si_30);
    testIsotopes(Element.Si, expectedIsotopes);

    expectedIsotopes.clear();
    expectedIsotopes.add(Isotope.B_10);
    expectedIsotopes.add(Isotope.B_11);
    testIsotopes(Element.B, expectedIsotopes);

    expectedIsotopes.clear();
    expectedIsotopes.add(Isotope.F_19);
    testIsotopes(Element.F, expectedIsotopes);

    expectedIsotopes.clear();
    expectedIsotopes.add(Isotope.Na_23);
    testIsotopes(Element.Na, expectedIsotopes);

    expectedIsotopes.clear();
    expectedIsotopes.add(Isotope.P_31);
    testIsotopes(Element.P, expectedIsotopes);

    expectedIsotopes.clear();
    expectedIsotopes.add(Isotope.S_32);
    expectedIsotopes.add(Isotope.S_33);
    expectedIsotopes.add(Isotope.S_34);
    testIsotopes(Element.S, expectedIsotopes);

    expectedIsotopes.clear();
    expectedIsotopes.add(Isotope.Cl_35);
    expectedIsotopes.add(Isotope.Cl_37);
    testIsotopes(Element.Cl, expectedIsotopes);

    expectedIsotopes.clear();
    expectedIsotopes.add(Isotope.K_39);
    expectedIsotopes.add(Isotope.K_40);
    expectedIsotopes.add(Isotope.K_41);
    testIsotopes(Element.K, expectedIsotopes);

    expectedIsotopes.clear();
    expectedIsotopes.add(Isotope.I_127);
    testIsotopes(Element.I, expectedIsotopes);

  }

  public void testIsotopes(Element element, IsotopeList expedtedIsotopes) {
    IsotopeList isotopes = element.getIsotopes();
    assertEquals(expedtedIsotopes, isotopes);
  }

  public void testLowestMass() {
    Element c = Element.C;
    assertEquals(12.000000, c.lowestMass());

    Element h = Element.H;
    assertEquals(1.007825, h.lowestMass());

    Element n = Element.N;
    assertEquals(14.003074, n.lowestMass());

    Element o = Element.O;
    assertEquals(15.994915, o.lowestMass());

    Element si = Element.Si;
    assertEquals(27.976928, si.lowestMass());

    Element p = Element.P;
    assertEquals(30.973763, p.lowestMass());
  }

  public void testHighestMass() {
    Element c = Element.C;
    assertEquals(13.003355, c.highestMass());

    Element h = Element.H;
    assertEquals(2.014102, h.highestMass());

    Element n = Element.N;
    assertEquals(15.000109, n.highestMass());

    Element o = Element.O;
    assertEquals(17.999159, o.highestMass());

    Element si = Element.Si;
    assertEquals(29.976928, si.highestMass());

    Element p = Element.P;
    assertEquals(30.973763, p.highestMass());
  }

  public void testHeaviestIsotope() {
    Element c = Element.C;
    assertEquals(Isotope.C_13, c.heaviestIsotope());

    Element h = Element.H;
    assertEquals(Isotope.H_2, h.heaviestIsotope());

    Element n = Element.N;
    assertEquals(Isotope.N_15, n.heaviestIsotope());

    Element o = Element.O;
    assertEquals(Isotope.O_18, o.heaviestIsotope());

    Element si = Element.Si;
    assertEquals(Isotope.Si_30, si.heaviestIsotope());

    Element p = Element.P;
    assertEquals(Isotope.P_31, p.heaviestIsotope());
  }

  public void testMultiElementSpectrum() {
    for (Element element : Element.values()) {
      if (element.equals(Element.UNDEFINED) || element.equals(Element.NONE)
          || element.getIsotopes().size() != 2) {
        continue;
      }
      Fragment fragment = new Fragment(element.name() + "2", "");
      LOGGER.info("Checking fragment" + fragment.getFormula());
      MassSpectrum combinatoricallyCalculatedSpectrum = element.multiElementSpectrum(2, 0.0);
      combinatoricallyCalculatedSpectrum = combinatoricallyCalculatedSpectrum.roundMasses(4)
          .roundFrequencies(4).sortAscendingByMass();
      LOGGER.info("combinatoricallyCalculatedSpectrum\n" + combinatoricallyCalculatedSpectrum);
      MassSpectrum combinatoricallyExpectedSpectrum = new MassSpectrum(IntensityType.MID);
      IsotopeList isotopes = element.getIsotopes();
      Double mass0 = 2 * isotopes.get(0).getAtomicMass();
      Double abundance0 = isotopes.get(0).getAbundance() * isotopes.get(0).getAbundance();
      combinatoricallyExpectedSpectrum.put(mass0, abundance0);
      Double mass1 = isotopes.get(0).getAtomicMass() + isotopes.get(1).getAtomicMass();
      Double abundance1 = 2 * isotopes.get(0).getAbundance() * isotopes.get(1).getAbundance();
      combinatoricallyExpectedSpectrum.put(mass1, abundance1);
      Double mass2 = 2 * isotopes.get(1).getAtomicMass();
      Double abundance2 = isotopes.get(1).getAbundance() * isotopes.get(1).getAbundance();
      combinatoricallyExpectedSpectrum.put(mass2, abundance2);
      combinatoricallyExpectedSpectrum =
          combinatoricallyExpectedSpectrum.roundMasses(4).roundFrequencies(4).sortAscendingByMass();
      LOGGER.info("combinatoricallyExpectedSpectrum\n" + combinatoricallyExpectedSpectrum);

    }
  }

}
