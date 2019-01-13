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

import java.util.ArrayList;

/**
 * All the headers and the corresponding columnIndex in a simulated MassSpectra csv file that
 * includes the analyzed mass shifts.
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public enum MSShiftDatabaseColKey {
  INC_RATE("IncRate", 0), //
  NATURAL_MASS("NaturalMass", 1), //
  NATURAL_FREQUENCY("NaturalFrequency", 2), //
  NATURAL_SHIFT_VALUES("NaturalShiftValues", 3), //
  NATURAL_SHIFT_ISOTOPES("NaturalShiftIsotopes", 4), //
  MARKED_MASS("MarkedMass", 5), //
  MARKED_FREQUENCY("MarkedFrequency", 6), //
  MARKED_SHIFT_VALUES("MarkedShiftValues", 7), //
  MARKED_SHIFT_ISOTOPES("MarkedShiftIsotopes", 8), //
  MIXED_MASS("MixedMass", 9), //
  MIXED_FREQUENCY("MixedFrequency", 10), //
  MIXED_SHIFT_VALUES("MixedShiftValues", 11), //
  MIXED_SHIFT_ISOTOPES("MixedShiftIsotopes", 12), //
  COMPOUND_FORMULA("CompoundFormula", 13), //
  INCORPORATED_TRACERS("IncorporatedTracers", 14);

  private String header;
  private int columnIndex;

  private MSShiftDatabaseColKey(String header, int columnIndex) {
    this.header = header;
    this.columnIndex = columnIndex;
  }

  public String getHeader() {
    return header;
  }

  public int getColumnIndex() {
    return columnIndex;
  }

  /**
   * 
   * @return all the headers as ArrayList
   */
  public static ArrayList<String> toHeaderList() {
    MSShiftDatabaseColKey[] keys = values();
    ArrayList<String> headers = new ArrayList<>();
    for (int index = 0; index < keys.length; index++) {
      headers.add(keys[index].header);
    }
    return headers;
  }
}
