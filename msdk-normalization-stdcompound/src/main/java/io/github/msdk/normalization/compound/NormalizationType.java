/*
 * Copyright 2006-2016 The MZmine 3 Development Team
 * 
 * This file is part of MZmine 3.
 * 
 * MZmine 3 is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * MZmine 3 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MZmine 3; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.msdk.normalization.compound;

/**
 * <p>
 * NormalizationType class.
 * </p>
 */
public enum NormalizationType {

  NEAREST_STANDARD("Nearest standard"), WEIGHTED_CONTRIBUTION(
      "Weighted contribution of all standards");

  private final String stringValue;

  NormalizationType(String stringValue) {
    this.stringValue = stringValue;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return stringValue;
  }

}
