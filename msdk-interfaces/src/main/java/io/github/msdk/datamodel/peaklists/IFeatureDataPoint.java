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

package io.github.msdk.datamodel.peaklists;

import io.github.msdk.datamodel.rawdata.IChromatographyData;
import io.github.msdk.datamodel.rawdata.IDataPoint;
import io.github.msdk.datamodel.rawdata.IMsScan;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 
 */
public interface IFeatureDataPoint extends IDataPoint {

    /**
     * 
     * @return
     */
    @Nullable
    IMsScan getScan();

    /**
     * 
     * @return
     */
    @Nonnull
    Integer getScanNumber();

    /**
     * 
     * @return
     */
    @Nullable
    IChromatographyData getChromatographyData();

}
