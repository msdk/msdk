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

package io.github.msdk.features.isotopegrouper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.util.tolerances.MzTolerance;
import io.github.msdk.util.tolerances.RTTolerance;

/**
 * This class searches through a feature table and groups isotopes under a single feature.
 */
public class IsotopeGrouperMethod implements MSDKMethod<FeatureTable> {

  /**
   * The isotopeDistance constant defines expected distance between isotopes. Actual weight of 1
   * neutron is 1.008665 Da, but part of this mass is consumed as binding energy to other
   * protons/neutrons. Actual mass increase of isotopes depends on chemical formula of the molecule.
   * Since we don't know the formula, we can assume the distance to be ~1.0033 Da, with user-defined
   * tolerance.
   */
  private static final double isotopeDistance = 1.0033;

  private final @Nonnull FeatureTable featureTable;
  private final @Nonnull DataPointStore dataStore;
  private final @Nonnull MzTolerance mzTolerance;
  private final @Nonnull RTTolerance rtTolerance;
  private final @Nonnull Integer maximumCharge;
  private final @Nonnull Boolean requireMonotonicShape;
  private final @Nonnull String featureTableName;
  private final @Nonnull FeatureTable result;

  private boolean canceled = false;
  private int processedFeatures = 0, totalFeatures = 0;

  /**
   * <p>
   * Constructor for IsotopeGrouperMethod.
   * </p>
   *
   */
  public IsotopeGrouperMethod(@Nonnull FeatureTable featureTable, @Nonnull DataPointStore dataStore,
      @Nonnull MzTolerance mzTolerance, @Nonnull RTTolerance rtTolerance,
      @Nonnull Integer maximumCharge, @Nonnull Boolean requireMonotonicShape,
      @Nonnull String featureTableName) {
    this.featureTable = featureTable;
    this.dataStore = dataStore;
    this.mzTolerance = mzTolerance;
    this.rtTolerance = rtTolerance;
    this.maximumCharge = maximumCharge;
    this.requireMonotonicShape = requireMonotonicShape;
    this.featureTableName = featureTableName;

    // Make a new feature table
    result = MSDKObjectBuilder.getFeatureTable(featureTableName, dataStore);
  }

  /** {@inheritDoc} */
  @Override
  public FeatureTable execute() throws MSDKException {

    // Set the total features
    totalFeatures = featureTable.getRows().size();

    // Collect all possible charge states
    int charges[] = new int[maximumCharge];
    for (int i = 0; i < maximumCharge; i++) {
      charges[i] = i + 1;
    }

    // Loop through all features in the feature table
    for (FeatureTableRow row : featureTable.getRows()) {

      /*
       * TODO: Write method
       */

      if (canceled)
        return null;

      processedFeatures++;
    }

    return null;
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
