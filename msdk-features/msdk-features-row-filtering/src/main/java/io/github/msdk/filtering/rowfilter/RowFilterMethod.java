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

package io.github.msdk.filtering.rowfilter;

import java.util.ArrayList;
import java.util.List;

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
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.util.FeatureTableUtil;
import io.github.msdk.util.MZTolerance;
import io.github.msdk.util.RTTolerance;

/**
 * This class creates a filtered feature table based on a feature table and a
 * set of row filters.
 */
public class RowFilterMethod implements MSDKMethod<FeatureTable> {

    // Boolean values
    private final boolean filterByMz;
    private final boolean filterByRt;
    private final boolean filterByDuration;
    private final boolean filterByCount;
    private final boolean filterByIsotopes;
    private final boolean filterByIonAnnotation;
    private final boolean requireAnnotation;
    private final boolean removeDuplicates;
    private final boolean duplicateRequireSameID;

    // Ranges values
    private final @Nullable Range<Double> mzRange;
    private final @Nullable Range<Double> rtRange;
    private final @Nullable Range<Double> durationRange;

    // Tolerance values
    private final @Nullable MZTolerance duplicateMzTolerance;
    private final @Nullable RTTolerance duplicateRtTolerance;

    // Other variables
    private final @Nullable Integer minCount;
    private final @Nullable Integer minIsotopes;
    private final @Nullable String ionAnnotation;
    private final @Nonnull FeatureTable featureTable;
    private final @Nonnull String nameSuffix;
    private final @Nonnull DataPointStore dataStore;
    private final @Nonnull FeatureTable result;
    private boolean canceled = false;
    private int processedRows = 0, totalRows = 0;

    /**
     * <p>
     * Constructor for RowFilterMethod.
     * </p>
     *
     * @param featureTable
     *            a {@link io.github.msdk.datamodel.featuretables.FeatureTable}
     *            object.
     * @param dataStore
     *            a
     *            {@link io.github.msdk.datamodel.datapointstore.DataPointStore}
     *            object.
     * @param nameSuffix
     *            a {@link java.lang.String} object.
     * @param filterByMz
     *            a boolean.
     * @param filterByRt
     *            a boolean.
     * @param filterByDuration
     *            a boolean.
     * @param filterByCount
     *            a boolean.
     * @param filterByIsotopes
     *            a boolean.
     * @param filterByIonAnnotation
     *            a boolean.
     * @param requireAnnotation
     *            a boolean.
     * @param mzRange
     *            a {@link com.google.common.collect.Range} object.
     * @param rtRange
     *            a {@link com.google.common.collect.Range} object.
     * @param durationRange
     *            a {@link com.google.common.collect.Range} object.
     * @param minCount
     *            a {@link java.lang.Integer} object.
     * @param minIsotopes
     *            a {@link java.lang.Integer} object.
     * @param ionAnnotation
     *            a {@link java.lang.String} object.
     * @param removeDuplicates
     *            a boolean.
     * @param duplicateMzTolerance
     *            a {@link io.github.msdk.util.MZTolerance} object.
     * @param duplicateRtTolerance
     *            a {@link io.github.msdk.util.RTTolerance} object.
     * @param duplicateRequireSameID
     *            a boolean.
     */
    public RowFilterMethod(@Nonnull FeatureTable featureTable,
            @Nonnull DataPointStore dataStore, @Nonnull String nameSuffix,
            boolean filterByMz, boolean filterByRt, boolean filterByDuration,
            boolean filterByCount, boolean filterByIsotopes,
            boolean filterByIonAnnotation, boolean requireAnnotation,
            @Nullable Range<Double> mzRange, @Nullable Range<Double> rtRange,
            @Nullable Range<Double> durationRange, @Nullable Integer minCount,
            @Nullable Integer minIsotopes, @Nullable String ionAnnotation,
            boolean removeDuplicates,
            @Nullable MZTolerance duplicateMzTolerance,
            @Nullable RTTolerance duplicateRtTolerance,
            boolean duplicateRequireSameID) {

        this.featureTable = featureTable;
        this.dataStore = dataStore;
        this.nameSuffix = nameSuffix;
        this.filterByMz = filterByMz;
        this.filterByRt = filterByRt;
        this.filterByDuration = filterByDuration;
        this.filterByCount = filterByCount;
        this.filterByIsotopes = filterByIsotopes;
        this.filterByIonAnnotation = filterByIonAnnotation;
        this.requireAnnotation = requireAnnotation;
        this.mzRange = mzRange;
        this.rtRange = rtRange;
        this.durationRange = durationRange;
        this.minCount = minCount;
        this.minIsotopes = minIsotopes;
        this.ionAnnotation = ionAnnotation;
        this.removeDuplicates = removeDuplicates;
        this.duplicateMzTolerance = duplicateMzTolerance;
        this.duplicateRtTolerance = duplicateRtTolerance;
        this.duplicateRequireSameID = duplicateRequireSameID;

        // Make a new feature table
        result = MSDKObjectBuilder.getFeatureTable(
                featureTable.getName() + nameSuffix, dataStore);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public FeatureTable execute() throws MSDKException {
        // Total features
        totalRows = featureTable.getRows().size();

        // If remove duplicates is selected, the features will looped twice
        if (removeDuplicates)
            totalRows = totalRows * 2;

        // Add columns
        for (FeatureTableColumn<?> column : featureTable.getColumns()) {
            result.addColumn(column);
        }

        // Loop through all features
        for (FeatureTableRow row : featureTable.getRows()) {
            FeatureTableColumn<?> column;
            processedRows++;

            // Check m/z
            if (filterByMz) {
                if (!mzRange.contains(row.getMz()))
                    continue;
            }

            // Check RT
            if (filterByRt) {
                double rowRT = (double) row.getChromatographyInfo()
                        .getRetentionTime();
                if (!rtRange.contains(rowRT))
                    continue;
            }

            // Check duration
            if (filterByDuration) {
                final Double averageDuration = FeatureTableUtil
                        .getAverageFeatureDuration(row);
                if (averageDuration == null)
                    continue;
                if (!durationRange.contains(averageDuration))
                    continue;
            }

            // Check count
            if (filterByCount) {
                final int rowCount = FeatureTableUtil.getRowCount(row);
                if (!(rowCount >= minCount))
                    continue;
            }

            // Check isotopes
            if (filterByIsotopes) {
                /*
                 * TODO
                 */
            }

            // Check ion annotation
            if (filterByIonAnnotation && ionAnnotation != null) {
                column = featureTable.getColumn(ColumnName.IONANNOTATION, null);
                if (column == null)
                    continue;
                if (row.getData(column) != null) {
                    final List<IonAnnotation> rowIonAnnotations = (List<IonAnnotation>) row
                            .getData(column);
                    Boolean keep = false;
                    for (IonAnnotation rowIonAnnotation : rowIonAnnotations) {
                        if (!rowIonAnnotation.getAnnotationId()
                                .contains(ionAnnotation))
                            keep = true;
                    }
                    if (!keep)
                        continue;
                }
            }

            // Require ion annotation?
            if (requireAnnotation) {
                column = featureTable.getColumn(ColumnName.IONANNOTATION, null);
                if (column == null)
                    continue;
                final List<IonAnnotation> rowIonAnnotations = (List<IonAnnotation>) row
                        .getData(column);
                Boolean keep = false;
                for (IonAnnotation rowIonAnnotation : rowIonAnnotations) {
                    if (rowIonAnnotation.getDescription() != null)
                        keep = true;
                }
                if (!keep)
                    continue;
            }

            // Add row if all filters are fulfilled
            result.addRow(copyRow(row, result));

            if (canceled)
                return null;
        }

        // Remove duplicate features?
        if (removeDuplicates) {

            final int rowCount = result.getRows().size();
            List<FeatureTableRow> rows = result.getRows();
            List<FeatureTableRow> removeRows = new ArrayList<FeatureTableRow>();

            // Recalculate the remaining rows in the result feature table
            totalRows = totalRows / 2 + rowCount;

            // Loop through all rows
            for (int firstRowIndex = 0; firstRowIndex < rowCount; firstRowIndex++) {
                FeatureTableRow firstRow = rows.get(firstRowIndex);
                final Double mz = firstRow.getMz();
                if (mz == null)
                    continue;

                // Loop through all the rows below the current
                for (int secondRowIndex = firstRowIndex
                        + 1; secondRowIndex < rowCount; secondRowIndex++) {
                    FeatureTableRow secondRow = rows.get(secondRowIndex);
                    if (removeRows.contains(secondRow))
                        continue;

                    // Compare m/z
                    final boolean sameMz = duplicateMzTolerance
                            .getToleranceRange(mz).contains(secondRow.getMz());

                    // Compare rt
                    ChromatographyInfo chromatographyInfo1 = firstRow
                            .getChromatographyInfo();
                    ChromatographyInfo chromatographyInfo2 = secondRow
                            .getChromatographyInfo();
                    final boolean sameRt = duplicateRtTolerance
                            .getToleranceRange(
                                    chromatographyInfo1.getRetentionTime())
                            .contains((double) chromatographyInfo2
                                    .getRetentionTime());

                    // Compare identifications
                    FeatureTableColumn<?> column = result
                            .getColumn(ColumnName.IONANNOTATION, null);
                    List<IonAnnotation> ionAnnotation1 = (List<IonAnnotation>) firstRow
                            .getData(column);
                    List<IonAnnotation> ionAnnotation2 = (List<IonAnnotation>) secondRow
                            .getData(column);
                    final boolean sameId = !duplicateRequireSameID
                            || ionAnnotation1.equals(ionAnnotation2);

                    // Duplicate peaks?
                    if (sameMz && sameRt && sameId) {
                        if (!removeRows.contains(secondRow))
                            removeRows.add(secondRow);
                    }

                    if (canceled)
                        return null;
                }

                processedRows++;

            }

            // Remove rows
            for (FeatureTableRow row : removeRows) {
                result.removeRow(row);
            }

        }

        // Return the new feature table
        return result;
    }

    private static FeatureTableRow copyRow(@Nonnull FeatureTableRow row,
            @Nonnull FeatureTable result) {

        // Create a new row with the common feature data
        final FeatureTableRow newRow = MSDKObjectBuilder
                .getFeatureTableRow(result, row.getId());
        FeatureTableUtil.copyCommonValues(row, newRow, false);

        // Copy the feature data for the samples
        for (Sample sample : row.getFeatureTable().getSamples()) {
            FeatureTableUtil.copyFeatureValues(row, newRow, sample);
        }

        return newRow;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Float getFinishedPercentage() {
        return totalRows == 0 ? null : (float) processedRows / totalRows;
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
