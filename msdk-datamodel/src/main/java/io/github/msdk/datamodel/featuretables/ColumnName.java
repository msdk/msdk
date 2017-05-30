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

package io.github.msdk.datamodel.featuretables;

import java.util.List;

import javax.annotation.Nonnull;

import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;

/**
 * Represents the name of the feature table columns.
 */
public enum ColumnName {

  ID("Id", Integer.class), //
  GROUPID("Group ID", Integer.class), //
  @SuppressWarnings("unchecked")
  IONANNOTATION("Ion Annotation", (Class<List<IonAnnotation>>) (Class<?>) List.class), //
  MZ("m/z", Double.class), //
  PPM("ppm", Double.class), //
  RT("RT", Float.class), //
  RTSTART("RT Start", Double.class), //
  RTEND("RT End", Double.class), //
  DURATION("Duration", Double.class), //
  AREA("Area", Double.class), //
  HEIGHT("Height", Float.class), //
  CHARGE("Charge", Integer.class), //
  NUMBEROFDATAPOINTS("# Data Points", Integer.class), //
  FWHM("FWHM", Double.class), //
  TAILINGFACTOR("Tailing Factor", Double.class), //
  ASYMMETRYFACTOR("Asymmetry Factor", Double.class), //
  CHROMATOGRAM("Chromatogram", Chromatogram.class), //
  RETENTIONINDEX("Retention Index", Double.class), //
  Q1("Q1", Double.class), //
  Q3("Q3", Double.class); //

  private final @Nonnull String name;
  private final @Nonnull Class<?> dataTypeClass;

  /**
   * Create a new column name instance.
   *
   * @param name the name of the column.
   * @param dataTypeClass the class of the column's values.
   */
  ColumnName(@Nonnull String name, @Nonnull Class<?> dataTypeClass) {
    this.name = name;
    this.dataTypeClass = dataTypeClass;
  }

  /**
   * Returns the name of the column.
   *
   * @return the name of the column.
   */
  @Nonnull
  public String getName() {
    return name;
  }

  /**
   * Returns the class of the column's values.
   *
   * @return the class of the column's values.
   */
  @Nonnull
  public Class<?> getDataTypeClass() {
    return dataTypeClass;
  }

}
