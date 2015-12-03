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
package io.github.msdk.util;

import java.util.List;

import javax.annotation.Nonnull;

import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.SeparationType;

public class FeatureTableUtil {

    /**
     * Re-calculates the average m/z and RT values for a feature table
     *
     * @param featureTable
     *            the {@link FeatureTable} to apply the recalculation on.
     */
    public static void recalculateAverages(@Nonnull FeatureTable featureTable) {

        List<FeatureTableRow> rows = featureTable.getRows();
        Double mz;
        Float rt;
        double totalMz;
        float totalRt;
        int mzCount, rtCount;

        for (FeatureTableRow row : rows) {
            List<Sample> samples = featureTable.getSamples();

            totalMz = 0;
            totalRt = 0;
            mzCount = 0;
            rtCount = 0;
            for (Sample sample : samples) {
                FeatureTableColumn<Double> mzColumn = featureTable
                        .getColumn(ColumnName.MZ, sample);
                if (mzColumn != null) {
                    mz = row.getData(mzColumn);
                    if (mz != null) {
                        totalMz += mz;
                        mzCount++;
                    }
                }

                FeatureTableColumn<ChromatographyInfo> rtColumn = featureTable
                        .getColumn(ColumnName.RT, sample);
                if (rtColumn != null) {
                    ChromatographyInfo ri = row.getData(rtColumn);
                    if (ri != null) {
                        rt = ri.getRetentionTime();
                        if (rt != null) {
                            totalRt += rt;
                            rtCount++;
                        }
                    }
                }
            }

            // Update m/z
            FeatureTableColumn<Double> mzColumn = featureTable
                    .getColumn(ColumnName.MZ, null);
            Double newMz = totalMz / mzCount;
            row.setData(mzColumn, newMz);

            // Update ppm
            FeatureTableColumn<IonAnnotation> ionAnnotationColumn = featureTable
                    .getColumn("Ion Annotation", null, IonAnnotation.class);
            if (ionAnnotationColumn != null) {
                IonAnnotation ionAnnotation = row.getData(ionAnnotationColumn);
                if (ionAnnotation != null) {
                    Double ionMz = ionAnnotation.getExpectedMz();
                    if (ionMz != null) {
                        FeatureTableColumn<Double> ppmColumn = featureTable
                                .getColumn(ColumnName.PPM, null);
                        Double diff = Math.abs(newMz - ionMz);
                        row.setData(ppmColumn, (diff / ionMz) * 1000000);
                    }
                }
            }
            // Update RT
            FeatureTableColumn<ChromatographyInfo> chromInfoColumn = featureTable
                    .getColumn("Chromatography Info", null,
                            ChromatographyInfo.class);
            if (chromInfoColumn != null) {
                ChromatographyInfo currentChromatographyInfo = row
                        .getData(chromInfoColumn);
                SeparationType separationType;
                if (currentChromatographyInfo == null) {
                    separationType = SeparationType.UNKNOWN;
                } else {
                    separationType = currentChromatographyInfo
                            .getSeparationType();
                }
                ChromatographyInfo chromatographyInfo = MSDKObjectBuilder
                        .getChromatographyInfo1D(separationType,
                                totalRt / rtCount);
                row.setData(chromInfoColumn, chromatographyInfo);
            }
        }
    }

    /**
     * Copies common values such as identification results and comments from the
     * source row to the target row.
     * 
     * @param sourceFeatureTableRow
     *            the source {@link FeatureTableRow} to copy the common values
     *            from.
     * @param newFeatureTableRow
     *            the target {@link FeatureTableRow} to copy the common values
     *            to.
     */
    public static void copyCommonValues(
            @Nonnull FeatureTableRow sourceFeatureTableRow,
            @Nonnull FeatureTableRow targetFeatureTableRow) {

        List<FeatureTableColumn<?>> sourceColumns = sourceFeatureTableRow
                .getFeatureTable().getColumns();
        List<FeatureTableColumn<?>> targetColumns = targetFeatureTableRow
                .getFeatureTable().getColumns();

        for (int i = 0; i < sourceColumns.size(); i++) {
            FeatureTableColumn sourceColumn = sourceColumns.get(i);
            FeatureTableColumn targetColumn = targetColumns.get(i);

            // Only add common values
            if (sourceColumns.get(i).getSample() == null) {
                targetFeatureTableRow.setData(targetColumn,
                        sourceFeatureTableRow.getData(sourceColumn));
            }
        }
    }

    /**
     * Copies sample specific feature values from the source row to the target
     * row.
     * 
     * @param sourceFeatureTableRow
     *            the source {@link FeatureTableRow} to copy the common values
     *            from.
     * @param newFeatureTableRow
     *            the target {@link FeatureTableRow} to copy the common values
     *            to.
     * @param sample
     *            the target {@link Sample}.
     */
    public static void copyFeatureValues(
            @Nonnull FeatureTableRow sourceFeatureTableRow,
            @Nonnull FeatureTableRow targetFeatureTableRow,
            @Nonnull Sample sample) {

        List<FeatureTableColumn<?>> sourceColumns = sourceFeatureTableRow
                .getFeatureTable().getColumns();
        List<FeatureTableColumn<?>> targetColumns = targetFeatureTableRow
                .getFeatureTable().getColumns();

        for (int i = 0; i < sourceColumns.size(); i++) {
            FeatureTableColumn sourceColumn = sourceColumns.get(i);
            FeatureTableColumn targetColumn = targetColumns.get(i);

            // Only add sample specific values
            if (sourceColumns.get(i).getSample() != null) {
                if (sourceColumns.get(i).getSample().equals(sample)) {
                    if (sourceFeatureTableRow.getData(sourceColumn) != null) {
                        targetFeatureTableRow.setData(targetColumn,
                                sourceFeatureTableRow.getData(sourceColumn));
                    }
                }
            }
        }
    }

}
