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

package io.github.msdk.id.localdatabase;

import java.io.File;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import io.github.msdk.datamodel.FeatureTable;
import io.github.msdk.io.mztab.MzTabFileImportMethod;

public class LocalDatabaseSearchMethodTest {

  @Ignore
  @Test
  public void identificationTest() throws Exception {

    // Import the file
    File inputFile =
        new File(this.getClass().getClassLoader().getResource("Sample-2.3_Small.mzTab").toURI());
    Assert.assertTrue("Cannot read test data", inputFile.canRead());
    MzTabFileImportMethod importer = new MzTabFileImportMethod(inputFile);
    FeatureTable featureTable = importer.execute();
    Assert.assertNotNull(featureTable);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    featureTable.dispose();
  }

}
