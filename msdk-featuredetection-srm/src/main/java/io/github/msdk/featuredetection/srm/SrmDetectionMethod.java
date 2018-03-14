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

package io.github.msdk.featuredetection.srm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.Chromatogram;
import io.github.msdk.datamodel.ChromatogramType;
import io.github.msdk.datamodel.IsolationInfo;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.datamodel.SimpleChromatogram;
import io.github.msdk.datamodel.SimpleIsolationInfo;
import io.github.msdk.util.ChromatogramUtil;

/**
 * This class creates a feature table based on the SRM chromatograms from a raw data file.
 */
public class SrmDetectionMethod implements MSDKMethod<List<Chromatogram>> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final @Nonnull RawDataFile rawDataFile;

  private List<Chromatogram> result = new ArrayList<>();
  private boolean canceled = false;
  private int parsed = 0, total = 0;

  /**
   * <p>
   * Constructor for SrmDetectionMethod.
   * </p>
   *
   * @param rawDataFile a {@link io.github.msdk.datamodel.RawDataFile} object.
   */
  public SrmDetectionMethod(@Nonnull RawDataFile rawDataFile) {
    this.rawDataFile = rawDataFile;
  }

  /** {@inheritDoc} */
  @Override
  public List<Chromatogram> execute() throws MSDKException {

    logger.info("Started SRM chromatogram builder on file " + rawDataFile.getName());

    // Chromatograms
    List<Chromatogram> chromatograms = rawDataFile.getChromatograms();
    total += chromatograms.size();

    // Scans
    List<MsScan> scans = rawDataFile.getScans();
    total += scans.size();

    // Check if we have any chromatograms or scans
    if (total == 0) {
      throw new MSDKException("No chromatograms or scans provided for SRM detection method");
    }

    // Iterate over all chromatograms
    for (Chromatogram chromatogram : chromatograms) {
      // Canceled
      if (canceled)
        return null;

      // Ignore non SRM chromatograms
      if (chromatogram.getChromatogramType() != ChromatogramType.MRM_SRM) {
        parsed++;
        continue;
      }

      // Add the SRM chromatogram to the list
      result.add(chromatogram);

      parsed++;
    }

    // Iterate over all scans
    Map<String, SimpleChromatogram> chromatogramMap = new HashMap<String, SimpleChromatogram>();
    Map<Double, Range<Double>> q1IsolationMzRangeMap = new HashMap<Double, Range<Double>>();
    Map<Double, Range<Double>> q3IsolationMzRangeMap = new HashMap<Double, Range<Double>>();
    for (MsScan scan : scans) {
      // Canceled
      if (canceled)
        return null;

      // Ignore non SRM scans
      String msFunction = scan.getMsFunction();
      if (msFunction == null || (!msFunction.toLowerCase().contains("srm"))) {
        parsed++;
        continue;
      }

      // Q1 data
      Double q1 = scan.getIsolations().get(0).getPrecursorMz();

      // Q3 data
      /*
       * TODO: This is a workaround for issue # 123: https://github.com/msdk/msdk/issues/127
       */
      String scanDefinition = scan.getScanDefinition();
      Pattern pattern = Pattern.compile("(?<=\\[)(.*)(?=\\])");
      Matcher matcher = pattern.matcher(scanDefinition);
      Double q3 = 1d;
      Range<Double> q3IsolationMzRange = Range.singleton(q3);
      if (matcher.find()) {
        String str = matcher.group(0);
        String[] mzValues = str.split("-");
        double mz1 = Double.parseDouble(mzValues[0]);
        double mz2 = Double.parseDouble(mzValues[1]);
        q3 = (mz1 + mz2) / 2;
        q3IsolationMzRange = Range.closed(mz1, mz2);
      }

      // Get the chromatogram for the Q1 and Q3 value or generate a new
      SimpleChromatogram buildingChromatogram = chromatogramMap.get(q1 + ";" + q3);
      if (buildingChromatogram == null) {
        buildingChromatogram = new SimpleChromatogram();
        chromatogramMap.put(q1 + ";" + q3, buildingChromatogram);

        // Store the mz isolation range for the q1 and q3 values
        q1IsolationMzRangeMap.put(q1, scan.getIsolations().get(0).getIsolationMzRange());
        q3IsolationMzRangeMap.put(q3, q3IsolationMzRange);
      }

      // Add the new data point
      Float rt = scan.getRetentionTime();
      float intenstiy = scan.getIntensityValues()[0]; // Assume only 1 value
      buildingChromatogram.addDataPoint(rt, null, intenstiy);

      parsed++;
    }

    // Add the newly generated chromatograms to the result list
    Iterator<Map.Entry<String, SimpleChromatogram>> iterator =
        chromatogramMap.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, SimpleChromatogram> entry = iterator.next();
      String q1q3 = entry.getKey();
      SimpleChromatogram chromatogram = entry.getValue();

      // Set the Q1 and Q3 values to the isolations for the chromatogram
      String[] strs = q1q3.split(";");
      double q1 = Double.parseDouble(strs[0]);
      double q3 = Double.parseDouble(strs[1]);
      List<IsolationInfo> isolations = chromatogram.getIsolations();
      // TODO Also add precursor scan number
      IsolationInfo isolationInfo =
          new SimpleIsolationInfo(q1IsolationMzRangeMap.get(q1), null, q1, null, null, null);
      isolations.add(isolationInfo);
      isolationInfo =
          new SimpleIsolationInfo(q3IsolationMzRangeMap.get(q3), null, q3, null, null, null);
      isolations.add(isolationInfo);

      if ((chromatogram.getMz() == null) && (chromatogram.getMzValues() != null)) {
        double mzValues[] = chromatogram.getMzValues();
        Integer size = chromatogram.getNumberOfDataPoints();
        Double newMz = ChromatogramUtil.getMedianMz(mzValues, size);
        chromatogram.setMz(newMz);
      }

      // Add the chromatogram
      result.add(chromatogram);

      iterator.remove();
    }

    return result;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Float getFinishedPercentage() {
    return total == 0 ? null : (float) parsed / total;
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
    canceled = true;
  }

}
