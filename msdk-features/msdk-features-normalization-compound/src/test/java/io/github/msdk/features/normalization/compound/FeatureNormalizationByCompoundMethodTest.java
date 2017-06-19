/*
 * (C) Copyright 2015-2017 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1 as published by the Free
 * Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation.
 */

package io.github.msdk.features.normalization.compound;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.io.mztab.MzTabFileImportMethod;

public class FeatureNormalizationByCompoundMethodTest {

  private static final String TEST_DATA_PATH = "src/test/resources/";

  @Ignore
  @Test
  public void testMzTab_Sample() throws MSDKException {

    // Create the data structures
    DataPointStore dataStore = DataPointStoreFactory.getMemoryDataStore();

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "singleSample.mzTab");
    Assert.assertTrue(inputFile.canRead());
    MzTabFileImportMethod importer = new MzTabFileImportMethod(inputFile, dataStore);
    FeatureTable featureTable = importer.execute();
    Assert.assertNotNull(featureTable);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);
    Assert.assertEquals(93, featureTable.getRows().size());

    // Variables
    int mzRtWeight = 1;
    NormalizationType normalizationType = NormalizationType.NEAREST_STANDARD;

    // Normalize the Area and Height columns
    List<Sample> Samples = featureTable.getSamples();

    // Set the internal standards to the first 9 rows
    List<FeatureTableRow> internalStandardRows = new ArrayList<FeatureTableRow>();
    List<FeatureTableRow> featureTableRows = featureTable.getRows();
    for (int i = 0; i < 9; i++) {
      internalStandardRows.add(featureTableRows.get(i));
    }

    // 1. Test the normalization based on nearest standard
    FeatureNormalizationByCompoundMethod method =
        new FeatureNormalizationByCompoundMethod(featureTable, dataStore, normalizationType,
            internalStandardRows, mzRtWeight);
    FeatureTable normalizedFeatureTable = method.execute();
    Assert.assertEquals(1.0, method.getFinishedPercentage(), 0.0001);

    // Verify data
    Assert.assertEquals(93, normalizedFeatureTable.getRows().size());
    // Assert.assertEquals(11, normalizedFeatureTable.getColumns().size());

    // Clean-up
    normalizedFeatureTable.dispose();
    featureTable.dispose();
  }

}
