package io.github.msdk.io.mzxml;

import java.io.InputStream;

import it.unimi.dsi.io.ByteBufferInputStream;

/**
 * <p>ByteBufferInputStreamAdapter class.</p>
 */
public class ByteBufferInputStreamAdapter extends InputStream {
  private long remainingBytes;
  private ByteBufferInputStream is;

  /**
   * <p>Constructor for ByteBufferInputStreamAdapter.</p>
   *
   * @param is a {@link it.unimi.dsi.io.ByteBufferInputStream} object.
   * @param position a long.
   * @param remainingBytes a long.
   */
  public ByteBufferInputStreamAdapter(ByteBufferInputStream is, long position,
      long remainingBytes) {
    this.is = is;
    this.remainingBytes = remainingBytes;
    is.position(position);
  }

  /**
   * <p>Constructor for ByteBufferInputStreamAdapter.</p>
   *
   * @param is a {@link it.unimi.dsi.io.ByteBufferInputStream} object.
   */
  public ByteBufferInputStreamAdapter(ByteBufferInputStream is) {
    this(is, 0, is.length());
  }

  /** {@inheritDoc} */
  @Override
  public int read() {
    return (remainingBytes-- <= 0 ? -1 : is.read());
  }

}
