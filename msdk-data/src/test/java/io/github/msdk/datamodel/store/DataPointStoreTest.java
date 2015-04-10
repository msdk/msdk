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
import io.github.msdk.datamodel.MSDKObjectBuilder;
import io.github.msdk.datamodel.rawdata.DataPoint;
import io.github.msdk.datamodel.rawdata.DataPointList;

import org.junit.Assert;

/**
 * Tests for DataPointStores
 */
public class DataPointStoreTest {

    public static void testStoreReadDataPoints(DataPointStore store) {

        final int numOfGeneratedLists = 3; //TODO
        final Object storageIds[] = new Object[numOfGeneratedLists];

        for (int i = 0; i < numOfGeneratedLists; i++) {
            DataPointList dataPoints = DataModelTestTools.generateDataPoints(i);
            storageIds[i] = store.storeDataPoints(dataPoints);
        }

        DataPointList tmpList = MSDKObjectBuilder.getDataPointList();

        for (int i = 0; i < numOfGeneratedLists; i++) {

            // Retrieve method 1
            DataPointList retrievedDataPoints = store
                    .readDataPoints(storageIds[i]);

            // Retrieve method 2
            store.readDataPoints(storageIds[i], tmpList);

            // Assert that retrieved lists are equal
            Assert.assertEquals(retrievedDataPoints, tmpList);

            // Check if the size of the retrieved list matches the initially
            // stored size
            Assert.assertEquals(i, retrievedDataPoints.size());

            if (i > 0) {
                // Check if the intensity value of the last data point matches
                // the specification of generateDataPoints()
                DataPoint lastDp = retrievedDataPoints.get(i - 1);
                Assert.assertEquals((double) i * 2, lastDp.getIntensity(),
                        0.00001);
            }
        }

    }

    public static void testRemoveDataPoints(DataPointStore store) {

        DataPointList dataPoints = DataModelTestTools.generateDataPoints(1000);
        Object storageId = store.storeDataPoints(dataPoints);

        store.removeDataPoints(storageId);

        store.readDataPoints(storageId);

    }

    public static void testDispose(DataPointStore store) {

        DataPointList dataPoints = DataModelTestTools.generateDataPoints(1000);
        Object storageId = store.storeDataPoints(dataPoints);
        store.dispose();
        store.readDataPoints(storageId);

    }

}
