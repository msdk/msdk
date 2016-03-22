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

package io.github.msdk.datamodel.impl.converter;

import io.github.msdk.MSDKConstraintViolationException;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableDataConverter;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;

/**
 * * <p>
 * Implementation of {@link FeatureTableDataConverter} for any kind of object
 * that should be copied as is to the new feature table.
 * </p>
 */
public class CopyConverter implements FeatureTableDataConverter<Object> {

    /** {@inheritDoc} */
    @Override
    public void apply(FeatureTableRow sourceRow,
            FeatureTableColumn<? extends Object> sourceColumn,
            FeatureTableRow targetRow,
            FeatureTableColumn<? extends Object> targetColumn) {
        Object data = sourceRow.getData(sourceColumn);
        if (targetColumn.getDataTypeClass().isAssignableFrom(
                sourceColumn.getDataTypeClass())) {
            if (data == null) {
                return;
            }
            targetRow.setData(targetColumn, data);
        } else {
            throw new MSDKConstraintViolationException(
                    "Target column data type '"
                            + targetColumn.getDataTypeClass().getName()
                            + "' is not assignable from source column data type '"
                            + sourceColumn.getDataTypeClass().getName() + "'.");
        }
    }

}
