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

import io.github.msdk.isotopes.tracing.data.ElementList;
import io.github.msdk.isotopes.tracing.data.IsotopeList;
import io.github.msdk.isotopes.tracing.util.StringUtils;

/**
 * An enumeration of isotopes, given by the corresponding element, their atomicMass and natural
 * abundance. The information if they are typically used as tracers is included as well as the mass
 * shift that will be induced if this isotope is incorporated in a fragment.
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public enum Isotope implements Comparable<Isotope> {

  H_1(Element.H, 1.007825, 0.99985, false, 0.0, 1), //
  H_2(Element.H, 2.014102, 0.00015, true, 1.006277, 2), //
  B_10(Element.B, 10.012938, 0.19900, false, 0.0, 10), //
  B_11(Element.B, 11.009305, 0.80100, false, 0.996367, 11), //
  C_12(Element.C, 12.000000, 0.98900, false, 0.0, 12), //
  C_13(Element.C, 13.003355, 0.01100, true, 1.003355, 13), //
  N_14(Element.N, 14.003074, 0.99634, false, 0.0, 14), //
  N_15(Element.N, 15.000109, 0.00366, true, 0.997035, 15), //
  O_16(Element.O, 15.994915, 0.99762, false, 0.0, 16), //
  O_17(Element.O, 16.999131, 0.00038, false, 1.004216, 17), //
  O_18(Element.O, 17.999159, 0.00200, false, 2.004244, 18), //
  F_19(Element.F, 18.998403, 1.00000, false, 0.0, 19), //
  Na_23(Element.Na, 22.989770, 1.00000, false, 0.0, 23), //
  Si_28(Element.Si, 27.976928, 0.92230, false, 0.0, 28), //
  Si_29(Element.Si, 28.976496, 0.04670, false, 0.999568, 29), //
  Si_30(Element.Si, 29.976928, 0.03100, false, 2.0, 30), //
  P_31(Element.P, 30.973763, 1.00000, false, 0.0, 31), //
  S_32(Element.S, 31.972072, 0.95020, false, 0.0, 32), //
  S_33(Element.S, 32.971459, 0.00750, false, 0.999387, 33), //
  S_34(Element.S, 33.967868, 0.04210, false, 1.995796, 34), //
  Cl_35(Element.Cl, 34.968853, 0.75770, false, 0.0, 35), //
  Cl_37(Element.Cl, 36.965903, 0.24230, false, 1.99705, 37), //
  K_39(Element.K, 38.963708, 0.932581, false, 0.0, 39), //
  K_40(Element.K, 39.963999, 0.000117, false, 1.000291, 40), //
  K_41(Element.K, 40.961825, 0.067302, false, 1.998117, 41), //
  I_127(Element.I, 126.904477, 1.00000, false, 0.0, 127), //
  UNDEFINED(Element.UNDEFINED, 0.0, 0.0, false, 0.0, 0), //
  NONE(Element.NONE, 0.0, 0.0, false, 0.0, 0);

  private Element element;
  private Double atomicMass;
  private Double abundance;
  private boolean isTracer;
  private Double massShiftValue;
  private Integer massNumber;

  private Isotope(Element element, Double atomicMass, Double abundance, boolean isTracer,
      Double massShift, Integer massNumber) {
    this.abundance = abundance;
    this.atomicMass = atomicMass;
    this.element = element;
    this.isTracer = isTracer;
    this.massShiftValue = massShift;
    this.massNumber = massNumber;
  }

  public Element getElement() {
    return element;
  }

  public Double getAtomicMass() {
    return atomicMass;
  }

  public Double getAbundance() {
    return abundance;
  }

  /**
   * An element is an tracer if it is used as tracer in isotope labeling experiments.
   * 
   * @return
   */
  public boolean isTracer() {
    return isTracer;
  }

  /**
   * 
   * @return The mass shift value, induced by this isotope with respect to the m0 peak.
   */
  public Double getMassShiftValue() {
    return massShiftValue;
  }

  /**
   * 
   * @param massShiftValue
   * @return The Isotope that may induce exactly the given massShiftValue. A massShiftValue == 0.0
   *         will be induced by Isotope.NONE. A massShiftValue that cannot be evaluated will return
   *         Isotope.UNDEFINED
   */
  public Isotope byMassShiftValue(Double massShiftValue) {
    if (massShiftValue.compareTo(0.0) == 0) {
      return Isotope.NONE;
    }
    for (Isotope isotope : values()) {
      if (massShiftValue.compareTo(isotope.getMassShiftValue()) == 0) {
        return isotope;
      }
    }
    return Isotope.UNDEFINED;
  }

  public Integer getMassNumber() {
    return massNumber;
  }

  /**
   * 
   * @param massShiftValue
   * @return The Isotope that may induce approximately the given massShiftValue (an error lower than
   *         0.0001 will be assumed to be okay). A massShiftValue == 0.0 will be induced by
   *         Isotope.NONE. A massShiftValue that cannot be evaluated will return Isotope.UNDEFINED
   */
  public static IsotopeList approximatelyByMassShiftValue(Double massShiftValue) {
    if (massShiftValue.compareTo(0.0) == 0) {
      return new IsotopeList(Isotope.NONE);
    }
    for (Isotope isotope : values()) {
      if (Math.abs(massShiftValue - isotope.getMassShiftValue()) < 0.0001) {
        return new IsotopeList(isotope);
      }
    }
    return new IsotopeList(Isotope.UNDEFINED);
  }

  /**
   * 
   * @param massShiftValue
   * @return The Isotope that may induce approximately the given massShiftValue (an error lower than
   *         0.0001 will be assumed to be okay). A massShiftValue == 0.0 will be induced by
   *         Isotope.NONE. A massShiftValue that cannot be evaluated will return Isotope.UNDEFINED
   */
  public static IsotopeList approximatelyByMassShiftValueAndAvailableElements(Double massShiftValue,
      ElementList elements) {
    if (massShiftValue.compareTo(0.0) == 0) {
      return new IsotopeList(Isotope.NONE);
    }
    for (Isotope isotope : values()) {
      if (Math.abs(massShiftValue - isotope.getMassShiftValue()) < 0.0001
          && elements.contains(isotope.getElement())) {
        return new IsotopeList(isotope);
      }
    }
    return new IsotopeList(Isotope.UNDEFINED);
  }

  /**
   * 
   * @param name, e.g. C_13 or N_15
   * @return the isotope with the given name
   */
  public static Isotope byName(String name) {
    for (Isotope isotope : values()) {
      if (isotope.name().equals(name)) {
        return isotope;
      }
    }
    throw new InputMismatchException(
        ErrorMessage.INVALID_ISOTOPE_NAME.getMessage() + "[" + name + "]");
  }

  /**
   * 
   * @return the most common isotope for the element corresponding to this isotope
   */
  public Isotope mostCommonIsotope() {
    IsotopeList allCorrespondingIsotopes = new IsotopeList();
    for (Isotope isotope : values()) {
      if (isotope.getElement().equals(this.getElement())) {
        allCorrespondingIsotopes.add(isotope);
      }
    }
    Isotope mostCommonIsotope = allCorrespondingIsotopes.get(0);
    for (Isotope isotope : allCorrespondingIsotopes) {
      if (isotope.getAbundance() > mostCommonIsotope.getAbundance()) {
        mostCommonIsotope = isotope;
      }
    }
    return mostCommonIsotope;
  }

  /**
   * Creates a string representation with mass number as leading superscript.
   * 
   * @return
   */
  public String toNiceFormattedString() {
    String superscript =
        getMassNumber() == 0 ? "" : StringUtils.superscript(String.valueOf(getMassNumber()));
    return superscript + getElement().toString();
  }

  /**
   * 
   * @return i.e. (13C)
   */
  public String toSimpleString() {
    return "(" + getMassNumber() + getElement().toString() + ")";
  }

}
