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

package io.github.msdk.featuredetection.chromatogramtofeaturetable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.SeparationType;
import io.github.msdk.util.ChromatogramUtil;
import io.github.msdk.util.FeatureTableUtil;
import java.util.EnumMap;

/**
 * This class adds a list of chromatograms to a feature table.
 */
public class ChromatogramToFeatureTableMethod implements
        MSDKMethod<FeatureTable> {

    private final @Nonnull List<Chromatogram> chromatograms;
    private final @Nonnull FeatureTable featureTable;
    private final @Nonnull Sample sample;

    private final Map<ColumnName, FeatureTableColumn<?>> tableColumns = new EnumMap<>(
            ColumnName.class);

    private boolean canceled = false;
    private int processedChromatograms = 0, totalChromatograms = 0;

    /**
     * <p>
     * Constructor for ChromatogramToFeatureTableMethod.
     * </p>
     *
     * @param chromatograms
     *            a {@link java.util.List} object.
     * @param featureTable
     *            a {@link io.github.msdk.datamodel.featuretables.FeatureTable}
     *            object.
     * @param sample
     *            a {@link io.github.msdk.datamodel.featuretables.Sample}
     *            object.
     */
    public ChromatogramToFeatureTableMethod(
            @Nonnull List<Chromatogram> chromatograms,
            @Nonnull FeatureTable featureTable, @Nonnull Sample sample) {
        this.chromatograms = chromatograms;
        this.featureTable = featureTable;
        this.sample = sample;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public FeatureTable execute() throws MSDKException {
        totalChromatograms = chromatograms.size();

        // Add the columns to the table if needed
        addColumns(featureTable);

        // Check if cancel is requested
        if (canceled)
            return null;

        // Id of last row in feature table
        int lastID = 0;
        List<FeatureTableRow> rows = featureTable.getRows();
        if (!rows.isEmpty()) {
            lastID = featureTable.getRows().get(featureTable.getRows().size())
                    .getId();
        }

        // Loop through all chromatograms and add values to the feature table
        FeatureTableColumn column;
        for (Chromatogram chromatogram : chromatograms) {
            lastID++;
            FeatureTableRow newRow = MSDKObjectBuilder.getFeatureTableRow(
                    featureTable, lastID);

            column = featureTable.getColumn(ColumnName.ID, null);
            newRow.setData(column, lastID);

            column = featureTable.getColumn("Ion Annotation", null,
                    IonAnnotation.class);
            newRow.setData(column, chromatogram.getIonAnnotation());

            column = tableColumns.get(ColumnName.CHROMATOGRAM);
            newRow.setData(column, chromatogram);

            double mz = chromatogram.getMz();
            column = tableColumns.get(ColumnName.MZ);
            newRow.setData(column, mz);

            float rt = ChromatogramUtil.getRt(chromatogram);
            ChromatographyInfo chromatographyInfo = MSDKObjectBuilder
                    .getChromatographyInfo1D(SeparationType.UNKNOWN, rt);
            column = tableColumns.get(ColumnName.RT);
            newRow.setData(column, chromatographyInfo);

            double rtStart = ChromatogramUtil.getRtStart(chromatogram);
            column = tableColumns.get(ColumnName.RTSTART);
            newRow.setData(column, rtStart);

            double rtEnd = ChromatogramUtil.getRtEnd(chromatogram);
            column = tableColumns.get(ColumnName.RTEND);
            newRow.setData(column, rtEnd);

            double duration = ChromatogramUtil.getDuration(chromatogram);
            column = tableColumns.get(ColumnName.DURATION);
            newRow.setData(column, duration);

            double area = ChromatogramUtil.getArea(chromatogram);
            column = tableColumns.get(ColumnName.AREA);
            newRow.setData(column, area);

            double height = ChromatogramUtil.getMaxHeight(chromatogram);
            column = tableColumns.get(ColumnName.HEIGHT);
            newRow.setData(column, height);

            int datapoints = ChromatogramUtil
                    .getNumberOfDataPoints(chromatogram);
            column = tableColumns.get(ColumnName.NUMBEROFDATAPOINTS);
            newRow.setData(column, datapoints);

            Double fwhm = ChromatogramUtil.getFwhm(chromatogram);
            if (fwhm != null) {
                column = tableColumns.get(ColumnName.FWHM);
                newRow.setData(column, fwhm);
            }

            Double tailingFactor = ChromatogramUtil
                    .getTailingFactor(chromatogram);
            if (tailingFactor != null) {
                column = tableColumns.get(ColumnName.TAILINGFACTOR);
                newRow.setData(column, tailingFactor);
            }

            Double asymmetryFactor = ChromatogramUtil
                    .getAsymmetryFactor(chromatogram);
            if (asymmetryFactor != null) {
                column = tableColumns.get(ColumnName.ASYMMETRYFACTOR);
                newRow.setData(column, asymmetryFactor);
            }

            // Add row to feature table
            featureTable.addRow(newRow);

            // Increase counter
            processedChromatograms++;

            // Check if cancel is requested
            if (canceled)
                return null;
        }

        // Re-calculate average row m/z and RT values
        FeatureTableUtil.recalculateAverages(featureTable);

        return featureTable;
    }

    private void addColumns(@Nonnull FeatureTable featureTable) {

        // Common columns
        // Only add common columns if the feature table is empty
        if (featureTable.getColumns().isEmpty()) {
            FeatureTableColumn<Integer> idColumn = MSDKObjectBuilder
                    .getIdFeatureTableColumn();
            FeatureTableColumn<Double> mzColumn = MSDKObjectBuilder
                    .getMzFeatureTableColumn();
            FeatureTableColumn<Double> ppmColumn = MSDKObjectBuilder
                    .getPpmFeatureTableColumn();
            FeatureTableColumn<ChromatographyInfo> chromatographyInfoColumn = MSDKObjectBuilder
                    .getChromatographyInfoFeatureTableColumn();
            FeatureTableColumn<IonAnnotation> ionAnnotationColumn = MSDKObjectBuilder
                    .getIonAnnotationFeatureTableColumn();
            featureTable.addColumn(idColumn);
            featureTable.addColumn(mzColumn);
            featureTable.addColumn(ppmColumn);
            featureTable.addColumn(chromatographyInfoColumn);
            featureTable.addColumn(ionAnnotationColumn);
        }

        // Sample columns
        ArrayList<ColumnName> sampleColumns = new ArrayList<>();
        sampleColumns.add(ColumnName.CHROMATOGRAM);
        sampleColumns.add(ColumnName.MZ);
        sampleColumns.add(ColumnName.RT);
        sampleColumns.add(ColumnName.RTSTART);
        sampleColumns.add(ColumnName.RTEND);
        sampleColumns.add(ColumnName.DURATION);
        sampleColumns.add(ColumnName.AREA);
        sampleColumns.add(ColumnName.HEIGHT);
        sampleColumns.add(ColumnName.NUMBEROFDATAPOINTS);
        sampleColumns.add(ColumnName.FWHM);
        sampleColumns.add(ColumnName.TAILINGFACTOR);
        sampleColumns.add(ColumnName.ASYMMETRYFACTOR);

        for (ColumnName columnName : sampleColumns) {
            FeatureTableColumn<?> column = featureTable.getColumn(columnName,
                    sample);
            if (column == null) {
                column = MSDKObjectBuilder.getFeatureTableColumn(columnName,
                        sample);
                featureTable.addColumn(column);
            }
            tableColumns.put(columnName, column);
        }

    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Float getFinishedPercentage() {
        return totalChromatograms == 0 ? null : (float) processedChromatograms
                / totalChromatograms;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public FeatureTable getResult() {
        return featureTable;
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() {
        canceled = true;
    }

}
