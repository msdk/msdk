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

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;

/**
 * Tests for SimpleChromatogram
 */
public class SimpleChromatogramTest {

  private static @Nonnull SimpleChromatogram chromatogram1 = new SimpleChromatogram();

  @Test
  public void testRawDataFile() throws MSDKException {
    // Verify raw data file
    Assert.assertEquals(null, chromatogram1.getRawDataFile());

    // Change raw data file
    final @Nonnull RawDataFile newRawDataFile =
        new SimpleRawDataFile("Sample A1", Optional.empty(), FileType.NETCDF);
    chromatogram1.setRawDataFile(newRawDataFile);

    // Verify raw data file
    Assert.assertEquals("Sample A1", chromatogram1.getRawDataFile().getName());
    Assert.assertEquals(FileType.NETCDF, chromatogram1.getRawDataFile().getRawDataFileType());
  }

  @Test
  public void testChromatogramType() throws MSDKException {

    // Set chromatogram type
    chromatogram1.setChromatogramType(ChromatogramType.TIC);

    // Verify chromatogram type
    Assert.assertEquals(ChromatogramType.TIC, chromatogram1.getChromatogramType());

    // Change chromatogram type
    chromatogram1.setChromatogramType(ChromatogramType.SIC);

    // Verify chromatogram type
    Assert.assertEquals(ChromatogramType.SIC, chromatogram1.getChromatogramType());
  }

  @Test
  public void testSeparationType() throws MSDKException {

    // Set separation type
    chromatogram1.setSeparationType(SeparationType.LC);

    // Verify separation type
    Assert.assertEquals(SeparationType.LC, chromatogram1.getSeparationType());

    // Change separation type
    chromatogram1.setSeparationType(SeparationType.GCxGC);

    // Verify separation type
    Assert.assertEquals(SeparationType.GCxGC, chromatogram1.getSeparationType());
  }


  @Test
  public void testIsolationInfo() throws MSDKException {
    // Verify isolation info
    Assert.assertEquals(new LinkedList<>(), chromatogram1.getIsolations());

    // Change isolation info
    final @Nonnull List<IsolationInfo> newIsolations = chromatogram1.getIsolations();
    newIsolations.add(new SimpleIsolationInfo(Range.closed(1.2, 8.9)));
    newIsolations.add(new SimpleIsolationInfo(Range.closed(0.0, 10.0), 0.5f, 500.123, 1, null, null));

    // Verify isolation info
    Assert.assertEquals(Range.closed(0.0, 10.0), newIsolations.get(1).getIsolationMzRange());
    Assert.assertEquals(new Float(0.5), newIsolations.get(1).getIonInjectTime());
    Assert.assertEquals(new Double(500.123), newIsolations.get(1).getPrecursorMz());
    Assert.assertEquals(new Integer(1), newIsolations.get(1).getPrecursorCharge());
  }

}
