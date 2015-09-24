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
 * 
 */
public interface FeatureTableRow {

    /**
     * Returns the feature table where this feature table row belongs. Each
     * feature table row is assigned to exactly one feature table.
     */
    @Nonnull
    FeatureTable getFeatureTable();

    /**
     * Shortcut to return the ID column value of this row
     */
    @Nonnull
    Integer getId();

    /**
     * Shortcut to return the m/z column value of this row
     */
    @Nullable
    Double getMz();

    /**
     * Shortcut to return the chromatography info (=retention time etc.) column
     * value of this row
     */
    @Nullable
    ChromatographyInfo getChromatographyInfo();

    /**
     * Return data assigned to this row
     */
    @Nullable
    <DataType> DataType getData(@Nonnull FeatureTableColumn<DataType> column);

    /**
     * Return data assigned to this row
     */
    <DataType> void setData(FeatureTableColumn<DataType> column,
            @Nonnull DataType data);

}
