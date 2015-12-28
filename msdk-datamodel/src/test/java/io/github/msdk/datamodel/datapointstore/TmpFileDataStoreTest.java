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

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;

/**
 * Tests for TmpFileDataPointStore
 */
public class TmpFileDataStoreTest {

    @SuppressWarnings({ "unused" })
    @Test
    public void testStoreReadDataPoints() {

        DataPointStore store = DataPointStoreFactory.getTmpFileDataStore();

        final int numOfGeneratedLists = 3; // TODO
        final Object storageIds[] = new Object[numOfGeneratedLists];

        int count = 1000;
        final double mzValues[] = new double[count];
        final float intensityValues[] = new float[count];
        for (int i = 0; i < count; i++) {
            mzValues[i] = (double) count / (double) (count - i);
            intensityValues[i] = (float) mzValues[i] * 2;
        }

        Object storageId = store.storeData(mzValues, count);
        Object storageId2 = store.storeData(intensityValues, count);
        Object storageId3 = store.storeData(intensityValues, count);
        Object storageId4 = store.storeData(mzValues, count);

        double readMzValues[] = new double[count * 2];
        float readIntensityValues[] = new float[count * 2];

        // Retrieve
        store.loadData(storageId3, readIntensityValues);
        store.loadData(storageId, readMzValues);

        for (int i = 0; i < count; i++) {
            Assert.assertEquals(mzValues[i], readMzValues[i], 0.000001);
            Assert.assertEquals(intensityValues[i], readIntensityValues[i],
                    0.00001f);
        }

    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testRemoveDataPoints() {

        DataPointStore store = DataPointStoreFactory.getTmpFileDataStore();

        int count = 1000;
        final double mzValues[] = new double[count];
        final float intensityValues[] = new float[count];
        for (int i = 0; i < count; i++) {
            mzValues[i] = (double) count / (double) (count - i);
            intensityValues[i] = (float) mzValues[i] * 2;
        }

        Object storageId = store.storeData(mzValues, count);
        Object storageId2 = store.storeData(intensityValues, count);

        store.removeData(storageId);

        store.loadData(storageId, mzValues);

    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalStateException.class)
    public void testDispose() {

        DataPointStore store = DataPointStoreFactory.getTmpFileDataStore();

        int count = 1000;
        final double mzValues[] = new double[count];
        final float intensityValues[] = new float[count];
        for (int i = 0; i < count; i++) {
            mzValues[i] = (double) count / (double) (count - i);
            intensityValues[i] = (float) mzValues[i] * 2;
        }

        Object storageId = store.storeData(mzValues, count);
        Object storageId2 = store.storeData(intensityValues, count);

        store.dispose();

        store.loadData(storageId, mzValues);

    }

}
