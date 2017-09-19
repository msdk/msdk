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

package io.github.msdk.alignment.ransac;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import io.github.msdk.alignment.ransac.RansacAlignerMethod;
import io.github.msdk.datamodel.FeatureTable;
import io.github.msdk.datamodel.FeatureTableRow;
import io.github.msdk.datamodel.IonAnnotation;
import io.github.msdk.io.mztab.MzTabFileImportMethod;
import io.github.msdk.util.tolerances.MaximumMzTolerance;
import io.github.msdk.util.tolerances.MzTolerance;
import io.github.msdk.util.tolerances.RTTolerance;

public class RansacAlignerMethodTest {

  @Test
  @Ignore("unfinished")
  public void testMzTab_Samples() throws Exception {

    // Import file 1
    File inputFile =
        new File(this.getClass().getClassLoader().getResource("Sample 1.mzTab").toURI());
    Assert.assertTrue(inputFile.canRead());
    MzTabFileImportMethod importer = new MzTabFileImportMethod(inputFile);
    FeatureTable featureTable1 = importer.execute();
    Assert.assertNotNull(featureTable1);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);
    List<FeatureTable> featureTables = new ArrayList<FeatureTable>();
    featureTables.add(featureTable1);

    // Import file 2
    inputFile = new File(this.getClass().getClassLoader().getResource("Sample 2.mzTab").toURI());
    Assert.assertTrue(inputFile.canRead());
    importer = new MzTabFileImportMethod(inputFile);
    FeatureTable featureTable2 = importer.execute();
    Assert.assertNotNull(featureTable2);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);
    featureTables.add(featureTable2);

    // Variables
    MzTolerance mzTolerance = new MaximumMzTolerance(0.003, 5.0);
    RTTolerance rtTolerance = new RTTolerance(0.1f, false);
    String featureTableName = "Aligned Feature Table";

    // 1. Test alignment based on m/z and RT only and linear model
    RansacAlignerMethod method = new RansacAlignerMethod(featureTables, mzTolerance, rtTolerance,
        featureTableName, 0.4, true, 0);

    FeatureTable featureTable = method.execute();
    Assert.assertEquals(1.0, method.getFinishedPercentage(), 0.0001);
    Assert.assertEquals(10, featureTable.getRows().size());

    // Verify that feature 1 has two ion annotations
    IonAnnotation ionAnnotation = featureTable.getRows().get(0).getFeature(0).getIonAnnotation();
    Assert.assertEquals("PE(17:0/17:0)", ionAnnotation.getDescription());

    // 2. Test alignment based on m/z and RT only and non-linear model
    method = new RansacAlignerMethod(featureTables, mzTolerance, rtTolerance, featureTableName, 0.4,
        false, 0);

    featureTable = method.execute();
    Assert.assertEquals(1.0, method.getFinishedPercentage(), 0.0001);
    Assert.assertEquals(10, featureTable.getRows().size());


    // 2. Test alignment of the one file with itself based on m/z and RT only
    // Import file 1
    featureTables = new ArrayList<FeatureTable>();
    featureTables.add(featureTable1);

    // Add file 1 again
    featureTables.add(featureTable1);

    method = new RansacAlignerMethod(featureTables, mzTolerance, rtTolerance, featureTableName, 0.4,
        true, 0);

    featureTable = method.execute();
    Assert.assertEquals(1.0, method.getFinishedPercentage(), 0.0001);
    Assert.assertEquals(10, featureTable.getRows().size());

    List<FeatureTableRow> rows = featureTable.getRows();

  }
}
