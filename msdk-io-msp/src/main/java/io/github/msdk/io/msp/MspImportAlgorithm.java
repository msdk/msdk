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

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.spectra.centroidprofiledetection.SpectrumTypeDetectionAlgorithm;
import io.github.msdk.util.ArrayUtil;
import io.github.msdk.util.DataPointSorter;
import io.github.msdk.util.DataPointSorter.SortingDirection;
import io.github.msdk.util.DataPointSorter.SortingProperty;

/**
 * <p>
 * MspImportAlgorithm class.
 * </p>
 */
public class MspImportAlgorithm {

  private static final Pattern propertyPattern = Pattern.compile("^([A-Z]+): (.+)");

  private static final Pattern numPeaksPattern = Pattern.compile("^Num Peaks: (\\d+)");

  private static final Pattern mzIntensityPattern =
      Pattern.compile("(\\d+(\\.\\d+)?)[^\\d]+(\\d+(\\.\\d+)?)");

  /**
   * <p>
   * parseMspFromFile.
   * </p>
   *
   * @param mspFile a {@link java.io.File} object.
   * @return a {@link io.github.msdk.io.msp.MspSpectrum} object.
   * @throws java.io.IOException if any.
   * @throws io.github.msdk.MSDKException if any.
   */
  public static @Nonnull MspSpectrum parseMspFromFile(@Nonnull File mspFile)
      throws IOException, MSDKException {

    String str = Files.toString(mspFile, Charsets.ISO_8859_1);

    if (Strings.isNullOrEmpty(str))
      throw new IOException("Could not parse content of file " + mspFile);

    return parseMspFromString(str);
  }

  /**
   * <p>
   * parseMspFromString.
   * </p>
   *
   * @param mspText a {@link java.lang.String} object.
   * @return a {@link io.github.msdk.io.msp.MspSpectrum} object.
   * @throws io.github.msdk.MSDKException if any.
   */
  public static @Nonnull MspSpectrum parseMspFromString(@Nonnull String mspText)
      throws MSDKException {


    // Create a new MSP spectrum
    MspSpectrum spectrum = new MspSpectrum();

    // Number of peaks, must be specified in the MSP file
    Integer numPeaks = null;

    Scanner scanner = new Scanner(mspText);
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();

      Matcher m = propertyPattern.matcher(line);
      if (m.find()) {
        String propName = m.group(1);
        String propValue = m.group(2);
        spectrum.setProperty(propName, propValue);
        continue;
      }

      m = numPeaksPattern.matcher(line);
      if (m.find()) {
        String numPeaksString = m.group(1);
        numPeaks = Integer.parseInt(numPeaksString);
        break;
      }

    }

    double mzValues[] = new double[16];
    float intensityValues[] = new float[16];
    int size = 0;

    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();

      Matcher m = mzIntensityPattern.matcher(line);
      while (m.find()) {
        String mzString = m.group(1);
        String intensityString = m.group(3);
        double mz = Double.parseDouble(mzString);
        float intensity = Float.parseFloat(intensityString);
        mzValues = ArrayUtil.addToArray(mzValues, mz, size);
        intensityValues = ArrayUtil.addToArray(intensityValues, intensity, size);
        size++;
      }

    }

    scanner.close();

    if (numPeaks == null)
      throw new MSDKException("Invalid format of MSP file, could not find Num Peaks: entry");

    if (numPeaks != size)
      throw new MSDKException(
          "Invalid format of MSP file, mismatch between Num Peaks: and actual number of entries");

    // Sort the data points, in case they were not ordered
    DataPointSorter.sortDataPoints(mzValues, intensityValues, size, SortingProperty.MZ,
        SortingDirection.ASCENDING);

    // Store the data points
    spectrum.setDataPoints(mzValues, intensityValues, size);

    // Detect the type of the spectrum
    MsSpectrumType specType =
        SpectrumTypeDetectionAlgorithm.detectSpectrumType(mzValues, intensityValues, size);
    spectrum.setSpectrumType(specType);

    return spectrum;

  }
}
