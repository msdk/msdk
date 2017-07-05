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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * ZipResourceExtractor class.
 * </p>
 *
 */
 class ZipResourceExtractor {

  private static final Logger log = LoggerFactory.getLogger(ZipResourceExtractor.class);

  /**
   * <p>
   * extract.
   * </p>
   *
   * @param resourcePath a {@link java.lang.String} object.
   * @param destDir a {@link java.io.File} object.
   * @return a {@link java.io.File} object.
   * @throws java.net.MalformedURLException if the given resource path is malformed.
   */
  public static File extract(String resourcePath, File destDir) throws MalformedURLException {
    log.info("Extracting " + resourcePath + " to directory: " + destDir);
    if (!destDir.exists()) {
      destDir.mkdirs();
    }
    URL resourceURL;
    File f = new File(resourcePath);
    if (f.isFile()) {
      resourceURL = f.toURI().toURL();
    } else {
      resourceURL = ZipResourceExtractor.class.getResource(resourcePath);
    }
    if (resourceURL == null) {
      throw new NullPointerException("Could not retrieve resource for path: " + resourcePath);
    }
    try (InputStream resourceInputStream = resourceURL.openStream()) {
      try {
        String outname = new File(resourceURL.getPath()).getName();
        outname = outname.replaceAll("%20", " ");
        log.info(outname);
        if (resourcePath.endsWith("zip")) {
          return extractZipArchive(resourceInputStream, destDir);
        } else if (resourcePath.endsWith("gz")) {
          outname = outname.substring(0, outname.lastIndexOf("."));
          return copyToOutputFile(destDir, outname,
              new GZIPInputStream(new BufferedInputStream(resourceInputStream)));
        } else {
          return copyToOutputFile(destDir, outname, new BufferedInputStream(resourceInputStream));
        }
      } catch (FileNotFoundException e) {
        log.warn(e.getLocalizedMessage());
        throw new RuntimeException(e);
      }
    } catch (IOException ex) {
      log.error("Caught IOException: ", ex);
      throw new RuntimeException(ex);
    }
  }

  private static File copyToOutputFile(File destDir, String outname, InputStream in)
      throws FileNotFoundException, IOException {
    File outputFile = new File(destDir, outname);
    try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
      // Transfer bytes from in to out
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
    }
    return outputFile;
  }

  /**
   * <p>
   * extractZipArchive.
   * </p>
   *
   * @param istream a {@link java.io.InputStream} object.
   * @param outputDir a {@link java.io.File} object.
   * @return a {@link java.io.File} object.
   */
  public static File extractZipArchive(InputStream istream, File outputDir) {
    try {
      File outDir;
      try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(istream))) {
        ZipEntry entry;
        outDir = null;
        while ((entry = zis.getNextEntry()) != null) {
          int size;
          byte[] buffer = new byte[2048];
          File outFile = new File(outputDir, entry.getName());
          if (entry.isDirectory()) {
            outFile.mkdirs();
            if (outDir == null) {
              outDir = outFile;
            }
          } else {
            try (BufferedOutputStream bos =
                new BufferedOutputStream(new FileOutputStream(outFile), buffer.length)) {
              while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
                bos.write(buffer, 0, size);
              }
              bos.flush();
            }
          }
        }
        if (outDir == null) {
          outDir = outputDir;
        }
      }
      istream.close();
      return outDir;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
