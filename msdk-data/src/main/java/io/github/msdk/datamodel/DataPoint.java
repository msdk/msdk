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

import javax.annotation.concurrent.Immutable;

/**
 * A single data point of a mass spectrum (a pair of m/z and intensity values).
 * DataPoints are immutable once created, to allow passing them by reference,
 * instead of cloning each data point instance when passing them between MSDK
 * modules.
 */
@Immutable
public interface DataPoint {

    /**
     * Returns the m/z value of this data point.
     * 
     * @return Data point m/z
     */
    double getMz();

    /**
     * Returns the intensity value of this data point.
     * 
     * @return Data point intensity.
     */
    double getIntensity();

}
