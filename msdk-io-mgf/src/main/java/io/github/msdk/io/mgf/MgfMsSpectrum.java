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

import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.AbstractMsSpectrum;
import io.github.msdk.datamodel.MsSpectrumType;
import javax.annotation.Nullable;

/**
 * <p>MgfMsSpectrum class.</p>
 *
 *         This class extends AbstractMsSpectrum and stores additional fields
 *         title - name of the spectrum
 *         precursor ion charge and mass
 *         level - MsLevel of the spectrum (1 or 2)
 */
public class MgfMsSpectrum extends AbstractMsSpectrum {
  private String title;
  private Integer precursorCharge;
  private Double precursorMass;
  private Integer level;

  /**
   *  <p> Constructor for MgfMsSpectrum class.</p>
   * @param mz - array of mz values (length of `size`)
   * @param intensity - array of intensity values (length of `size`)
   * @param size - size of the arrays (specifies amount of data points)
   * @param type - type of the MsSpectrum
   */
  public MgfMsSpectrum(double[] mz, float[] intensity, int size, MsSpectrumType type) {
    setSpectrumType(type);
    setDataPoints(mz, intensity, size);
  }

  /**
   * <p>Setter for spectrum title</p>
   * @param title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * <p> Method configures params of Precursor Ion </p>
   * @param mass
   * @param charge
   */
  public void setPrecursor(Double mass, Integer charge) {
    precursorCharge = charge;
    precursorMass = mass;
  }

  /**
   * <p> Getter of the precursor ion mass </p>
   * @return the mass of the precursor ion
   */
  public double getPrecursorMass() {
    return precursorMass;
  }

  /**
   * <p> Setter for MS level of spectrum </p>
   * @param level
   * @throws MSDKRuntimeException
   */
  public void setMsLevel(Integer level) throws MSDKRuntimeException {
    if (level != null && level <= 0)
      throw new MSDKRuntimeException("Wrong MS level");
    this.level = level;
  }

  /**
   * <p> Getter of the precursor ion charge </p>
   * @return charge of the precursor ion, null if not specified
   */
  public @Nullable Integer getPrecursorCharge() {
    return precursorCharge;
  }

  /**
   * <p> Getter of the MgfMsSpectrum title </p>
   * @return the title of the MgfMsSpectrum, null if not specified
   */
  public @Nullable String getTitle() {
    return title;
  }

  /**
   * <p> Getter for MS level of spectrum </p>
   *
   * @return spectrum's ms level, null if not specified
   */
  public @Nullable Integer getMsLevel() {
    return level;
  }
}
