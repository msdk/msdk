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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Text processing utilities
 */
class TextUtils {

  /**
   * Reads a line of text from a given input stream or null if the end of the stream is reached.
   */
  static String readLineFromStream(InputStream in) throws IOException {
    byte buf[] = new byte[1024];
    int pos = 0;
    while (true) {
      int ch = in.read();
      if ((ch == '\n') || (ch < 0))
        break;
      buf[pos++] = (byte) ch;
      if (pos == buf.length)
        buf = Arrays.copyOf(buf, pos * 2);
    }
    if (pos == 0)
      return null;

    return new String(Arrays.copyOf(buf, pos), "UTF-8");
  }

}
