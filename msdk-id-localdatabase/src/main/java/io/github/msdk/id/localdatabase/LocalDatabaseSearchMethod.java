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

package io.github.msdk.id.localdatabase;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.FeatureTable;
import io.github.msdk.datamodel.FeatureTableRow;
import io.github.msdk.datamodel.IonAnnotation;
import io.github.msdk.datamodel.SimpleIonAnnotation;
import io.github.msdk.util.tolerances.MzTolerance;
import io.github.msdk.util.tolerances.RTTolerance;

/**
 * This class searches through a feature table to find hits in a local database using m/z and
 * retention time values.
 */
public class LocalDatabaseSearchMethod implements MSDKMethod<Void> {

  private final @Nonnull FeatureTable featureTable;
  private final @Nonnull List<SimpleIonAnnotation> ionAnnotations;
  private final @Nonnull MzTolerance mzTolerance;
  private final @Nonnull RTTolerance rtTolerance;

  private boolean canceled = false;
  private int processedFeatures = 0, totalFeatures = 0;

  /**
   * <p>
   * Constructor for LocalDatabaseSearchMethod.
   * </p>
   *
   * @param featureTable a {@link io.github.msdk.datamodel.FeatureTable} object.
   * @param ionAnnotations a {@link java.util.List} of
   *        {@link io.github.msdk.datamodel.IonAnnotation} objects.
   * @param mzTolerance an object
   * @param rtTolerance a {@link io.github.msdk.util.tolerances.RTTolerance} object.
   */
  public LocalDatabaseSearchMethod(@Nonnull FeatureTable featureTable,
      @Nonnull List<SimpleIonAnnotation> ionAnnotations, @Nonnull MzTolerance mzTolerance,
      @Nonnull RTTolerance rtTolerance) {
    this.featureTable = featureTable;
    this.ionAnnotations = ionAnnotations;
    this.mzTolerance = mzTolerance;
    this.rtTolerance = rtTolerance;
  }

  /** {@inheritDoc} */
  @Override
  public Void execute() throws MSDKException {

    totalFeatures = featureTable.getRows().size();

    // Loop through all features in the feature table
    for (FeatureTableRow row : featureTable.getRows()) {

      final Double mz = row.getMz();
      final Float rt = row.getRT();
      if ((mz == null) || (rt == null))
        continue;

      // Row values
      Range<Double> mzRange = mzTolerance.getToleranceRange(mz);
      Range<Float> rtRange = rtTolerance.getToleranceRange(rt);
      List<SimpleIonAnnotation> rowIonAnnotations = null;

      // Empty rowIonAnnotations
      if (rowIonAnnotations == null)
        rowIonAnnotations = new ArrayList<SimpleIonAnnotation>();

      // Loop through all ion annotations from the local database
      for (SimpleIonAnnotation ionAnnotation : ionAnnotations) {

        // Ion values
        final Double ionMz = ionAnnotation.getExpectedMz();
        final Float ionRT = ionAnnotation.getExpectedRetentionTime();
        if ((ionMz == null) || (ionRT == null))
          continue;

        // Convert from seconds to minutes
        float ionRtSec = ionRT / 60.0f;
        final boolean mzMatch = mzRange.contains(ionMz);
        final boolean rtMatch = rtRange.contains(ionRtSec);

        // If match, add the ion annotation to the list
        if (mzMatch && rtMatch) {

          // If first ion annotation is empty then remove it
          if (rowIonAnnotations.size() > 0) {
            IonAnnotation firstionAnnotation = rowIonAnnotations.get(0);
          }

          rowIonAnnotations.add(ionAnnotation);
        }

      }

      // Update the ion annotations of the feature
      //row.setData(ionAnnotationColumn, rowIonAnnotations);

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
  public Void getResult() {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    canceled = true;
  }

}
