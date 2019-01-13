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

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.isotopes.tracing.data.IsotopeList;
import io.github.msdk.isotopes.tracing.data.MassSpectrum;
import io.github.msdk.isotopes.tracing.data.Partition;
import io.github.msdk.isotopes.tracing.data.PermutationSet;
import io.github.msdk.isotopes.tracing.util.MathUtils;


/**
 * An enumeration of chemical elements, given by their name, atomicNumber and relativeAtomicMass.
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public enum Element {
  // TODO: complete this list
  H("Hydrogen", 1, 1.0079), //
  B("Boron", 5, 10.811), //
  C("Carbon", 6, 12.011), //
  N("Nitrogen", 7, 14.0067), //
  O("Oxygen", 8, 15.9994), //
  F("Fluorine", 9, 18.9984), //
  Si("Silicon", 14, 28.0855), //
  Na("Sodium", 11, 22.9898), //
  P("Phosphorus", 15, 30.9738), //
  S("Sulfur", 16, 32.066), //
  Cl("Chlorine", 17, 35.4527), //
  K("Potassium", 19, 39.0983), //
  I("Iodine", 53, 126.9044), //
  UNDEFINED("Undefined", 0, 0), //
  NONE("None", 0, 0);

  private static final Logger LOGGER = LoggerFactory.getLogger(Element.class);
  private String name;
  private int atomicNumber;
  private double relativeAtomicMass;

  private Element(String name, int atomicNumber, double relativeAtomicMass) {
    this.name = name;
    this.atomicNumber = atomicNumber;
    this.relativeAtomicMass = relativeAtomicMass;
  }

  public String getName() {
    return name;
  }

  public int getAtomicNumber() {
    return atomicNumber;
  }

  public double getRelativeAtomicMass() {
    return relativeAtomicMass;
  }


  public IsotopeList getIsotopes() {
    IsotopeList isotopes = new IsotopeList();
    for (Isotope isotope : Isotope.values()) {
      if (isotope.getElement().equals(this)) {
        isotopes.add(isotope);
      }
    }
    return isotopes;
  }

  public double lowestMass() {
    return getIsotopes().get(0).getAtomicMass();
  }

  public double highestMass() {
    return heaviestIsotope().getAtomicMass();
  }

  public Isotope heaviestIsotope() {
    int index = getIsotopes().size() - 1;
    return getIsotopes().get(index);
  }

  /**
   * use a combinatorial approach to determine the masses of all isotopologues of a molecule E_n,
   * where E is some element and n a natural number. Principles a based on: Yergey, James A.: A
   * General Approach to Calculating Isotopic Distributions For Mass Spectrometry. In: International
   * Journal of Mass Spectrometry 52(1983), 2/3, S. 337-349. - ISSN 1387-3806
   * 
   * @param numberOfElements
   * @param treshFaktor factor to determine which low abundance masses shall be excluded.
   * @return mass spectrum/isotope pattern of E_n
   */
  public MassSpectrum multiElementSpectrum(int numberOfElements, Double treshFaktor) {
    MassSpectrum spectrum = new MassSpectrum(IntensityType.MID);
    IsotopeList isotopes = this.getIsotopes();
    ArrayList<PermutationSet> permutationSets =
        PermutationSet.allIsotopeCombinations(isotopes.size(), numberOfElements);
    ArrayList<Partition> allCombinations = new ArrayList<>();
    for (PermutationSet set : permutationSets) {
      allCombinations.addAll(set);
    }
    ArrayList<Double> massAbundancies =
        MathUtils.calculateAbundancies(allCombinations, isotopes, numberOfElements, treshFaktor);
    ArrayList<Double> masses = new ArrayList<>();
    for (Partition partition : allCombinations) {
      Double mass = 0.0;
      for (int index = 0; index < partition.size(); index++) {
        LOGGER.debug("summand", partition.get(index));
        Isotope isotope = isotopes.get(index);
        LOGGER.debug("isotope", isotope);
        LOGGER.debug("atomicMass", isotope.getAtomicMass());
        mass = mass + partition.get(index) * isotope.getAtomicMass();

      }
      masses.add(mass);
    }
    for (Double mass : masses) {
      Double abundance = massAbundancies.get(masses.indexOf(mass));
      if (spectrum.get(mass) != null) {
        spectrum.put(mass, spectrum.get(mass) + abundance);
      } else {
        spectrum.put(mass, abundance);
      }
    }
    return spectrum;
  }

  /**
   * 
   * @return The tracer that refers to this element.
   */
  public Isotope getTracer() {
    IsotopeList isotopes = this.getIsotopes();
    for (Isotope isotope : isotopes) {
      if (isotope.isTracer()) {
        return isotope;
      }
    }
    return Isotope.NONE;
  }

  /**
   * 
   * @return the most common isotope
   */
  public Isotope mostCommonIsotope() {
    IsotopeList isotopeList = this.getIsotopes();
    Isotope mostCommonIsotope = isotopeList.get(0);
    for (Isotope isotope : isotopeList) {
      if (isotope.getAbundance() > mostCommonIsotope.getAbundance()) {
        mostCommonIsotope = isotope;
      }
    }
    return mostCommonIsotope;
  }

  public Isotope lightestIsotope() {
    return this.getIsotopes().get(0);
  }
}
