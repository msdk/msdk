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

package io.github.msdk.isotopes.tracing.data;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.isotopes.tracing.data.constants.PathConstants;
import junit.framework.TestCase;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class MSShiftDatabaseTest extends TestCase {

  private File[] testFiles = new File(PathConstants.TEST_RESOURCES.toAbsolutePath()).listFiles();
  private static final Logger LOGGER = LoggerFactory.getLogger(MSShiftDatabaseTest.class);

  public void testReadWriteCsv() throws IOException {
    String filePath = "";
    for (File file : testFiles) {
      if (file.getName().contains("MSShiftDatabaseTest")) {
        filePath = file.getAbsolutePath();
        LOGGER.info("Test file" + file.getName());
        MSShiftDatabase msDatabase = new MSShiftDatabase(filePath);
        LOGGER.info("msDatabase\n" + msDatabase);
        msDatabase.writeCsv(PathConstants.TMP_FOLDER.toAbsolutePath());
      }
    }
    File[] created = new File(PathConstants.TMP_FOLDER.toAbsolutePath()).listFiles();
    for (File file : created) {
      file.delete();
    }
  }

}
