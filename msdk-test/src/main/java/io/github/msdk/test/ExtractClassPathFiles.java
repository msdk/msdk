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

package io.github.msdk.test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

/**
 * <p>
 * ExtractClassPathFiles class.
 * </p>
 *
 * @author plusik
 * @version $Id: $Id
 */
public class ExtractClassPathFiles extends ExternalResource {

  private final TemporaryFolder tf;
  private final String[] resourcePaths;
  private final List<File> files = new LinkedList<>();
  private File baseFolder;

  /**
   * <p>
   * Constructor for ExtractClassPathFiles.
   * </p>
   *
   * @param tf a {@link org.junit.rules.TemporaryFolder} object.
   * @param resourcePaths a {@link java.lang.String} object.
   */
  public ExtractClassPathFiles(TemporaryFolder tf, String... resourcePaths) {
    this.tf = tf;
    this.resourcePaths = resourcePaths;
  }

  /** {@inheritDoc} */
  @Override
  protected void before() throws Throwable {
    try {
      this.tf.create();
    } catch (IOException ex) {
      throw ex;
    }
    baseFolder = tf.newFolder();
    for (String resource : resourcePaths) {
      File file = ZipResourceExtractor.extract(resource, baseFolder);
      files.add(file);
    }
  }

  /** {@inheritDoc} */
  @Override
  protected void after() {
    for (File f : files) {
      f.delete();
    }
  }

  /**
   * <p>
   * Getter for the field <code>files</code>.
   * </p>
   *
   * @return a {@link java.util.List} object.
   */
  public List<File> getFiles() {
    return this.files;
  }

  /**
   * <p>
   * getBaseDir.
   * </p>
   *
   * @return a {@link java.io.File} object.
   */
  public File getBaseDir() {
    return baseFolder;
  }
}
