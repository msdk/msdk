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
package io.github.msdk.filtering.scanfilters;

import com.google.common.collect.Range;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.filtering.MSDKFilteringAlgorithm;
import javax.annotation.Nonnull;

/**
 * <p>CropFilterAlgorithm class.</p>
 */
public class CropFilterAlgorithm implements MSDKFilteringAlgorithm {

    private final @Nonnull Range<Double> mzRange;
    private final @Nonnull Range<Float> rtRange;
    private final @Nonnull DataPointStore store;

    /**
     * <p>Constructor for CropFilterAlgorithm.</p>
     *
     * @param mzRange a {@link com.google.common.collect.Range} object.
     * @param rtRange a {@link com.google.common.collect.Range} object.
     * @param store a {@link io.github.msdk.datamodel.datapointstore.DataPointStore} object.
     */
    public CropFilterAlgorithm(@Nonnull Range<Double> mzRange, @Nonnull Range<Float> rtRange, @Nonnull DataPointStore store) {
        this.mzRange = mzRange;
        this.rtRange = rtRange;
        this.store = store;

    }

    /** {@inheritDoc} */
    @Override
    public MsScan performFilter(@Nonnull MsScan scan) {

        MsSpectrumDataPointList dataPoints = MSDKObjectBuilder.getMsSpectrumDataPointList();
        Float rt = scan.getChromatographyInfo().getRetentionTime();

        // Do only if the scan's retention time is inside the user defined retention time range
        if (rt != null && rtRange.contains(rt.floatValue())) {

            // Select the data points with mz value inside the user defined mz range                
            Range<Float> intensityRange = Range.all();
            scan.getDataPoints(dataPoints);

            // Create a new scan
            MsScan newScan = MSDKObjectBuilder.getMsScan(store, scan.getScanNumber(), scan.getMsFunction());
            newScan.setChromatographyInfo(scan.getChromatographyInfo());
            newScan.setRawDataFile(scan.getRawDataFile());

            // Store the new data points
            newScan.setDataPoints(dataPoints.selectDataPoints(mzRange, intensityRange));
            return newScan;
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "Crop Filter";
    }

}
