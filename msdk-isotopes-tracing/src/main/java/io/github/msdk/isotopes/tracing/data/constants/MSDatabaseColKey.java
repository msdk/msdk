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
 * All the headers in a simulated MassSpectra csv file with the corresponding columns
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public enum MSDatabaseColKey {
  INC_RATE("IncRate", 0), //
  NATURAL_MASS("NaturalMass", 1), //
  NATURAL_FREQUENCY("NaturalFrequency", 2), //
  MARKED_MASS("MarkedMass", 3), //
  MARKED_FREQUENCY("MarkedFrequency", 4), //
  MIXED_MASS("MixedMass", 5), //
  MIXED_FREQUENCY("MixedFrequency", 6), //
  COMPOUND_FORMULA("CompoundFormula", 7), //
  INCORPORATED_TRACERS("IncorporatedTracers", 8);

  private String header;
  private int columnIndex;

  private MSDatabaseColKey(String header, int columnIndex) {
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
    MSDatabaseColKey[] keys = values();
    ArrayList<String> headers = new ArrayList<>();
    for (int index = 0; index < keys.length; index++) {
      headers.add(keys[index].header);
    }
    return headers;
  }

}
