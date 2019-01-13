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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.msdk.isotopes.tracing.data.constants.Isotope;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
@SuppressWarnings("serial")
public class IsotopeListList extends ArrayList<IsotopeList> {

  public IsotopeListList() {}

  public IsotopeListList(IsotopeList... isotopeLists) {
    for (int i = 0; i < isotopeLists.length; i++) {
      this.add(isotopeLists[i]);
    }
  }

  /**
   * Converts as list containing the list C_13, C_13, N_15, H_1 and H_2, C12 to the following string
   * : [C_13|C_13|N_15|H_1][H_2|C12]
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (IsotopeList isotopeList : this) {
      builder.append("[" + isotopeList.toString() + "]");
    }
    return builder.toString();
  }

  private static final String LIST_STRING_REGEX = "(\\[[^\\[]*\\])";

  /**
   * Creates an IsotopeListList from a string like [C_13|C_13|N_15|H_1][H_2|C12], where the isotopes
   * in brackets determine the {@link IsotopeList} entries.
   * 
   * @param isotopesListString, e.g [C_13|C_13|N_15|H_1][H_2|C12]
   * @return An IsotopeListList corresponding to the string representation
   */
  public static IsotopeListList fromString(String isotopesListString) {
    IsotopeListList isotopeListList = new IsotopeListList();
    Matcher matcher = Pattern.compile(LIST_STRING_REGEX).matcher(isotopesListString);
    ArrayList<String> entryStrings = new ArrayList<String>();
    while (matcher.find()) {
      entryStrings.add(matcher.group());
    }
    for (String entryString : entryStrings) {
      Matcher elementMatcher = Pattern.compile(LIST_STRING_REGEX).matcher(entryString);
      if (elementMatcher.matches()) {
        String isotopeListString = elementMatcher.group(1).replace("[", "").replaceAll("]", "");
        IsotopeList isotopeList = IsotopeList.fromString(isotopeListString);
        isotopeListList.add(isotopeList);
      }
    }
    return isotopeListList;
  }

  /**
   * 
   * @return a map of the isotopes in this list to their abundance in this list
   */
  public IsotopeFormula toIsotopeFormula() {
    IsotopeFormula isotopeCount = new IsotopeFormula();
    for (IsotopeList isotopeList : this) {
      for (Isotope isotope : isotopeList) {
        if (isotopeCount.get(isotope) == null) {
          isotopeCount.put(isotope, 1);
        } else {
          isotopeCount.put(isotope, isotopeCount.get(isotope) + 1);
        }
      }
    }
    return isotopeCount;
  }

  /**
   * 
   * @return a comma separated representation of isotope counts in this list. E.g. C_13: 2, H_2: 3,
   *         N_15: 3, O_17: 1
   */
  public String toCommaSeparatedCountString() {
    IsotopeFormula isotopeFormula = this.toIsotopeFormula();
    String countString = isotopeFormula.toString().replaceAll("=", ": ");
    countString = countString.replace("{", "").replace("}", "");
    return countString;
  }

  public String toNiceFormattedFormula() {
    return this.toIsotopeFormula().toNiceFormattedFormula();
  }

  /**
   * 
   * @param isotopes isotopes to count
   * @return an {@link IsotopeFormula} that maps each isotope in isotopes to its abundance in this
   *         {@link IsotopeListList}
   */
  public IsotopeFormula countIsotopes(IsotopeList isotopes) {
    IsotopeFormula allIsotopes = this.toIsotopeFormula();
    IsotopeFormula countedIsotopes = new IsotopeFormula();
    for (Isotope isotope : isotopes) {
      if (allIsotopes.get(isotope) != null) {
        countedIsotopes.put(isotope, allIsotopes.get(isotope));
      } else {
        countedIsotopes.put(isotope, 0);
      }
    }
    return countedIsotopes;
  }

  /**
   * Converts as list containing the list C_13, C_13, N_15, H_1 and H_2, C12 to the following string
   * : [C_13|C_13|N_15|H_1][H_2|C12] (12C)(13C)2(15N)(1H)(2H)
   */
  public String toSimpleString() {
    return this.toIsotopeFormula().toSimpleString();
  }

}
