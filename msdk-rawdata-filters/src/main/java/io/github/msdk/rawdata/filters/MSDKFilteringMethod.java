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

package io.github.msdk.rawdata.filters;

import java.util.List;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.datamodel.SimpleRawDataFile;

/**
 * <p>
 * MSDKFilteringMethod class.
 * </p>
 */
public class MSDKFilteringMethod implements MSDKMethod<RawDataFile> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final @Nonnull MSDKFilteringAlgorithm filteringAlgorithm;
  private final @Nonnull RawDataFile rawDataFile;

  private int processedScans = 0, totalScans = 0;
  private SimpleRawDataFile result;
  private boolean canceled = false;

  /**
   * <p>
   * Constructor for MSDKFilteringMethod.
   * </p>
   *
   * @param rawDataFile a {@link io.github.msdk.datamodel.RawDataFile} object.
   * @param filteringAlgorithm a {@link io.github.msdk.rawdata.filters.MSDKFilteringAlgorithm}
   *        object.
   */
  public MSDKFilteringMethod(@Nonnull RawDataFile rawDataFile,
      @Nonnull MSDKFilteringAlgorithm filteringAlgorithm) {
    this.filteringAlgorithm = filteringAlgorithm;
    this.rawDataFile = rawDataFile;
  }

  /** {@inheritDoc} */
  @Override
  public Float getFinishedPercentage() {
    if (totalScans == 0) {
      return null;
    } else {
      return (float) processedScans / totalScans;
    }
  }

  /** {@inheritDoc} */
  @Override
  public RawDataFile execute() throws MSDKException {
    logger.info("Started filter " + filteringAlgorithm.getClass().getName() + " on raw data file "
        + rawDataFile.getName());

    // Create a new raw data file
    result = new SimpleRawDataFile(rawDataFile.getName(), rawDataFile.getOriginalFile(),
        rawDataFile.getRawDataFileType());

    List<MsScan> scans = rawDataFile.getScans();
    totalScans = scans.size();

    for (MsScan scan : scans) {

      if (canceled)
        return null;

      if (scan == null)
        continue;

      MsScan newScan = filteringAlgorithm.performFilter(scan);

      // Add the new scan to the created raw data file
      if (newScan != null)
        result.addScan(newScan);

      processedScans++;
    }
    logger.info("Finished filter " + filteringAlgorithm.getClass().getName() + " on raw data file "
        + rawDataFile.getName());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public RawDataFile getResult() {
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    this.canceled = true;
  }

}
