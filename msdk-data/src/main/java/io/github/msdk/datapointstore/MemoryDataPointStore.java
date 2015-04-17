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

import io.github.msdk.datamodel.MSDKObjectBuilder;
import io.github.msdk.datamodel.rawdata.DataPointList;
import io.github.msdk.datapointstore.DataPointStore;

import java.util.HashMap;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DataPointStore implementation that stores the data points in memory. Use
 * with caution. When DataPointLists are stored or retrieved, they are not
 * referenced but copied, so the original list can be used for other purpose.
 * 
 * The methods of this class are synchronized, therefore it can be safely used
 * by multiple threads.
 */
class MemoryDataPointStore implements DataPointStore {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private HashMap<Object, DataPointList> dataPointLists = new HashMap<>();

    private int lastStorageId = 0;

    /**
     * Stores new array of data points.
     * 
     * @return Storage ID for the newly stored data.
     */
    @Override
    synchronized public @Nonnull Integer storeDataPoints(
            @Nonnull DataPointList dataPoints) {

        if (dataPointLists == null)
            throw new IllegalStateException("This object has been disposed");

        // Clone the given list for storage
        final DataPointList newList = MSDKObjectBuilder
                .getDataPointList(dataPoints);

        // Increase the storage ID
        lastStorageId++;

        logger.debug("Storing " + dataPoints.size() + " data points under id "
                + lastStorageId);

        // Save the reference to the new list
        dataPointLists.put(lastStorageId, newList);

        return lastStorageId;

    }

    /**
     * Reads the data points associated with given ID.
     */
    @Override
    synchronized public @Nonnull DataPointList readDataPoints(@Nonnull Object ID) {

        if (dataPointLists == null)
            throw new IllegalStateException("This object has been disposed");

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
    synchronized public void readDataPoints(@Nonnull Object ID,
            @Nonnull DataPointList list) {

        if (dataPointLists == null)
            throw new IllegalStateException("This object has been disposed");

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
    synchronized public void removeDataPoints(@Nonnull Object ID) {

        if (dataPointLists == null)
            throw new IllegalStateException("This object has been disposed");

        dataPointLists.remove(ID);
    }

    @Override
    synchronized public void dispose() {
        dataPointLists = null;
    }

}
