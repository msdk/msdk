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

package io.github.msdk.featuredetection.openms;

import java.io.File;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Strings;

import io.github.msdk.MSDKException;
import io.github.msdk.featuredetection.openms.FeatureFinderMetaboDetector;
import io.github.msdk.featuredetection.openms.FeatureFinderMetaboLocator;

public class FeatureFinderMetaboDetectorTest {

  @Before
  public void runCondition() throws MSDKException {
    // This code will ensure that the tests are executed only when
    // FeatureFinderMetabo is installed
    String programLocation = FeatureFinderMetaboLocator.findFeatureFinderMetabo();
    Assume.assumeFalse(Strings.isNullOrEmpty(programLocation));
  }

  @Test
  public void testValidmzMLfile() throws Exception {

    File inputFile = new File(this.getClass().getClassLoader().getResource("msms.mzML").toURI());

    Assert.assertNotNull(inputFile);
    FeatureFinderMetaboDetector mzDetector = new FeatureFinderMetaboDetector(inputFile);
    Assert.assertEquals(233, mzDetector.execute().size());

    FeatureFinderMetaboDetector mzDetector2 = new FeatureFinderMetaboDetector(inputFile);
    Assert.assertEquals(233, mzDetector2.execute().size());
  }

  @Test(expected = MSDKException.class)
  public void testInvalidmzMLFile() throws Exception {

    File inputFile =
        new File(this.getClass().getClassLoader().getResource("truncated.mzML").toURI());

    Assert.assertNotNull(inputFile);
    FeatureFinderMetaboDetector mzDetector3 = new FeatureFinderMetaboDetector(inputFile);
    Assert.assertEquals(0, mzDetector3.execute().size());

    FeatureFinderMetaboDetector mzDetector4 = new FeatureFinderMetaboDetector(inputFile);
    Assert.assertEquals(0, mzDetector4.execute().size());
  }

}
