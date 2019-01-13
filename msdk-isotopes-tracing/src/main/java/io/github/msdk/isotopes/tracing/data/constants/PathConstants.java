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

package io.github.msdk.isotopes.tracing.data.constants;

import java.io.File;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public enum PathConstants {
  TMP_FOLDER("\\tmp\\"), //
  TEST_RESOURCES("\\src\\test\\resources\\");

  private String relativePath;

  PathConstants(String realativePath) {
    this.relativePath = realativePath;
  }

  public String getRelativePath() {
    return relativePath;
  }

  /**
   * 
   * @return the relative path of this PathConstant converted to the absolute path.
   */
  public String toAbsolutePath() {
    return getProjectPath() + relativePath;
  }

  /**
   * 
   * @return the absolute path to the isotopeincorporation project
   */
  public static String getProjectPath() {
    return new File("").getAbsolutePath();
  }

  /**
   * 
   * @param filename
   * @return the filename concatenated with the absolute path of this PathConstant
   */
  public String toAbsolutePath(String filename) {
    return toAbsolutePath() + filename;
  }

}
