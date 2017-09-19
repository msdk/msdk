/* 
 * (C) Copyright 2015-2017 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */

package io.github.msdk.alignment.ransac;

import io.github.msdk.datamodel.FeatureTableRow;

/**
 * This class represents a score between a feature table row and an aligned
 * feature table row
 */
class RowVsRowScore implements Comparable<RowVsRowScore> {

    private FeatureTableRow featureTableRow, alignedRow;
    double score;

    RowVsRowScore(FeatureTableRow featureTableRow, FeatureTableRow alignedRow,
            double mzMaxDiff, double rtMaxDiff, Float correctedRT) {

        this.featureTableRow = featureTableRow;
        this.alignedRow = alignedRow;

        // Get m/z and RT values
        Double mz1 = featureTableRow.getMz();
        Double mz2 = alignedRow.getMz();
        Float rt2 = alignedRow.getRT();
       
        // Calculate difference between m/z
        double mzDiff = 999;
        if (mz1 != null && mz2 != null)
            mzDiff = Math.abs(mz1 - mz2);

        // Calculate difference between RT values
        double rtDiff = 999;
        if (correctedRT != null && rt2 != null)
            rtDiff = Math.abs(correctedRT - rt2);

        score = ((1 - mzDiff / mzMaxDiff))
                + ((1 - rtDiff / rtMaxDiff));

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
     * This method returns score between the these two features (the lower
     * score, the better the match)
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
     * @param object
     *            a {@link io.github.msdk.features.joinaligner.RowVsRowScore}
     *            object.
     * @return an int.
     */
    public int compareTo(RowVsRowScore object) {
        if (score < object.getScore())
            return 1;
        else
            return -1;

    }

}
