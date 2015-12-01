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

/**
 * A feature table column has a name, a fixed type for its element values, and
 * an optional {@link Sample} associated to it.
 *
 * @param <DATATYPE>
 *            the generic type of the element values.
 */
public interface FeatureTableColumn<DATATYPE> {

    /**
     * Returns the column's name.
     *
     * @return Short descriptive name for the feature table column.
     */
    @Nonnull
    String getName();

    /**
     * Change the name of this feature table column.
     *
     * @param name
     *            the column's name.
     */
    void setName(@Nonnull String name);

    /**
     * Returns the class of the values contained in this column.
     *
     * @return the class of the column' datatype.
     */
    @Nonnull
    Class<DATATYPE> getDataTypeClass();

    /**
     * Returns the sample associated with this column, or null if no sample is
     * associated.
     *
     * @return the sample associated to this column, or null.
     */
    @Nullable
    Sample getSample();

}
