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
import java.util.InputMismatchException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.msdk.isotopes.tracing.data.constants.Element;
import io.github.msdk.isotopes.tracing.data.constants.ErrorMessage;

/**
 * A map of each element in a formula to its count.
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
@SuppressWarnings("serial")
public class ElementFormula extends LinkedHashMap<Element, Integer> {

  public static final String FORMULA_REG_EX = "([A-Z][a-z]{0,1})([0-9]{0,3})";

  public List<Entry<Element, Integer>> toEntryList() {
    List<Entry<Element, Integer>> entryList = new ArrayList<>(this.entrySet());
    return entryList;
  }

  public static ElementFormula fromString(String formula) {
    if (formula.contains("(")) {
      throw new InputMismatchException(
          ErrorMessage.INVALID_FORMULA.getMessage() + "[" + formula + "]");
    }
    if (formula.equals("NA")) {
      return new ElementFormula();
    }
    ElementFormula elements = new ElementFormula();
    Matcher formulaMatcher = Pattern.compile(FORMULA_REG_EX).matcher(formula);
    ArrayList<String> elementTokens = new ArrayList<String>();
    while (formulaMatcher.find()) {
      elementTokens.add(formulaMatcher.group());
    }
    for (String elementToken : elementTokens) {
      Matcher elementMatcher = Pattern.compile(FORMULA_REG_EX).matcher(elementToken);
      if (elementMatcher.matches()) {
        Element element = Element.valueOf(elementMatcher.group(1));
        Integer quantity = elementMatcher.group(2).equals("") ? Integer.valueOf(1)
            : Integer.valueOf(elementMatcher.group(2));
        elements.put(element, quantity);
      }
    }
    return elements;
  }

  public String toSimpleString() {
    StringBuilder builder = new StringBuilder();
    for (Entry<Element, Integer> entry : this.entrySet()) {
      builder.append(entry.getKey().name() + entry.getValue());
    }
    return builder.toString();
  }

  public ElementFormula copy() {
    ElementFormula copy = new ElementFormula();
    for (Entry<Element, Integer> entry : this.entrySet()) {
      copy.put(entry.getKey(), entry.getValue());
    }
    return copy;
  }

  public IsotopeFormula toIsotopeFormula() {
    IsotopeFormula formula = new IsotopeFormula();
    for (Entry<Element, Integer> entry : this.entrySet()) {
      formula.put(entry.getKey().lightestIsotope(), entry.getValue());
    }
    return formula;
  }

}
