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

package io.github.msdk.datamodel.datapointstore;

import javax.annotation.Nonnull;

/**
 * Represents a storage mechanism for data points represented by DataPointList.
 * Each RawDataFile and FeatureTable will use this mechanism to store their data
 * points somewhere, to avoid consuming too much memory. Multiple
 * implementations of this interface may be provided, depending on the method of
 * serialization and storage.
 */
public interface DataPointStore {

    /**
     * Stores new data point list into this store. No reference to the
     * DataPointList is saved, so it can be safely discarded or reused after
     * calling this method.
     *
     * @return Storage ID for the newly stored data.
     * @param data a {@link java.lang.Object} object.
     * @param size a {@link java.lang.Integer} object.
     */
    @Nonnull
    Object storeData(@Nonnull Object data, @Nonnull Integer size);

    /**
     * <p>loadData.</p>
     *
     * @param id a {@link java.lang.Object} object.
     * @param array a {@link java.lang.Object} object.
     */
    void loadData(@Nonnull Object id, @Nonnull Object array);

    /**
     * Discards data points stored under given ID.
     *
     * @param id
     *            Storage id to discard
     */
    void removeData(@Nonnull Object id);

    /**
     * Completely discards this data point store. After this method is called,
     * any subsequent method calls on this object will throw
     * IllegalStateException.
     */
    void dispose();

}
