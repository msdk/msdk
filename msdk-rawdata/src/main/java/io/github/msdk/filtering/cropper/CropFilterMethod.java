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
package io.github.msdk.filtering.cropper;

import com.google.common.collect.Range;
import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.datapointstore.DataPointStoreFactory;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import java.util.List;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CropFilterMethod implements MSDKMethod<RawDataFile> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final @Nonnull
    RawDataFile rawDataFile;
    private final @Nonnull
    Range<Double> mzRange;
    private final @Nonnull
    Range<Float> rtRange;

    private int processedScans = 0, totalScans = 0;
    private RawDataFile result;
    private boolean canceled = false;

    public CropFilterMethod(@Nonnull RawDataFile rawDataFile, @Nonnull Range<Double> mzRange, @Nonnull Range<Float> rtRange) {
        this.rawDataFile = rawDataFile;
        this.mzRange = mzRange;
        this.rtRange = rtRange;
    }

    @Override
    public Float getFinishedPercentage() {
        if (totalScans == 0) {
            return null;
        } else {
            return (float) processedScans / totalScans;
        }
    }

    @Override
    public void cancel() {
        this.canceled = true;
    }

    @Override
    public RawDataFile execute() throws MSDKException {
        logger.info("Started Crop Filter with Raw Data File #"
                + rawDataFile.getName());

        result = MSDKObjectBuilder.getRawDataFile(this.rawDataFile.getName(), this.rawDataFile.getOriginalFile(), this.rawDataFile.getRawDataFileType(), DataPointStoreFactory.getMemoryDataStore());
        List<MsScan> scans = rawDataFile.getScans();
        totalScans = scans.size();
        for (MsScan scan : scans) {
            Float rt = scan.getChromatographyInfo().getRetentionTime();
            if (rt != null && rtRange.contains(rt.floatValue())) {
                MsSpectrumDataPointList dataPoints = MSDKObjectBuilder.getMsSpectrumDataPointList();
                scan.getDataPoints(dataPoints);
                Range<Float> intensityRange = Range.closed(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
                scan.getDataPointsByMzAndIntensity(dataPoints, mzRange, intensityRange);
                DataPointStore store = DataPointStoreFactory.getMemoryDataStore();
                store.storeDataPoints(dataPoints);
                MsScan newScan =MSDKObjectBuilder.getMsScan(store, scan.getScanNumber(), scan.getMsFunction());
                 if (canceled) {
                    return null;
                }
                result.addScan(newScan);
            }
            processedScans++;
        }

        
        logger.info("Finished Crop Filter with Raw Data File #"
                + rawDataFile.getName());
        return result;
    }

    @Override
    public RawDataFile getResult() {
        return result;
    }

}
