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

package io.github.msdk.alignment.joinaligner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import io.github.msdk.alignment.joinaligner.JoinAlignerMethod;
import io.github.msdk.datamodel.FeatureTable;
import io.github.msdk.io.mztab.MzTabFileImportMethod;
import io.github.msdk.util.tolerances.MaximumMzTolerance;
import io.github.msdk.util.tolerances.MzTolerance;
import io.github.msdk.util.tolerances.RTTolerance;

public class JoinAlignerMethodTest {

  @Test
  @Ignore
  public void testMzTab_Samples() throws Exception {

    // Import file 1
    File inputFile = new File(this.getClass().getClassLoader().getResource("Sample 1.mzTab").toURI());
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

    // 1. Test alignment based on m/z and RT only
    JoinAlignerMethod method = new JoinAlignerMethod(featureTables, mzTolerance, rtTolerance);
    FeatureTable featureTable = method.execute();
    Assert.assertEquals(1.0, method.getFinishedPercentage(), 0.0001);
    Assert.assertEquals(10, featureTable.getRows().size());;

    featureTable.dispose();
  }
}
