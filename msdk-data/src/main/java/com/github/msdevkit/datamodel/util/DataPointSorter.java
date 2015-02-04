/* 
 * Copyright 2015 MSDK Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.github.msdevkit.datamodel.util;

import java.util.Comparator;

import com.github.msdevkit.datamodel.DataPoint;

/**
 * This class implements Comparator class to provide a comparison between two
 * DataPoints.
 * 
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

	    if (direction == SortingDirection.Ascending)
		return result;
	    else
		return -result;

	case Intensity:
	    result = Double.compare(dp1.getIntensity(), dp2.getIntensity());

	    // If the data points have same intensity, we do a second comparison
	    // of m/z, to ensure that this comparator is consistent with
	    // equality: (compare(x, y)==0) == (x.equals(y)),
	    if (result == 0)
		result = Double.compare(dp1.getMz(), dp2.getMz());

	    if (direction == SortingDirection.Ascending)
		return result;
	    else
		return -result;
	default:
	    // We should never get here, so throw an exception
	    throw (new IllegalStateException());
	}

    }
}
