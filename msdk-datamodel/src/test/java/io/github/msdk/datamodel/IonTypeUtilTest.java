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

package io.github.msdk.datamodel;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.util.IonTypeUtil;

/**
 * Tests for IonTypeUtil
 */
public class IonTypeUtilTest {

  @Test
  public void test1() {
    IonType t = IonTypeUtil.createIonType("[3M+2H]2+");
    Assert.assertEquals(3, t.getNumberOfMolecules());
    Assert.assertEquals("H2", t.getAdductFormula());
    Assert.assertEquals(Integer.valueOf(2), t.getCharge());
    Assert.assertEquals(PolarityType.POSITIVE, t.getPolarity());
  }

  @Test
  public void test2() {
    IonType t = IonTypeUtil.createIonType("[2M+B-2H]-");
    Assert.assertEquals(2, t.getNumberOfMolecules());
    Assert.assertEquals("H-2B", t.getAdductFormula());
    Assert.assertEquals(Integer.valueOf(1), t.getCharge());
    Assert.assertEquals(PolarityType.NEGATIVE, t.getPolarity());
  }

  @Test
  public void test3() {
    IonType t = IonTypeUtil.createIonType("[M]+");
    Assert.assertEquals(1, t.getNumberOfMolecules());
    Assert.assertEquals("", t.getAdductFormula());
    Assert.assertEquals(Integer.valueOf(1), t.getCharge());
    Assert.assertEquals(PolarityType.POSITIVE, t.getPolarity());
  }

  @Test
  public void test4() {
    IonType t = IonTypeUtil.createIonType("[M-5H]5-");
    Assert.assertEquals(1, t.getNumberOfMolecules());
    Assert.assertEquals("H-5", t.getAdductFormula());
    Assert.assertEquals(Integer.valueOf(5), t.getCharge());
    Assert.assertEquals(PolarityType.NEGATIVE, t.getPolarity());
  }

  @Test
  public void test5() {
    IonType t = IonTypeUtil.createIonType("[2M-H2O+NH4]+");
    Assert.assertEquals(2, t.getNumberOfMolecules());
    Assert.assertEquals("H2NO-1", t.getAdductFormula());
    Assert.assertEquals(Integer.valueOf(1), t.getCharge());
    Assert.assertEquals(PolarityType.POSITIVE, t.getPolarity());
  }

  @Test(expected = MSDKRuntimeException.class)
  public void testNonsense1() {
    IonTypeUtil.createIonType("nonsense");
  }

  @Test(expected = MSDKRuntimeException.class)
  public void testNonsense2() {
    IonTypeUtil.createIonType("M+N");
  }

}
