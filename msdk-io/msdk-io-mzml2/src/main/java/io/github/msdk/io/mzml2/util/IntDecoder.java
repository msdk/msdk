/*
 * IntDecoder.java johan.teleman@immun.lth.se
 * 
 * Copyright 2013 Johan Teleman
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
package io.github.msdk.io.mzml2.util;

/**
 * Decodes ints from the half bytes in bytes. Lossless reverse of encodeInt, although not
 * symmetrical in input arguments.
 */
class IntDecoder {

  int pos = 0;
  boolean half = false;
  byte[] bytes;

  public IntDecoder(byte[] _bytes, int _pos) {
    bytes = _bytes;
    pos = _pos;
  }

  public long next() {
    int head;
    int i, n;
    long res = 0;
    long mask, m;
    int hb;

    if (!half)
      head = (0xff & bytes[pos]) >> 4;
    else
      head = 0xf & bytes[pos++];

    half = !half;

    if (head <= 8)
      n = head;
    else {
      // leading ones, fill in res
      n = head - 8;
      mask = 0xf0000000;
      for (i = 0; i < n; i++) {
        m = mask >> (4 * i);
        res = res | m;
      }
    }

    if (n == 8)
      return 0;

    for (i = n; i < 8; i++) {
      if (!half)
        hb = (0xff & bytes[pos]) >> 4;
      else
        hb = 0xf & bytes[pos++];

      res = res | (hb << ((i - n) * 4));
      half = !half;
    }

    return res;
  }
}
