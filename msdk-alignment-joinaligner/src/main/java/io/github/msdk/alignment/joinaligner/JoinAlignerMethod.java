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

package io.github.msdk.alignment.joinaligner;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
 * This class aligns feature tables based on a match score. The score is calculated based on the
 * mass and retention time of each peak using a set of tolerances.
 */
public class JoinAlignerMethod implements MSDKMethod<FeatureTable> {

  // Variables
  private final @Nonnull MzTolerance mzTolerance;
  private final @Nonnull RTTolerance rtTolerance;
  private final int mzWeight = 10;
  private final int rtWeight = 10;
  private final @Nonnull List<FeatureTable> featureTables;
  private final @Nonnull SimpleFeatureTable result;
  private boolean canceled = false;
  private int processedFeatures = 0, totalFeatures = 0;

  // ID counter for the new feature table
  private int newRowID = 1;

  /**
   * <p>
   * Constructor for MatchAlignerMethod.
   * </p>
   *
   * @param featureTables a {@link java.util.List} object.
   * @param mzTolerance an objectt
   * @param rtTolerance a {@link io.github.msdk.util.tolerances.RTTolerance} object.
   */
  public JoinAlignerMethod(@Nonnull List<FeatureTable> featureTables,
      @Nonnull MzTolerance mzTolerance, @Nonnull RTTolerance rtTolerance) {
    this.featureTables = featureTables;
    this.mzTolerance = mzTolerance;
    this.rtTolerance = rtTolerance;

    // Make a new feature table
    this.result = new SimpleFeatureTable();

  }

  /** {@inheritDoc} */
  @Override
  public FeatureTable execute() throws MSDKException {

    // Calculate number of feature to process.
    for (FeatureTable featureTable : featureTables) {
      totalFeatures += featureTable.getRows().size();
    }

    // Add all samples
    ArrayList<Sample> allSamples = new ArrayList<>();
    for (FeatureTable featureTable : featureTables) {
      allSamples.addAll(featureTable.getSamples());
    }
    result.setSamples(allSamples);

    // Iterate through all feature tables
    for (FeatureTable featureTable : featureTables) {

      // Create a sorted array of matching scores between two rows
      List<RowVsRowScore> scoreSet = new ArrayList<RowVsRowScore>();

      // Calculate scores for all possible alignments of this row
      for (FeatureTableRow row : featureTable.getRows()) {

        final Double mz = row.getMz();
        if (mz == null)
          continue;

        // Calculate the m/z range limit for the current row
        Range<Double> mzRange = mzTolerance.getToleranceRange(mz);

        // Continue if no chromatography info is available
        Float rt = row.getRT();
        if (rt == null)
          continue;

        // Calculate the RT range limit for the current row
        Range<Float> rtRange = rtTolerance.getToleranceRange(rt);

        // Get all rows of the aligned feature table within the m/z and
        // RT limits
        List<FeatureTableRow> candidateRows =
            FeatureTableUtil.getRowsInsideRange(result, rtRange, mzRange);

        // Calculate scores and store them
        for (FeatureTableRow candidateRow : candidateRows) {

          // Check charge
          Integer charge1 = row.getCharge();
          Integer charge2 = candidateRow.getCharge();
          if ((charge1 != null) && (charge2 != null) && (!charge1.equals(charge2)))
            continue;

          // Calculate score
          double mzLength = mzRange.upperEndpoint() - mzRange.lowerEndpoint();
          double rtLength = rtRange.upperEndpoint() - rtRange.lowerEndpoint();
          RowVsRowScore score = new RowVsRowScore(row, (SimpleFeatureTableRow) candidateRow, mzLength / 2.0, mzWeight,
              rtLength / 2.0, rtWeight);

          // Add the score to the array
          scoreSet.add(score);

        }

        // processedFeatures++;

        if (canceled)
          return null;
      }

      // Create a table of mappings for best scores
      Hashtable<FeatureTableRow, SimpleFeatureTableRow> alignmentMapping = new Hashtable<>();

      // Iterate scores by descending order
      Iterator<RowVsRowScore> scoreIterator = scoreSet.iterator();
      while (scoreIterator.hasNext()) {
        RowVsRowScore score = scoreIterator.next();

        // Check if the row is already mapped
        if (alignmentMapping.containsKey(score.getFeatureTableRow()))
          continue;

        // Check if the aligned row is already filled
        if (alignmentMapping.containsValue(score.getAlignedRow()))
          continue;

        alignmentMapping.put(score.getFeatureTableRow(), score.getAlignedRow());
      }

      // Align all rows using the mapping
      for (FeatureTableRow sourceRow : featureTable.getRows()) {
        SimpleFeatureTableRow targetRow = alignmentMapping.get(sourceRow);

        // If we have no mapping for this row, add a new one
        if (targetRow == null) {
          targetRow = new SimpleFeatureTableRow(result);
          result.addRow(targetRow);

          Integer sourceCharge = sourceRow.getCharge();

          if (sourceCharge != null)
            targetRow.setCharge(sourceCharge);

          List<Sample> samples = featureTable.getSamples();
          for (Sample s : samples) {
            targetRow.setFeature(s, sourceRow.getFeature(s));
          }

          newRowID++;
        }

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

}
