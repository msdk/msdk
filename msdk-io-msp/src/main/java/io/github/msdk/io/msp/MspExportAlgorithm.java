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

package io.github.msdk.io.msp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

import com.google.common.base.Strings;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.MsSpectrum;

/**
 * <p>
 * MspExportAlgorithm class.
 * </p>
 */
public class MspExportAlgorithm {

  /**
   * <p>
   * exportSpectrum.
   * </p>
   *
   * @param exportFile a {@link java.io.File} object.
   * @param spectrum a {@link io.github.msdk.datamodel.MsSpectrum} object.
   * @throws java.io.IOException if any.
   * @throws io.github.msdk.MSDKException if any.
   */
  public static void exportSpectrum(@Nonnull File exportFile, @Nonnull MsSpectrum spectrum)
      throws IOException, MSDKException {
    exportSpectra(exportFile, Collections.singleton(spectrum));
  }

  /**
   * <p>
   * exportSpectra.
   * </p>
   *
   * @param exportFile a {@link java.io.File} object.
   * @param spectra a {@link java.util.Collection} object.
   * @throws java.io.IOException if any.
   * @throws io.github.msdk.MSDKException if any.
   */
  public static void exportSpectra(@Nonnull File exportFile,
      @Nonnull Collection<MsSpectrum> spectra) throws IOException, MSDKException {

    // Open the writer
    final BufferedWriter writer = new BufferedWriter(new FileWriter(exportFile));

    double mzValues[] = null;
    float intensityValues[] = null;
    int numOfDataPoints;

    // Write the data points
    for (MsSpectrum spectrum : spectra) {

      // Load data
      mzValues = spectrum.getMzValues();
      intensityValues = spectrum.getIntensityValues();
      numOfDataPoints = spectrum.getNumberOfDataPoints();

      if (spectrum instanceof MspSpectrum) {
        MspSpectrum mspSpectrum = (MspSpectrum) spectrum;
        String name = mspSpectrum.getProperty("NAME");
        if (Strings.isNullOrEmpty(name))
          name = "Spectrum";
        writer.write("NAME: " + spectrum.getNumberOfDataPoints());
        writer.newLine();
      } else {
        writer.write("NAME: Spectrum");
        writer.newLine();
      }

      writer.write("Num Peaks: " + numOfDataPoints);
      writer.newLine();

      for (int i = 0; i < numOfDataPoints; i++) {
        // Write data point row
        writer.write(mzValues[i] + " " + intensityValues[i]);
        writer.newLine();
      }

      writer.newLine();

    }

    writer.close();

  }

}
