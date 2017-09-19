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

package io.github.msdk.normalization.compound;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.FeatureTable;
import io.github.msdk.datamodel.FeatureTableRow;
import io.github.msdk.datamodel.SimpleFeatureTable;

/**
 * This class normalized a list of feature table columns based on a set of features.
 */
public class FeatureNormalizationByCompoundMethod implements MSDKMethod<FeatureTable> {

  // Variables
  private final @Nonnull Integer mzRtWeight;
  private final @Nonnull NormalizationType normalizationType;
  private final @Nonnull List<FeatureTableRow> internalStandardRows;

  // Other variables
  private final @Nonnull FeatureTable featureTable;
  private final @Nonnull FeatureTable result;
  private boolean canceled = false;
  private int processedFeatures = 0, totalFeatures = 0;

  /**
   * <p>
   * Constructor for FeatureNormalizationByCompoundMethod.
   * </p>
   *
   * @param featureTable a {@link io.github.msdk.datamodel.FeatureTable} object.
   * @param normalizationType a
   *        {@link io.github.msdk.normalization.compound.NormalizationType} object.
   * @param internalStandardRows a {@link java.util.List} object of
   *        {@link io.github.msdk.datamodel.FeatureTableRow} .
   * @param mzRtWeight a {@link java.lang.Integer} object.
   */
  public FeatureNormalizationByCompoundMethod(@Nonnull FeatureTable featureTable,
      @Nonnull NormalizationType normalizationType,
      @Nonnull List<FeatureTableRow> internalStandardRows, @Nonnull Integer mzRtWeight) {
    this.featureTable = featureTable;
    this.normalizationType = normalizationType;
    this.mzRtWeight = mzRtWeight;
    this.internalStandardRows = internalStandardRows;

    // Make a copy of the feature table
    result = new SimpleFeatureTable();
  }

  /** {@inheritDoc} */
  @Override
  public FeatureTable execute() throws MSDKException {
    // Total features
    totalFeatures = featureTable.getRows().size();

    /*
     * TODO: Write method
     */
    processedFeatures = totalFeatures;

    // Return the new feature table
    return result;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Float getFinishedPercentage() {
    return totalFeatures == 0 ? null : (float) processedFeatures / totalFeatures;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public FeatureTable getResult() {
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    canceled = true;
  }

}
