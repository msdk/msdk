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

package io.github.msdk.io.mzml2;

import java.util.Optional;

class MzMLCVParam {

  private final String accession;
  private final Optional<String> value;
  private final Optional<String> unitAccession;

  /**
   * <p>Constructor for MzMLCVParam.</p>
   *
   * @param accession a {@link java.lang.String} object.
   * @param value a {@link java.lang.String} object.
   * @param unitAccession a {@link java.lang.String} object.
   */
  public MzMLCVParam(String accession, String value, String unitAccession) {
    this.accession = accession;
    this.value = Optional.ofNullable(value);
    this.unitAccession = Optional.ofNullable(unitAccession);
  }

  /**
   * <p>Getter for the field <code>accession</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getAccession() {
    return accession;
  }


  /**
   * <p>Getter for the field <code>value</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public Optional<String> getValue() {
    return value;
  }


  /**
   * <p>Getter for the field <code>unitAccession</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public Optional<String> getUnitAccession() {
    return unitAccession;
  }

}
