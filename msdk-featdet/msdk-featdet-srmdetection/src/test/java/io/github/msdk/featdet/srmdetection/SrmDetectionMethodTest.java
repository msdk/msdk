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

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzml.MzMLFileImportMethod;

public class SrmDetectionMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @SuppressWarnings("null")
    @Test
    public void testSRM() throws MSDKException {

        // Create the data structures
        final DataPointStore dataStore = DataPointStoreFactory
                .getMemoryDataStore();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "SRM.mzML");
        Assert.assertTrue("Cannot read test data", inputFile.canRead());
        MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
        RawDataFile rawFile = importer.execute();
        Assert.assertNotNull(rawFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // Parameters
        final Double minHeight = 100d;
        final Double intensityTolerance = 0.10d;

        // MS/MS detection method
        SrmDetectionMethod msMethod = new SrmDetectionMethod(rawFile, dataStore,
                minHeight, intensityTolerance, " SRM");
        final FeatureTable featureTable = msMethod.execute();
        Assert.assertEquals(1.0, msMethod.getFinishedPercentage(), 0.0001);

        /*
         * TODO: Write tests
         */
    }

}
