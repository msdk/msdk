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

package io.github.msdk.io.txt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

import io.github.msdk.datamodel.MsSpectrum;

/**
 * <p>
 * A class containing methods to export {@link io.github.msdk.datamodel.MsSpectrum}
 * objects to string representations or files.
 * </p>
 */
public class TxtExportAlgorithm {

  /**
   * <p>
   * Export a single spectrum to a file.
   * </p>
   *
   * @param exportFile a {@link java.io.File} object.
   * @param spectrum a {@link io.github.msdk.datamodel.MsSpectrum} object.
   * @throws java.io.IOException if any.
   */
  public static void exportSpectrum(@Nonnull File exportFile, @Nonnull MsSpectrum spectrum)
      throws IOException {
    exportSpectra(exportFile, Collections.singleton(spectrum));
  }

  /**
   * <p>
   * Export one or more spectra to a file. A single space is used as the delimiter.
   * </p>
   *
   * @param exportFile a {@link java.io.File} object.
   * @param spectra a {@link java.util.Collection} object.
   * @throws java.io.IOException if any.
   */
  public static void exportSpectra(@Nonnull File exportFile,
      @Nonnull Collection<MsSpectrum> spectra) throws IOException {
    exportSpectra(exportFile, spectra, " ");
  }

  /**
   * <p>
   * Export one or more spectra to a file.
   * </p>
   *
   * @param exportFile a {@link java.io.File} object.
   * @param spectra a {@link java.util.Collection} object.
   * @param delimiter a {@link java.lang.String} object.
   * @throws java.io.IOException if any.
   */
  public static void exportSpectra(@Nonnull File exportFile,
      @Nonnull Collection<MsSpectrum> spectra, @Nonnull String delimiter) throws IOException {

    // Open the writer
    final BufferedWriter writer = new BufferedWriter(new FileWriter(exportFile));

    // Write the data points
    for (MsSpectrum spectrum : spectra) {
      spectrumToWriter(spectrum, writer, delimiter);
      writer.newLine();
    }

    writer.close();

  }

  /**
   * <p>
   * Export a spectrum to a writer. A single space is used as the delimiter.
   * </p>
   *
   * @param spectrum a {@link io.github.msdk.datamodel.MsSpectrum} object.
   * @param writer a {@link java.io.Writer} object.
   * @throws java.io.IOException if any.
   */
  public static void spectrumToWriter(@Nonnull MsSpectrum spectrum, @Nonnull Writer writer)
      throws IOException {
    spectrumToWriter(spectrum, writer, " ");
  }

  /**
   * <p>
   * Export a spectrum to a writer.
   * </p>
   *
   * @param spectrum a {@link io.github.msdk.datamodel.MsSpectrum} object.
   * @param writer a {@link java.io.Writer} object.
   * @param delimiter a {@link java.lang.String object}.
   * @throws java.io.IOException if any.
   */
  public static void spectrumToWriter(@Nonnull MsSpectrum spectrum, @Nonnull Writer writer,
      @Nonnull String delimiter) throws IOException {

    double mzValues[] = spectrum.getMzValues();
    float intensityValues[] = spectrum.getIntensityValues();
    int numOfDataPoints = spectrum.getNumberOfDataPoints();

    for (int i = 0; i < numOfDataPoints; i++) {
      // Write data point row
      writer.write(mzValues[i] + delimiter + intensityValues[i]);
      writer.write(System.lineSeparator());
    }

  }

  /**
   * <p>
   * Export a spectrum to a string. Uses a {@link java.io.StringWriter} object.
   * </p>
   * A single space is used as the delimiter.
   *
   * @param spectrum a {@link io.github.msdk.datamodel.MsSpectrum} object.
   * @return a {@link java.lang.String} object.
   */
  public static @Nonnull String spectrumToString(@Nonnull MsSpectrum spectrum) {
    return spectrumToString(spectrum, " ");
  }

  /**
   * <p>
   * Export a spectrum to a string. Uses a {@link java.io.StringWriter} object.
   * </p>
   *
   * @param spectrum a {@link io.github.msdk.datamodel.MsSpectrum} object.
   * @param delimiter a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static @Nonnull String spectrumToString(@Nonnull MsSpectrum spectrum,
      @Nonnull String delimiter) {

    StringWriter sw = new StringWriter();
    try {
      spectrumToWriter(spectrum, sw, delimiter);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return sw.toString();
  }
}
