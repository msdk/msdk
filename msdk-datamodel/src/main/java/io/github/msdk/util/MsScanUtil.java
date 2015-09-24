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

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.rawdata.MsScan;

public class MsScanUtil {

    @Nonnull
    static public MsScan clone(@Nonnull DataPointStore newStore,
            @Nonnull MsScan scan, @Nonnull Boolean copyDataPoints) {

        Preconditions.checkNotNull(newStore);
        Preconditions.checkNotNull(scan);
        Preconditions.checkNotNull(copyDataPoints);

        MsScan newScan = MSDKObjectBuilder.getMsScan(newStore,
                scan.getScanNumber(), scan.getMsFunction());

        newScan.setPolarity(scan.getPolarity());
        newScan.setMsScanType(scan.getMsScanType());
        newScan.setScanningRange(scan.getScanningRange());
        newScan.setChromatographyInfo(scan.getChromatographyInfo());
        newScan.setSourceInducedFragmentation(
                scan.getSourceInducedFragmentation());
        newScan.getIsolations().addAll(scan.getIsolations());

        if (copyDataPoints) {
            final MsSpectrumDataPointList dataPointList = MSDKObjectBuilder
                    .getMsSpectrumDataPointList();
            scan.getDataPoints(dataPointList);
            newScan.setDataPoints(dataPointList);
        }

        return newScan;
    }

}
