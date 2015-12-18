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

package io.github.msdk.matchaligner.matchaligner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.datapointstore.DataPointStoreFactory;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.io.mztab.MzTabFileImportMethod;
import io.github.msdk.util.MZTolerance;
import io.github.msdk.util.RTTolerance;

public class MatchAlignerMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @Test
    public void testMzTab_Samples() throws MSDKException {

        // Create the data structures
        DataPointStore dataStore = DataPointStoreFactory
                .getTmpFileDataPointStore();

        // Import file 1
        File inputFile = new File(TEST_DATA_PATH + "Sample 1.mzTab");
        Assert.assertTrue(inputFile.canRead());
        MzTabFileImportMethod importer = new MzTabFileImportMethod(inputFile,
                dataStore);
        FeatureTable featureTable1 = importer.execute();
        Assert.assertNotNull(featureTable1);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);
        List<FeatureTable> featureTables = new ArrayList<FeatureTable>();
        featureTables.add(featureTable1);

        // Import file 2
        inputFile = new File(TEST_DATA_PATH + "Sample 2.mzTab");
        Assert.assertTrue(inputFile.canRead());
        importer = new MzTabFileImportMethod(inputFile, dataStore);
        FeatureTable featureTable2 = importer.execute();
        Assert.assertNotNull(featureTable2);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);
        featureTables.add(featureTable2);

        // Variables
        MZTolerance mzTolerance = new MZTolerance(0.003, 5.0);
        RTTolerance rtTolerance = new RTTolerance(0.1, false);
        int mzWeight = 10;
        int rtWeight = 10;
        boolean requireSameCharge = false;
        boolean requireSameAnnotation = false;
        String featureTableName = "Aligned Feature Table";

        // 1. Test alignment based on m/z and RT only
        MatchAlignerMethod method = new MatchAlignerMethod(featureTables,
                dataStore, mzTolerance, rtTolerance, mzWeight, rtWeight,
                requireSameCharge, requireSameAnnotation, featureTableName);
        FeatureTable featureTable = method.execute();
        Assert.assertEquals(1.0, method.getFinishedPercentage(), 0.0001);
        Assert.assertEquals(10, featureTable.getRows().size());

        // 2. Test alignment based on m/z, RT and same annotation
        requireSameAnnotation = true;
        method = new MatchAlignerMethod(featureTables, dataStore, mzTolerance,
                rtTolerance, mzWeight, rtWeight, requireSameCharge,
                requireSameAnnotation, featureTableName);
        featureTable = method.execute();
        Assert.assertEquals(1.0, method.getFinishedPercentage(), 0.0001);
        Assert.assertEquals(12, featureTable.getRows().size());

    }
}
