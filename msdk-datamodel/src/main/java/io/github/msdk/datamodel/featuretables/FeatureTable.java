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
package io.github.msdk.datamodel.featuretables;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * A feature table consists of a list of named columns, each with a specific
 * type for the values contained in it.
 */
@NotThreadSafe
public interface FeatureTable {

    /**
     * <p>getName.</p>
     *
     * @return Short descriptive name for the feature table
     */
    @Nonnull
    String getName();

    /**
     * Change the name of this feature table.
     *
     * @param name
     *            the name of the feature table.
     */
    void setName(@Nonnull String name);

    /**
     * Returns an immutable list of columns
     *
     * @return the list of feature table columns.
     */
    @Nonnull
    List<FeatureTableColumn<?>> getColumns();

    /**
     * Returns a specific column for the given {@code columnName}. May return
     * {@code null} if no column for the given name is known.
     *
     * @param <DATATYPE>
     *            the generic datatype of the column's entries.
     * @param columnName
     *            the name of the column.
     * @param sample
     *            the {@link Sample}.
     * @param dtType
     *            the class of the column' datatype.
     *
     * @return the feature table column for the given name, or null.
     */
    @Nullable
    <DATATYPE> FeatureTableColumn<DATATYPE> getColumn(
            @Nonnull String columnName, Sample sample,
            Class<? extends DATATYPE> dtType);

    /**
     * Returns a specific column for the given name.
     *
     * @param <DATATYPE>
     *            the generic datatype of the column's entries.
     * @param columnName
     *            the {@link ColumnName} of the column.
     * @param sample
     *            the {@link Sample}.
     * @return the {@link FeatureTableColumn} for the given name and sample.
     */
    @Nullable
    <DATATYPE> FeatureTableColumn<DATATYPE> getColumn(
            @Nonnull ColumnName columnName, Sample sample);

    /**
     * Add a new column to the feature table.
     *
     * @param col
     *            the {@link FeatureTableColumn} to add.
     */
    void addColumn(@Nonnull FeatureTableColumn<?> col);

    /**
     * Removes a column from this feature table.
     *
     * @param col
     *            the {@link FeatureTableColumn} to remove.
     *
     */
    void removeColumn(@Nonnull FeatureTableColumn<?> col);

    /**
     * Returns an immutable list of rows
     *
     * @return a list of {@link FeatureTableRow}s.
     */
    @Nonnull
    List<FeatureTableRow> getRows();

    /**
     * Add a new row to the feature table.
     *
     * @param row
     *            the {@link FeatureTableRow} to add.
     */
    void addRow(@Nonnull FeatureTableRow row);

    /**
     * Removes a row from this feature table.
     *
     * @param row
     *            the {@link FeatureTableRow} to remove.
     *
     */
    void removeRow(@Nonnull FeatureTableRow row);

    /**
     * Shortcut to return an immutable list of {@link Sample}s found in this
     * feature table.
     *
     * @return the list of samples.
     */
    @Nonnull
    List<Sample> getSamples();

    /**
     * Remove all data associated to this feature table.
     */
    void dispose();

}
