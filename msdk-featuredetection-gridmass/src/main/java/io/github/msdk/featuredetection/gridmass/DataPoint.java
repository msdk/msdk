/*
 * Copyright 2006-2015 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MZmine 2; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.msdk.featuredetection.gridmass;

/**
 * This class represents one data point of a spectrum (m/z and intensity pair). Data point is
 * immutable once created, to make things simple.
 */
class DataPoint {

  private double mz, intensity;

  /**
   * Constructor which copies the data from another DataPoint
   *
   * @param dp a {@link io.github.msdk.featuredetection.gridmass.DataPoint} object.
   */
  public DataPoint(DataPoint dp) {
    this.mz = dp.getMZ();
    this.intensity = dp.getIntensity();
  }

  /**
   * <p>Constructor for DataPoint.</p>
   *
   * @param mz a double.
   * @param intensity a double.
   */
  public DataPoint(double mz, double intensity) {
    this.mz = mz;
    this.intensity = intensity;
  }

  /**
   * <p>Getter for the field <code>intensity</code>.</p>
   *
   * @return a double.
   */
  public double getIntensity() {
    return intensity;
  }

  /**
   * <p>getMZ.</p>
   *
   * @return a double.
   */
  public double getMZ() {
    return mz;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof DataPoint))
      return false;
    DataPoint dp = (DataPoint) obj;
    return (this.mz == dp.getMZ()) && (this.intensity == dp.getIntensity());
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return (int) (this.mz + this.intensity);
  }

}
