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

package io.github.msdk.io.nativeformats;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

class ZipUtils {

  /**
   * Size of the buffer to read/write data
   */
  private static final int BUFFER_SIZE = 4096;

  /**
   * Extracts a zip file from stream to a directory specified by destDirectory
   */
  static void extractStreamToFolder(final @Nonnull InputStream stream,
      final @Nonnull File destDirectory) throws IOException {
    Preconditions.checkNotNull(stream);
    Preconditions.checkNotNull(destDirectory);
    ZipInputStream zipStream = new ZipInputStream(stream);
    // iterates over entries in the zip file
    ZipEntry entry;
    while ((entry = zipStream.getNextEntry()) != null) {
      File filePath = new File(destDirectory, entry.getName());
      // Create the folder for this entry, if it does not exist
      filePath.getParentFile().mkdirs();
      if (!entry.isDirectory()) {
        // if the entry is a file, extracts it
        extractFile(zipStream, filePath);
      }
      zipStream.closeEntry();
    }
    zipStream.close();

  }

  /**
   * Extracts a zip entry (file entry)
   */
  private static void extractFile(final @Nonnull ZipInputStream zipIn, final @Nonnull File filePath)
      throws IOException {
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
    byte[] bytesIn = new byte[BUFFER_SIZE];
    int read = 0;
    while ((read = zipIn.read(bytesIn)) != -1) {
      bos.write(bytesIn, 0, read);
    }
    bos.close();
  }

}
