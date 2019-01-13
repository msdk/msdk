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

/**
 * The rate of incorporated molecules within a set of fragments used to simulate the corresponding
 * isotope pattern.
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class IncorporationRate {

  private double rateValue;

  /**
   * Generates an {@link IncorporationRate} with rateValue = 1.0
   */
  public IncorporationRate() {
    this.rateValue = 1.0;
  }

  /**
   * Generates an {@link IncorporationRate} with the specified value.
   * 
   * @param rateValue
   */
  public IncorporationRate(double rateValue) {
    if (rateValue < 0 || rateValue > 1) {
      throw new InputMismatchException(
          "Incorporation rate value " + rateValue + " is not in [0,1]");
    }
    this.rateValue = rateValue;
  }

  /**
   * 
   * @return the rate value in percent
   */
  public int perCernt() {
    return (int) (this.rateValue * 100);
  }

  public double getRateValue() {
    return rateValue;
  }

  public void setRateValue(double rateValue) {
    if (rateValue < 0 || rateValue > 1) {
      throw new InputMismatchException(
          "Incorporation rate value " + rateValue + " is not in [0,1]");
    }
    this.rateValue = rateValue;
  }

  @Override
  public String toString() {
    return String.valueOf(rateValue);
  }

}
