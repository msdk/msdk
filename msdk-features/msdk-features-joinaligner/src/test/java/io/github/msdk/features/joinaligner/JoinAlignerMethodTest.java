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

package io.github.msdk.features.joinaligner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.io.mztab.MzTabFileImportMethod;
import io.github.msdk.util.RTTolerance;
import io.github.msdk.util.tolerances.MaximumMzTolerance;
import io.github.msdk.util.tolerances.MzTolerance;

public class JoinAlignerMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @SuppressWarnings("null")
    @Test
    public void testMzTab_Samples() throws MSDKException {

        // Create the data structures
        DataPointStore dataStore = DataPointStoreFactory.getTmpFileDataStore();

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
        MzTolerance mzTolerance = new MaximumMzTolerance(0.003, 5.0);
        RTTolerance rtTolerance = new RTTolerance(0.1, false);
        int mzWeight = 10;
        int rtWeight = 10;
        boolean requireSameCharge = false;
        boolean requireSameAnnotation = false;
        String featureTableName = "Aligned Feature Table";

        // 1. Test alignment based on m/z and RT only
        JoinAlignerMethod method = new JoinAlignerMethod(featureTables,
                dataStore, mzTolerance, rtTolerance, mzWeight, rtWeight,
                requireSameCharge, requireSameAnnotation, featureTableName);
        FeatureTable featureTable = method.execute();
        Assert.assertEquals(1.0, method.getFinishedPercentage(), 0.0001);
        Assert.assertEquals(10, featureTable.getRows().size());

        // Verify that feature 1 has two ion annotations
        FeatureTableColumn<List<IonAnnotation>> column = featureTable
                .getColumn(ColumnName.IONANNOTATION, null);
        List<IonAnnotation> ionAnnotations = (List<IonAnnotation>) featureTable
                .getRows().get(0).getData(column);
        Assert.assertNotNull(ionAnnotations);
        Assert.assertEquals(2, ionAnnotations.size());
        Assert.assertEquals("PE(17:0/17:0)",
                ionAnnotations.get(0).getDescription());
        Assert.assertEquals("1. PE(17:0/17:0)",
                ionAnnotations.get(1).getDescription());

        // Verify that feature 3 has one ion annotation
        ionAnnotations = featureTable.getRows().get(2).getData(column);
        Assert.assertNotNull(ionAnnotations);
        Assert.assertEquals(1, ionAnnotations.size());
        Assert.assertEquals("Cer(d18:1/17:0)",
                ionAnnotations.get(0).getDescription());

        // 2. Test alignment based on m/z, RT and same annotation
        requireSameAnnotation = true;
        method = new JoinAlignerMethod(featureTables, dataStore, mzTolerance,
                rtTolerance, mzWeight, rtWeight, requireSameCharge,
                requireSameAnnotation, featureTableName);
        featureTable = method.execute();
        Assert.assertEquals(1.0, method.getFinishedPercentage(), 0.0001);
        Assert.assertEquals(12, featureTable.getRows().size());

        featureTable.dispose();
    }
}
