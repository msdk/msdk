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

package io.github.msdk.datamodel.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;

/**
 * Implementation of the FeatureTable interface.
 */
class SimpleFeatureTable implements FeatureTable {

    private @Nonnull String name;
    private @Nonnull DataPointStore dataPointStore;
    private final @Nonnull ArrayList<FeatureTableRow> featureTableRows;
    private final @Nonnull ArrayList<FeatureTableColumn<?>> featureTableColumns;

    SimpleFeatureTable(@Nonnull String name,
            @Nonnull DataPointStore dataPointStore) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(dataPointStore);
        this.name = name;
        this.dataPointStore = dataPointStore;
        featureTableRows = new ArrayList<FeatureTableRow>();
        featureTableColumns = new ArrayList<FeatureTableColumn<?>>();
    }

    /** {@inheritDoc} */
    @Override
    public @Nonnull String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public void setName(@Nonnull String name) {
        Preconditions.checkNotNull(name);
        this.name = name;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("null")
    @Override
    public @Nonnull List<FeatureTableRow> getRows() {
        List<FeatureTableRow> featureTableRowCopy = ImmutableList
                .copyOf(featureTableRows);
        return featureTableRowCopy;
    }

    /** {@inheritDoc} */
    @Override
    public void addRow(@Nonnull FeatureTableRow row) {
        Preconditions.checkNotNull(row);
        synchronized (featureTableRows) {
            featureTableRows.add(row);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeRow(@Nonnull FeatureTableRow row) {
        Preconditions.checkNotNull(row);
        synchronized (featureTableRows) {
            featureTableRows.remove(row);
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings("null")
    @Override
    public @Nonnull List<FeatureTableColumn<?>> getColumns() {
        List<FeatureTableColumn<?>> featureTableColumnsCopy = ImmutableList
                .copyOf(featureTableColumns);
        return featureTableColumnsCopy;
    }

    /** {@inheritDoc} */
    @Override
    public <DATATYPE> FeatureTableColumn<DATATYPE> getColumn(
            @Nonnull String columnName, Sample sample,
            Class<? extends DATATYPE> dtClass) {
        for (FeatureTableColumn<?> column : featureTableColumns) {
            if (column.getName().equals(columnName)) {

                if (column.getSample() == null) {
                    if (sample == null)
                        return (FeatureTableColumn<DATATYPE>) column;
                } else if (column.getSample().equals(sample)) {
                    return (FeatureTableColumn<DATATYPE>) column;
                }

            }

        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public <DATATYPE> FeatureTableColumn<DATATYPE> getColumn(
            @Nonnull ColumnName columnName, Sample sample) {
        FeatureTableColumn<?> column = getColumn(columnName.getName(), sample,
                columnName.getDataTypeClass());
        if (column != null) {
            return (FeatureTableColumn<DATATYPE>) column;
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void addColumn(@Nonnull FeatureTableColumn<?> col) {
        Preconditions.checkNotNull(col);
        synchronized (featureTableColumns) {
            featureTableColumns.add(col);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeColumn(@Nonnull FeatureTableColumn<?> col) {
        Preconditions.checkNotNull(col);
        synchronized (featureTableColumns) {
            featureTableColumns.remove(col);
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings("null")
    @Override
    public @Nonnull List<Sample> getSamples() {
        ArrayList<Sample> sampleList = new ArrayList<Sample>();
        synchronized (featureTableColumns) {
            for (FeatureTableColumn<?> col : featureTableColumns) {
                Sample s = col.getSample();
                if (s != null && !sampleList.contains(s))
                    sampleList.add(s);
            }
        }
        return ImmutableList.copyOf(sampleList);
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        dataPointStore.dispose();
    }

    /** {@inheritDoc} */
    @Override
    public List<FeatureTableRow> getRowsInsideRange(Range<Double> rtRange,
            Range<Double> mzRange) {
        List<FeatureTableRow> result = new ArrayList<FeatureTableRow>();
        for (FeatureTableRow row : featureTableRows) {
            ChromatographyInfo rowChromatographyInfo = row
                    .getChromatographyInfo();
            if (rtRange
                    .contains((double) rowChromatographyInfo.getRetentionTime())
                    && mzRange.contains(row.getMz()))
                result.add(row);
        }
        return result;
    }

}
