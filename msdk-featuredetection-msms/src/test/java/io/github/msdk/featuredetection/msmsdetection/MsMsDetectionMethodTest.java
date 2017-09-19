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

package io.github.msdk.featuredetection.msmsdetection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.datamodel.IonAnnotation;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.featuredetection.msmsdetection.MsMsDetectionMethod;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import io.github.msdk.util.tolerances.MaximumMzTolerance;
import io.github.msdk.util.tolerances.MzTolerance;
import io.github.msdk.util.tolerances.RTTolerance;

public class MsMsDetectionMethodTest {

  @Test
  public void testOrbitrap() throws Exception {

    // Import the file
    File inputFile = new File(this.getClass().getClassLoader().getResource("msms.mzML").toURI());
    Assert.assertTrue("Cannot read test data", inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // MS2 scans
    List<MsScan> msScans = new ArrayList<MsScan>();
    for (MsScan scan : rawFile.getScans()) {
      Integer msLevel = scan.getMsLevel();
      if (msLevel != null) {
        if (msLevel.equals(2))
          msScans.add(scan);
      }
    }

    // Parameters
    final MzTolerance mzTolerance = new MaximumMzTolerance(0.003, 5.0);
    final RTTolerance rtTolerance = new RTTolerance(0.2f, false);
    final Double intensityTolerance = 0.10d;

    // MS/MS detection method
    MsMsDetectionMethod msMethod =
        new MsMsDetectionMethod(rawFile, msScans, mzTolerance, rtTolerance, intensityTolerance);
    final List<IonAnnotation> ionAnnotations = msMethod.execute();
    Assert.assertEquals(1.0, msMethod.getFinishedPercentage(), 0.0001);

    // Verify ions
    Assert.assertEquals(13, ionAnnotations.size());
    for (int i = 0; i < ionAnnotations.size(); i++) {
      Assert.assertNotNull(ionAnnotations.get(i));
      Double mz = ionAnnotations.get(i).getExpectedMz();
      Assert.assertNotNull(mz);
      Float rt = ionAnnotations.get(i).getExpectedRetentionTime();
      switch (i) {
        case 0:
          Assert.assertEquals(809.6484375, mz, 0.0001);
          Assert.assertEquals(435.9, rt, 0.1);
          break;
        case 1:
          Assert.assertEquals(534.5249023, mz, 0.0001);
          Assert.assertEquals(453.6, rt, 0.1);
          break;
        case 2:
          Assert.assertEquals(797.6456299, mz, 0.0001);
          Assert.assertEquals(449.2, rt, 0.1);
          break;
        case 3:
          Assert.assertEquals(806.5674438, mz, 0.0001);
          Assert.assertEquals(420.1, rt, 0.1);
          break;
        case 4:
          Assert.assertEquals(750.5430298, mz, 0.0001);
          Assert.assertEquals(427.4, rt, 0.1);
          break;
        case 5:
          Assert.assertEquals(796.6193848, mz, 0.0001);
          Assert.assertEquals(435.0, rt, 0.1);
          break;
        case 6:
          Assert.assertEquals(753.5879517, mz, 0.0001);
          Assert.assertEquals(444.3, rt, 0.1);
          break;
        case 7:
          Assert.assertEquals(776.5801392, mz, 0.0001);
          Assert.assertEquals(409.0, rt, 0.1);
          break;
        case 8:
          Assert.assertEquals(816.5888062, mz, 0.0001);
          Assert.assertEquals(422.7, rt, 0.1);
          break;
        case 9:
          Assert.assertEquals(756.5533447, mz, 0.0001);
          Assert.assertEquals(443.3, rt, 0.1);
          break;
        case 10:
          Assert.assertEquals(774.5436401, mz, 0.0001);
          Assert.assertEquals(416.1, rt, 0.1);
          break;
        case 11:
          Assert.assertEquals(754.5394897, mz, 0.0001);
          Assert.assertEquals(409.8, rt, 0.1);
          break;
        case 12:
          Assert.assertEquals(715.5751953, mz, 0.0001);
          Assert.assertEquals(402.7, rt, 0.1);
          break;
      }
    }

  }

}
