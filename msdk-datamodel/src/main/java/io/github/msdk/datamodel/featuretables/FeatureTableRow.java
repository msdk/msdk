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
package io.github.msdk.datamodel.featuretables;

import io.github.msdk.MSDKConstraintViolationException;
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
     * @return a DATATYPE object.
     */
    @Nullable
    <DATATYPE> DATATYPE getData(@Nonnull FeatureTableColumn<? extends DATATYPE> column);

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
    <DATATYPE> void setData(FeatureTableColumn<? extends DATATYPE> column,
            @Nonnull DATATYPE data);

    /**
     * Copy data from a source column value to a target column in the given
     * target row. The copy operation is performed by the passed in
     * {@code featureTableDataConverter}. See
     * {@link FeatureTableIdentityDataConverter} for the default implementation.
     *
     * @param <DATATYPE>
     *            the generic element data type.
     * @param sourceColumn
     *            the source column from where to copy.
     * @param targetRow
     *            the target row to received the data.
     * @param targetColumn
     *            the target column to which to copy.
     * @param featureTableDataConverter
     *            the data converter, may perform additional operations on the
     *            data.
     * @throws io.github.msdk.MSDKConstraintViolationException
     *             if the provided target column data type is not the same class
     *             or a compatible subclass as the source column data type.
     */
    <DATATYPE> void copyData(FeatureTableColumn<? extends DATATYPE> sourceColumn,
            FeatureTableRow targetRow,
            FeatureTableColumn<? extends DATATYPE> targetColumn,
            FeatureTableDataConverter<DATATYPE> featureTableDataConverter)
            throws MSDKConstraintViolationException;

}
