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

/**
 * This class provides a MzTolerance that is constant for every MsScan.
 */
public class ConstantMzToleranceProvider implements MzToleranceProvider {

    private final MzTolerance mzTolerance;

    public ConstantMzToleranceProvider(MzTolerance mzTolerance) {
        this.mzTolerance = mzTolerance;
    }

    @Override
    public MzTolerance getMzTolerance(MsScan scan) {
        return mzTolerance;
    }

}
