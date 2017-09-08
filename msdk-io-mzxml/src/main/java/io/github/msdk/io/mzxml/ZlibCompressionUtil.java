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

package io.github.msdk.io.mzxml;

import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Compression related utilities
 */
public class ZlibCompressionUtil {

  /**
   * Decompress the zlib-compressed bytes and return an array of decompressed bytes
   *
   * @param compressedBytes an array of byte.
   * @return an array of byte.
   * @throws java.util.zip.DataFormatException if any.
   */
  public static byte[] decompress(byte compressedBytes[]) throws DataFormatException {

    Inflater decompresser = new Inflater();

    decompresser.setInput(compressedBytes);

    byte[] resultBuffer = new byte[compressedBytes.length * 2];
    byte[] resultTotal = new byte[0];

    int resultLength = decompresser.inflate(resultBuffer);

    while (resultLength > 0) {
      byte previousResult[] = resultTotal;
      resultTotal = new byte[resultTotal.length + resultLength];
      System.arraycopy(previousResult, 0, resultTotal, 0, previousResult.length);
      System.arraycopy(resultBuffer, 0, resultTotal, previousResult.length, resultLength);
      resultLength = decompresser.inflate(resultBuffer);
    }

    decompresser.end();

    return resultTotal;
  }

}
