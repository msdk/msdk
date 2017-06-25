/*
 * (C) Copyright 2015-2016 by MSDK Development Team
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

package io.github.msdk.io.mzml2.data;

public enum MzMLArrayType {
  MZ("MS:1000514"), INTENSITY("MS:1000515"), TIME("MS:1000595");

  private String accession;

  private MzMLArrayType(String accession) {
    this.accession = accession;
  }

  public String getValue() {
    return accession;
  }
}
