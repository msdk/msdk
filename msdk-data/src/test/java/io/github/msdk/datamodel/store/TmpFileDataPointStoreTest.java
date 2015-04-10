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

import org.junit.Test;

/**
 * Tests for TmpFileDataPointStore
 */
public class TmpFileDataPointStoreTest {

    @Test
    public void testStoreReadDataPoints() {

        TmpFileDataPointStore store = new TmpFileDataPointStore();
        DataPointStoreTest.testStoreReadDataPoints(store);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveDataPoints() {

        TmpFileDataPointStore store = new TmpFileDataPointStore();
        DataPointStoreTest.testRemoveDataPoints(store);

    }

    @Test(expected = IllegalStateException.class)
    public void testDispose() {

        TmpFileDataPointStore store = new TmpFileDataPointStore();
        DataPointStoreTest.testDispose(store);

    }

}
