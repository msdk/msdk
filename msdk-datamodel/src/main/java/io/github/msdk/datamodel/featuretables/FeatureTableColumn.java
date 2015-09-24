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
 * 
 *
 * @param <DataType>
 */
public interface FeatureTableColumn<DataType> {

    /**
     * @return Short descriptive name for the feature table column
     */
    @Nonnull String getName();

    /**
     * Change the name of this feature table column
     */
    void setName(@Nonnull String name);

    /**
     * @return
     */
    @Nonnull Class<DataType> getDataTypeClass();

    /**
     * Returns the sample associated with this column, or null if no sample is associated.
     */
    @Nullable Sample getSample();
    
}
