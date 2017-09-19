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

import io.github.msdk.util.DataPointSorter;
import io.github.msdk.util.DataPointSorter.SortingDirection;
import io.github.msdk.util.DataPointSorter.SortingProperty;

/**
 * Tests for DataPointSorter
 */
public class DataPointSorterTest {

  @Test
  public void testMzSortAsc() {
    double mzBuffer[] = {200.0, 200.0001, 200.0002, 300.0, 333.0, 32.0, 35.0, 0.0, 0.0};
    float intensityBuffer[] = {320f, 35f, 20f, 200.0001f, 200.0002f, 3f, 30f, 0f, 20f};
    int size = 7;
    DataPointSorter.sortDataPoints(mzBuffer, intensityBuffer, size, SortingProperty.MZ,
        SortingDirection.ASCENDING);
    for (int i = 1; i < size; i++) {
      Assert.assertTrue(mzBuffer[i] >= mzBuffer[i - 1]);
    }
  }

  @Test
  public void testMzSortDesc() {
    double mzBuffer[] = {32.0, 35.0, 200.0, 200.0001, 200.0002, 333.0, 300.0, 0.0, 0.0};
    float intensityBuffer[] = {320f, 35f, 20f, 200.0001f, 200.0002f, 3f, 30f, 0f, 20f};
    int size = 7;
    DataPointSorter.sortDataPoints(mzBuffer, intensityBuffer, size, SortingProperty.MZ,
        SortingDirection.DESCENDING);
    for (int i = 1; i < size; i++) {
      Assert.assertTrue(mzBuffer[i] <= mzBuffer[i - 1]);
    }
  }

  @Test
  public void testIntensitySortAsc() {
    double mzBuffer[] = {32.0, 35.0, 200.0, 200.0001, 200.0002, 333.0, 300.0, 0.0, 0.0};
    float intensityBuffer[] = {320f, 35f, 20f, 200.0001f, 200.0002f, 3f, 30f, 0f, 20f};
    int size = 7;
    DataPointSorter.sortDataPoints(mzBuffer, intensityBuffer, size, SortingProperty.INTENSITY,
        SortingDirection.ASCENDING);
    for (int i = 1; i < size; i++) {
      Assert.assertTrue(intensityBuffer[i] >= intensityBuffer[i - 1]);
    }
  }

  @Test
  public void testIntensitySortDesc() {
    double mzBuffer[] = {32.0, 35.0, 200.0, 200.0001, 200.0002, 333.0, 300.0, 0.0, 0.0};
    float intensityBuffer[] = {320f, 35f, 20f, 200.0001f, 200.0002f, 3f, 30f, 0f, 20f};
    int size = 7;
    DataPointSorter.sortDataPoints(mzBuffer, intensityBuffer, size, SortingProperty.INTENSITY,
        SortingDirection.DESCENDING);
    for (int i = 1; i < size; i++) {
      Assert.assertTrue(intensityBuffer[i] <= intensityBuffer[i - 1]);
    }
  }

}
