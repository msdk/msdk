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

/**
 * <p>
 * Convert data from a source feature table row and column to a target row and
 * column.
 * </p>
 * 
 * @param <DATATYPE>
 *            the generic type of the element values.
 */
public interface FeatureTableDataConverter<DATATYPE> {

    /**
     * Apply the conversion from the given source row and column to the given
     * target row and column.
     * 
     * @param sourceRow
     *            the source data's row.
     * @param sourceColumn
     *            the source data's column.
     * @param targetRow
     *            the target data's row.
     * @param targetColumn
     *            the target data's column.
     */
    void apply(FeatureTableRow sourceRow,
            FeatureTableColumn<? extends DATATYPE> sourceColumn,
            FeatureTableRow targetRow,
            FeatureTableColumn<? extends DATATYPE> targetColumn);

}
