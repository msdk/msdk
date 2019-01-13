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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.msdk.isotopes.tracing.data.constants.ErrorMessage;

/**
 * Let P represent an isotope pattern consisting of mass peaks p_0,... ,p_n corresponding to masses
 * m_0,... ,m_n (in ascending order), then a MassShift from peak p_i to p_k for i < k is
 * characterized by these peaks and the mass difference m_k - m_i. So we will set: peak1 := p_i
 * peak_2 := p_k shiftValue := m_k - m_i
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class MassShift {
  private int peak1;
  private int peak2;
  private Double shiftValue;

  public MassShift(int peak1, int peak2, Double shiftValue) {
    super();
    this.peak1 = peak1;
    this.peak2 = peak2;
    this.shiftValue = shiftValue;
  }

  public int getPeak1() {
    return peak1;
  }

  public void setPeak1(int peak1) {
    this.peak1 = peak1;
  }

  public int getPeak2() {
    return peak2;
  }

  public void setPeak2(int peak2) {
    this.peak2 = peak2;
  }

  public Double getShiftValue() {
    return shiftValue;
  }

  public void setShiftValue(Double shiftValue) {
    this.shiftValue = shiftValue;
  }

  /**
   * @return a string representation of this shift using the format: value[peak1-peak2]
   */
  @Override
  public String toString() {
    return shiftValue + "[" + peak1 + "-" + peak2 + "]";
  }

  private static final String MASS_SHIFT_PATTERN = "([0-9\\.]*)\\[([0-9]*)\\-([0-9]*)\\]";

  /**
   * Creates a MassShift object from a string, matching the following format: value[peak1-peak2],
   * e.g 1.0033[1-2]
   * 
   * @param massShiftString, expected format: value[peak1-peak2], e.g 1.0033[1-2]
   * @return a MassShift that corresponds to the input string
   */
  public static MassShift fromString(String massShiftString) {
    massShiftString = massShiftString.replaceAll(" ", "");
    Matcher massShiftMatcher = Pattern.compile(MASS_SHIFT_PATTERN).matcher(massShiftString);
    if (massShiftMatcher.matches()) {
      String shiftValue = massShiftMatcher.group(1);
      String peak1 = massShiftMatcher.group(2);
      String peak2 = massShiftMatcher.group(3);
      return new MassShift(Integer.valueOf(peak1), Integer.valueOf(peak2),
          Double.valueOf(shiftValue));
    } else {
      throw new InputMismatchException(
          ErrorMessage.INVALID_MASS_SHIFT_PATTERN.getMessage() + "[" + massShiftString + "]");
    }
  }

  /**
   * This shift is said to follow an other shift if this shift starts in the end peak of the other
   * shift.
   * 
   * @param otherShift
   * @return true if this shift follows the other shift, otherwise false.
   */
  public boolean follows(MassShift otherShift) {
    int startOfThisShift = this.peak1;
    int endOfOtherShift = otherShift.peak2;
    if (endOfOtherShift == startOfThisShift) {
      return true;
    }
    return false;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (!(other instanceof MassShift)) {
      return false;
    }
    if (this.peak1 != ((MassShift) other).peak1) {
      return false;
    }
    if (this.peak2 != ((MassShift) other).peak2) {
      return false;
    }
    if (this.shiftValue.compareTo(((MassShift) other).shiftValue) != 0) {
      return false;
    }
    return true;

  }

}
