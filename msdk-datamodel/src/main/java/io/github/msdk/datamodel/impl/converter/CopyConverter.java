/*
 * Copyright (C) 2015 nilshoffmann.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package io.github.msdk.datamodel.impl.converter;

import io.github.msdk.MSDKConstraintViolationException;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableDataConverter;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;

/**
 *
 * @author nilshoffmann
 */
public class CopyConverter implements
        FeatureTableDataConverter<Object> {

    @Override
    public void apply(FeatureTableRow sourceRow, FeatureTableColumn<? extends Object> sourceColumn, FeatureTableRow targetRow, FeatureTableColumn<? extends Object> targetColumn) {
        Object data = sourceRow.getData(sourceColumn);
        if (targetColumn.getDataTypeClass().isAssignableFrom(sourceColumn.getDataTypeClass())) {
            if (data == null) {
                throw new NullPointerException("Data for column "
                        + sourceColumn.getName() + " for table row "
                        + sourceRow.getId());
            }
            targetRow.setData(targetColumn, data);
        } else {
            throw new MSDKConstraintViolationException("Target column data type '" + targetColumn.getDataTypeClass().getName() + "' is not assignable from source column data type '" + sourceColumn.getDataTypeClass().getName() + "'.");
        }
    }

}
