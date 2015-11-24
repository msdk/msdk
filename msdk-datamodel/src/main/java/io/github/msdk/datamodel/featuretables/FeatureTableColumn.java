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
 * <p>FeatureTableColumn interface.</p>
 *
 * @param <DATATYPE> Data type of this column
 */
public interface FeatureTableColumn<DATATYPE> {

    /**
     * <p>getName.</p>
     *
     * @return Short descriptive name for the feature table column
     */
    @Nonnull String getName();

    /**
     * Change the name of this feature table column
     *
     * @param name a {@link java.lang.String} object.
     */
    void setName(@Nonnull String name);

    /**
     * <p>getDataTypeClass.</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    @Nonnull Class<DATATYPE> getDataTypeClass();

    /**
     * Returns the sample associated with this column, or null if no sample is associated.
     *
     * @return a {@link io.github.msdk.datamodel.featuretables.Sample} object.
     */
    @Nullable Sample getSample();
    
}
