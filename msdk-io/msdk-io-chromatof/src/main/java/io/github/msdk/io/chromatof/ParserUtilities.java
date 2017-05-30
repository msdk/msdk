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

package io.github.msdk.io.chromatof;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic utility methods for csv parsing and numeric conversion.
 */
public class ParserUtilities {

  private static final Logger log = LoggerFactory.getLogger(ParserUtilities.class);

  /**
   * Parse a numeric string using the specified locale. When a ParseException is caught, the method
   * returns Double.NaN.
   *
   * @param s the string to parse.
   * @param locale the locale to use for numeric conversion.
   * @return the double value. May be NaN if s is null, empty, or unparseable
   */
  public static double parseDouble(String s, Locale locale) {
    if (s == null || s.isEmpty()) {
      return Double.NaN;
    }
    try {
      return NumberFormat.getNumberInstance(locale).parse(s).doubleValue();
    } catch (ParseException ex) {
      try {
        return NumberFormat.getNumberInstance(Locale.US).parse(s).doubleValue();
      } catch (ParseException ex1) {
        return Double.NaN;
      }
    }
  }

  /**
   * Parse a numeric string using the specified locale. When a ParseException is caught, the method
   * returns Float.NaN.
   *
   * @param s the string to parse.
   * @param locale the locale to use for numeric conversion.
   * @return the float value. May be NaN if s is null, empty, or unparseable
   */
  public static float parseFloat(String s, Locale locale) {
    if (s == null || s.isEmpty()) {
      return Float.NaN;
    }
    try {
      return NumberFormat.getNumberInstance(locale).parse(s).floatValue();
    } catch (ParseException ex) {
      try {
        return NumberFormat.getNumberInstance(Locale.US).parse(s).floatValue();
      } catch (ParseException ex1) {
        return Float.NaN;
      }
    }
  }

  /**
   * <p>
   * getFilenameToGroupMap.
   * </p>
   *
   * @param f a {@link java.io.File} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @return a {@link java.util.HashMap} object.
   */
  public static HashMap<String, String> getFilenameToGroupMap(File f, String fieldSeparator) {
    List<String> header = null;
    HashMap<String, String> filenameToGroupMap = new LinkedHashMap<>();
    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
      String line = "";
      int lineCount = 0;
      while ((line = br.readLine()) != null) {
        if (!line.isEmpty()) {
          String[] lineArray = line.split(String.valueOf(fieldSeparator));
          if (lineCount > 0) {
            filenameToGroupMap.put(lineArray[0], lineArray[1]);
          }
          lineCount++;
        }
      }
    } catch (IOException ex) {
      log.warn("Caught an IO Exception while reading file " + f, ex);
    }
    return filenameToGroupMap;
  }

  /**
   * Method to convert a mass spectrum in the format contained in ChromaTOF peak files as pairs of
   * mz and intensity, separated by space : {@code 102:956 107:119}.
   *
   * @param massSpectrum the mass spectrum string to parse.
   * @return a tuple of double[] masses and int[] intensities.
   */
  public static Pair<double[], int[]> convertMassSpectrum(String massSpectrum) {
    if (massSpectrum == null) {
      log.warn("Warning: mass spectral data was null!");
      return new Pair<>(new double[0], new int[0]);
    }
    String[] mziTuples = massSpectrum.split(" ");
    TreeMap<Double, Integer> tm = new TreeMap<>();
    for (String tuple : mziTuples) {
      if (tuple.contains(":")) {
        String[] tplArray = tuple.split(":");
        tm.put(Double.valueOf(tplArray[0]), Integer.valueOf(tplArray[1]));
      } else {
        log.warn("Warning: encountered malformed tuple: {} within ms: {}",
            new Object[] {tuple, massSpectrum});
      }
    }
    double[] masses = new double[tm.keySet().size()];
    int[] intensities = new int[tm.keySet().size()];
    int i = 0;
    for (Double key : tm.keySet()) {
      masses[i] = key;
      intensities[i] = tm.get(key);
      i++;
    }
    return new Pair<>(masses, intensities);
  }
}
