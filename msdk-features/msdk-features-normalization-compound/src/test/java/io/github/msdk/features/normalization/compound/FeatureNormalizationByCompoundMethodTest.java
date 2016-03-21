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

package io.github.msdk.features.normalization.compound;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.io.mztab.MzTabFileImportMethod;

public class FeatureNormalizationByCompoundMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @Test
    public void testMzTab_Sample() throws MSDKException {

        // Create the data structures
        DataPointStore dataStore = DataPointStoreFactory.getTmpFileDataStore();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "singleSample.mzTab");
        Assert.assertTrue(inputFile.canRead());
        MzTabFileImportMethod importer = new MzTabFileImportMethod(inputFile,
                dataStore);
        FeatureTable featureTable = importer.execute();
        Assert.assertNotNull(featureTable);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);
        Assert.assertEquals(93, featureTable.getRows().size());

        featureTable.dispose();
    }

}
