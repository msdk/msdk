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

package io.github.msdk.util;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import io.github.msdk.datamodel.impl.SimpleMsScan;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;

/**
 * <p>
 * MsScanUtil class.
 * </p>
 *
 */
public class MsScanUtil {

  /**
   * <p>
   * clone.
   * </p>
   *
   * @param newStore a {@link io.github.msdk.datamodel.datastore.DataPointStore} object.
   * @param scan a {@link io.github.msdk.datamodel.rawdata.MsScan} object.
   * @param copyDataPoints a {@link java.lang.Boolean} object.
   * @return a {@link io.github.msdk.datamodel.rawdata.MsScan} object.
   */
  @Nonnull
  static public SimpleMsScan clone(@Nonnull MsScan scan, @Nonnull Boolean copyDataPoints) {

    Preconditions.checkNotNull(scan);
    Preconditions.checkNotNull(copyDataPoints);

    SimpleMsScan newScan = new SimpleMsScan(scan.getScanNumber(), scan.getMsFunction());

    newScan.setPolarity(scan.getPolarity());
    newScan.setMsScanType(scan.getMsScanType());
    newScan.setScanningRange(scan.getScanningRange());
    newScan.setRetentionTime(scan.getRetentionTime());
    newScan.setSourceInducedFragmentation(scan.getSourceInducedFragmentation());
    newScan.getIsolations().addAll(scan.getIsolations());

    if (copyDataPoints) {
      double mzValues[] = scan.getMzValues();
      float intensityValues[] = scan.getIntensityValues();
      newScan.setDataPoints(mzValues, intensityValues, scan.getNumberOfDataPoints());
    }

    return newScan;
  }

  /** {@inheritDoc} */
  public String msScanToString(@Nonnull MsScan scan) {
    StringBuilder buf = new StringBuilder();
    buf.append("Scan ");
    final RawDataFile rawDataFile2 = scan.getRawDataFile();
    if (rawDataFile2 != null && rawDataFile2.getOriginalFile() != null) {
      buf.append(rawDataFile2.getOriginalFilename());
      buf.append(" ");
    }
    if (scan.getMsFunction() != null) {
      buf.append(scan.getMsFunction());
      buf.append(" ");
    }
    buf.append("#");
    buf.append(scan.getScanNumber());
    return buf.toString();
  }

}
