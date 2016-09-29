/* 
 * (C) Copyright 2015-2016 by MSDK Development Team
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

package io.github.msdk.util.tolerances;

import io.github.msdk.datamodel.rawdata.MsScan;

public interface MzToleranceProvider {

    /**
     * <p>
     * Get a {@link io.github.msdk.util.tolerance.MzTolerance} for a given scan.
     * </p>
     *
     * @param MsScan
     *            an object that implements the
     *            {@link io.github.msdk.datamodel.rawdata.MsScan} interface.
     * @return A MzTolerance object.
     */
    MzTolerance getMzTolerance(MsScan scan);
}
