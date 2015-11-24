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

import java.util.Hashtable;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;

/**
 * Implementation of FeatureTableRow
 */
class SimpleFeatureTableRow implements FeatureTableRow {

    private int rowId;
    private @Nonnull FeatureTable featureTable;
    private @Nonnull Hashtable<FeatureTableColumn<?>, Object> rowData;

    SimpleFeatureTableRow(@Nonnull FeatureTable featureTable, int rowId) {
        Preconditions.checkNotNull(featureTable);
        this.featureTable = featureTable;
        this.rowId = rowId;
        rowData = new Hashtable<>();
    }

    /** {@inheritDoc} */
    @Override
    public @Nonnull FeatureTable getFeatureTable() {
        return featureTable;
    }

    /** {@inheritDoc} */
    @Override
    public @Nonnull Integer getId() {
        return rowId;
    }

    /** {@inheritDoc} */
    @Override
    public Double getMz() {
        return getData(MSDKObjectBuilder.getMzFeatureTableColumn(),
                Double.class);
    }

    /** {@inheritDoc} */
    @Override
    public ChromatographyInfo getChromatographyInfo() {
        return getData(
                MSDKObjectBuilder.getChromatographyInfoFeatureTableColumn(),
                ChromatographyInfo.class);
    }

    /** {@inheritDoc} */
    @Override
    public <DataType> void setData(FeatureTableColumn<DataType> column,
            @Nonnull DataType data) {
        Preconditions.checkNotNull(column);
        Preconditions.checkNotNull(data);
        rowData.put(column, data);
    }

    /** {@inheritDoc} */
    @Override
    public <DataType> DataType getData(
            @Nonnull FeatureTableColumn<DataType> column,
            @Nonnull Class<? extends DataType> dtClass) {
        Preconditions.checkNotNull(column);
        return dtClass.cast(rowData.get(column));
    }

}
