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

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.datapointstore.DataPointStoreFactory;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.filtering.scanfilters.MeanFilterAlgorithm;
import java.util.List;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeanFilterMethod implements MSDKMethod<RawDataFile> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final @Nonnull RawDataFile rawDataFile;
    private final @Nonnull double windowLength;
    private final @Nonnull DataPointStore store;

    private int processedScans = 0, totalScans = 0;
    private RawDataFile result;
    private boolean canceled = false;

    public MeanFilterMethod(@Nonnull RawDataFile rawDataFile, @Nonnull double windowLength, @Nonnull DataPointStore store) {
        this.rawDataFile = rawDataFile;
        this.windowLength = windowLength;
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
        logger.info("Started Mean Filter with Raw Data File #"
                + rawDataFile.getName());

        // Create a new raw data file
        result = MSDKObjectBuilder.getRawDataFile(this.rawDataFile.getName(), this.rawDataFile.getOriginalFile(), this.rawDataFile.getRawDataFileType(), DataPointStoreFactory.getMemoryDataStore());

        // Get the scans from the user defined raw data file
        List<MsScan> scans = rawDataFile.getScans();
        totalScans = scans.size();
        for (MsScan scan : scans) {
            if (canceled) {
                return null;
            }
            MeanFilterAlgorithm meanFilter = new MeanFilterAlgorithm(scan, windowLength, store);
            MsScan newScan = meanFilter.execute();

            // Add the new scan to the created raw data file
            result.addScan(newScan);
            processedScans++;
        }
        
        logger.info("Finished Mean Filter with Raw Data File #"
                + rawDataFile.getName());
        return result;
    }

    @Override
    public RawDataFile getResult() {
        return result;
    }
}
