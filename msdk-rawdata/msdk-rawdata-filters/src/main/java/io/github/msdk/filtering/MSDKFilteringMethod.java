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
import io.github.msdk.datamodel.msspectra.MsSpectrum;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import java.util.List;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>MSDKFilteringMethod class.</p>
 *
 */
public class MSDKFilteringMethod implements MSDKMethod<RawDataFile> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final @Nonnull MSDKFilteringAlgorithm filteringAlgorithm;
    private final @Nonnull RawDataFile rawDataFile;
    private final @Nonnull DataPointStore store;

    private int processedScans = 0, totalScans = 0;
    private RawDataFile result;
    private boolean canceled = false;

    /**
     * <p>Constructor for MSDKFilteringMethod.</p>
     *
     * @param rawDataFile a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
     * @param filteringAlgorithm a {@link io.github.msdk.filtering.MSDKFilteringAlgorithm} object.
     * @param store a {@link io.github.msdk.datamodel.datapointstore.DataPointStore} object.
     */
    public MSDKFilteringMethod(@Nonnull RawDataFile rawDataFile, @Nonnull MSDKFilteringAlgorithm filteringAlgorithm, @Nonnull DataPointStore store) {
        this.filteringAlgorithm = filteringAlgorithm;
        this.rawDataFile = rawDataFile;
        this.store = store;
    }

    /** {@inheritDoc} */
    @Override
    public Float getFinishedPercentage() {
        if (totalScans == 0) {
            return null;
        } else {
            return (float) processedScans / totalScans;
        }
    }

    /** {@inheritDoc} */
    @Override
    public RawDataFile execute() throws MSDKException {
        logger.info("Started Filter " + this.filteringAlgorithm.getName() + " with Raw Data File #"
                + rawDataFile.getName());

        // Create a new raw data file
        result = MSDKObjectBuilder.getRawDataFile(this.rawDataFile.getName(), this.rawDataFile.getOriginalFile(), this.rawDataFile.getRawDataFileType(), store);

        List<MsScan> scans = rawDataFile.getScans();
        totalScans = scans.size();

        for (MsScan scan : scans) {
            if (canceled) {
                return null;
            }

            MsSpectrum newScan = this.filteringAlgorithm.performFilter(scan);

            // Add the new scan to the created raw data file
            if (newScan != null) {
                result.addScan((MsScan) newScan);
            }
            processedScans++;
        }
        logger.info("Finished Filter " + this.filteringAlgorithm.getName() + " with Raw Data File #"
                + rawDataFile.getName());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public RawDataFile getResult() {
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() {
        this.canceled = true;
    }

}
