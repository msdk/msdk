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

package io.github.msdk.datamodel.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.msspectra.MsIon;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;

/**
 * Tests for SimpleMSSpectrumDataPointList
 */
public class SimpleMSSpectrumDataPointListTest {

    @Test
    public void testSetSize() throws MSDKException {

        MsSpectrumDataPointList dpl = MSDKObjectBuilder
                .getMsSpectrumDataPointList();
        dpl.allocate(100);

        double mzBuffer[] = dpl.getMzBuffer();
        float intBuffer[] = dpl.getIntensityBuffer();

        for (int i = 0; i < 100; i++) {
            mzBuffer[i] = Math.PI / (i + 1);
            intBuffer[i] = (float) Math.PI * i;
        }

        dpl.setSize(50);

        // Check the size was correctly updated
        Assert.assertEquals(50, dpl.getSize());

        // Change the capacity, just for fun
        dpl.allocate(10000);

        // Check the size has not chaged
        Assert.assertEquals(50, dpl.getSize());

        // Check that the m/z array is properly sorted
        mzBuffer = dpl.getMzBuffer();
        for (int i = 1; i < dpl.getSize(); i++) {
            Assert.assertTrue("array is not sorted",
                    mzBuffer[i] > mzBuffer[i - 1]);
        }

        // Check that the last data point is 0 intensity
        intBuffer = dpl.getIntensityBuffer();
        Assert.assertEquals(0.0f, intBuffer[dpl.getSize() - 1], 0.0001f);

    }

    @Test(expected = MSDKRuntimeException.class)
    public void testFailSetSize() throws MSDKException {

        MsSpectrumDataPointList dpl = MSDKObjectBuilder
                .getMsSpectrumDataPointList();
        dpl.allocate(10);
        dpl.setSize(1000000);

    }

    @Test
    public void testIterator() throws MSDKException {

        MsSpectrumDataPointList dpl = MSDKObjectBuilder
                .getMsSpectrumDataPointList();
        dpl.setBuffers(
                new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0 },
                new float[] { 10f, 20f, 30f, 40f, 50f, 60f, 70f, 80f, 90f }, 5);

        List<Double> mzValues = Lists.newArrayList();
        List<Float> intValues = Lists.newArrayList();
        for (MsIon ion : dpl) {
            mzValues.add(ion.getMz());
            intValues.add(ion.getIntensity());
        }
        Assert.assertEquals(dpl.getSize(), mzValues.size());

        for (int i = 0; i < dpl.getSize(); i++) {
            Assert.assertEquals(dpl.getMzBuffer()[i], mzValues.get(i), 0.00001);
            Assert.assertEquals(dpl.getIntensityBuffer()[i], intValues.get(i),
                    0.00001);
        }
    }

}
