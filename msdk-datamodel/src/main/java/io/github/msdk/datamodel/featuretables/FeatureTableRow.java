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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.datamodel.rawdata.ChromatographyInfo;

/**
 * <p>
 * FeatureTableRow interface.
 * </p>
 */
public interface FeatureTableRow {

    /**
     * Returns the feature table where this feature table row belongs. Each
     * feature table row is assigned to exactly one feature table.
     *
     * @return the feature table.
     */
    @Nonnull
    FeatureTable getFeatureTable();

    /**
     * Shortcut to return the ID column value of this row
     *
     * @return the ID column value of this row.
     */
    @Nonnull
    Integer getId();

    /**
     * Shortcut to return the m/z column value of this row
     *
     * @return the m/z column value of this row.
     */
    @Nullable
    Double getMz();

    /**
     * Shortcut to return the chromatography info (=retention time etc.) column
     * value of this row.
     *
     * @return the {@link ChromatographyInfo}.
     */
    @Nullable
    ChromatographyInfo getChromatographyInfo();

    /**
     * Return data assigned to this row
     *
     * @param <DATATYPE>
     *            Generic data type of the column.
     * @param column
     *            the column to retrieve data from this row.
     *
     * @return a DATATYPE object.
     */
    @Nullable
    <DATATYPE> DATATYPE getData(@Nonnull FeatureTableColumn<DATATYPE> column);

    /**
     * Return data assigned to this row
     *
     * @param <DATATYPE>
     *            Generic data type of the column.
     * @param column
     *            a
     *            {@link io.github.msdk.datamodel.featuretables.FeatureTableColumn}
     *            object.
     * @param data
     *            a DATATYPE object.
     */
    <DATATYPE> void setData(FeatureTableColumn<DATATYPE> column,
            @Nonnull DATATYPE data);

}
