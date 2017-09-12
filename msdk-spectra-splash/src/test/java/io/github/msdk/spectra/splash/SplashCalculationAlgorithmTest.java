/*
 * (C) Copyright 2015-2017 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1 as published by the Free
 * Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation.
 */

package io.github.msdk.spectra.splash;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;

public class SplashCalculationAlgorithmTest {

  @Test
  public void testSplash1() throws MSDKException {

    double mzValues[] = new double[] {100.0, 101.0, 102.0};
    float intValues[] = new float[] {1.0f, 2.0f, 3.0f};

    final String correctSplash = "splash10-0udi-0900000000-f5bf6f6a4a1520a35d4f";
    final String calculatedSplash =
        SplashCalculationAlgorithm.calculateSplash(mzValues, intValues, mzValues.length);

    Assert.assertEquals(correctSplash, calculatedSplash);

  }

  @Test
  public void testSplash2() throws MSDKException {

    double mzValues[] = new double[] {100.0};
    float intValues[] = new float[] {50f};

    final String correctSplash = "splash10-0udi-0900000000-cc0ed451a7eca3bcb4a6";
    final String calculatedSplash =
        SplashCalculationAlgorithm.calculateSplash(mzValues, intValues, mzValues.length);

    Assert.assertEquals(correctSplash, calculatedSplash);

  }
}
