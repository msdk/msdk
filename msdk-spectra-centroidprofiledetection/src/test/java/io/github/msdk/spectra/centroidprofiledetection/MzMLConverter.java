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

package io.github.msdk.spectra.centroidprofiledetection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.Range;

import io.github.msdk.datamodel.ActivationInfo;
import io.github.msdk.datamodel.ActivationType;
import io.github.msdk.datamodel.ChromatogramType;
import io.github.msdk.datamodel.IsolationInfo;
import io.github.msdk.datamodel.MsScanType;
import io.github.msdk.datamodel.PolarityType;
import io.github.msdk.datamodel.SeparationType;
import io.github.msdk.datamodel.SimpleActivationInfo;
import io.github.msdk.datamodel.SimpleIsolationInfo;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArrayList;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.ParamGroup;
import uk.ac.ebi.jmzml.model.mzml.Precursor;
import uk.ac.ebi.jmzml.model.mzml.PrecursorList;
import uk.ac.ebi.jmzml.model.mzml.Scan;
import uk.ac.ebi.jmzml.model.mzml.ScanList;
import uk.ac.ebi.jmzml.model.mzml.SelectedIonList;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;

/**
 * This class provides conversions between the jmzml data model and the MSDK data model
 */
class MzMLConverter {

  private int lastScanNumber = 0;

  private Map<String, Integer> scanIdTable = new Hashtable<String, Integer>();

  @Nonnull
  Integer extractScanNumber(Spectrum spectrum) {

    String spectrumId = spectrum.getId();

    Integer storedScanNumber = scanIdTable.get(spectrumId);
    if (storedScanNumber != null)
      return storedScanNumber;

    final Pattern pattern = Pattern.compile("scan=([0-9]+)");
    final Matcher matcher = pattern.matcher(spectrumId);
    boolean scanNumberFound = matcher.find();

    // Some vendors include scan=XX in the ID, some don't, such as
    // mzML converted from WIFF files. See the definition of nativeID in
    // http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo
    if (scanNumberFound) {
      Integer scanNumber = Integer.parseInt(matcher.group(1));
      lastScanNumber = scanNumber;
      scanIdTable.put(spectrumId, scanNumber);
      return scanNumber;
    }

    Integer scanNumber = lastScanNumber + 1;
    lastScanNumber++;
    scanIdTable.put(spectrumId, scanNumber);
    return scanNumber;
  }

  @Nonnull
  Boolean isMsSpectrum(Spectrum spectrum) {

    String value = extractCVValue(spectrum.getCvParam(), MzMLCV.cvUVSpectrum);
    if (value != null)
      return false;

    value = extractCVValue(spectrum.getCvParam(), MzMLCV.cvMS1Spectrum);
    if (value != null)
      return true;

    // By default, let's assume unidentified spectra are MS spectra
    return true;
  }

  @Nonnull
  Integer extractMsLevel(Spectrum spectrum) {
    Integer msLevel = 1;
    String value = extractCVValue(spectrum.getCvParam(), MzMLCV.cvMSLevel);
    if (!Strings.isNullOrEmpty(value))
      msLevel = Integer.parseInt(value);
    return msLevel;
  }

  @Nullable
  Float extractChromatographyData(Spectrum spectrum) {

    ScanList scanListElement = spectrum.getScanList();
    if (scanListElement == null)
      return null;
    List<Scan> scanElements = scanListElement.getScan();
    if (scanElements == null)
      return null;

    for (Scan scan : scanElements) {
      List<CVParam> cvParams = scan.getCvParam();
      if (cvParams == null)
        continue;

      for (CVParam param : cvParams) {
        String accession = param.getAccession();
        String unitAccession = param.getUnitAccession();
        String value = param.getValue();
        if ((accession == null) || (value == null))
          continue;

        // Retention time (actually "Scan start time") MS:1000016
        if (accession.equals(MzMLCV.cvScanStartTime)) {
          try {
            float retentionTime;
            if ((unitAccession == null) || (unitAccession.equals(MzMLCV.cvUnitsMin1))
                || unitAccession.equals(MzMLCV.cvUnitsMin2)) {
              // Minutes
              retentionTime = Float.parseFloat(value) * 60f;
            } else {
              // Seconds
              retentionTime = Float.parseFloat(value);
            }
            return retentionTime;
          } catch (Exception e) {
            // Ignore incorrectly formatted numbers, just dump the
            // exception
            e.printStackTrace();
          }

        }
      }
    }

    return null;
  }


  @Nonnull
  String extractScanDefinition(Spectrum spectrum) {

    String scanFilter = extractCVValue(spectrum.getCvParam(), MzMLCV.cvScanFilterString);
    if (!Strings.isNullOrEmpty(scanFilter))
      return scanFilter;

    ScanList scanListElement = spectrum.getScanList();
    if (scanListElement != null) {
      List<Scan> scanElements = scanListElement.getScan();
      if (scanElements != null) {
        for (Scan scan : scanElements) {
          scanFilter = extractCVValue(scan.getCvParam(), MzMLCV.cvScanFilterString);
          if (!Strings.isNullOrEmpty(scanFilter))
            return scanFilter;
        }
      }
    }
    return spectrum.getId();
  }

  @Nonnull
  MsScanType extractScanType(Spectrum spectrum) {
    return MsScanType.UNKNOWN;
  }

  @Nonnull
  PolarityType extractPolarity(Spectrum spectrum) {

    if (haveCVParam(spectrum.getCvParam(), MzMLCV.cvPolarityPositive))
      return PolarityType.POSITIVE;

    if (haveCVParam(spectrum.getCvParam(), MzMLCV.cvPolarityNegative))
      return PolarityType.NEGATIVE;

    ScanList scanListElement = spectrum.getScanList();
    if (scanListElement != null) {
      List<Scan> scanElements = scanListElement.getScan();
      if (scanElements != null) {
        for (Scan scan : scanElements) {

          if (haveCVParam(scan.getCvParam(), MzMLCV.cvPolarityPositive))
            return PolarityType.POSITIVE;

          if (haveCVParam(scan.getCvParam(), MzMLCV.cvPolarityNegative))
            return PolarityType.NEGATIVE;

        }
      }
    }
    return PolarityType.UNKNOWN;

  }

  @Nullable
  ActivationInfo extractSourceFragmentation(Spectrum spectrum) {
    return null;
  }


  @Nonnull
  List<IsolationInfo> extractIsolations(Spectrum spectrum) {
    PrecursorList precursorListElement = spectrum.getPrecursorList();
    if ((precursorListElement == null) || (precursorListElement.getCount().equals(0)))
      return Collections.emptyList();

    List<IsolationInfo> isolations = new ArrayList<>();

    List<Precursor> precursorList = precursorListElement.getPrecursor();
    for (Precursor parent : precursorList) {

      Double precursorMz = null;
      Double isolationWindowTarget = null;
      Double isolationWindowLower = null;
      Double isolationWindowUpper = null;
      Integer precursorCharge = null;
      Optional<Integer> precursorScanNumber = getScanNumber(parent.getSpectrumRef());

      SelectedIonList selectedIonListElement = parent.getSelectedIonList();
      if ((selectedIonListElement == null) || (selectedIonListElement.getCount().equals(0)))
        return Collections.emptyList();
      List<ParamGroup> selectedIonParams = selectedIonListElement.getSelectedIon();
      if (selectedIonParams == null)
        continue;

      for (ParamGroup pg : selectedIonParams) {
        // cvMz is sometimes used is used in mzML 1.0 files
        String cvVal = extractCVValue(pg, MzMLCV.cvMz);
        if (!Strings.isNullOrEmpty(cvVal))
          precursorMz = Double.parseDouble(cvVal);

        cvVal = extractCVValue(pg, MzMLCV.cvPrecursorMz);
        if (!Strings.isNullOrEmpty(cvVal))
          precursorMz = Double.parseDouble(cvVal);

        cvVal = extractCVValue(pg, MzMLCV.cvChargeState);
        if (!Strings.isNullOrEmpty(cvVal))
          precursorCharge = Integer.parseInt(cvVal);

      }

      String cvVal =
          extractCVValue(parent.getIsolationWindow(), MzMLCV.cvIsolationWindowLowerOffset);
      if (!Strings.isNullOrEmpty(cvVal))
        isolationWindowLower = Double.parseDouble(cvVal);

      cvVal = extractCVValue(parent.getIsolationWindow(), MzMLCV.cvIsolationWindowUpperOffset);
      if (!Strings.isNullOrEmpty(cvVal))
        isolationWindowUpper = Double.parseDouble(cvVal);

      cvVal = extractCVValue(parent.getIsolationWindow(), MzMLCV.cvIsolationWindowTarget);
      if (!Strings.isNullOrEmpty(cvVal))
        isolationWindowTarget = Double.parseDouble(cvVal);

      Integer precursorScanNumberInt =
          precursorScanNumber.isPresent() ? Integer.valueOf(precursorScanNumber.get()) : null;

      if (precursorMz != null) {
        if (isolationWindowTarget == null)
          isolationWindowTarget = precursorMz;
        if (isolationWindowLower == null)
          isolationWindowLower = 0.5;
        if (isolationWindowUpper == null)
          isolationWindowUpper = 0.5;
        Range<Double> isolationRange = Range.closed(isolationWindowTarget - isolationWindowLower,
            isolationWindowTarget + isolationWindowUpper);
        IsolationInfo isolation = new SimpleIsolationInfo(isolationRange, null, precursorMz,
            precursorCharge, null, precursorScanNumberInt);
        isolations.add(isolation);
      }
    }

    return Collections.unmodifiableList(isolations);
  }

  @Nonnull
  SeparationType extractSeparationType(Spectrum spectrum) {
    return SeparationType.UNKNOWN;
  }

  /**
   * <p>
   * extractSeparationType.
   * </p>
   *
   * @param chromatogram a {@link uk.ac.ebi.jmzml.model.mzml.Chromatogram} object.
   * @return a {@link io.github.msdk.datamodel.SeparationType} object.
   */
  @Nonnull
  public SeparationType extractSeparationType(
      uk.ac.ebi.jmzml.model.mzml.Chromatogram chromatogram) {
    return SeparationType.UNKNOWN;
  }

  /**
   * <p>
   * extractChromatogramType.
   * </p>
   *
   * @param chromatogram a {@link uk.ac.ebi.jmzml.model.mzml.Chromatogram} object.
   * @return a {@link io.github.msdk.datamodel.ChromatogramType} object.
   */
  @Nonnull
  public ChromatogramType extractChromatogramType(
      uk.ac.ebi.jmzml.model.mzml.Chromatogram chromatogram) {

    if (haveCVParam(chromatogram.getCvParam(), MzMLCV.cvChromatogramTIC))
      return ChromatogramType.TIC;

    if (haveCVParam(chromatogram.getCvParam(), MzMLCV.cvChromatogramMRM_SRM))
      return ChromatogramType.MRM_SRM;

    if (haveCVParam(chromatogram.getCvParam(), MzMLCV.cvChromatogramSIC))
      return ChromatogramType.SIC;

    if (haveCVParam(chromatogram.getCvParam(), MzMLCV.cvChromatogramBPC))
      return ChromatogramType.BPC;

    return ChromatogramType.UNKNOWN;
  }

  /**
   * <p>
   * extractIsolations.
   * </p>
   *
   * @param chromatogram a {@link uk.ac.ebi.jmzml.model.mzml.Chromatogram} object.
   * @return a {@link java.util.List} object.
   */

  @Nonnull
  public List<IsolationInfo> extractIsolations(
      uk.ac.ebi.jmzml.model.mzml.Chromatogram chromatogram) {
    if (extractChromatogramType(chromatogram) == ChromatogramType.MRM_SRM) {
      Double precursorIsolationMz = null, productIsolationMz = null,
          precursorActivationEnergy = null;
      ActivationType precursorActivation = ActivationType.UNKNOWN;
      ActivationInfo activationInfo = null;

      // Precursor isolation window
      String cvVal = extractCVValue(chromatogram.getPrecursor().getIsolationWindow().getCvParam(),
          MzMLCV.cvIsolationWindowTarget);
      if (!Strings.isNullOrEmpty(cvVal))
        precursorIsolationMz = Double.parseDouble(cvVal);

      // Precursor activation
      if (haveCVParam(chromatogram.getPrecursor().getActivation().getCvParam(),
          MzMLCV.cvActivationCID))
        precursorActivation = ActivationType.CID;

      cvVal = extractCVValue(chromatogram.getPrecursor().getActivation().getCvParam(),
          MzMLCV.cvActivationEnergy);
      if (!Strings.isNullOrEmpty(cvVal))
        precursorActivationEnergy = Double.parseDouble(cvVal);

      Optional<Integer> precursorScanNumber =
          getScanNumber(chromatogram.getPrecursor().getSpectrumRef());

      // Product isolation window
      cvVal = extractCVValue(chromatogram.getProduct().getIsolationWindow().getCvParam(),
          MzMLCV.cvIsolationWindowTarget);
      if (!Strings.isNullOrEmpty(cvVal))
        productIsolationMz = Double.parseDouble(cvVal);

      if (precursorActivationEnergy != null) {
        activationInfo = new SimpleActivationInfo(precursorActivationEnergy, precursorActivation);
      }

      List<IsolationInfo> isolations = new ArrayList<>();
      IsolationInfo isolationInfo = null;

      Integer precursorScanNumberInt =
          precursorScanNumber.isPresent() ? Integer.valueOf(precursorScanNumber.get()) : null;

      if (precursorIsolationMz != null) {
        isolationInfo = new SimpleIsolationInfo(Range.singleton(precursorIsolationMz), null,
            precursorIsolationMz, null, activationInfo, precursorScanNumberInt);
        isolations.add(isolationInfo);
      }

      if (productIsolationMz != null) {
        isolationInfo = new SimpleIsolationInfo(Range.singleton(productIsolationMz), null,
            productIsolationMz, null, null, null);
        isolations.add(isolationInfo);
      }

      return Collections.unmodifiableList(isolations);
    }

    return Collections.emptyList();

  }

  static @Nonnull double[] extractMzValues(Spectrum spectrum, @Nullable double[] array) {

    BinaryDataArrayList dataList = spectrum.getBinaryDataArrayList();

    if ((dataList == null) || (dataList.getCount().equals(0)))
      return new double[0];

    // Obtain the data arrays from spectrum
    final BinaryDataArray mzArray = dataList.getBinaryDataArray().get(0);
    final Number mzValues[] = mzArray.getBinaryDataAsNumberArray();

    // Allocate space for the data points
    if ((array == null) || (array.length < mzValues.length))
      array = new double[mzValues.length];

    // Copy the actual data point values
    for (int i = 0; i < mzValues.length; i++) {
      array[i] = mzValues[i].doubleValue();
    }

    return array;
  }

  static @Nonnull float[] extractIntensityValues(Spectrum spectrum, @Nullable float[] array) {

    BinaryDataArrayList dataList = spectrum.getBinaryDataArrayList();

    if ((dataList == null) || (dataList.getCount().equals(0)))
      return new float[0];

    // Obtain the data arrays from spectrum
    final BinaryDataArray intensityArray = dataList.getBinaryDataArray().get(1);
    final Number intensityValues[] = intensityArray.getBinaryDataAsNumberArray();

    // Allocate space for the data points
    if ((array == null) || (array.length < intensityValues.length))
      array = new float[intensityValues.length];

    // Copy the actual data point values
    for (int i = 0; i < intensityValues.length; i++) {
      array[i] = intensityValues[i].floatValue();
    }

    return array;

  }

  static @Nonnull float[] extractRtValues(uk.ac.ebi.jmzml.model.mzml.Chromatogram jmzChromatogram,
      @Nullable float[] array) {

    BinaryDataArrayList dataList = jmzChromatogram.getBinaryDataArrayList();

    if ((dataList == null) || (dataList.getCount().equals(0)))
      return new float[0];

    // Obtain the data arrays from chromatogram
    final BinaryDataArray rtArray = dataList.getBinaryDataArray().get(0);
    final Number rtValues[] = rtArray.getBinaryDataAsNumberArray();

    // Allocate space for the data points
    if ((array == null) || (array.length < rtValues.length))
      array = new float[rtValues.length];

    // Copy the actual data point values
    for (int i = 0; i < rtValues.length; i++) {
      final float rt = rtValues[i].floatValue();
      array[i] = rt;
    }

    return array;

  }

  static @Nonnull float[] extractIntensityValues(
      uk.ac.ebi.jmzml.model.mzml.Chromatogram jmzChromatogram, @Nullable float[] array) {

    BinaryDataArrayList dataList = jmzChromatogram.getBinaryDataArrayList();

    if ((dataList == null) || (dataList.getCount().equals(0)))
      return new float[0];

    // Obtain the data arrays from chromatogram
    final BinaryDataArray intensityArray = dataList.getBinaryDataArray().get(1);
    final Number intensityValues[] = intensityArray.getBinaryDataAsNumberArray();

    // Allocate space for the data points
    if ((array == null) || (array.length < intensityValues.length))
      array = new float[intensityValues.length];

    // Copy the actual data point values
    for (int i = 0; i < intensityValues.length; i++) {
      array[i] = intensityValues[i].floatValue();
    }

    return array;

  }

  @Nullable
  Double extractMz(uk.ac.ebi.jmzml.model.mzml.Chromatogram jmzChromatogram) {
    return null;
  }

  private boolean haveCVParam(@Nullable List<CVParam> cvParams, @Nonnull String cvParam) {
    if (cvParams == null)
      return false;
    for (CVParam param : cvParams) {
      String accession = param.getAccession();
      if (accession == null)
        continue;
      if (accession.equals(cvParam)) {
        return true;
      }
    }
    return false;
  }

  private @Nullable String extractCVValue(@Nullable ParamGroup pg, @Nonnull String cvParam) {
    if (pg == null)
      return null;
    return extractCVValue(pg.getCvParam(), cvParam);
  }

  private @Nullable String extractCVValue(@Nullable List<CVParam> cvParams,
      @Nonnull String cvParam) {
    if (cvParams == null)
      return null;
    for (CVParam param : cvParams) {
      String accession = param.getAccession();
      if (accession == null)
        continue;
      if (accession.equals(cvParam)) {
        return param.getValue();
      }
    }
    return null;
  }

  /**
   * <p>
   * getScanNumber.
   * </p>
   *
   * @param spectrumId a {@link java.lang.String} object.
   * @return a {@link java.lang.Integer} object.
   */
  public Optional<Integer> getScanNumber(String spectrumId) {
    final Pattern pattern = Pattern.compile("scan=([0-9]+)");
    final Matcher matcher = pattern.matcher(spectrumId);
    boolean scanNumberFound = matcher.find();

    // Some vendors include scan=XX in the ID, some don't, such as
    // mzML converted from WIFF files. See the definition of nativeID in
    // http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo
    // So, get the value of the index tag if the scanNumber is not present in the ID
    if (scanNumberFound) {
      Integer scanNumber = Integer.parseInt(matcher.group(1));
      return Optional.ofNullable(scanNumber);
    }

    return Optional.ofNullable(null);
  }

}
