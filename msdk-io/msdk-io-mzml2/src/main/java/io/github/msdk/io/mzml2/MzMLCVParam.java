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

class MzMLCVParam {

  private String accession;
  private String value;
  private String unitAccession;

  /**
   * <p>Constructor for MzMLCVParam.</p>
   *
   * @param accession a {@link java.lang.String} object.
   * @param value a {@link java.lang.String} object.
   * @param unitAccession a {@link java.lang.String} object.
   */
  public MzMLCVParam(String accession, String value, String unitAccession) {
    this.setAccession(accession);
    this.setValue(value);
    this.setUnitAccession(unitAccession);
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
   * <p>Setter for the field <code>accession</code>.</p>
   *
   * @param accession a {@link java.lang.String} object.
   */
  public void setAccession(String accession) {
    this.accession = accession;
  }

  /**
   * <p>Getter for the field <code>value</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getValue() {
    return value;
  }

  /**
   * <p>Setter for the field <code>value</code>.</p>
   *
   * @param value a {@link java.lang.String} object.
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * <p>Getter for the field <code>unitAccession</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getUnitAccession() {
    return unitAccession;
  }

  /**
   * <p>Setter for the field <code>unitAccession</code>.</p>
   *
   * @param unitAccession a {@link java.lang.String} object.
   */
  public void setUnitAccession(String unitAccession) {
    this.unitAccession = unitAccession;
  }
}
