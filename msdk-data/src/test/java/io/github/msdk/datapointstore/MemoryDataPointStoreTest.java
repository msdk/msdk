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

import io.github.msdk.datapointstore.DataPointStore;
import io.github.msdk.datapointstore.MSDKDataStore;

import org.junit.Test;

/**
 * Tests for MemoryDataPointStore
 */
public class MemoryDataPointStoreTest {

    @Test
    public void testStoreReadDataPoints() {

        DataPointStore store = MSDKDataStore.getMemoryDataStore();
        DataPointStoreTest.testStoreReadDataPoints(store);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveDataPoints() {

        DataPointStore store = MSDKDataStore.getMemoryDataStore();
        DataPointStoreTest.testRemoveDataPoints(store);

    }

    @Test(expected = IllegalStateException.class)
    public void testDispose() {

        DataPointStore store = MSDKDataStore.getMemoryDataStore();
        DataPointStoreTest.testDispose(store);

    }

}
