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

package io.github.msdk.io.mzml;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;

/**
 * This class reads mzML data format using the jmzml library.
 */
public class MzMLFileExportMethod implements MSDKMethod<File> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final @Nonnull File targetFile;

    private boolean canceled = false;

    private long totalScans = 0, totalChromatograms = 0, parsedScans,
            parsedChromatograms;

    /**
     * <p>
     * Constructor for MzMLFileImportMethod.
     * </p>
     *
     * @param targetFile
     *            a {@link java.io.File} object.
     */
    public MzMLFileExportMethod(@Nonnull File targetFile) {
        this.targetFile = targetFile;
    }

    /** {@inheritDoc} */
    @Override
    public File execute() throws MSDKException {

        logger.info("Started writing to file " + targetFile);

        // TODO

        parsedChromatograms = totalChromatograms;
        logger.info("Finished exporting");

        return targetFile;

    }

    /** {@inheritDoc} */
    @Override
    public Float getFinishedPercentage() {
        return (totalScans + totalChromatograms) == 0 ? null
                : (float) (parsedScans + parsedChromatograms)
                        / (totalScans + totalChromatograms);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public File getResult() {
        return targetFile;
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() {
        this.canceled = true;
    }

}
