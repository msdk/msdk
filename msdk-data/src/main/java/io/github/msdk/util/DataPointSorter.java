/* 
 * (C) Copyright 2015 by MSDK Development Team
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

package io.github.msdk.util;

import io.github.msdk.datamodel.DataPoint;

import java.util.Comparator;

/**
 * This class implements a Comparator for two DataPoints.
 */
public class DataPointSorter implements Comparator<DataPoint> {

    private SortingProperty property;
    private SortingDirection direction;

    public DataPointSorter(SortingProperty property, SortingDirection direction) {
	this.property = property;
	this.direction = direction;
    }

    public int compare(DataPoint dp1, DataPoint dp2) {

	int result;

	switch (property) {
	case MZ:

	    result = Double.compare(dp1.getMz(), dp2.getMz());

	    // If the data points have same m/z, we do a second comparison of
	    // intensity, to ensure that this comparator is consistent with
	    // equality: (compare(x, y)==0) == (x.equals(y)),
	    if (result == 0)
		result = Double.compare(dp1.getIntensity(), dp2.getIntensity());

	    if (direction == SortingDirection.ASCENDING)
		return result;
	    else
		return -result;

	case INTENSITY:
	    result = Double.compare(dp1.getIntensity(), dp2.getIntensity());

	    // If the data points have same intensity, we do a second comparison
	    // of m/z, to ensure that this comparator is consistent with
	    // equality: (compare(x, y)==0) == (x.equals(y)),
	    if (result == 0)
		result = Double.compare(dp1.getMz(), dp2.getMz());

	    if (direction == SortingDirection.ASCENDING)
		return result;
	    else
		return -result;
	default:
	    // We should never get here, so throw an exception
	    throw (new IllegalStateException());
	}

    }
}
