/*
 * Copyright 2016 Dmitry Avtonomov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.msdk.io.mzml2;

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import org.apache.commons.pool2.ObjectPool;

import io.github.msdk.MSDKException;
import io.github.msdk.io.mzml2.util.ByteArrayHolder;
import io.github.msdk.io.mzml2.util.PooledByteArrayHolders;

/**
 * Utility methods to inflate zlib compressed spectra (byte arrays).
 * 
 * @author Dmitry Avtonomov
 */
public class ZlibInflater {
  private static final transient ObjectPool<ByteArrayHolder> pool =
      PooledByteArrayHolders.getInstance().getPool();

  private ZlibInflater() {}

  /**
   * The ByteArrayHolder pool, that this decoder is using. If you use uncompression methods from
   * this class, e.g. {@link #zlibUncompressBuffer(byte[], int, Integer)}, you will need to return
   * the ByteArrayHolder, that was returned to you, into that pool.
   * 
   * @return
   */
  public static ObjectPool<ByteArrayHolder> getPool() {
    return pool;
  }

  /**
   * Convenience method for {@link #zlibUncompressBuffer(byte[], int, Integer)}.<br/>
   * Inflates zLib compressed byte[].
   * 
   * @param compressed zLib compressed bytes
   * @param uncompressedLen length of data in bytes when uncompressed. Optional.
   * @return inflated byte array, which is borrowed from pool ({@link #getPool() }). You MUST return
   *         the byte holder back to the pool after usage.
   * @throws IOException should never happen, ByteArrayOutputStream is in-memory
   * @throws DataFormatException in case of malformed input byte array
   * @throws MSDKException
   */
  public static ByteArrayHolder zlibUncompressBuffer(byte[] compressed, Integer uncompressedLen)
      throws IOException, DataFormatException, MSDKException {
    return zlibUncompressBuffer(compressed, compressed.length, uncompressedLen);
  }

  /**
   * Inflates zLib compressed byte[].
   * 
   * @param bytes zLib compressed bytes in a holder, with properly set position
   * @param length length of data in the input array to be used
   * @param uncompressedLen length of data in bytes when uncompressed. Optional.
   * @return inflated byte array, which is borrowed from pool ({@link #getPool() }). You MUST return
   *         the byte holder back to the pool after usage.
   * @throws IOException should never happen, ByteArrayOutputStream is in-memory
   * @throws DataFormatException in case of malformed input byte array
   * @throws MSDKException
   */
  public static ByteArrayHolder zlibUncompressBuffer(byte[] bytes, int length,
      Integer uncompressedLen) throws IOException, DataFormatException, MSDKException {

    Inflater inflater = new Inflater();
    inflater.setInput(bytes, 0, length);
    int bufSize = uncompressedLen == null ? length * 2 : uncompressedLen;

    ByteArrayHolder bah = null;
    try {
      bah = pool.borrowObject();
    } catch (Exception e) {
      throw new MSDKException("Could not borrow ByteArrayHolder from the pool.\n" + e);
    }

    try {
      // Decompress the data
      int pos = 0;
      // at the beginning we can afford to just allocate a new buffer, if
      // the one we got was not enough
      bah.ensureCapacity(bufSize, false);
      while (!inflater.finished()) {
        if (pos != 0) {
          // when pos>0, it means we were not able to decompress the
          // whole thing
          // in one iteration, meaning there is something left in the
          // input buffer
          // but most likely not much, so we won't be overzealous
          // about
          // additional space allocation
          // System.out.println("RAN OUT OF SPACE IN INFLATER!");
          bah.ensureHasSpace(length / 2);
        }
        pos += inflater.inflate(bah.getUnderlyingBytes(), pos, bah.getCapacityLeft());
        bah.setPosition(pos);
      }

    } finally {
      inflater.end();
    }
    return bah;
  }
}
