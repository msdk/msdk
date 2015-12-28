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

import java.lang.reflect.Array;
import java.util.HashMap;

import javax.annotation.Nonnull;

/**
 * A DataPointStore implementation that stores the data points in memory. Use
 * with caution. When DataPointLists are stored or retrieved, they are not
 * referenced but copied, so the original list can be used for other purpose.
 * 
 * The methods of this class are synchronized, therefore it can be safely used
 * by multiple threads.
 */
class MemoryDataStore implements DataPointStore {

    private HashMap<Integer, Object> storageMap = new HashMap<>();

    private int lastStorageId = 0;

    /**
     * {@inheritDoc}
     *
     * Stores new array of data points.
     */
    @Override
    public @Nonnull Object storeData(@Nonnull Object data,
            @Nonnull Integer size) {

        if (storageMap == null)
            throw new IllegalStateException("This object has been disposed");

        // Clone the data for storage
        Class<?> componentType = data.getClass().getComponentType();
        Object clone = Array.newInstance(componentType, size);
        System.arraycopy(data, 0, clone, 0, size);

        // Save the reference to the new array
        synchronized (storageMap) {
            // Increase the storage ID
            lastStorageId++;
            storageMap.put(lastStorageId, clone);
        }

        return lastStorageId;
    }

    /** {@inheritDoc} */
    @Override
    synchronized public void dispose() {
        storageMap = null;
    }

    /** {@inheritDoc} */
    @Override
    public void loadData(@Nonnull Object ID, @Nonnull Object array) {

        if (storageMap == null)
            throw new IllegalStateException("This object has been disposed");

        // Get the stored array
        final Object storedArray = storageMap.get(ID);

        if (storedArray == null)
            throw new IllegalArgumentException(
                    "ID " + ID + " not found in storage");

        if (!array.getClass().isArray())
            throw new IllegalArgumentException(
                    "The provided argument is not an array");

        if (Array.getLength(array) < Array.getLength(storedArray))
            throw new IllegalArgumentException(
                    "The provided array does not fit all loaded objects");

        // Copy the data
        System.arraycopy(storedArray, 0, array, 0,
                Array.getLength(storedArray));

    }

    /** {@inheritDoc} */
    @Override
    public void removeData(@Nonnull Object ID) {

        if (storageMap == null)
            throw new IllegalStateException("This object has been disposed");

        storageMap.remove(ID);
    }

}
