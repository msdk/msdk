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

package io.github.msdk.io.mzml2;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import javax.xml.stream.XMLStreamException;

import io.github.msdk.MSDKException;

class MzMLFileMemoryMapper {

  public MappedByteBufferInputStream mapToMemory(File mzMLFile)
      throws IOException, XMLStreamException, MSDKException {

    RandomAccessFile aFile = new RandomAccessFile(mzMLFile, "r");
    FileChannel inChannel = aFile.getChannel();
    MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
    MappedByteBufferInputStream is = new MappedByteBufferInputStream(buffer);
    aFile.close();
    is.close();

    return is;
  }
}
