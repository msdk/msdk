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

package io.github.msdk.spectra.splash;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;

public class SplashCalculatorTest {

    @Test
    public void testSplash() throws MSDKException {

        MsSpectrumDataPointList dataPoints = MSDKObjectBuilder
                .getMsSpectrumDataPointList();
        dataPoints.allocate(10);
        double mzValues[] = new double[] { 100.0, 101.0, 102.0 };
        float intValues[] = new float[] { 1.0f, 2.0f, 3.0f };
        dataPoints.setBuffers(mzValues, intValues, mzValues.length);

        final String correctSplash = "splash10-0z00000000-f5bf6f6a4a1520a35d4f";
        final String calculatedSplash = SplashCalculator
                .calculateSplash(dataPoints);

        Assert.assertEquals(correctSplash, calculatedSplash);

    }
}
