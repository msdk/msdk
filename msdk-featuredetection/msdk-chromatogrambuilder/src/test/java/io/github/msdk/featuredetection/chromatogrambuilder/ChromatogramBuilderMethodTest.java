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

package io.github.msdk.featuredetection.chromatogrambuilder;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.datapointstore.DataPointStoreFactory;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.rawdataimport.mzml.MzMLFileImportMethod;
import io.github.msdk.util.MZTolerance;

public class ChromatogramBuilderMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @SuppressWarnings("null")
    @Test
    public void testOrbitrap() throws MSDKException {

        // Create the data structures
        DataPointStore dataStore = DataPointStoreFactory.getMemoryDataStore();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "orbitrap_300-600mz.mzML");
        Assert.assertTrue("Cannot read test data", inputFile.canRead());
        MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
        RawDataFile rawFile = importer.execute();
        Assert.assertNotNull(rawFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        final double minimumTimeSpan = 6; // 6s
        final double minimumHeight = 1E4;
        final MZTolerance mzTolerance = new MZTolerance(0.001, 5.0);
        ChromatogramBuilderMethod chromBuilder = new ChromatogramBuilderMethod(
                dataStore, rawFile, minimumTimeSpan, minimumHeight,
                mzTolerance);
        final List<Chromatogram> detectedFeatures = chromBuilder.execute();
        Assert.assertEquals(1.0, chromBuilder.getFinishedPercentage(), 0.0001);

        Assert.assertTrue(detectedFeatures.size() > 10);

    
    
    }
}
