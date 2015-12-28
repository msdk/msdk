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

package io.github.msdk.features.filtering;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.util.FeatureTableUtil;

/**
 * This class creates a filtered feature table based on a feature table and a
 * set of feature filters.
 */
public class FeatureFilterMethod implements MSDKMethod<FeatureTable> {

    // Boolean values
    private final boolean filterByDuration;
    private final boolean filterByArea;
    private final boolean filterByHeight;
    private final boolean filterByDataPoints;
    private final boolean filterByFWHM;
    private final boolean filterByTailingFactor;
    private final boolean filterByAsymmetryFactor;

    // Ranges values
    private final @Nullable Range<Double> durationRange;
    private final @Nullable Range<Double> areaRange;
    private final @Nullable Range<Double> heightRange;
    private final @Nullable Range<Integer> dataPointsRange;
    private final @Nullable Range<Double> fwhmRange;
    private final @Nullable Range<Double> tailingFactorRange;
    private final @Nullable Range<Double> asymmetryFactorRange;

    // Other variables
    private final @Nonnull FeatureTable featureTable;
    private final @Nonnull String nameSuffix;
    private final @Nonnull DataPointStore dataStore;
    private final @Nonnull FeatureTable result;
    private boolean canceled = false;
    private int processedFeatures = 0, totalFeatures = 0;

    /**
     * <p>
     * Constructor for FeatureFilterMethod.
     * </p>
     *
     * @param featureTable
     *            a {@link io.github.msdk.datamodel.featuretables.FeatureTable}
     *            object.
     * @param dataStore
     *            a
     *            {@link io.github.msdk.datamodel.datapointstore.DataPointStore}
     *            object.
     * @param filterByDuration
     *            a boolean.
     * @param filterByArea
     *            a boolean.
     * @param filterByHeight
     *            a boolean.
     * @param filterByDataPoints
     *            a boolean.
     * @param filterByFWHM
     *            a boolean.
     * @param filterByTailingFactor
     *            a boolean.
     * @param filterByAsymmetryFactor
     *            a boolean.
     * @param durationRange
     *            a {@link com.google.common.collect.Range} object.
     * @param areaRange
     *            a {@link com.google.common.collect.Range} object.
     * @param heightRange
     *            a {@link com.google.common.collect.Range} object.
     * @param dataPointsRange
     *            a {@link com.google.common.collect.Range} object.
     * @param fwhmRange
     *            a {@link com.google.common.collect.Range} object.
     * @param tailingFactorRange
     *            a {@link com.google.common.collect.Range} object.
     * @param asymmetryFactorRange
     *            a {@link com.google.common.collect.Range} object.
     * @param nameSuffix
     *            a {@link java.lang.String} object.
     */
    public FeatureFilterMethod(@Nonnull FeatureTable featureTable,
            @Nonnull DataPointStore dataStore, boolean filterByDuration,
            boolean filterByArea, boolean filterByHeight,
            boolean filterByDataPoints, boolean filterByFWHM,
            boolean filterByTailingFactor, boolean filterByAsymmetryFactor,
            @Nullable Range<Double> durationRange,
            @Nullable Range<Double> areaRange,
            @Nullable Range<Double> heightRange,
            @Nullable Range<Integer> dataPointsRange,
            @Nullable Range<Double> fwhmRange,
            @Nullable Range<Double> tailingFactorRange,
            @Nullable Range<Double> asymmetryFactorRange,
            @Nonnull String nameSuffix) {
        this.featureTable = featureTable;
        this.dataStore = dataStore;
        this.filterByDuration = filterByDuration;
        this.filterByArea = filterByArea;
        this.filterByHeight = filterByHeight;
        this.filterByDataPoints = filterByDataPoints;
        this.filterByFWHM = filterByFWHM;
        this.filterByTailingFactor = filterByTailingFactor;
        this.filterByAsymmetryFactor = filterByAsymmetryFactor;
        this.durationRange = durationRange;
        this.areaRange = areaRange;
        this.heightRange = heightRange;
        this.dataPointsRange = dataPointsRange;
        this.fwhmRange = fwhmRange;
        this.tailingFactorRange = tailingFactorRange;
        this.asymmetryFactorRange = asymmetryFactorRange;
        this.nameSuffix = nameSuffix;

        // Make a new feature table
        result = MSDKObjectBuilder.getFeatureTable(
                featureTable.getName() + nameSuffix, dataStore);
    }

    /** {@inheritDoc} */
    @Override
    public FeatureTable execute() throws MSDKException {
        // Total features
        totalFeatures = featureTable.getRows().size()
                * featureTable.getSamples().size();

        // Add columns
        for (FeatureTableColumn<?> column : featureTable.getColumns()) {
            result.addColumn(column);
        }

        // Loop through all features
        for (FeatureTableRow row : featureTable.getRows()) {

            // Values for keeping track of features for a sample
            boolean[] keepFeature = new boolean[featureTable.getSamples()
                    .size()];
            int i = 0;

            // Loop through all samples for the feature
            for (Sample sample : featureTable.getSamples()) {

                FeatureTableColumn<?> column;
                keepFeature[i] = true;

                // Check Duration
                if (filterByDuration) {
                    column = featureTable.getColumn(ColumnName.DURATION,
                            sample);
                    if (column != null) {
                        if (row.getData(column) != null) {
                            final Double peakDuration = (Double) row
                                    .getData(column);
                            if (!durationRange.contains(peakDuration))
                                keepFeature[i] = false;
                        }
                    }
                }

                // Check Area
                if (filterByArea) {
                    column = featureTable.getColumn(ColumnName.AREA, sample);
                    if (column != null) {
                        if (row.getData(column) != null) {
                            final Double peakArea = (Double) row
                                    .getData(column);
                            if (!areaRange.contains(peakArea))
                                keepFeature[i] = false;
                        }
                    }
                }

                // Check Height
                if (filterByHeight) {
                    column = featureTable.getColumn(ColumnName.HEIGHT, sample);
                    if (column != null) {
                        if (row.getData(column) != null) {
                            final Double peakHeight = (Double) row
                                    .getData(column);
                            if (!heightRange.contains(peakHeight))
                                keepFeature[i] = false;
                        }
                    }
                }

                // Check # Data Points
                if (filterByDataPoints) {
                    column = featureTable
                            .getColumn(ColumnName.NUMBEROFDATAPOINTS, sample);
                    if (column != null) {
                        if (row.getData(column) != null) {
                            final Integer peakDataPoints = (Integer) row
                                    .getData(column);
                            if (!dataPointsRange.contains(peakDataPoints))
                                keepFeature[i] = false;
                        }
                    }
                }

                // Check FWHM
                if (filterByFWHM) {
                    column = featureTable.getColumn(ColumnName.FWHM, sample);
                    if (column != null) {
                        if (row.getData(column) != null) {
                            final Double peakFWHM = (Double) row
                                    .getData(column);
                            if (!fwhmRange.contains(peakFWHM))
                                keepFeature[i] = false;
                        }
                    }
                }

                // Check Tailing Factor
                if (filterByTailingFactor) {
                    column = featureTable.getColumn(ColumnName.TAILINGFACTOR,
                            sample);
                    if (column != null) {
                        if (row.getData(column) != null) {
                            final Double peakTF = (Double) row.getData(column);
                            if (!tailingFactorRange.contains(peakTF))
                                keepFeature[i] = false;
                        }
                    }
                }

                // Check Asymmetry Factor
                if (filterByAsymmetryFactor) {
                    column = featureTable.getColumn(ColumnName.ASYMMETRYFACTOR,
                            sample);
                    if (column != null) {
                        if (row.getData(column) != null) {
                            final Double peakAF = (Double) row.getData(column);
                            if (!asymmetryFactorRange.contains(peakAF))
                                keepFeature[i] = false;
                        }
                    }
                }

                // If no value is found in the m/z column for the sample then
                // the feature is not present for this sample
                column = featureTable.getColumn(ColumnName.MZ, sample);
                if (row.getData(column) == null) {
                    keepFeature[i] = false;
                }

                i++;
                processedFeatures++;

                if (canceled)
                    return null;
            }

            // Add the feature row to the table if it is not null
            FeatureTableRow newRow = copyRow(row, keepFeature, result);
            if (newRow != null)
                result.addRow(newRow);
        }

        // Re-calculate average row m/z and RT values
        FeatureTableUtil.recalculateAverages(result);

        // Return the new feature table
        return result;
    }

    /**
     * Create a copy of a peak list row.
     */
    private static FeatureTableRow copyRow(@Nonnull FeatureTableRow row,
            @Nonnull boolean[] keepFeatures, @Nonnull FeatureTable result) {

        // Return null if no sample has data for the feature
        boolean noSamples = true;
        for (boolean value : keepFeatures) {
            if (value)
                noSamples = false;
        }
        if (noSamples)
            return null;

        // Create a new row with the common feature data
        final FeatureTableRow newRow = MSDKObjectBuilder
                .getFeatureTableRow(result, row.getId());
        FeatureTableUtil.copyCommonValues(row, newRow, false);

        // Copy the feature data for the samples
        int i = 0;
        for (Sample sample : row.getFeatureTable().getSamples()) {

            // Only keep feature data if it fulfills the filter criteria
            if (keepFeatures[i]) {
                FeatureTableUtil.copyFeatureValues(row, newRow, sample);
            }
            i++;

        }

        return newRow;
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
    public FeatureTable getResult() {
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() {
        canceled = true;
    }

}
