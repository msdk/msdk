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

import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.impl.SimpleMsScan;
import io.github.msdk.datamodel.rawdata.MsScan;

/**
 * <p>
 * MsScanUtil class.
 * </p>
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
  static public SimpleMsScan clone(@Nonnull DataPointStore newStore, @Nonnull MsScan scan,
      @Nonnull Boolean copyDataPoints) {

    Preconditions.checkNotNull(newStore);
    Preconditions.checkNotNull(scan);
    Preconditions.checkNotNull(copyDataPoints);

    SimpleMsScan newScan =
        new SimpleMsScan(newStore, scan.getScanNumber(), scan.getMsFunction());

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

}
