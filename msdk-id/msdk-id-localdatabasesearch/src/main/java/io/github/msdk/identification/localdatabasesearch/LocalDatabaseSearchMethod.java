/* 
 * (C) Copyright 2015 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */

package io.github.msdk.identification.localdatabasesearch;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.util.MZTolerance;
import io.github.msdk.util.RTTolerance;

/**
 * This class searches through a feature table to find hits in a local database
 * using m/z and retention time values.
 */
public class LocalDatabaseSearchMethod implements MSDKMethod<Void> {

    private final @Nonnull FeatureTable featureTable;
    private final @Nonnull List<IonAnnotation> ionAnnotations;
    private final @Nonnull MZTolerance mzTolerance;
    private final @Nonnull RTTolerance rtTolerance;

    private boolean canceled = false;
    private int processedFeatures = 0, totalFeatures = 0;

    /**
     * <p>
     * Constructor for LocalDatabaseSearchMethod.
     * </p>
     *
     * @param featureTable
     * @param ionAnnotations
     *            a {@link java.util.List} of
     *            {@link io.github.msdk.datamodel.ionannotations.IonAnnotation}
     *            objects.
     * @param mzTolerance
     *            a {@link io.github.msdk.util.MZTolerance} object.
     * @param rtTolerance
     *            a {@link io.github.msdk.util.RTTolerance} object.
     */
    public LocalDatabaseSearchMethod(@Nonnull FeatureTable featureTable,
            @Nonnull List<IonAnnotation> ionAnnotations,
            @Nonnull MZTolerance mzTolerance,
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
        FeatureTableColumn<List<IonAnnotation>> ionAnnotationColumn = featureTable
                .getColumn(ColumnName.IONANNOTATION, null);

        // Loop through all features in the feature table
        for (FeatureTableRow row : featureTable.getRows()) {

            // Row values
            Range<Double> mzRange = mzTolerance.getToleranceRange(row.getMz());
            Range<Double> rtRange = rtTolerance.getToleranceRange(
                    row.getChromatographyInfo().getRetentionTime());
            List<IonAnnotation> rowIonAnnotations = row.getData(ionAnnotationColumn);

            // Loop through all ion annotations from the local database
            for (IonAnnotation ionAnnotation : ionAnnotations) {

                // Ion values
                double ionMz = ionAnnotation.getExpectedMz();
                double ionRt = (double) ionAnnotation.getChromatographyInfo()
                        .getRetentionTime();
                ionRt = ionRt / 60; // Convert from seconds to minutes
                final boolean mzMatch = mzRange.contains(ionMz);
                final boolean rtMatch = rtRange.contains(ionRt);

                // If match, add the ion annotation to the list
                if (mzMatch && rtMatch) {
                    // Is first ion annotation is empty then remove it
                    IonAnnotation firstionAnnotation = rowIonAnnotations.get(0);
                    if (firstionAnnotation.isNA())
                        rowIonAnnotations.remove(0);

                    rowIonAnnotations.add(ionAnnotation);
                }

            }

            // Update the ion annotations of the feature
            row.setData(ionAnnotationColumn, rowIonAnnotations);

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
        return totalFeatures == 0 ? null
                : (float) processedFeatures / totalFeatures;
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
