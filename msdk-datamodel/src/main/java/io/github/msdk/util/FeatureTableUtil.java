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
package io.github.msdk.util;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.impl.converter.CopyConverter;
import io.github.msdk.datamodel.impl.converter.IonAnnotationConverter;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.SeparationType;

/**
 * <p>
 * FeatureTableUtil class.
 * </p>
 */
public class FeatureTableUtil {

  /**
   * Re-calculates the average m/z and RT values for a feature table
   *
   * @param featureTable the {@link io.github.msdk.datamodel.featuretables.FeatureTable} to apply
   *        the recalculation on.
   */
  public static void recalculateAverages(@Nonnull FeatureTable featureTable) {

    List<FeatureTableRow> rows = featureTable.getRows();
    Double mz;
    Float rt;
    double totalMz;
    float totalRt;
    int mzCount, rtCount;

    // Create row m/z and RT columns is they are missing in the table
    FeatureTableColumn<?> column;
    if (featureTable.getColumn(ColumnName.MZ, null) == null) {
      column = MSDKObjectBuilder.getMzFeatureTableColumn();
      featureTable.addColumn(column);
    }
    if (featureTable.getColumn("Chromatography Info", null, ChromatographyInfo.class) == null) {
      column = MSDKObjectBuilder.getChromatographyInfoFeatureTableColumn();
      featureTable.addColumn(column);
    }

    for (FeatureTableRow row : rows) {
      List<Sample> samples = featureTable.getSamples();

      totalMz = 0;
      totalRt = 0;
      mzCount = 0;
      rtCount = 0;
      for (Sample sample : samples) {
        FeatureTableColumn<Double> mzColumn = featureTable.getColumn(ColumnName.MZ, sample);
        if (mzColumn != null) {
          mz = row.getData(mzColumn);
          if (mz != null) {
            totalMz += mz;
            mzCount++;
          }
        }

        FeatureTableColumn<ChromatographyInfo> rtColumn =
            featureTable.getColumn(ColumnName.RT, sample);
        if (rtColumn != null) {
          ChromatographyInfo ri = row.getData(rtColumn);
          if (ri != null) {
            rt = ri.getRetentionTime();
            if (rt != null) {
              totalRt += rt;
              rtCount++;
            }
          }
        }
      }

      // Update m/z
      FeatureTableColumn<Double> mzColumn = featureTable.getColumn(ColumnName.MZ, null);
      Double newMz = totalMz / mzCount;
      row.setData(mzColumn, newMz);

      // Update ppm
      FeatureTableColumn<List<IonAnnotation>> ionAnnotationColumn =
          featureTable.getColumn(ColumnName.IONANNOTATION, null);
      if (ionAnnotationColumn != null) {
        List<IonAnnotation> ionAnnotations = row.getData(ionAnnotationColumn);
        if (ionAnnotations != null) {
          Double totalIonMz = 0d;
          Integer counter = 0;
          for (IonAnnotation ionAnnotation : ionAnnotations) {
            if (ionAnnotation != null) {
              Double ionMz = ionAnnotation.getExpectedMz();
              if (ionMz != null) {
                totalIonMz = totalIonMz + ionMz;
                counter++;
              }
            }
          }
          if (counter > 0) {
            Double ionMz = totalIonMz / counter;
            FeatureTableColumn<Double> ppmColumn = featureTable.getColumn(ColumnName.PPM, null);
            Double diff = Math.abs(newMz - ionMz);
            row.setData(ppmColumn, (diff / ionMz) * 1000000);
          }
        }
      }

      // Update RT
      FeatureTableColumn<ChromatographyInfo> chromInfoColumn =
          featureTable.getColumn("Chromatography Info", null, ChromatographyInfo.class);
      if (chromInfoColumn != null) {
        ChromatographyInfo currentChromatographyInfo = row.getData(chromInfoColumn);
        SeparationType separationType;
        if (currentChromatographyInfo == null) {
          separationType = SeparationType.UNKNOWN;
        } else {
          separationType = currentChromatographyInfo.getSeparationType();
        }
        ChromatographyInfo chromatographyInfo =
            MSDKObjectBuilder.getChromatographyInfo1D(separationType, totalRt / rtCount);
        row.setData(chromInfoColumn, chromatographyInfo);
      }
    }
  }

  /**
   * Copies common values such as identification results and comments from the source row to the
   * target row.
   *
   * @param sourceFeatureTableRow the source
   *        {@link io.github.msdk.datamodel.featuretables.FeatureTableRow} to copy the common values
   *        from.
   * @param targetFeatureTableRow a {@link io.github.msdk.datamodel.featuretables.FeatureTableRow}
   *        object.
   * @param combineData a {@link java.lang.Boolean} object which specifies if data will be added or
   *        replaced.
   */
  @SuppressWarnings("null")
  public static void copyCommonValues(@Nonnull FeatureTableRow sourceFeatureTableRow,
      @Nonnull FeatureTableRow targetFeatureTableRow, @Nonnull Boolean combineData) {

    List<FeatureTableColumn<?>> sourceColumns =
        sourceFeatureTableRow.getFeatureTable().getColumns();
    List<FeatureTableColumn<?>> targetColumns =
        targetFeatureTableRow.getFeatureTable().getColumns();

    for (FeatureTableColumn sourceColumn : sourceColumns) {

      // Only add common values and ignore ID column
      if (sourceColumn.getSample() == null
          & !sourceColumn.getName().equals(ColumnName.ID.getName())) {

        // Find target column
        FeatureTableColumn targetColumn = null;
        for (FeatureTableColumn<?> column : targetColumns) {
          boolean equalName = sourceColumn.getName().equals(column.getName());
          boolean equalSample = true;
          if (sourceColumn.getSample() == null & column.getSample() != null)
            equalSample = false;
          else if (sourceColumn.getSample() != null & column.getSample() == null)
            equalSample = false;
          else if (sourceColumn.getSample() != null & column.getSample() != null)
            equalSample = sourceColumn.getSample().getName().equals(column.getSample().getName());

          if (equalName & equalSample) {
            targetColumn = column;
            continue;
          }
        }

        // Handle combine option
        if (combineData) {
          switch (sourceColumn.getName()) {
            case "Ion Annotation":
              new IonAnnotationConverter().apply(sourceFeatureTableRow, sourceColumn,
                  targetFeatureTableRow, targetColumn);
              break;
          }
        } else {
          // Only add common values
          if (sourceColumn.getSample() == null) {
            new CopyConverter().apply(sourceFeatureTableRow, sourceColumn, targetFeatureTableRow,
                targetColumn);

          }
        }
      }
    }
  }

  /**
   * Copies sample specific feature values from the source row to the target row.
   *
   * @param sourceFeatureTableRow the source
   *        {@link io.github.msdk.datamodel.featuretables.FeatureTableRow} to copy the common values
   *        from.
   * @param sample the target {@link Sample}.
   * @param targetFeatureTableRow a {@link io.github.msdk.datamodel.featuretables.FeatureTableRow}
   *        object.
   */
  public static void copyFeatureValues(@Nonnull FeatureTableRow sourceFeatureTableRow,
      @Nonnull FeatureTableRow targetFeatureTableRow, @Nonnull Sample sample) {

    List<FeatureTableColumn<?>> sourceColumns =
        sourceFeatureTableRow.getFeatureTable().getColumns();
    List<FeatureTableColumn<?>> targetColumns =
        targetFeatureTableRow.getFeatureTable().getColumns();

    for (final FeatureTableColumn<?> sourceColumn : sourceColumns) {

      // Only add sample specific values
      if (sourceColumn.getSample() != null) {
        if (sourceColumn.getSample().equals(sample)) {

          // Find target column
          FeatureTableColumn targetColumn = null;
          for (FeatureTableColumn<?> column : targetColumns) {
            boolean equalName = sourceColumn.getName().equals(column.getName());
            boolean equalSample = true;
            if (sourceColumn.getSample() != null & column.getSample() != null)
              equalSample = sourceColumn.getSample().getName().equals(column.getSample().getName());

            if (equalName & equalSample) {
              targetColumn = column;
              continue;
            }
          }

          Object data = sourceFeatureTableRow.getData(sourceColumn);
          if (data != null) {
            new CopyConverter().apply(sourceFeatureTableRow, sourceColumn, targetFeatureTableRow,
                targetColumn);
          }
        }
      }
    }
  }

  /**
   * Calculates in how many sample the feature is found. It is assumed that at least one of the
   * following columns are present in the feature table: m/z, area or height.
   *
   * @param featureTableRow the {@link io.github.msdk.datamodel.featuretables.FeatureTableRow} to
   *        apply the calculation on.
   * @return a int.
   */
  public static int getRowCount(FeatureTableRow featureTableRow) {
    int count = 0;
    FeatureTable featureTable = featureTableRow.getFeatureTable();
    FeatureTableColumn<?> column;
    for (Sample sample : featureTable.getSamples()) {
      column = featureTable.getColumn(ColumnName.MZ, sample);
      if (column != null) {
        if (featureTableRow.getData(column) != null) {
          count++;
          continue;
        }
      }
      column = featureTable.getColumn(ColumnName.AREA, sample);
      if (column != null) {
        if (featureTableRow.getData(column) != null) {
          count++;
          continue;
        }
      }
      column = featureTable.getColumn(ColumnName.HEIGHT, sample);
      if (column != null) {
        if (featureTableRow.getData(column) != null) {
          count++;
          continue;
        }
      }
    }
    return count;
  }

  /**
   * Calculates the average duration of a feature across a set of samples in a feature table row.
   *
   * @param featureTableRow the {@link io.github.msdk.datamodel.featuretables.FeatureTableRow} to
   *        apply the calculation on.
   * @return a {@link java.lang.Double} object.
   */
  public static Double getAverageFeatureDuration(FeatureTableRow featureTableRow) {
    FeatureTable featureTable = featureTableRow.getFeatureTable();
    FeatureTableColumn<?> column;
    Double averageDuration = 0d;
    int sampleCount = 0;

    for (Sample sample : featureTable.getSamples()) {
      column = featureTable.getColumn(ColumnName.DURATION, sample);
      if (column != null) {
        double duration = (Double) featureTableRow.getData(column);
        averageDuration = averageDuration + duration;
        sampleCount++;
      }
    }

    // Return null if no duration is found
    if (sampleCount == 0) {
      averageDuration = null;
    } else {
      averageDuration /= sampleCount;
    }

    return averageDuration;
  }

  /**
   * <p>
   * clone.
   * </p>
   *
   * @param dataStore a {@link io.github.msdk.datamodel.datastore.DataPointStore} object.
   * @param featureTable a {@link io.github.msdk.datamodel.featuretables.FeatureTable} object.
   * @param newName {@link java.lang.String} object.
   * @return a {@link io.github.msdk.datamodel.featuretables.FeatureTable} object.
   */
  @Nonnull
  static public FeatureTable clone(@Nonnull DataPointStore dataStore,
      @Nonnull FeatureTable featureTable, @Nonnull String newName) {

    Preconditions.checkNotNull(dataStore);
    Preconditions.checkNotNull(featureTable);
    Preconditions.checkNotNull(newName);

    // Create new feature table
    FeatureTable newFeatureTable = MSDKObjectBuilder.getFeatureTable(newName, dataStore);

    // Copy columns
    for (FeatureTableColumn<?> column : featureTable.getColumns()) {
      FeatureTableColumn<?> newColumn = MSDKObjectBuilder.getFeatureTableColumn(column.getName(),
          column.getDataTypeClass(), column.getSample());

      // Common columns
      if (column.getSample() == null) {
        if (column.getName() == ColumnName.MZ.getName())
          newColumn = MSDKObjectBuilder.getMzFeatureTableColumn();
        if (column.getName() == ColumnName.CHARGE.getName())
          MSDKObjectBuilder.getChargeFeatureTableColumn();
        if (column.getName() == "Chromatography Info")
          MSDKObjectBuilder.getChromatographyInfoFeatureTableColumn();
        if (column.getName() == ColumnName.ID.getName())
          MSDKObjectBuilder.getIdFeatureTableColumn();
        if (column.getName() == "Ion Annotation")
          MSDKObjectBuilder.getIonAnnotationFeatureTableColumn();
        if (column.getName() == ColumnName.PPM.getName())
          MSDKObjectBuilder.getPpmFeatureTableColumn();
      }

      newFeatureTable.addColumn(newColumn);
    }

    // Copy rows
    for (FeatureTableRow row : featureTable.getRows()) {

      // Create a new row with the common feature data
      FeatureTableRow newRow = MSDKObjectBuilder.getFeatureTableRow(newFeatureTable, row.getId());
      copyCommonValues(row, newRow, false);

      // ID column
      FeatureTableColumn<Integer> column = featureTable.getColumn(ColumnName.ID, null);
      newRow.setData(column, row.getId());

      // Copy the feature data for the samples
      for (Sample sample : row.getFeatureTable().getSamples()) {
        copyFeatureValues(row, newRow, sample);
      }

      // Add the feature row to the table if it is not null
      if (newRow != null)
        newFeatureTable.addRow(newRow);
    }

    return newFeatureTable;
  }

  /**
   * <p>
   * getFeatureMzRange.
   * </p>
   *
   * @param row a {@link io.github.msdk.datamodel.featuretables.FeatureTableRow} object.
   * @return a {@link com.google.common.collect.Range} object.
   */
  public static Range<Double> getFeatureMzRange(FeatureTableRow row) {
    FeatureTable featureTable = row.getFeatureTable();
    FeatureTableColumn<?> column;
    double min = 0d;
    double max = 0d;

    for (Sample sample : featureTable.getSamples()) {
      column = featureTable.getColumn(ColumnName.MZ, sample);
      if (column != null) {
        if (row.getData(column) != null) {
          double mz = (double) row.getData(column);
          if (mz < min || min == 0d)
            min = mz;
          if (mz > max || max == 0d)
            max = mz;
        }
      }
    }

    return Range.closed(min, max);
  }

  /**
   * <p>
   * getFeatureRt1Range.
   * </p>
   *
   * @param row a {@link io.github.msdk.datamodel.featuretables.FeatureTableRow} object.
   * @return a {@link com.google.common.collect.Range} object.
   */
  public static Range<Float> getFeatureRt1Range(FeatureTableRow row) {
    FeatureTable featureTable = row.getFeatureTable();
    FeatureTableColumn<ChromatographyInfo> column;
    float min = 0f;
    float max = 0f;

    for (Sample sample : featureTable.getSamples()) {
      column = featureTable.getColumn(ColumnName.RT, sample);
      if (column != null) {
        if (row.getData(column) != null) {
          ChromatographyInfo chromatographyInfo = row.getData(column);
          if (chromatographyInfo.getRetentionTime() != null) {
            float rt = (float) chromatographyInfo.getRetentionTime();
            if (rt < min || min == 0d)
              min = rt;
            if (rt > max || max == 0d)
              max = rt;
          }
        }
      }
    }

    return Range.closed(min, max);
  }

  /**
   * <p>
   * copyIdValues.
   * </p>
   *
   * @param featureTable a {@link io.github.msdk.datamodel.featuretables.FeatureTable} object.
   * @param result a {@link io.github.msdk.datamodel.featuretables.FeatureTable} object.
   */
  public static void copyIdValues(FeatureTable featureTable, FeatureTable result) {

    // Copy rows
    List<FeatureTableRow> sourceRows = featureTable.getRows();
    List<FeatureTableRow> targetRows = result.getRows();
    for (int i = 0; i < sourceRows.size(); i++) {
      FeatureTableColumn<Integer> sourceIdColumn = featureTable.getColumn(ColumnName.ID, null);
      FeatureTableColumn<Integer> targetIdColumn = result.getColumn(ColumnName.ID, null);
      new CopyConverter().apply(sourceRows.get(i), sourceIdColumn, targetRows.get(i),
          targetIdColumn);

    }

  }

}
