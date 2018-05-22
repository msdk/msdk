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

public class MgfMsSpectrum extends AbstractMsSpectrum {

  private String title;
  private int precursorCharge;
  private double precursorMass;

  public MgfMsSpectrum(double[] mz, float[] intensity, int size, String title, int precursorCharge,
      double precursorMass, MsSpectrumType type) {
    setSpectrumType(type);
    setDataPoints(mz, intensity, size);
    this.precursorMass = precursorMass;
    this.precursorCharge = precursorCharge;
    this.title = title;
  }

  public double getPrecursorMass() {
    return precursorMass;
  }

  public int getPrecursorCharge() {
    return precursorCharge;
  }

  public String getTitle() {
    return title;
  }
}
