/*
 * (C) Copyright 2015-2018 by MSDK Development Team
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

package io.github.msdk.io.mgf;

import io.github.msdk.datamodel.AbstractMsSpectrum;
import io.github.msdk.datamodel.MsSpectrumType;

/**
 * <p>MgfMsSpectrum class.</p>
 *
 *         This class extends AbstractMsSpectrum and stores additional fields
 *         title - name of the spectrum
 *         precursor ion charge and mass
 */
public class MgfMsSpectrum extends AbstractMsSpectrum {
  private String title;
  private int precursorCharge;
  private double precursorMass;

  /**
   *  <p> Constructor for MgfMsSpectrum class.</p>
   * @param mz - array of mz values (length of `size`)
   * @param intensity - array of intensity values (length of `size`)
   * @param size - size of the arrays (specifies amount of data points)
   * @param title - string title of the MsSpectrum
   * @param precursorCharge - charge of the precursor ion
   * @param precursorMass - mass of the precursor ion
   * @param type - type of the MsSpectrum
   */
  public MgfMsSpectrum(double[] mz, float[] intensity, int size, String title, int precursorCharge,
      double precursorMass, MsSpectrumType type) {
    setSpectrumType(type);
    setDataPoints(mz, intensity, size);
    this.precursorMass = precursorMass;
    this.precursorCharge = precursorCharge;
    this.title = title;
  }

  /**
   * <p> Getter of the precursor ion mass </p>
   * @return the mass of the precursor ion
   */
  public double getPrecursorMass() {
    return precursorMass;
  }

  /**
   * <p> Getter of the precursor ion charge </p>
   * @return charge of the precursor ion
   */
  public int getPrecursorCharge() {
    return precursorCharge;
  }

  /**
   * <p> Getter of the MgfMsSpectrum title </p>
   * @return the title of the MgfMsSpectrum
   */
  public String getTitle() {
    return title;
  }
}
