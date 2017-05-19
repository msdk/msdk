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

package io.github.msdk.features.joinaligner;

import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;

/**
 * This class represents a score between a feature table row and an aligned feature table row
 */
class RowVsRowScore implements Comparable<RowVsRowScore> {

  private FeatureTableRow featureTableRow, alignedRow;
  double score;

  RowVsRowScore(FeatureTableRow featureTableRow, FeatureTableRow alignedRow, double mzMaxDiff,
      double mzWeight, double rtMaxDiff, double rtWeight) {

    this.featureTableRow = featureTableRow;
    this.alignedRow = alignedRow;

    // Get m/z and RT values
    Double mz1 = featureTableRow.getMz();
    Double mz2 = alignedRow.getMz();
    ChromatographyInfo ChromatographyInfo1 = featureTableRow.getChromatographyInfo();
    ChromatographyInfo ChromatographyInfo2 = alignedRow.getChromatographyInfo();
    Float rt1 = null;
    Float rt2 = null;
    if (ChromatographyInfo1 != null) {
      rt1 = ChromatographyInfo1.getRetentionTime();
    }
    if (ChromatographyInfo2 != null) {
      rt2 = ChromatographyInfo2.getRetentionTime();
    }

    // Calculate difference between m/z
    double mzDiff = 999;
    if (mz1 != null && mz2 != null)
      mzDiff = Math.abs(mz1 - mz2);

    // Calculate difference between RT values
    double rtDiff = 999;
    if (rt1 != null && rt2 != null)
      rtDiff = Math.abs(rt1 - rt2);

    score = ((1 - mzDiff / mzMaxDiff) * mzWeight) + ((1 - rtDiff / rtMaxDiff) * rtWeight);

  }

  /**
   * This method returns the feature table row which is being aligned
   */
  FeatureTableRow getFeatureTableRow() {
    return featureTableRow;
  }

  /**
   * This method returns the row of aligned feature table
   */
  FeatureTableRow getAlignedRow() {
    return alignedRow;
  }

  /**
   * This method returns score between the these two features (the lower score, the better the
   * match)
   */
  double getScore() {
    return score;
  }

  /**
   * <p>
   * compareTo.
   * </p>
   *
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   * @param object a {@link io.github.msdk.features.joinaligner.RowVsRowScore} object.
   * @return an int.
   */
  public int compareTo(RowVsRowScore object) {
    if (score < object.getScore())
      return 1;
    else
      return -1;

  }

}
