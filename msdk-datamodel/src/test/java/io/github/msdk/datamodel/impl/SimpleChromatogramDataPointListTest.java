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

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.chromatograms.ChromatogramDataPointList;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.SeparationType;

/**
 * Tests for SimpleChromatogramDataPointList
 */
public class SimpleChromatogramDataPointListTest {

    @Test
    public void testSetSize() throws MSDKException {

        ChromatogramDataPointList dpl = MSDKObjectBuilder
                .getChromatogramDataPointList();
        dpl.allocate(100);

        ChromatographyInfo rtBuffer[] = dpl.getRtBuffer();
        float intBuffer[] = dpl.getIntensityBuffer();

        for (int i = 0; i < 100; i++) {
            rtBuffer[i] = MSDKObjectBuilder.getChromatographyInfo1D(
                    SeparationType.UNKNOWN, (float) Math.PI / (i + 1));
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
        rtBuffer = dpl.getRtBuffer();
        for (int i = 1; i < dpl.getSize(); i++) {
            Assert.assertTrue(rtBuffer[i].compareTo(rtBuffer[i - 1]) > 0);
        }

        // Check that the last data point is 0 intensity
        intBuffer = dpl.getIntensityBuffer();
        Assert.assertEquals(0.0f, intBuffer[dpl.getSize() - 1], 0.0001f);

    }

    @Test(expected = MSDKRuntimeException.class)
    public void testFailSetSize() throws MSDKException {

        ChromatogramDataPointList dpl = MSDKObjectBuilder
                .getChromatogramDataPointList();
        dpl.allocate(10);
        dpl.setSize(1000000);

    }
}
