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

package io.github.msdk.featuredetection.chromatogrambuilder;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.Chromatogram;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.util.tolerances.MzTolerance;

/**
 * <p>
 * ChromatogramBuilderMethod class.
 * </p>
 */
public class ChromatogramBuilderMethod implements MSDKMethod<List<Chromatogram>> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final @Nonnull RawDataFile inputFile;
  private final @Nonnull List<MsScan> inputScans;
  private final @Nonnull Double noiseLevel;
  private final @Nonnull Double minimumTimeSpan, minimumHeight;
  private final @Nonnull MzTolerance mzTolerance;

  private int processedScans = 0, totalScans = 0;
  private boolean canceled = false;
  private List<Chromatogram> result;

  /**
   * <p>
   * Constructor for ChromatogramBuilderMethod. This version uses the same MzTolerance for every
   * scan that does not provide its own MzTolerance.
   * </p>
   *
   * @param inputFile a {@link io.github.msdk.datamodel.RawDataFile} object.
   * @param minimumTimeSpan a {@link java.lang.Double} object.
   * @param minimumHeight a {@link java.lang.Double} object.
   * @param mzTolerance an object that implements the MZTolerance interface.
   * @param noiseLevel a {@link java.lang.Float} object.
   */
  public ChromatogramBuilderMethod(RawDataFile inputFile, double noiseLevel, double minimumTimeSpan,
      double minimumHeight, MzTolerance mzTolerance) {
    this(inputFile, inputFile.getScans(), noiseLevel, minimumTimeSpan, minimumHeight, mzTolerance);
  }

  /**
   * <p>
   * Constructor for ChromatogramBuilderMethod. This version uses the same MzTolerance for every
   * scan that does not provide its own MzTolerance.
   * </p>
   *
   * @param inputFile a {@link io.github.msdk.datamodel.RawDataFile} object.
   * @param inputScans a {@link java.util.List} object.
   * @param minimumTimeSpan a {@link java.lang.Double} object.
   * @param minimumHeight a {@link java.lang.Double} object.
   * @param mzTolerance an object that implements the MZTolerance interface.
   * @param noiseLevel a {@link java.lang.Float} object.
   */
  public ChromatogramBuilderMethod(@Nonnull RawDataFile inputFile, @Nonnull List<MsScan> inputScans,
      @Nonnull Double noiseLevel, @Nonnull Double minimumTimeSpan, @Nonnull Double minimumHeight,
      @Nonnull MzTolerance mzTolerance) {

    this.inputFile = inputFile;
    this.inputScans = inputScans;
    this.noiseLevel = noiseLevel;
    this.minimumTimeSpan = minimumTimeSpan;
    this.minimumHeight = minimumHeight;
    this.mzTolerance = mzTolerance;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public List<Chromatogram> execute() throws MSDKException {

    logger.info("Started chromatogram builder on file " + inputFile.getName());

    // Check if we have any scans
    totalScans = inputScans.size();
    if (totalScans == 0) {
      throw new MSDKException("No scans provided for Chromatogram Builder");
    }

    // Check if the scans are properly ordered by RT
    Float prevRT = null;
    for (MsScan s : inputScans) {
      if (s.getRetentionTime() == null)
        continue;
      if (prevRT == null) {
        prevRT = s.getRetentionTime();
        continue;
      }
      if (prevRT.compareTo(s.getRetentionTime()) > 0) {
        final String msg = "Retention time of scan #" + s.getScanNumber()
            + " is smaller then the retention time of the previous scan."
            + " Please make sure you only use scans with increasing retention times.";
        throw new MSDKException(msg);
      }
      prevRT = s.getRetentionTime();
    }

    HighestDataPointConnector massConnector =
        new HighestDataPointConnector(noiseLevel, minimumTimeSpan, minimumHeight);

    for (MsScan scan : inputScans) {

      if (canceled)
        return null;

      MzTolerance mzTolerance = scan.getMzTolerance();
      if (mzTolerance == null && this.mzTolerance != null) {
        mzTolerance = this.mzTolerance;
      } else {
        throw new MSDKException("Unable to find a m/z tolerance for scan" + scan.getScanNumber());
      }
      massConnector.addScan(inputFile, scan, mzTolerance);
      processedScans++;
    }

    result = new ArrayList<>();
    massConnector.finishChromatograms(inputFile, result);

    logger.info("Finished chromatogram builder on file " + inputFile.getName());

    return result;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Float getFinishedPercentage() {
    if (totalScans == 0)
      return null;
    else
      return (float) processedScans / totalScans;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public List<Chromatogram> getResult() {
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    this.canceled = true;
  }

}
