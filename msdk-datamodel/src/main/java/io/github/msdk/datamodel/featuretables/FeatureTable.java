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

/**
 * This data structure is not thread-safe.
 */
public interface FeatureTable {

    /**
     * @return Short descriptive name for the feature table
     */
    @Nonnull
    String getName();

    /**
     * Change the name of this feature table
     */
    void setName(@Nonnull String name);

    /**
     * Returns an immutable list of columns
     */
    @Nonnull
    List<FeatureTableColumn<?>> getColumns();

    /**
     * Returns an immutable list of columns
     */
    @Nullable
    FeatureTableColumn<?> getColumn(@Nonnull String columnName, Sample sample);

    /**
     * Add a new column to the feature table
     */
    void addColumn(@Nonnull FeatureTableColumn<?> col);

    /**
     * Removes a column from this feature table
     * 
     */
    void removeColumn(@Nonnull FeatureTableColumn<?> col);

    /**
     * Returns an immutable list of rows
     */
    @Nonnull
    List<FeatureTableRow> getRows();

    /**
     * Add a new row to the feature table
     */
    void addRow(@Nonnull FeatureTableRow row);

    /**
     * Removes a row from this feature table
     * 
     */
    void removeRow(@Nonnull FeatureTableRow row);

    /**
     * Shortcut to return an immutable list of samples found in this feature table
     */
    @Nonnull
    List<Sample> getSamples();

    /**
     * Remove all data associated to this feature table from the disk
     */
    void dispose();

}
