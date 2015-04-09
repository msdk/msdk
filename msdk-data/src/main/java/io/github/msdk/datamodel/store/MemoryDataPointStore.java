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

package io.github.msdk.datamodel.store;

import io.github.msdk.datamodel.MSDKObjectBuilder;
import io.github.msdk.datamodel.rawdata.DataPointList;

import java.util.TreeMap;

import javax.annotation.Nonnull;

/**
 * A DataPointStore implementation that stores the data points in memory. Use
 * with caution.
 * 
 * The methods of this class are synchronized, therefore it can be safely used
 * by multiple threads.
 */
public class MemoryDataPointStore implements DataPointStore {

    private final TreeMap<Integer, DataPointList> dataPointLists = new TreeMap<>();
    private int lastStorageId = 0;

    /**
     * Stores new array of data points.
     * 
     * @return Storage ID for the newly stored data.
     */
    @Override
    synchronized public @Nonnull Integer storeDataPoints(
            @Nonnull DataPointList dataPoints) {

        // Clone the given list
        final DataPointList newList = MSDKObjectBuilder
                .getDataPointList(dataPoints);

        // Increase the storage ID
        lastStorageId++;

        // Save the reference to the new list
        dataPointLists.put(lastStorageId, newList);

        return lastStorageId;

    }

    /**
     * Reads the data points associated with given ID.
     */
    @Override
    synchronized public @Nonnull DataPointList readDataPoints(
            @Nonnull Integer ID) {

        if (!dataPointLists.containsKey(ID))
            throw new IllegalArgumentException("ID " + ID
                    + " not found in storage");

        // Get the stored DataPointList
        final DataPointList storedList = dataPointLists.get(ID);

        // Clone the stored DataPointList
        final DataPointList newList = MSDKObjectBuilder
                .getDataPointList(storedList);

        return newList;
    }

    /**
     * Reads the data points associated with given ID.
     */
    @Override
    synchronized public void readDataPoints(@Nonnull Integer ID,
            @Nonnull DataPointList list) {

        if (!dataPointLists.containsKey(ID))
            throw new IllegalArgumentException("ID " + ID
                    + " not found in storage");

        // Get the stored DataPointList
        final DataPointList storedList = dataPointLists.get(ID);

        // Copy data
        list.copyFrom(storedList);

    }

    /**
     * Remove data associated with given storage ID.
     */
    @Override
    synchronized public void removeDataPoints(@Nonnull Integer ID) {
        dataPointLists.remove(ID);
    }

    @Override
    synchronized public void dispose() {
        dataPointLists.clear();
    }

}
