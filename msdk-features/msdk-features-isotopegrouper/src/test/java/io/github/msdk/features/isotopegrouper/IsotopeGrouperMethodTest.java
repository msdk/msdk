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

package io.github.msdk.features.isotopegrouper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.io.csv.CsvFileImportMethod;
import io.github.msdk.util.tolerances.MaximumMzTolerance;
import io.github.msdk.util.tolerances.MzTolerance;
import io.github.msdk.util.tolerances.RTTolerance;

public class IsotopeGrouperMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @Ignore("Ignored because the module is unfinished")
    @SuppressWarnings("null")
    @Test
    public void singleSampleTest() throws MSDKException {

        // Create the data structures
        DataPointStore dataStore = DataPointStoreFactory.getMemoryDataStore();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "Standard1.csv");
        Assert.assertTrue(inputFile.canRead());
        CsvFileImportMethod importer = new CsvFileImportMethod(inputFile,
                dataStore);
        FeatureTable featureTable = importer.execute();
        Assert.assertNotNull(featureTable);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);
        Assert.assertNotNull(featureTable);
        Assert.assertEquals(19, featureTable.getRows().size());

        // Verify that there is no group column
        FeatureTableColumn<Integer> groupColumn = featureTable
                .getColumn(ColumnName.GROUPID, null);
        Assert.assertNull(groupColumn);

        // Variables
        MzTolerance mzTolerance = new MaximumMzTolerance(0.003, 5.0);
        RTTolerance rtTolerance = new RTTolerance(0.1, false);
        int maximumCharge = 2;
        boolean requireMonotonicShape = false;
        String featureTableName = featureTable.getName() + " deisotoped";

        // 1. Group isotopes based on narrow m/z and RT tolerance
        IsotopeGrouperMethod method = new IsotopeGrouperMethod(featureTable,
                dataStore, mzTolerance, rtTolerance, maximumCharge,
                requireMonotonicShape, featureTableName);
        FeatureTable groupedFeatureTable = method.execute();
        Assert.assertEquals(1.0, method.getFinishedPercentage(), 0.0001);
        Assert.assertNotNull(groupedFeatureTable);
        Assert.assertEquals(19, groupedFeatureTable.getRows().size());

        // Verify groups
        groupColumn = groupedFeatureTable.getColumn(ColumnName.GROUPID, null);
        Assert.assertNotNull(groupColumn);
        int groupedFeatures = 0;
        List<Integer> groups = new ArrayList<Integer>();
        for (FeatureTableRow row : featureTable.getRows()) {
            Integer groupID = row.getData(groupColumn);
            if (groupID != null) {
                if (!groups.contains(groupID))
                    groups.add(groupID);

                groupedFeatures++;
            }
        }
        Assert.assertEquals(9, groupedFeatures);
        Assert.assertEquals(2, groups.size());

        featureTable.dispose();
        groupedFeatureTable.dispose();
    }

    @Ignore("Ignored because the module is unfinished")
    @SuppressWarnings("null")
    @Test
    public void multiSampleTest() throws MSDKException {

        // Create the data structures
        DataPointStore dataStore = DataPointStoreFactory.getMemoryDataStore();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "Standard1-4.csv");
        Assert.assertTrue(inputFile.canRead());
        CsvFileImportMethod importer = new CsvFileImportMethod(inputFile,
                dataStore);
        FeatureTable featureTable = importer.execute();
        Assert.assertNotNull(featureTable);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);
        Assert.assertNotNull(featureTable);
        Assert.assertEquals(19, featureTable.getRows().size());

        // Verify that there is no group column
        FeatureTableColumn<Integer> groupColumn = featureTable
                .getColumn(ColumnName.GROUPID, null);
        Assert.assertNull(groupColumn);

        // Variables
        MzTolerance mzTolerance = new MaximumMzTolerance(0.003, 5.0);
        RTTolerance rtTolerance = new RTTolerance(0.1, false);
        int maximumCharge = 2;
        boolean requireMonotonicShape = false;
        String featureTableName = featureTable.getName() + " deisotoped";

        // 1. Group isotopes based on narrow m/z and RT tolerance
        IsotopeGrouperMethod method = new IsotopeGrouperMethod(featureTable,
                dataStore, mzTolerance, rtTolerance, maximumCharge,
                requireMonotonicShape, featureTableName);
        FeatureTable groupedFeatureTable = method.execute();
        Assert.assertEquals(1.0, method.getFinishedPercentage(), 0.0001);
        Assert.assertNotNull(groupedFeatureTable);
        Assert.assertEquals(19, groupedFeatureTable.getRows().size());

        // Verify groups
        groupColumn = groupedFeatureTable.getColumn(ColumnName.GROUPID, null);
        Assert.assertNotNull(groupColumn);
        int groupedFeatures = 0;
        List<Integer> groups = new ArrayList<Integer>();
        for (FeatureTableRow row : featureTable.getRows()) {
            Integer groupID = row.getData(groupColumn);
            if (groupID != null) {
                if (!groups.contains(groupID))
                    groups.add(groupID);

                groupedFeatures++;
            }
        }
        Assert.assertEquals(9, groupedFeatures);
        Assert.assertEquals(2, groups.size());

        featureTable.dispose();
        groupedFeatureTable.dispose();
    }
}
