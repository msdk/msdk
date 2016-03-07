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

package io.github.msdk.featdet.srmdetection;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.chromatograms.ChromatogramType;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.rawdata.RawDataFile;

/**
 * This class creates a feature table based on the SRM chromatograms from a raw
 * data file.
 */
public class SrmDetectionMethod implements MSDKMethod<List<Chromatogram>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final @Nonnull RawDataFile rawDataFile;
    private final @Nonnull DataPointStore dataStore;

    private List<Chromatogram> result;
    private boolean canceled = false;
    private int processedChromatograms = 0, totalChromatograms = 0;

    /**
     * <p>
     * Constructor for SrmDetectionMethod.
     * </p>
     *
     * @param rawDataFile
     *            a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
     * @param dataPointStore
     *            a {@link io.github.msdk.datamodel.datastore.DataPointStore}
     *            object.
     */
    public SrmDetectionMethod(@Nonnull RawDataFile rawDataFile,
            @Nonnull DataPointStore dataStore) {
        this.rawDataFile = rawDataFile;
        this.dataStore = dataStore;

        // Make a new array
        result = new ArrayList<>();
    }

    /** {@inheritDoc} */
    @Override
    public List<Chromatogram> execute() throws MSDKException {

        logger.info("Started Srm chromatogram builder on file "
                + rawDataFile.getName());

        List<Chromatogram> chromatograms = rawDataFile.getChromatograms();
        totalChromatograms = chromatograms.size();

        // Check if we have any chomatograms
        if (totalChromatograms == 0) {
            throw new MSDKException(
                    "No chromatograms provided for SRM detection method");
        }

        // Iterate over all chromatograms
        for (Chromatogram chromatogram : chromatograms) {
            // Canceled
            if (canceled)
                return null;

            // Ignore non SRM chromatograms
            if (chromatogram
                    .getChromatogramType() != ChromatogramType.MRM_SRM) {
                totalChromatograms += -1;
                continue;
            }

            // Add the SRM chromatogram to the list
            result.add(chromatogram);

            processedChromatograms++;
        }

        return result;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Float getFinishedPercentage() {
        return totalChromatograms == 0 ? null
                : (float) processedChromatograms / totalChromatograms;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public List<Chromatogram> getResult() {
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() {
        canceled = true;
    }

}
