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

import io.github.msdk.datamodel.DataModelTestTools;
import io.github.msdk.datamodel.rawdata.DataPoint;
import io.github.msdk.datamodel.rawdata.DataPointList;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for MemoryDataPointStore
 */
public class MemoryDataPointStoreTest {

    @Test
    public void testStoreReadDataPoints() {

        MemoryDataPointStore store = new MemoryDataPointStore();

        DataPointList dataPoints = DataModelTestTools.generateDataPoints(5000);

        Integer storedId = store.storeDataPoints(dataPoints);

        DataPointList retrievedDataPoints = store.readDataPoints(storedId);
        Assert.assertEquals(3, retrievedDataPoints.size());

        // Check if the intensity value matches the specification of
        // DataModelTestTools.generateDataPoints()
        DataPoint lastDp = retrievedDataPoints.get(4999);
        Assert.assertEquals(5000.0 * 2, lastDp.getIntensity(), 0.00001);

    }

    @Test
    public void testRemoveDataPoints() {

    }

    @Test
    public void testDispose() {

    }

}
