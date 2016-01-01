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

package io.github.msdk.io.csv;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.Sample;

public class CsvFileImportMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @SuppressWarnings("null")
    @Test
    public void CSV_Multi_Samples_Import() throws MSDKException {

        // Create the data structures
        DataPointStore dataStore = DataPointStoreFactory.getTmpFileDataStore();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "Sample-2.3.csv");
        Assert.assertTrue(inputFile.canRead());
        CsvFileImportMethod importer = new CsvFileImportMethod(inputFile,
                dataStore);
        FeatureTable featureTable = importer.execute();
        Assert.assertNotNull(featureTable);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // The table has columns
        Assert.assertFalse(featureTable.getColumns().isEmpty());
        Assert.assertEquals(88, featureTable.getColumns().size());

        // The table has 7 samples
        List<Sample> samples = featureTable.getSamples();
        Assert.assertNotNull(samples);
        Assert.assertEquals(7, samples.size());

        // The table has 298 features
        Assert.assertFalse(featureTable.getRows().isEmpty());
        Assert.assertEquals(298, featureTable.getRows().size());

        featureTable.dispose();
    }

    @SuppressWarnings("null")
    @Test
    public void CSV_Single_Data_Import() throws MSDKException {

        // Create the data structures
        DataPointStore dataStore = DataPointStoreFactory.getTmpFileDataStore();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "Standards.csv");
        Assert.assertTrue(inputFile.canRead());
        CsvFileImportMethod importer = new CsvFileImportMethod(inputFile,
                dataStore);
        FeatureTable featureTable = importer.execute();
        Assert.assertNotNull(featureTable);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // The table has columns
        Assert.assertFalse(featureTable.getColumns().isEmpty());
        Assert.assertEquals(16, featureTable.getColumns().size());

        // The table has 12 samples
        List<Sample> samples = featureTable.getSamples();
        Assert.assertNotNull(samples);
        Assert.assertEquals(12, samples.size());

        // The table has 10 features
        Assert.assertFalse(featureTable.getRows().isEmpty());
        Assert.assertEquals(10, featureTable.getRows().size());

        featureTable.dispose();
    }

    @SuppressWarnings("null")
    @Test
    public void GCxGC_Import() throws MSDKException {

        // Create the data structures
        DataPointStore dataStore = DataPointStoreFactory.getTmpFileDataStore();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "GGT1.txt");
        Assert.assertTrue(inputFile.canRead());
        CsvFileImportMethod importer = new CsvFileImportMethod(inputFile,
                dataStore);
        FeatureTable featureTable = importer.execute();
        Assert.assertNotNull(featureTable);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // The table has columns
        Assert.assertFalse(featureTable.getColumns().isEmpty());

        // The table has 1 sample
        List<Sample> samples = featureTable.getSamples();
        Assert.assertNotNull(samples);
        Assert.assertEquals(1, samples.size());

        // The table has 15 features
        Assert.assertFalse(featureTable.getRows().isEmpty());
        Assert.assertEquals(15, featureTable.getRows().size());

        featureTable.dispose();
    }
}
