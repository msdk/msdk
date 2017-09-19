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

import java.util.Comparator;

/**
 * <p>RTs class.</p>
 *
 */
public class RTs implements Comparator<RTs> {

    public double RT;
    public double RT2;
    int map;

    /**
     * <p>Constructor for RTs.</p>
     */
    public RTs() {
    }

    /**
     * <p>Constructor for RTs.</p>
     *
     * @param RT a double.
     * @param RT2 a double.
     */
    public RTs(double RT, double RT2) {
	this.RT = RT + 0.001 / Math.random();
	this.RT2 = RT2 + 0.001 / Math.random();
    }

    /**
     * <p>compare.</p>
     *
     * @param arg0 a {@link io.github.msdk.alignment.ransac.RTs} object.
     * @param arg1 a {@link io.github.msdk.alignment.ransac.RTs} object.
     * @return a int.
     */
    public int compare(RTs arg0, RTs arg1) {
	if (arg0.RT < arg1.RT) {
	    return -1;
	} else {
	    return 1;
	}

    }
}
