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

package io.github.msdk.isotopes.tracing.util;

import java.io.File;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class FileWriterUtils {

  public static final String CSV_EXTENSION = ".csv";
  public static final String JPEG_EXTENSION = ".jpeg";

  /**
   * Checks if the filename already exists and returns a serially numbered name for the file. E.g if
   * file.xy already exists but no file(1).xy, file(1).xy will be returned
   * 
   * @param pathToFile
   * @param extension
   * @return a serially numbered name for the file if the input file string already exists.
   *         Otherwise the original filename.
   */
  public static String checkFilePath(String pathToFile, String extension) {
    if (pathToFile.endsWith(extension)) {
      pathToFile = pathToFile.substring(0, pathToFile.lastIndexOf("."));
    }
    int fileCount = 1;
    String newPathToFile = pathToFile;
    File file = new File(newPathToFile + extension);
    while (file.exists()) {
      newPathToFile = pathToFile + "(" + fileCount + ")";
      file = new File(newPathToFile + extension);
      fileCount++;
    }
    return newPathToFile + extension;
  }

}
