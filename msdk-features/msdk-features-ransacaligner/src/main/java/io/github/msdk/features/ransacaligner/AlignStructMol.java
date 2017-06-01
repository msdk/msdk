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
package io.github.msdk.features.ransacaligner;

import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import java.util.Comparator;

public class AlignStructMol implements Comparator<AlignStructMol> {

    public FeatureTableRow row1, row2;
    public double RT, RT2;
    public boolean Aligned = false;
    public boolean ransacMaybeInLiers;
    public boolean ransacAlsoInLiers;

    public AlignStructMol(FeatureTableRow row1, FeatureTableRow row2) {
	this.row1 = row1;
	this.row2 = row2;
	RT = row1.getChromatographyInfo().getRetentionTime();
	RT2 = row2.getChromatographyInfo().getRetentionTime();
    }

   /* public AlignStructMol(FeatureTableRow row1, FeatureTableRow row2, RawDataFile file,
	    RawDataFile file2) {
	this.row1 = row1;
	this.row2 = row2;
	if (row1.getPeak(file) != null) {
	    RT = row1.getPeak(file).getRT();
	} else {
	    RT = row1.getAverageRT();
	}

	if (row2.getPeak(file2) != null) {
	    RT2 = row2.getPeak(file2).getRT();
	} else {
	    RT = row1.getAverageRT();
	}
    }*/

    AlignStructMol() {

    }

    public int compare(AlignStructMol arg0, AlignStructMol arg1) {
	if (arg0.RT < arg1.RT) {
	    return -1;
	} else {
	    return 1;
	}
    }
}