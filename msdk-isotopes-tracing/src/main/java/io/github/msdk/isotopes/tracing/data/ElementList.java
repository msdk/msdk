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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.msdk.isotopes.tracing.data.constants.Element;
import io.github.msdk.isotopes.tracing.data.constants.ErrorMessage;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
@SuppressWarnings("serial")
public class ElementList extends ArrayList<Element> {

  public ElementList() {

  }

  public ElementList(Element... elements) {
    for (Element element : elements) {
      this.add(element);
    }
  }

  /**
   * 
   * @param formula, i.e C5H14NSi
   * @return A list of elements, corresponding to the formula (i.e. C, H, N, Si).
   */
  public static ElementList fromFormula(String formula) {
    ElementList elements = new ElementList();
    if (formula.contains("(")) {
      throw new InputMismatchException(
          ErrorMessage.INVALID_FORMULA.getMessage() + "[" + formula + "]");
    }
    Matcher formulaMatcher = Pattern.compile(ElementFormula.FORMULA_REG_EX).matcher(formula);
    ArrayList<String> elementTokens = new ArrayList<String>();
    while (formulaMatcher.find()) {
      elementTokens.add(formulaMatcher.group());
    }
    for (String elementToken : elementTokens) {
      Matcher elementMatcher = Pattern.compile(ElementFormula.FORMULA_REG_EX).matcher(elementToken);
      if (elementMatcher.matches()) {
        Element element = Element.valueOf(elementMatcher.group(1));
        elements.add(element);
      }
    }
    return elements;
  }

}
