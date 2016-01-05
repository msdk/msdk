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
 * Convert data from a source feature table row and column to a target row and
 * column.
 * 
 * @author nilshoffmann
 * @param <DATATYPE>
 * 
 */
public interface FeatureTableDataConverter<DATATYPE> {

    /**
     * 
     * @param sourceRow
     * @param sourceColumn
     * @param targetRow
     * @param targetColumn
     */
    void apply(FeatureTableRow sourceRow,
            FeatureTableColumn<? extends DATATYPE> sourceColumn,
            FeatureTableRow targetRow,
            FeatureTableColumn<? extends DATATYPE> targetColumn);

}
