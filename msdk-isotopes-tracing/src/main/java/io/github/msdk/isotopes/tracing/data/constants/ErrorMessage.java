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

/**
 * An enumeration of error messages, used for the tracer module.
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public enum ErrorMessage {
  INVALID_FORMULA("Cannot read formula. Please enter a valid format.",
      "A valid format does not contain any brackets."), //
  INVALID_MASS_SHIFT_PATTERN("Cannot read MassShift from string.",
      "The input string does not match the ecpected pattern."), //
  INTENSITY_TYPE_MISMATCH("Can only merge maps with IntensityType.ABSOLUTE", " "), //
  INVALID_ISOTOPE_NAME("No such isotope.", ""), //
  NO_TRACER("There is no tracer defined for this element.", "");

  private String message;
  private String detail;

  private ErrorMessage(String message, String detail) {
    this.message = message;
    this.detail = detail;
  }

  public String getMessage() {
    return message;
  }

  public String getDetail() {
    return detail;
  }
}
