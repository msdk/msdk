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

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CropFilterMethod implements MSDKMethod<RawDataFile> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final @Nonnull
    RawDataFile rawDataFile;

    private float methodProgress = 0f;
    private MsScan newScan;

    public CropFilterMethod(@Nonnull RawDataFile rawDataFile) {
        this.rawDataFile = rawDataFile;
    }

    @Override
    public Float getFinishedPercentage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cancel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RawDataFile execute() throws MSDKException {
        logger.info("Started Crop Filter with Raw Data File #"
            + rawDataFile.getName());

        logger.info("Finished Crop Filter with Raw Data File #"
            + rawDataFile.getName());
        return this.rawDataFile;
    }

    @Override
    public RawDataFile getResult() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
