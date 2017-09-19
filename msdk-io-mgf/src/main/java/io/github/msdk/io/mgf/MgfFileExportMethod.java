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

package io.github.msdk.io.mgf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.IsolationInfo;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.MsSpectrum;

/**
 * <p>
 * MgfExportAlgorithm class.
 * </p>
 */
public class MgfFileExportMethod implements MSDKMethod<Void> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final @Nonnull Collection<MsSpectrum> spectra;
  private final @Nonnull File target;

  private boolean canceled = false;

  private long processedSpectra = 0;

  /**
   * <p>
   * Constructor for MgfFileExportMethod.
   * </p>
   *
   * @param spectra a collection of {@link io.github.msdk.datamodel.MsSpectrum} objects.
   * @param target a {@link java.io.File} object.
   */
  public MgfFileExportMethod(@Nonnull Collection<MsSpectrum> spectra, @Nonnull File target) {
    this.spectra = spectra;
    this.target = target;
  }

  /**
   * <p>
   * Constructor for MgfFileExportMethod.
   * </p>
   *
   * @param target a {@link java.io.File} object.
   * @param spectrum a {@link io.github.msdk.datamodel.MsSpectrum} object.
   */
  public MgfFileExportMethod(@Nonnull MsSpectrum spectrum, @Nonnull File target) {
    this.spectra = Collections.singleton(spectrum);
    this.target = target;
  }

  /** {@inheritDoc} */
  @Override
  public Void execute() throws MSDKException {
    logger.info("Started MGF export of {} spectra to {}", spectra.size(), target);

    // Open the writer
    try (final BufferedWriter writer = new BufferedWriter(new FileWriter(target))) {

      double mzValues[] = null;
      float intensityValues[] = null;
      int numOfDataPoints;

      for (MsSpectrum spectrum : spectra) {

        if (canceled) {
          writer.close();
          target.delete();
          return null;
        }

        mzValues = spectrum.getMzValues();
        intensityValues = spectrum.getIntensityValues();
        numOfDataPoints = spectrum.getNumberOfDataPoints();

        writer.write("BEGIN IONS");
        writer.newLine();

        if (spectrum instanceof MsScan) {
          MsScan scan = (MsScan) spectrum;

          for (IsolationInfo ii : scan.getIsolations()) {
            Double precursorMz = ii.getPrecursorMz();
            if (precursorMz == null)
              continue;
            writer.write("PEPMASS=" + precursorMz);
            writer.newLine();
            if (ii.getPrecursorCharge() != null) {
              writer.write("CHARGE=" + ii.getPrecursorCharge());
              writer.newLine();
            }
            break;
          }

          Float rt = scan.getRetentionTime();
          if (rt != null) {
            writer.write("RTINSECONDS=" + rt);
            writer.newLine();
          }
          writer.write("Title=Scan #" + scan.getScanNumber());
          writer.newLine();

        }

        // Write ions
        for (int i = 0; i < numOfDataPoints; i++) {
          writer.write(mzValues[i] + " " + intensityValues[i]);
          writer.newLine();
        }

        writer.write("END IONS");
        writer.newLine();
        writer.newLine();

        processedSpectra++;
      }

    } catch (IOException e) {
      throw new MSDKException(e);
    }

    logger.info("Finished export of spectra to {}", target);
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public Float getFinishedPercentage() {
    int totalSpectra = spectra.size();
    return totalSpectra == 0 ? null : (float) (processedSpectra / totalSpectra);
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Void getResult() {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    this.canceled = true;
  }
}
