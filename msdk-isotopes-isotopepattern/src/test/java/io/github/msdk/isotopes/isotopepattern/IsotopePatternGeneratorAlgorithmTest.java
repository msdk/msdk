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

package io.github.msdk.isotopes.isotopepattern;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.isotopes.isotopepattern.IsotopePatternGeneratorAlgorithm;

public class IsotopePatternGeneratorAlgorithmTest {

  @Test
  public void testC10() throws MSDKException {

    String formula = "C10";

    MsSpectrum pattern =
        IsotopePatternGeneratorAlgorithm.generateIsotopes(formula, 0.0001, 1000.0f, 0.001);

    Assert.assertEquals(new Integer(4), pattern.getNumberOfDataPoints());
    Assert.assertEquals(1000.0f, pattern.getIntensityValues()[0], 0.0001);
    Assert.assertEquals(123.01006452, pattern.getMzValues()[3], 0.0000001);
    Assert.assertEquals(5.264099f, pattern.getIntensityValues()[2], 0.0001);

  }

  @Test
  public void testC39H60N14O14() throws MSDKException {
    String formula = "C39H60N14O14";

    MsSpectrum pattern =
        IsotopePatternGeneratorAlgorithm.generateIsotopes(formula, 0.01, 1.0f, 0.1);

    Assert.assertEquals(new Integer(4), pattern.getNumberOfDataPoints());
    Assert.assertEquals(1.0f, pattern.getIntensityValues()[0], 0.000001);
    Assert.assertEquals(951.4501830990874, pattern.getMzValues()[3], 0.00001);
  }

  @Ignore // Test ignored because it fails when executed on Travis - see
          // https://github.com/cdk/cdk/pull/196 for details
  @Test
  public void testC20H30Fe2P2S4Cl4() throws MSDKException {

    String formula = "C20H30Fe2P2S4Cl4";

    MsSpectrum pattern =
        IsotopePatternGeneratorAlgorithm.generateIsotopes(formula, 0.01, 1000.0f, 0.0);

    Assert.assertEquals(new Integer(35), pattern.getNumberOfDataPoints());
    Assert.assertEquals(1000.0f, pattern.getIntensityValues()[10], 0.000001);
    Assert.assertEquals(713.8128938499999, pattern.getMzValues()[10], 0.0000001);

  }

  @Test
  public void testF1000() throws MSDKException {

    String formula = "F1000";

    MsSpectrum pattern =
        IsotopePatternGeneratorAlgorithm.generateIsotopes(formula, 0.001, 1000.0f, 0.001);

    Assert.assertEquals(new Integer(1), pattern.getNumberOfDataPoints());
    Assert.assertEquals(1000.0f, pattern.getIntensityValues()[0], 0.000001);
    Assert.assertEquals(18998.40322, pattern.getMzValues()[0], 0.0000001);

  }

  @Test
  public void testCharge1Pos() throws MSDKException {

    String formula = "C+";

    MsSpectrum pattern =
        IsotopePatternGeneratorAlgorithm.generateIsotopes(formula, 0.01, 1000.0f, 0.005);

    Assert.assertEquals(new Integer(2), pattern.getNumberOfDataPoints());
    Assert.assertEquals(1000.0f, pattern.getIntensityValues()[0], 0.000001);
    Assert.assertEquals(11.99945142009, pattern.getMzValues()[0], 0.0000001);

  }

  @Test
  public void testCharge2Pos() throws MSDKException {

    String formula = "[C]2+";

    MsSpectrum pattern =
        IsotopePatternGeneratorAlgorithm.generateIsotopes(formula, 0.01, 1000.0f, 0.0);

    Assert.assertEquals(new Integer(2), pattern.getNumberOfDataPoints());
    Assert.assertEquals(1000.0f, pattern.getIntensityValues()[0], 0.000001);
    Assert.assertEquals(5.99945142, pattern.getMzValues()[0], 0.0000001);

  }

  @Test
  public void testCharge2Neg() throws MSDKException {

    String formula = "[C]2-";

    MsSpectrum pattern =
        IsotopePatternGeneratorAlgorithm.generateIsotopes(formula, 0.01, 1000.0f, 0.0);

    Assert.assertEquals(new Integer(2), pattern.getNumberOfDataPoints());
    Assert.assertEquals(1000.0f, pattern.getIntensityValues()[0], 0.000001);
    Assert.assertEquals(6.00054858, pattern.getMzValues()[0], 0.0000001);

  }

}
