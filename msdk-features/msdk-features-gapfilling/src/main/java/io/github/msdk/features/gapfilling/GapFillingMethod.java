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

package io.github.msdk.features.gapfilling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.impl.SimpleIonAnnotation;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.featdet.chromatogramtofeaturetable.ChromatogramToFeatureTableMethod;
import io.github.msdk.featdet.targeteddetection.TargetedDetectionMethod;
import io.github.msdk.util.FeatureTableUtil;
import io.github.msdk.util.tolerances.MzTolerance;
import io.github.msdk.util.tolerances.RTTolerance;

/**
 * This class fills in the missing gaps in a FeatureTable.
 */
public class GapFillingMethod implements MSDKMethod<FeatureTable> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  // Input variables
  private final @Nonnull FeatureTable featureTable;
  private final @Nonnull DataPointStore dataStore;
  private @Nonnull MzTolerance mzTolerance;
  private @Nonnull RTTolerance rtTolerance;
  private final @Nonnull Double intensityTolerance;
  private final @Nonnull String nameSuffix;

  // Helper variables
  private boolean canceled = false;
  private int processedGaps = 0, totalGaps = 0;
  private final @Nonnull FeatureTable result;

  /**
   * <p>
   * Constructor for GapFillingMethod.
   * </p>
   *
   * @param featureTable a {@link io.github.msdk.datamodel.featuretables.FeatureTable} object.
   * @param dataStore a {@link io.github.msdk.datamodel.datastore.DataPointStore} object.
   * @param mzTolerance an object that implements the
   *        {@link io.github.msdk.util.tolerances.MzTolerance} interface.
   * @param rtTolerance a {@link io.github.msdk.util.tolerances.RTTolerance} object.
   * @param intensityTolerance a {@link java.lang.Double} object.
   * @param nameSuffix a {@link java.lang.String} object.
   */
  public GapFillingMethod(@Nonnull FeatureTable featureTable, @Nonnull DataPointStore dataStore,
      @Nonnull MzTolerance mzTolerance, @Nonnull RTTolerance rtTolerance,
      @Nonnull Double intensityTolerance, @Nonnull String nameSuffix) {
    this.featureTable = featureTable;
    this.dataStore = dataStore;
    this.mzTolerance = mzTolerance;
    this.rtTolerance = rtTolerance;
    this.intensityTolerance = intensityTolerance;
    this.nameSuffix = nameSuffix;

    // Make a copy of the input feature table
    result = FeatureTableUtil.clone(dataStore, featureTable, featureTable.getName() + nameSuffix);

    // Copy ID values
    FeatureTableUtil.copyIdValues(featureTable, result);

  }

  /** {@inheritDoc} */
  @Override
  public FeatureTable execute() throws MSDKException {
    List<FeatureTableRow> gapRow = new ArrayList<FeatureTableRow>();
    List<Sample> gapSample = new ArrayList<Sample>();

    // Total gaps
    for (FeatureTableRow row : result.getRows()) {
      for (Sample sample : result.getSamples()) {
        FeatureTableColumn<Double> areaColumn = result.getColumn(ColumnName.AREA, sample);
        Double area = row.getData(areaColumn);

        // Add the gap to arrays
        if (area == null) {
          gapRow.add(row);
          gapSample.add(sample);
        }
      }
    }

    totalGaps = gapRow.size();
    logger.info("Started gap filling " + totalGaps + " gap(s) in '" + featureTable.getName() + "'");

    if (totalGaps == 0)
      return result;

    // Iterate over all the gaps
    for (int i = 0; i < totalGaps; i++) {
      FeatureTableRow row = gapRow.get(i);
      Sample sample = gapSample.get(i);
      RawDataFile rawFile = sample.getRawDataFile();

      // Create an ion annotation
      SimpleIonAnnotation ion = new SimpleIonAnnotation();
      ion.setAnnotationId(row.getId().toString());
      FeatureTableColumn<Float> column =
          result.getColumn(ColumnName.RT.getName(), null, Float.class);
      ion.setExpectedMz(row.getMz());
      ion.setExpectedRetentionTime(row.getData(column));
      List<IonAnnotation> ionAnnotations = new ArrayList<IonAnnotation>();
      ionAnnotations.add(ion);

      TargetedDetectionMethod chromBuilder = new TargetedDetectionMethod(ionAnnotations, rawFile,
          dataStore, mzTolerance, rtTolerance, intensityTolerance, 0.0);
      final List<Chromatogram> chromatograms = chromBuilder.execute();

      // Add the data to the feature table row
      Chromatogram chromatogram = chromatograms.get(0);
      // chromatogram.setIonAnnotation(null);
      Map<ColumnName, FeatureTableColumn<Object>> tableColumns =
          ChromatogramToFeatureTableMethod.addSampleColumns(result, sample);
      ChromatogramToFeatureTableMethod.addDataToRow(row, chromatogram, tableColumns);

      processedGaps++;

      // Cancel?
      if (canceled)
        return null;
    }

    // Recalculate average values
    FeatureTableUtil.recalculateAverages(result);

    return result;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Float getFinishedPercentage() {
    return totalGaps == 0 ? null : (float) processedGaps / totalGaps;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public FeatureTable getResult() {
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    canceled = true;
  }

}
