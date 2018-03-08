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

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.SimpleMsSpectrum;
import io.github.msdk.spectra.centroidprofiledetection.SpectrumTypeDetectionAlgorithm;
import io.github.msdk.util.ArrayUtil;
import io.github.msdk.util.DataPointSorter;
import io.github.msdk.util.DataPointSorter.SortingDirection;
import io.github.msdk.util.DataPointSorter.SortingProperty;

/**
 * <p>
 * TxtImportAlgorithm class.
 * </p>
 */
public class TxtImportAlgorithm {

  private static final Pattern linePattern =
      Pattern.compile("(\\d+(\\.\\d+)?)[^\\d]+(\\d+(\\.\\d+)?)");

  /**
   * Parse a MsSpectrum object from the given string that has input data in two columns.
   * 
   * @param scanner An open {@link java.util.Scanner} object.
   * @return A {@link io.github.msdk.datamodel.MsSpectrum} object containing the parsed data.
   */
  private static @Nonnull SimpleMsSpectrum parseMsSpectrum(@Nonnull Scanner scanner) {

    double mzValues[] = new double[16];
    float intensityValues[] = new float[16];
    int size = 0;

    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      Matcher m = linePattern.matcher(line);
      if (!m.find())
        break;

      String mzString = m.group(1);
      String intensityString = m.group(3);

      double mz = Double.parseDouble(mzString);
      float intensity = Float.parseFloat(intensityString);
      mzValues = ArrayUtil.addToArray(mzValues, mz, size);
      intensityValues = ArrayUtil.addToArray(intensityValues, intensity, size);
      size++;

    }

    if (size == 0) {
      return null;
    }

    // Sort the data points, in case they were not ordered
    DataPointSorter.sortDataPoints(mzValues, intensityValues, size, SortingProperty.MZ,
        SortingDirection.ASCENDING);

    MsSpectrumType specType =
        SpectrumTypeDetectionAlgorithm.detectSpectrumType(mzValues, intensityValues, size);
    SimpleMsSpectrum result = new SimpleMsSpectrum(mzValues, intensityValues, size, specType);

    return result;
  }

  /**
   * Parse a MsSpectrum object from the given string that has input data in two columns.
   *
   * @param spectrumText A String containing the input text.
   * @return A {@link io.github.msdk.datamodel.MsSpectrum} object containing the parsed data.
   */
  public static SimpleMsSpectrum parseMsSpectrum(@Nonnull String spectrumText) {

    Collection<SimpleMsSpectrum> result = parseMsSpectra(new StringReader(spectrumText));
    if (result.size() == 0) {
      return null;
    }

    Iterator<SimpleMsSpectrum> iterator = result.iterator();
    return iterator.next();
  }

  /**
   * Parse a MsSpectrum object from the given readers. The reader is closed when this method
   * returns.
   *
   * @param reader An open @{java.io.Reader} object that provides the data in two columns.
   * @return A {@link java.util.Collection} of {@link io.github.msdk.datamodel.MsSpectrum} objects
   *         containing the parsed data.
   */
  public static @Nonnull Collection<SimpleMsSpectrum> parseMsSpectra(@Nonnull Reader reader) {

    Collection<SimpleMsSpectrum> result = new ArrayList<>();
    Scanner scanner = new Scanner(reader);

    while (scanner.hasNextLine()) {
      SimpleMsSpectrum spectrum = parseMsSpectrum(scanner);
      if (spectrum != null) {
        result.add(spectrum);
      }
    }
    scanner.close();

    return result;
  }

  /**
   * Parse a collection (i.e. ArrayList) of MsSpectrum objects from the given readers. The reader is
   * closed when this method returns.
   *
   * @param reader An open @{java.io.Reader} object that provides the data in two columns.
   * @return A {@link io.github.msdk.datamodel.MsSpectrum} object containing the parsed data, or
   *         null if the data could not be parsed.
   */
  public static SimpleMsSpectrum parseMsSpectrum(@Nonnull Reader reader) {

    Collection<SimpleMsSpectrum> result = parseMsSpectra(reader);
    if (result.size() == 0) {
      return null;
    }

    Iterator<SimpleMsSpectrum> iterator = result.iterator();
    return iterator.next();
  }
}
