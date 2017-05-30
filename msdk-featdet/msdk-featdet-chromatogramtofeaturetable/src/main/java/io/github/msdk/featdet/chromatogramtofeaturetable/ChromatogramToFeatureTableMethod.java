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

package io.github.msdk.featdet.chromatogramtofeaturetable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.chromatograms.ChromatogramType;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.impl.SimpleIonAnnotation;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.util.ChromatogramUtil;
import io.github.msdk.util.FeatureTableUtil;

/**
 * This class adds a list of chromatograms to a feature table.
 */
public class ChromatogramToFeatureTableMethod implements MSDKMethod<FeatureTable> {

  private final @Nonnull List<Chromatogram> chromatograms;
  private final @Nonnull FeatureTable featureTable;
  private final @Nonnull Sample sample;
  private final @Nonnull FeatureTableColumn<Double> q1Column =
      MSDKObjectBuilder.getFeatureTableColumn(ColumnName.Q1, null);
  private final @Nonnull FeatureTableColumn<Double> q3Column =
      MSDKObjectBuilder.getFeatureTableColumn(ColumnName.Q3, null);
  private final @Nonnull FeatureTableColumn<Integer> groupIdColumn =
      MSDKObjectBuilder.getFeatureTableColumn(ColumnName.GROUPID, null);
  Map<Double, Integer> srmGroups = new HashMap<Double, Integer>();

  private boolean canceled = false;
  private int processedChromatograms = 0, totalChromatograms = 0;

  /**
   * <p>
   * Constructor for ChromatogramToFeatureTableMethod.
   * </p>
   *
   * @param chromatograms a list of {@link io.github.msdk.datamodel.chromatograms.Chromatogram}
   *        objects.
   * @param featureTable a {@link io.github.msdk.datamodel.featuretables.FeatureTable} object.
   * @param sample a {@link io.github.msdk.datamodel.featuretables.Sample} object.
   */
  public ChromatogramToFeatureTableMethod(@Nonnull List<Chromatogram> chromatograms,
      @Nonnull FeatureTable featureTable, @Nonnull Sample sample) {
    this.chromatograms = chromatograms;
    this.featureTable = featureTable;
    this.sample = sample;
  }

  /** {@inheritDoc} */
  @Override
  public FeatureTable execute() throws MSDKException {
    totalChromatograms = chromatograms.size();

    // Add the common columns to the table if needed
    addCommonColumns(featureTable);

    // Add the sample columns to the table if needed
    Map<ColumnName, FeatureTableColumn<Object>> tableColumns =
        addSampleColumns(featureTable, sample);

    // Check if cancel is requested
    if (canceled)
      return null;

    // Id of last row in feature table
    int lastID = 0;
    List<FeatureTableRow> rows = featureTable.getRows();
    if (!rows.isEmpty()) {
      lastID = featureTable.getRows().get(featureTable.getRows().size()).getId();
    }

    // Loop through all chromatograms and add values to the feature table
    FeatureTableColumn<Object> column;
    for (Chromatogram chromatogram : chromatograms) {
      lastID++;
      FeatureTableRow newRow = MSDKObjectBuilder.getFeatureTableRow(featureTable, lastID);
      column = featureTable.getColumn(ColumnName.ID, null);
      newRow.setData(column, lastID);

      // Add the data to the feature table row
      addDataToRow(newRow, chromatogram, tableColumns);

      // Add Q3 column and data for SRM chromatograms
      if (chromatogram.getChromatogramType() == ChromatogramType.MRM_SRM) {

        // Get Q1 and Q3 values
        List<IsolationInfo> isolations = chromatogram.getIsolations();
        Double mzQ1 = isolations.get(0).getPrecursorMz();
        Double mzQ3 = isolations.get(1).getPrecursorMz();

        // m/z column
        column = featureTable.getColumn(ColumnName.MZ, sample);
        newRow.setData(column, mzQ1);

        // SRM Q1 column
        column = featureTable.getColumn(ColumnName.Q1, null);
        if (column == null)
          featureTable.addColumn(q1Column);
        column = featureTable.getColumn(ColumnName.Q1, null);
        newRow.setData(column, mzQ1);

        // SRM Q3 column
        column = featureTable.getColumn(ColumnName.Q3, null);
        if (column == null)
          featureTable.addColumn(q3Column);
        column = featureTable.getColumn(ColumnName.Q3, null);
        newRow.setData(column, mzQ3);

        // Group ID column
        column = featureTable.getColumn(ColumnName.GROUPID, null);
        if (column == null)
          featureTable.addColumn(groupIdColumn);
        column = featureTable.getColumn(ColumnName.GROUPID, null);

        // Group ID
        Integer groupID = srmGroups.get(mzQ1);
        if (groupID == null) {
          srmGroups.put(mzQ1, processedChromatograms + 1);

          // Assign the first row to the 0 ID
          newRow.setData(column, 0);
        } else {
          newRow.setData(column, groupID);
        }
      }

      // Add row to feature table
      featureTable.addRow(newRow);

      // Increase counter
      processedChromatograms++;

      // Check if cancel is requested
      if (canceled)
        return null;
    }

    // Re-calculate average row m/z and RT values
    FeatureTableUtil.recalculateAverages(featureTable);

    return featureTable;
  }

  /**
   * <p>
   * addDataToRow.
   * </p>
   *
   * @param row a {@link io.github.msdk.datamodel.featuretables.FeatureTableRow} object.
   * @param chromatogram a {@link io.github.msdk.datamodel.chromatograms.Chromatogram} object.
   * @param tableColumns a {@link java.util.Map} object.
   */
  @SuppressWarnings("unchecked")
  public static void addDataToRow(@Nonnull FeatureTableRow row, @Nonnull Chromatogram chromatogram,
      @Nullable Map<ColumnName, FeatureTableColumn<Object>> tableColumns) {

    FeatureTable featureTable = row.getFeatureTable();

    // Get tableColumns
    if (tableColumns == null) {

    }

    // Data structures
    float rtBuffer[];
    float intensityBuffer[] = new float[10000];

    // Load data
    rtBuffer = chromatogram.getRetentionTimes();
    intensityBuffer = chromatogram.getIntensityValues(intensityBuffer);
    int numOfDataPoints = chromatogram.getNumberOfDataPoints();

    FeatureTableColumn<Object> column;

    if (chromatogram.getIonAnnotation() != null) {
      column = featureTable.getColumn(ColumnName.IONANNOTATION, null);
      List<IonAnnotation> ionAnnotations = (List<IonAnnotation>) row.getData(column);
      if (ionAnnotations == null)
        ionAnnotations = new ArrayList<IonAnnotation>();
      ionAnnotations.add(chromatogram.getIonAnnotation());
      row.setData(column, ionAnnotations);
    }

    column = tableColumns.get(ColumnName.CHROMATOGRAM);
    row.setData(column, chromatogram);

    Double mz = chromatogram.getMz();
    column = tableColumns.get(ColumnName.MZ);
    if (mz != null)
      row.setData(column, mz);

    Float rt = ChromatogramUtil.getRt(rtBuffer, intensityBuffer, numOfDataPoints);
    column = tableColumns.get(ColumnName.RT);
    row.setData(column, rt);

    Float rtStart = ChromatogramUtil.getRtStart(rtBuffer, numOfDataPoints);
    column = tableColumns.get(ColumnName.RTSTART);
    if (rtStart != null) {
      double doubleVal = rtStart;
      row.setData(column, doubleVal);
    }

    Float rtEnd = ChromatogramUtil.getRtEnd(rtBuffer, numOfDataPoints);
    column = tableColumns.get(ColumnName.RTEND);
    if (rtEnd != null) {
      double doubleVal = rtEnd;
      row.setData(column, doubleVal);
    }

    Float duration = ChromatogramUtil.getDuration(rtBuffer, numOfDataPoints);
    column = tableColumns.get(ColumnName.DURATION);
    if (duration != null) {
      double doubleVal = duration;
      row.setData(column, doubleVal);
    }

    Double area = ChromatogramUtil.getArea(rtBuffer, intensityBuffer, numOfDataPoints);
    column = tableColumns.get(ColumnName.AREA);
    if (area != null)
      row.setData(column, area);

    Float height = ChromatogramUtil.getMaxHeight(intensityBuffer, numOfDataPoints);
    column = tableColumns.get(ColumnName.HEIGHT);
    if (height != null)
      row.setData(column, height);

    column = tableColumns.get(ColumnName.NUMBEROFDATAPOINTS);
    row.setData(column, numOfDataPoints);

    Double fwhm = ChromatogramUtil.getFwhm(rtBuffer, intensityBuffer, numOfDataPoints);
    if (fwhm != null) {
      column = tableColumns.get(ColumnName.FWHM);
      row.setData(column, fwhm);
    }

    Double tailingFactor =
        ChromatogramUtil.getTailingFactor(rtBuffer, intensityBuffer, numOfDataPoints);
    if (tailingFactor != null) {
      column = tableColumns.get(ColumnName.TAILINGFACTOR);
      row.setData(column, tailingFactor);
    }

    Double asymmetryFactor =
        ChromatogramUtil.getAsymmetryFactor(rtBuffer, intensityBuffer, numOfDataPoints);
    if (asymmetryFactor != null) {
      column = tableColumns.get(ColumnName.ASYMMETRYFACTOR);
      row.setData(column, asymmetryFactor);
    }

  }

  private void addCommonColumns(@Nonnull FeatureTable featureTable) {
    // Common columns
    // Only add common columns if the feature table is empty
    if (featureTable.getColumns().isEmpty()) {
      FeatureTableColumn<Integer> idColumn = MSDKObjectBuilder.getIdFeatureTableColumn();
      FeatureTableColumn<Double> mzColumn = MSDKObjectBuilder.getMzFeatureTableColumn();
      FeatureTableColumn<Double> ppmColumn = MSDKObjectBuilder.getPpmFeatureTableColumn();
      FeatureTableColumn<Float> rtColumn =
          MSDKObjectBuilder.getRetentionTimeFeatureTableColumn();
      FeatureTableColumn<List<SimpleIonAnnotation>> ionAnnotationColumn =
          MSDKObjectBuilder.getIonAnnotationFeatureTableColumn();
      featureTable.addColumn(idColumn);
      featureTable.addColumn(mzColumn);
      featureTable.addColumn(ppmColumn);
      featureTable.addColumn(rtColumn);
      featureTable.addColumn(ionAnnotationColumn);
    }

  }

  /**
   * <p>
   * addSampleColumns.
   * </p>
   *
   * @param featureTable a {@link io.github.msdk.datamodel.featuretables.FeatureTable} object.
   * @param sample a {@link io.github.msdk.datamodel.featuretables.Sample} object.
   * @return a {@link java.util.Map} object.
   */
  public static Map<ColumnName, FeatureTableColumn<Object>> addSampleColumns(
      @Nonnull FeatureTable featureTable, @Nonnull Sample sample) {
    final Map<ColumnName, FeatureTableColumn<Object>> tableColumns =
        new EnumMap<>(ColumnName.class);

    // Sample columns
    ArrayList<ColumnName> sampleColumns = new ArrayList<>();
    sampleColumns.add(ColumnName.CHROMATOGRAM);
    sampleColumns.add(ColumnName.MZ);
    sampleColumns.add(ColumnName.RT);
    sampleColumns.add(ColumnName.RTSTART);
    sampleColumns.add(ColumnName.RTEND);
    sampleColumns.add(ColumnName.DURATION);
    sampleColumns.add(ColumnName.AREA);
    sampleColumns.add(ColumnName.HEIGHT);
    sampleColumns.add(ColumnName.NUMBEROFDATAPOINTS);
    sampleColumns.add(ColumnName.FWHM);
    sampleColumns.add(ColumnName.TAILINGFACTOR);
    sampleColumns.add(ColumnName.ASYMMETRYFACTOR);

    for (ColumnName columnName : sampleColumns) {
      FeatureTableColumn<Object> column = featureTable.getColumn(columnName, sample);
      if (column == null) {
        column = MSDKObjectBuilder.getFeatureTableColumn(columnName, sample);
        featureTable.addColumn(column);
      }
      tableColumns.put(columnName, column);
    }

    return tableColumns;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Float getFinishedPercentage() {
    return totalChromatograms == 0 ? null : (float) processedChromatograms / totalChromatograms;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public FeatureTable getResult() {
    return featureTable;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    canceled = true;
  }

}
