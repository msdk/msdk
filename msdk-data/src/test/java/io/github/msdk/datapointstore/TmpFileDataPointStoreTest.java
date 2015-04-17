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

import io.github.msdk.MSDKException;
import io.github.msdk.datapointstore.DataPointStore;
import io.github.msdk.datapointstore.MSDKDataStore;

import org.junit.Test;

/**
 * Tests for TmpFileDataPointStore
 */
public class TmpFileDataPointStoreTest {

    @Test
    public void testStoreReadDataPoints() throws MSDKException {

        DataPointStore store = MSDKDataStore.getTmpFileDataPointStore();
        DataPointStoreTest.testStoreReadDataPoints(store);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveDataPoints() throws MSDKException {

        DataPointStore store = MSDKDataStore.getTmpFileDataPointStore();
        DataPointStoreTest.testRemoveDataPoints(store);

    }

    @Test(expected = IllegalStateException.class)
    public void testDispose() throws MSDKException {

        DataPointStore store = MSDKDataStore.getTmpFileDataPointStore();
        DataPointStoreTest.testDispose(store);

    }

}
