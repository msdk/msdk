/* 
 * (C) Copyright 2015-2016 by MSDK Development Team
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

package io.github.msdk.features.rowfilter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.util.FeatureTableUtil;
import io.github.msdk.util.tolerances.MzTolerance;
import io.github.msdk.util.tolerances.RTTolerance;

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
    private final @Nullable MzTolerance duplicateMzTolerance;
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
     *            a {@link io.github.msdk.datamodel.datastore.DataPointStore}
     *            object.
     * @param nameSuffix
     *            a {@link java.lang.String} object.
     * @param filterByMz
     *            a {@link java.lang.Boolean} object.
     * @param filterByRt
     *            a {@link java.lang.Boolean} object.
     * @param filterByDuration
     *            a {@link java.lang.Boolean} object.
     * @param filterByCount
     *            a {@link java.lang.Boolean} object.
     * @param filterByIsotopes
     *            a {@link java.lang.Boolean} object.
     * @param filterByIonAnnotation
     *            a {@link java.lang.Boolean} object.
     * @param requireAnnotation
     *            a {@link java.lang.Boolean} object.
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
     *            a {@link java.lang.Boolean} object.
     * @param duplicateMzTolerance
     *            an object that implements the {@link io.github.msdk.util.tolerances.MZTolerance} interface.
     * @param duplicateRtTolerance
     *            a {@link io.github.msdk.util.tolerances.RTTolerance} object.
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
            @Nullable MzTolerance duplicateMzTolerance,
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
            FeatureTableColumn<Object> column;
            processedRows++;

            // Check m/z
            if (filterByMz && mzRange != null) {
                final Double mz = row.getMz();
                if ((mz == null) || (!mzRange.contains(mz)))
                    continue;
            }

            // Check RT
            if (filterByRt && rtRange != null) {
                ChromatographyInfo chromatographyInfo = row
                        .getChromatographyInfo();
                if (chromatographyInfo != null) {
                    double rowRT = (double) chromatographyInfo
                            .getRetentionTime();
                    if (!rtRange.contains(rowRT))
                        continue;
                }
            }

            // Check duration
            if (filterByDuration && durationRange != null) {
                final Double averageDuration = FeatureTableUtil
                        .getAverageFeatureDuration(row);
                if (averageDuration == null)
                    continue;
                if (!durationRange.contains(averageDuration))
                    continue;
            }

            // Check count
            if (filterByCount && minCount != null) {
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
                FeatureTableColumn<List<IonAnnotation>> ionColumn = featureTable
                        .getColumn(ColumnName.IONANNOTATION, null);
                if (ionColumn == null)
                    continue;
                if (row.getData(ionColumn) != null) {
                    final List<IonAnnotation> rowIonAnnotations = row
                            .getData(ionColumn);
                    Boolean keep = false;
                    if (rowIonAnnotations != null) {
                        for (IonAnnotation rowIonAnnotation : rowIonAnnotations) {
                            String annotationId = rowIonAnnotation
                                    .getAnnotationId();
                            if (annotationId != null)
                                if (!annotationId.contains(ionAnnotation))
                                    keep = true;
                        }
                    }
                    if (!keep)
                        continue;
                }
            }

            // Require ion annotation?
            if (requireAnnotation) {
                FeatureTableColumn<List<IonAnnotation>> ionColumn = featureTable
                        .getColumn(ColumnName.IONANNOTATION, null);
                if (ionColumn == null)
                    continue;
                final List<IonAnnotation> rowIonAnnotations = row
                        .getData(ionColumn);
                Boolean keep = false;
                if (rowIonAnnotations != null) {
                    for (IonAnnotation rowIonAnnotation : rowIonAnnotations) {
                        if (rowIonAnnotation.getDescription() != null)
                            keep = true;
                    }
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
        if (removeDuplicates && duplicateMzTolerance != null) {

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

                    // Compare retention time
                    ChromatographyInfo chromatographyInfo1 = firstRow
                            .getChromatographyInfo();
                    ChromatographyInfo chromatographyInfo2 = secondRow
                            .getChromatographyInfo();
                    boolean sameRt = false;
                    if (chromatographyInfo1 != null
                            && chromatographyInfo2 != null) {
                        sameRt = duplicateRtTolerance
                                .getToleranceRange(
                                        chromatographyInfo1.getRetentionTime())
                                .contains((double) chromatographyInfo2
                                        .getRetentionTime());
                    }

                    // Compare identifications
                    FeatureTableColumn<List<IonAnnotation>> column = result
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
