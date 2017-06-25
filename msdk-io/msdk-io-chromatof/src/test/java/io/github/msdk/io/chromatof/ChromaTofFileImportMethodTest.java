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
package io.github.msdk.io.chromatof;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.Sample;

/**
 * Test for the {@link ChromaTofFileImportMethod}.
 */
public class ChromaTofFileImportMethodTest {

  private static final String TEST_DATA_PATH = "src/test/resources/";


  @Test
  @Ignore
  public void GCxGC_Import() throws MSDKException {

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "GGT1.txt");
    Assert.assertTrue(inputFile.canRead());
    ChromaTofFileImportMethod importer = new ChromaTofFileImportMethod(inputFile, Locale.US,
        ChromaTofParser.FIELD_SEPARATOR_TAB, ChromaTofParser.QUOTATION_CHARACTER_NONE);
    FeatureTable featureTable = importer.execute();
    Assert.assertNotNull(featureTable);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);


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
