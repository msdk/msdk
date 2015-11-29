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


import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Implementation of FeatureTableRow. Backed by a non-thread safe Map.
 */
@NotThreadSafe
class SimpleFeatureTableRow implements FeatureTableRow {

    private final int rowId;
    private final @Nonnull FeatureTable featureTable;
    private final @Nonnull Map<FeatureTableColumn<?>, Object> rowData;

    SimpleFeatureTableRow(@Nonnull FeatureTable featureTable, int rowId) {
        Preconditions.checkNotNull(featureTable);
        this.featureTable = featureTable;
        this.rowId = rowId;
        rowData = new HashMap<>();
    }

    @Override
    public @Nonnull FeatureTable getFeatureTable() {
        return featureTable;
    }

    @Override
    public @Nonnull Integer getId() {
        return rowId;
    }

    @Override
    public Double getMz() {
        return getData(MSDKObjectBuilder.getMzFeatureTableColumn());
    }

    @Override
    public ChromatographyInfo getChromatographyInfo() {
        return getData(
                MSDKObjectBuilder.getChromatographyInfoFeatureTableColumn());
    }

    @Override
    public <DataType> void setData(FeatureTableColumn<DataType> column,
            @Nonnull DataType data) {
        Preconditions.checkNotNull(column);
        Preconditions.checkNotNull(data);
        rowData.put(column, data);
    }

    @Override
    public <DataType> DataType getData(
            @Nonnull FeatureTableColumn<DataType> column) {
        Preconditions.checkNotNull(column);
        return column.getDataTypeClass().cast(rowData.get(column));
    }

}
