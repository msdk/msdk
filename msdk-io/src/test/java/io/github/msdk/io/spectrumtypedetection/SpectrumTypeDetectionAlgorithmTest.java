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

package io.github.msdk.io.spectrumtypedetection;

import io.github.msdk.datamodel.rawdata.MassSpectrumType;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.rawdataimport.RawDataFileImportAlgorithm;

import java.io.File;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpectrumTypeDetectionAlgorithmTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Test the SpectrumTypeDetectionAlgorithm
     */
    @Ignore("not ready yet")
    @Test
    public void testSpectrumTypeDetectionAlgorithm() throws Exception {

        File inputFiles[] = new File("src/test/resources/spectrumtypedetection")
                .listFiles();

        Assert.assertNotNull(inputFiles);
        Assert.assertNotEquals(0, inputFiles.length);

        int filesTested = 0;

        for (File inputFile : inputFiles) {

            MassSpectrumType trueType;
            if (inputFile.getName().startsWith("centroided"))
                trueType = MassSpectrumType.CENTROIDED;
            else if (inputFile.getName().startsWith("thresholded"))
                trueType = MassSpectrumType.THRESHOLDED;
            else if (inputFile.getName().startsWith("profile"))
                trueType = MassSpectrumType.PROFILE;
            else
                continue;

            logger.info("Checking autodetection of centroided/thresholded/profile scans on file "
                    + inputFile.getName());

            RawDataFileImportAlgorithm importer = new RawDataFileImportAlgorithm(
                    inputFile);
            importer.execute();
            RawDataFile rawFile = importer.getResult();

            Assert.assertNotNull(rawFile);

            for (MsScan scan : rawFile.getScans()) {
                SpectrumTypeDetectionAlgorithm detector = new SpectrumTypeDetectionAlgorithm(
                        scan);
                detector.execute();
                MassSpectrumType detectedType = detector.getResult();
                Assert.assertNotNull(detectedType);

                Assert.assertEquals("Scan type wrongly detected for scan "
                        + scan.getScanNumber() + " in " + rawFile.getName(),
                        trueType, detectedType);
            }
            filesTested++;
        }

        // make sure we tested 10+ files
        Assert.assertTrue(filesTested > 10);
    }

}
