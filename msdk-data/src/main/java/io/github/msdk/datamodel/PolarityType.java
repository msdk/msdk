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

package io.github.msdk.datamodel;

/**
 * Represents the polarity of ionization.
 */
public enum PolarityType {

    POSITIVE(+1), //
    NEGATIVE(-1), //
    NEUTRAL(0), //
    UNKNOWN(0);

    private final int sign;

    PolarityType(int sign) {
	this.sign = sign;
    }

    /**
     * @return +1 for positive polarity, -1 for negative polarity, and 0 for
     *         neutral or unknown polarity.
     */
    public int getSign() {
	return sign;
    }
}
