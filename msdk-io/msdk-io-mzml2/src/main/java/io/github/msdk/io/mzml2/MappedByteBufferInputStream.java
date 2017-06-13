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

import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;

class MappedByteBufferInputStream extends InputStream {
  private MappedByteBuffer buf;

  MappedByteBufferInputStream(MappedByteBuffer buf) {
    this.buf = buf;
  }

  public synchronized int read() throws IOException {
    if (!buf.hasRemaining()) {
      return -1;
    }
    int readBuffer = buf.get();
    return readBuffer;
  }

  public synchronized int read(byte[] bytes, int off, int len) throws IOException {
    if (!buf.hasRemaining()) {
      return -1;
    }

    len = Math.min(len, buf.remaining());
    buf.get(bytes, off, len);
    return len;
  }

  public synchronized InputStream getInputStream(int position, int length) {
    MappedByteBuffer newBuf = (MappedByteBuffer) buf.position(position);
    newBuf.limit(position + length);
    return (new MappedByteBufferInputStream(newBuf));
  }

  public synchronized int getCurrentPosition() {
    return buf.position();
  }

  public synchronized void setPosition(int pos) {
    buf = (MappedByteBuffer) buf.position(pos);
  }
}
