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
package io.github.msdk.alignment.ransac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.optimization.fitting.PolynomialFitter;
import org.apache.commons.math.optimization.general.GaussNewtonOptimizer;
import org.apache.commons.math.stat.regression.SimpleRegression;

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.FeatureTable;
import io.github.msdk.datamodel.FeatureTableRow;
import io.github.msdk.datamodel.Sample;
import io.github.msdk.datamodel.SimpleFeatureTable;
import io.github.msdk.datamodel.SimpleFeatureTableRow;
import io.github.msdk.util.FeatureTableUtil;
import io.github.msdk.util.tolerances.MzTolerance;
import io.github.msdk.util.tolerances.RTTolerance;

/**
 * This class aligns feature tables using the RANSAC method.
 */
public class RansacAlignerMethod implements MSDKMethod<FeatureTable> {
  // Variables
  private final @Nonnull MzTolerance mzTolerance;
  private final @Nonnull RTTolerance rtTolerance;
  private final @Nonnull RTTolerance rtToleranceAfterCorrection;
  private final @Nonnull String featureTableName;
  private final @Nonnull List<FeatureTable> featureTables;
  private final @Nonnull SimpleFeatureTable result;
  private boolean canceled = false;
  private int processedFeatures = 0, totalFeatures = 0;
  private double t, dataPointsRate;
  private boolean linear;


  // ID counter for the new feature table
  private int newRowID = 1;

  /**
   * <p>
   * Constructor for RansacAlignerMethod.
   * </p>
   *
   * @param featureTables a {@link java.util.List} object.
   * @param dataStore a {@link io.github.msdk.datamodel.datastore.DataPointStore} object.
   * @param mzTolerance a {@link io.github.msdk.util.MZTolerance} object.
   * @param featureTableName a {@link java.lang.String} object.
   * @param rtTolerance a {@link io.github.msdk.util.RTTolerance} object.
   * @param t a threshold value for determining when a data point fits a mode
   * @param linear a {@link java.lang.Boolean} object.
   * @param dataPointsRate % of datapoints from the data required to assert that a model fits well
   *        to data. If it is 0, the variable will be set as 0.1
   */
  public RansacAlignerMethod(@Nonnull List<FeatureTable> featureTables,
      @Nonnull MzTolerance mzTolerance, @Nonnull RTTolerance rtTolerance,
      @Nonnull String featureTableName, @Nonnull double t, @Nonnull boolean linear,
      @Nonnull double dataPointsRate) {
    this.featureTables = featureTables;
    this.mzTolerance = mzTolerance;
    this.rtToleranceAfterCorrection = rtTolerance;
    this.rtTolerance = new RTTolerance(rtToleranceAfterCorrection.getTolerance() * 2, false);
    this.featureTableName = featureTableName;
    this.t = t;
    this.linear = linear;
    this.dataPointsRate = dataPointsRate;

    // Make a new feature table
    result = new SimpleFeatureTable();
  }

  /** {@inheritDoc} */
  @Override
  public FeatureTable execute() throws MSDKException {

    // Calculate number of feature to process. Each feature will be
    // processed twice: first for score calculation and then for actual
    // alignment.
    for (FeatureTable featureTable : featureTables) {
      totalFeatures += featureTable.getRows().size() * 2;
    }

    // Iterate through all feature tables
    Boolean firstFeatureTable = true;
    for (FeatureTable featureTable : featureTables) {

      // Add columns from the original feature table to the result table
      firstFeatureTable = false;

      // Create a sorted array of matching scores between two rows
      List<RowVsRowScore> scoreSet = new ArrayList<RowVsRowScore>();

      // Calculate scores for all possible alignments of this row
      for (FeatureTableRow row : featureTable.getRows()) {

        final Double mz = row.getMz();
        if (mz == null)
          continue;

        // Calculate the m/z range limit for the current row
        Range<Double> mzRange = mzTolerance.getToleranceRange(mz);

        // Calculate the RT range limit for the current row
        Range<Float> rtRange = rtTolerance.getToleranceRange(row.getRT());



        processedFeatures++;

        if (canceled)
          return null;
      }

      // Create a table of mappings for best scores
      Hashtable<FeatureTableRow, FeatureTableRow> alignmentMapping =
          this.getAlignmentMap(featureTable);


      // Align all rows using the mapping
      for (FeatureTableRow sourceRow : featureTable.getRows()) {
        FeatureTableRow targetRow = alignmentMapping.get(sourceRow);

        // If we have no mapping for this row, add a new one
        if (targetRow == null) {
          targetRow = new SimpleFeatureTableRow(result);
          result.addRow(targetRow);
          // FeatureTableColumn<Integer> column = result.getColumn(ColumnName.ID, null);
          // targetRow.setData(column, newRowID);
          newRowID++;
        }

        // Add all features from the original row to the aligned row
        for (Sample sample : sourceRow.getFeatureTable().getSamples()) {
          // FeatureTableUtil.copyFeatureValues(sourceRow, targetRow, sample);
        }

        // Combine common values from the original row with the aligned
        // row
        // FeatureTableUtil.copyCommonValues(sourceRow, targetRow, true);

        processedFeatures++;
      }

      if (canceled)
        return null;

    }

    // Return the new feature table
    return result;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Float getFinishedPercentage() {
    return totalFeatures == 0 ? null : (float) processedFeatures / totalFeatures;
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

  private Hashtable<FeatureTableRow, FeatureTableRow> getAlignmentMap(FeatureTable featureTable) {

    // Create a table of mappings for best scores
    Hashtable<FeatureTableRow, FeatureTableRow> alignmentMapping =
        new Hashtable<FeatureTableRow, FeatureTableRow>();


    // Create a sorted set of scores matching
    TreeSet<RowVsRowScore> scoreSet = new TreeSet<RowVsRowScore>();

    // RANSAC algorithm
    List<AlignStructMol> list = ransacPeakLists(result, featureTable);
    PolynomialFunction function = this.getPolynomialFunction(list);

    List<FeatureTableRow> allRows = featureTable.getRows();

    for (FeatureTableRow row : allRows) {
      // Calculate limits for a row with which the row can be aligned
      Range<Double> mzRange = mzTolerance.getToleranceRange(row.getMz());

      Float rt;
      try {
        rt = new Float(function.value(row.getRT()));
      } catch (NullPointerException e) {
        rt = row.getRT();
      }
      if (Double.isNaN(rt) || rt == -1) {
        rt = row.getRT();
      }

      Range<Float> rtRange = rtToleranceAfterCorrection.getToleranceRange(rt);

      // Get all rows of the aligned feature table within the m/z and
      // RT limits
      List<FeatureTableRow> candidateRows =
          FeatureTableUtil.getRowsInsideRange(result, rtRange, mzRange);

      for (FeatureTableRow candidateRow : candidateRows) {
        RowVsRowScore score;



        try {
          double mzLength = mzRange.upperEndpoint() - mzRange.lowerEndpoint();
          double rtLength = rtRange.upperEndpoint() - rtRange.lowerEndpoint();
          score = new RowVsRowScore(row, candidateRow, mzLength, rtLength, new Float(rt));

          scoreSet.add(score);


        } catch (Exception e) {
          return null;
        }
      }
    }

    // Iterate scores by descending order
    Iterator<RowVsRowScore> scoreIterator = scoreSet.iterator();
    while (scoreIterator.hasNext()) {

      RowVsRowScore score = scoreIterator.next();

      // Check if the row is already mapped
      if (alignmentMapping.containsKey(score.getFeatureTableRow())) {
        continue;
      }

      // Check if the aligned row is already filled
      if (alignmentMapping.containsValue(score.getAlignedRow())) {
        continue;
      }

      alignmentMapping.put(score.getFeatureTableRow(), score.getAlignedRow());

    }

    return alignmentMapping;
  }

  /**
   * RANSAC
   * 
   * @param alignedPeakList
   * @param peakList
   * @return
   */
  private List<AlignStructMol> ransacPeakLists(FeatureTable alignedPeakList,
      FeatureTable peakList) {
    List<AlignStructMol> list = this.getVectorAlignment(alignedPeakList, peakList);
    RANSAC ransac = new RANSAC(t, linear, dataPointsRate);
    ransac.alignment(list);
    return list;
  }

  /**
   * Return the corrected RT of the row
   * 
   * @param row
   * @param list
   * @return
   */
  private PolynomialFunction getPolynomialFunction(List<AlignStructMol> list) {
    List<RTs> data = new ArrayList<RTs>();
    for (AlignStructMol m : list) {
      if (m.Aligned) {
        data.add(new RTs(m.RT2, m.RT));
      }
    }

    data = this.smooth(data);
    Collections.sort(data, new RTs());

    double[] xval = new double[data.size()];
    double[] yval = new double[data.size()];
    int i = 0;

    for (RTs rt : data) {
      xval[i] = rt.RT;
      yval[i++] = rt.RT2;
    }

    PolynomialFitter fitter = new PolynomialFitter(3, new GaussNewtonOptimizer(true));
    for (RTs rt : data) {
      fitter.addObservedPoint(1, rt.RT, rt.RT2);
    }
    try {
      return fitter.fit();

    } catch (Exception ex) {
      return null;
    }
  }

  private List<RTs> smooth(List<RTs> list) {
    // Add points to the model in between of the real points to smooth the
    // regression model
    Collections.sort(list, new RTs());

    for (int i = 0; i < list.size() - 1; i++) {
      RTs point1 = list.get(i);
      RTs point2 = list.get(i + 1);
      if (point1.RT < point2.RT - 2) {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(point1.RT, point1.RT2);
        regression.addData(point2.RT, point2.RT2);
        double rt = point1.RT + 1;
        while (rt < point2.RT) {
          RTs newPoint = new RTs(rt, regression.predict(rt));
          list.add(newPoint);
          rt++;
        }

      }
    }

    return list;
  }

  /**
   * Create the vector which contains all the possible aligned peaks.
   * 
   * @param peakListX
   * @param peakListY
   * @return vector which contains all the possible aligned peaks.
   */
  private List<AlignStructMol> getVectorAlignment(FeatureTable peakListX, FeatureTable peakListY) {

    List<AlignStructMol> alignMol = new ArrayList<AlignStructMol>();
    for (FeatureTableRow row : peakListX.getRows()) {

      // Calculate limits for a row with which the row can be aligned
      Range<Double> mzRange = mzTolerance.getToleranceRange(row.getMz());
      Range<Float> rtRange = rtTolerance.getToleranceRange(row.getRT());

      // Get all rows of the aligned peaklist within parameter limits
      List<FeatureTableRow> candidateRows =
          FeatureTableUtil.getRowsInsideRange(peakListY, rtRange, mzRange);

      for (FeatureTableRow candidateRow : candidateRows) {
        alignMol.add(new AlignStructMol(row, candidateRow));
      }
    }

    return alignMol;
  }

}
