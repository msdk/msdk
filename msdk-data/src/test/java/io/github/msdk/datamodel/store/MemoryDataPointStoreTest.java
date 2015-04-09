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

        DataPointList dataPoints = MSDKObjectBuilder.getDataPointList();
        dataPoints.add(MSDKObjectBuilder.getDataPoint(10.0, 20.0f));
        dataPoints.add(MSDKObjectBuilder.getDataPoint(30.0, 40.0f));
        dataPoints.add(MSDKObjectBuilder.getDataPoint(50.0, 60.0f));

        Integer storedId = store.storeDataPoints(dataPoints);

        DataPointList retrievedDataPoints = store.readDataPoints(storedId);
        Assert.assertEquals(3, retrievedDataPoints.size());

        DataPoint lastDp = retrievedDataPoints.get(2);
        Assert.assertEquals(60.0, lastDp.getIntensity(), 0.00001);

    }

    @Test
    public void testRemoveDataPoints() {

    }

    @Test
    public void testDispose() {

    }

}
