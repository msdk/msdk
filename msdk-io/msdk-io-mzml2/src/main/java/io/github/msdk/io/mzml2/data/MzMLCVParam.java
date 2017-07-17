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

import java.util.Optional;

public class MzMLCVParam {

  private final String accession;
  private final Optional<String> value;
  private final Optional<String> name;
  private final Optional<String> unitAccession;

  /**
   * <p>
   * Constructor for MzMLCVParam.
   * </p>
   *
   * @param accession a {@link java.lang.String} object.
   * @param value a {@link java.lang.String} object.
   * @param name a {@link java.lang.String} object.
   * @param unitAccession a {@link java.lang.String} object.
   */
  public MzMLCVParam(String accession, String value, String name, String unitAccession) {
    if (accession == null)
      throw new IllegalArgumentException("Accession can't be null");
    if (accession.length() == 0)
      throw new IllegalArgumentException("Accession can't be an empty string");
    this.accession = accession;
    if (value != null && value.length() == 0)
      value = null;
    this.value = Optional.ofNullable(value);
    if (name != null && name.length() == 0)
      name = null;
    this.name = Optional.ofNullable(name);
    if (unitAccession != null && unitAccession.length() == 0)
      unitAccession = null;
    this.unitAccession = Optional.ofNullable(unitAccession);
  }

  /**
   * <p>
   * Getter for the field <code>accession</code>.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getAccession() {
    return accession;
  }


  /**
   * <p>
   * Getter for the field <code>value</code>.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  public Optional<String> getValue() {
    return value;
  }

  /**
   * <p>
   * Getter for the field <code>name</code>.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  public Optional<String> getName() {
    return name;
  }

  /**
   * <p>
   * Getter for the field <code>unitAccession</code>.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  public Optional<String> getUnitAccession() {
    return unitAccession;
  }

}
