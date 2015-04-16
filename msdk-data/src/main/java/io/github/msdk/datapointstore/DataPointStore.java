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

package io.github.msdk.datapointstore;

import io.github.msdk.datamodel.rawdata.DataPointList;

import javax.annotation.Nonnull;

/**
 * Represents a storage mechanism for data points represented by DataPointList.
 * Each RawDataFile and PeakList will use this mechanism to store their data
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
     * @param dataPoints
     *            Data points to store.
     * @return Storage ID for the newly stored data.
     */
    @Nonnull
    Object storeDataPoints(@Nonnull DataPointList dataPoints);

    /**
     * Reads the data points associated with given ID. Returns a newly created
     * DataPointList.
     * 
     * @param id
     *            Storage id obtained by storeDataPoints()
     * @return New DataPointList
     * @throws IllegalIllegalArgumentException
     *             If the given id is not present in this store.
     * 
     */
    @Nonnull
    DataPointList readDataPoints(@Nonnull Object id);

    /**
     * Reads the data points associated with given ID into a given
     * DataPointList. If the data point list does not have enough space, it is
     * expanded accordingly.
     *
     * @param id
     *            Storage id obtained by storeDataPoints()
     * @param list
     *            List to store the loaded data points
     * @throws IllegalIllegalArgumentException
     *             If the given id is not present in this store.
     */
    void readDataPoints(@Nonnull Object id, @Nonnull DataPointList list);

    /**
     * Discards data points stored under given ID.
     * 
     * @param id
     *            Storage id to discard
     */
    void removeDataPoints(@Nonnull Object id);

    /**
     * Completely discards this data point store. After this method is called,
     * any subsequent method calls on this object will throw
     * IllegalStateException.
     */
    void dispose();

}
