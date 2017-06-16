package io.github.msdk.io.mzml2;

import java.io.InputStream;

import it.unimi.dsi.io.ByteBufferInputStream;

public class ByteBufferInputStreamAdapter extends InputStream {
  private long remainingBytes;
  private ByteBufferInputStream is;

  public ByteBufferInputStreamAdapter(ByteBufferInputStream is, long position,
      long remainingBytes) {
    this.is = is;
    this.remainingBytes = remainingBytes;
    is.position(position);
  }

  public ByteBufferInputStreamAdapter(ByteBufferInputStream is) {
    this(is, 0, is.length());
  }

  @Override
  public int read() {
    return (remainingBytes-- <= 0 ? -1 : is.read());
  }

}
