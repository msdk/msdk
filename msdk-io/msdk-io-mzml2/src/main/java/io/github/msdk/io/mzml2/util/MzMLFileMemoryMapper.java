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

package io.github.msdk.io.mzml2.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import it.unimi.dsi.io.ByteBufferInputStream;

public abstract class MzMLFileMemoryMapper {

  /**
   * <p>mapToMemory.</p>
   *
   * @param mzMLFile a {@link java.io.File} object.
   * @return a {@link it.unimi.dsi.io.ByteBufferInputStream} object.
   * @throws java.io.IOException if any.
   * @throws io.github.msdk.MSDKException if any.
   */
  public static ByteBufferInputStream mapToMemory(File mzMLFile) throws IOException {

    RandomAccessFile aFile = new RandomAccessFile(mzMLFile, "r");
    FileChannel inChannel = aFile.getChannel();
    ByteBufferInputStream is = ByteBufferInputStream.map(inChannel);
    aFile.close();
    is.close();

    return is;
  }
}
