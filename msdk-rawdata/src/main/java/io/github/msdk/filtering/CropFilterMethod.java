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
package io.github.msdk.filtering;

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

    private final @Nonnull RawDataFile rawDataFile;
    private final @Nonnull Range<Double> mzRange;
    private final @Nonnull Range<Float> rtRange;
    private final @Nonnull DataPointStore store;

    private int processedScans = 0, totalScans = 0;
    private RawDataFile result;
    private boolean canceled = false;

    public CropFilterMethod(@Nonnull RawDataFile rawDataFile, @Nonnull Range<Double> mzRange, @Nonnull Range<Float> rtRange, @Nonnull DataPointStore store) {
        this.rawDataFile = rawDataFile;
        this.mzRange = mzRange;
        this.rtRange = rtRange;
        this.store = store;

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

        // Create a new raw data file
        result = MSDKObjectBuilder.getRawDataFile(this.rawDataFile.getName(), this.rawDataFile.getOriginalFile(), this.rawDataFile.getRawDataFileType(), DataPointStoreFactory.getMemoryDataStore());

        // Get the scans from the user defined raw data file
        List<MsScan> scans = rawDataFile.getScans();
        totalScans = scans.size();

                
        // Check that the original RT Range includes the user defined RT Range
        Range<Float> originalRTRange = Range.closed(scans.get(0).getChromatographyInfo().getRetentionTime(), scans.get(scans.size() - 1).getChromatographyInfo().getRetentionTime());
        if (!originalRTRange.encloses(rtRange)) {
            throw new MSDKException(
                    "The RT Range excedes the limits of the RT range in the data.");
        }

        MsSpectrumDataPointList dataPoints = MSDKObjectBuilder.getMsSpectrumDataPointList();

        // Iterate through all the scans
        for (MsScan scan : scans) {
            Float rt = scan.getChromatographyInfo().getRetentionTime();

            // Do only if the scan's retention time is inside the user defined retention time range
            if (rt != null && rtRange.contains(rt.floatValue())) {
                // Check that the user defined MZ range is included into the data point's MZ range
                Range originalMZRange = scan.getMzRange();
                if (!originalMZRange.encloses(mzRange)) {
                    throw new MSDKException(
                            "The MZ Range excedes the limits of the MZ range in the data.");
                }

                // Select the data points with mz value inside the user defined mz range                
                Range<Float> intensityRange = Range.all();
                scan.getDataPoints(dataPoints);

                // Create a new scan
                MsScan newScan = MSDKObjectBuilder.getMsScan(store, scan.getScanNumber(), scan.getMsFunction());
                newScan.setChromatographyInfo(scan.getChromatographyInfo());
                newScan.setRawDataFile(result);
                // Store the new data points
                newScan.setDataPoints(dataPoints.selectDataPoints(mzRange, intensityRange));

                if (canceled) {
                    return null;
                }
                // Add the new scan to the created raw data file
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
