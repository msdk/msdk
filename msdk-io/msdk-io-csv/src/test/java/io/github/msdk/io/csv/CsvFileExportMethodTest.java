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
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Files;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;

public class CsvFileExportMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

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

        // Create temp file
        File tempFile = null;
        try {
            tempFile = File.createTempFile("MZmine_TestFile_", ".csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Parameters
        final String separator = ",";
        final String itemSeparator = ";";
        final Boolean exportAllIds = true;
        final List<FeatureTableColumn<?>> columns = featureTable.getColumns();

        // Export the file
        Assert.assertNotNull(tempFile);
        CsvFileExportMethod exporter = new CsvFileExportMethod(featureTable,
                tempFile, separator, itemSeparator, exportAllIds, columns);
        exporter.execute();
        Assert.assertEquals(1.0, exporter.getFinishedPercentage(), 0.0001);
        Assert.assertTrue(tempFile.canRead());

        // Read all lines from the CSV file into an array
        final List<String> lines;
        try {
            lines = Files.asCharSource(tempFile, Charset.defaultCharset())
                    .readLines();
        } catch (IOException ex) {
            throw new MSDKException(ex);
        }

        // The CSV file has 299 lines
        Assert.assertEquals(299, lines.size());

        // The CSV file has 102 columns
        String line = lines.get(0);
        int count = line.length() - line.replace(separator, "").length() + 1;
        Assert.assertEquals(102, count);

        // The 177th line is L-Arginine
        line = lines.get(176);
        String[] data = line.split(separator);
        Assert.assertEquals(176, Integer.parseInt(data[0]));
        Assert.assertEquals(175.119450157, Double.parseDouble(data[1]), 0.0001);
        Assert.assertEquals(22.814413, Double.parseDouble(data[2]), 0.0001);
        Assert.assertEquals("L-Arginine", data[3]);
        Assert.assertEquals(93056.24, Double.parseDouble(data[23]), 0.0001);
        Assert.assertEquals(4159043.95, Double.parseDouble(data[24]), 0.0001);

        // Clean up
        tempFile.delete();
        featureTable.dispose();
    }

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

        // Create temp file
        File tempFile = null;
        try {
            tempFile = File.createTempFile("MZmine_TestFile_", ".csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Parameters
        final String separator = ",";
        final String itemSeparator = ";";
        final Boolean exportAllIds = true;
        final List<FeatureTableColumn<?>> columns = featureTable.getColumns();

        // Export the file
        Assert.assertNotNull(tempFile);
        CsvFileExportMethod exporter = new CsvFileExportMethod(featureTable,
                tempFile, separator, itemSeparator, exportAllIds, columns);
        exporter.execute();
        Assert.assertEquals(1.0, exporter.getFinishedPercentage(), 0.0001);
        Assert.assertTrue(tempFile.canRead());

        // Read all lines from the CSV file into an array
        final List<String> lines;
        try {
            lines = Files.asCharSource(tempFile, Charset.defaultCharset())
                    .readLines();
        } catch (IOException ex) {
            throw new MSDKException(ex);
        }

        // The CSV file has 11 lines
        Assert.assertEquals(11, lines.size());

        // The CSV file has 30 columns
        String line = lines.get(0);
        int count = line.length() - line.replace(separator, "").length() + 1;
        Assert.assertEquals(30, count);

        // Clean up
        tempFile.delete();
        featureTable.dispose();

    }

}
