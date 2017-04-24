/*
 * (C) Copyright 2015-2016 by MSDK Development Team
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

package io.github.msdk.featdet.srmdetection;

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
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.chromatograms.ChromatogramType;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;

/**
 * This class creates a feature table based on the SRM chromatograms from a raw data file.
 */
public class SrmDetectionMethod implements MSDKMethod<List<Chromatogram>> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final @Nonnull RawDataFile rawDataFile;
  private final @Nonnull DataPointStore dataStore;

  private List<Chromatogram> result;
  private boolean canceled = false;
  private int parsed = 0, total = 0;

  /**
   * <p>
   * Constructor for SrmDetectionMethod.
   * </p>
   *
   * @param rawDataFile a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
   * @param dataPointStore a {@link io.github.msdk.datamodel.datastore.DataPointStore} object.
   */
  public SrmDetectionMethod(@Nonnull RawDataFile rawDataFile,
      @Nonnull DataPointStore dataPointStore) {
    this.rawDataFile = rawDataFile;
    this.dataStore = dataPointStore;

    // Make a new array
    result = new ArrayList<>();
  }

  /** {@inheritDoc} */
  @Override
  public List<Chromatogram> execute() throws MSDKException {

    logger.info("Started Srm chromatogram builder on file " + rawDataFile.getName());

    // Chromatograms
    List<Chromatogram> chromatograms = rawDataFile.getChromatograms();
    total += chromatograms.size();

    // Scans
    List<MsScan> scans = rawDataFile.getScans();
    total += scans.size();

    // Check if we have any chomatograms or scans
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
    Map<String, BuildingChromatogram> chromatogramMap = new HashMap<String, BuildingChromatogram>();
    Map<Double, Range<Double>> q1IsolationMzRangeMap = new HashMap<Double, Range<Double>>();
    Map<Double, Range<Double>> q3IsolationMzRangeMap = new HashMap<Double, Range<Double>>();
    for (MsScan scan : scans) {
      // Canceled
      if (canceled)
        return null;

      // Ignore non SRM scans
      MsFunction msFunction = scan.getMsFunction();
      if (!msFunction.getName().equals("srm")) {
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
      BuildingChromatogram buildingChromatogram = chromatogramMap.get(q1 + ";" + q3);
      if (buildingChromatogram == null) {
        buildingChromatogram = new BuildingChromatogram();
        chromatogramMap.put(q1 + ";" + q3, buildingChromatogram);

        // Store the mz isolation range for the q1 and q3 values
        q1IsolationMzRangeMap.put(q1, scan.getIsolations().get(0).getIsolationMzRange());
        q3IsolationMzRangeMap.put(q3, q3IsolationMzRange);
      }

      // Add the new data point
      ChromatographyInfo rt = scan.getChromatographyInfo();
      float intenstiy = scan.getIntensityValues()[0]; // Assume only 1
                                                      // value
      buildingChromatogram.addDataPoint(rt, 0d, intenstiy);

      parsed++;
    }

    // Add the newly generated chromatograms to the result list
    int chromatogramNumber = 1;
    Iterator<Map.Entry<String, BuildingChromatogram>> iterator =
        chromatogramMap.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, BuildingChromatogram> entry = iterator.next();
      String q1q3 = entry.getKey();
      BuildingChromatogram buildingChromatogram = entry.getValue();

      // Create the final chromatogram
      Chromatogram chromatogram = MSDKObjectBuilder.getChromatogram(dataStore, chromatogramNumber,
          ChromatogramType.MRM_SRM, SeparationType.UNKNOWN);

      // Add the data points to the final chromatogram
      ChromatographyInfo[] rtValues = buildingChromatogram.getRtValues();
      double[] mzValues = buildingChromatogram.getMzValues();
      float[] intensityValues = buildingChromatogram.getIntensityValues();
      int size = buildingChromatogram.getSize();
      chromatogram.setDataPoints(rtValues, mzValues, intensityValues, size);

      // Set the Q1 and Q3 values to the isolations for the chromatogram
      String[] strs = q1q3.split(";");
      double q1 = Double.parseDouble(strs[0]);
      double q3 = Double.parseDouble(strs[1]);
      List<IsolationInfo> isolations = chromatogram.getIsolations();
      IsolationInfo isolationInfo =
          MSDKObjectBuilder.getIsolationInfo(q1IsolationMzRangeMap.get(q1), null, q1, null, null);
      isolations.add(isolationInfo);
      isolationInfo =
          MSDKObjectBuilder.getIsolationInfo(q3IsolationMzRangeMap.get(q3), null, q3, null, null);
      isolations.add(isolationInfo);

      // Add the chromatogram
      result.add(chromatogram);

      chromatogramNumber++;

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
