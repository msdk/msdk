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

package io.github.msdk.id.localdatabasesearch;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.io.mztab.MzTabFileImportMethod;

public class LocalDatabaseSearchMethodTest {

  private static final String TEST_DATA_PATH = "src/test/resources/";


  @Test
  public void identificationTest() throws MSDKException {

    // Create the data structures
    DataPointStore dataStore = DataPointStoreFactory.getMemoryDataStore();

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "Sample-2.3_Small.mzTab");
    Assert.assertTrue("Cannot read test data", inputFile.canRead());
    MzTabFileImportMethod importer = new MzTabFileImportMethod(inputFile, dataStore);
    FeatureTable featureTable = importer.execute();
    Assert.assertNotNull(featureTable);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);
 
    featureTable.dispose();
  }

}
