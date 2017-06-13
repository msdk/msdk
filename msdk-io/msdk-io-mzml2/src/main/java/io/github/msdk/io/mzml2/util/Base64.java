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
package io.github.msdk.io.mzml2.util;

import java.nio.charset.StandardCharsets;

import io.github.msdk.MSDKException;

/**
 * Follows implementation from Apache Commons Codec (org.apache.commons.codec.binary.Base64).
 * 
 * @author Dmitry Avtonomov
 */
public class Base64 {

  //////////////////////////////
  //
  // Fields from BaseNCodec
  //
  //////////////////////////////
  static final int EOF = -1;
  public static final int MIME_CHUNK_SIZE = 76;
  public static final int PEM_CHUNK_SIZE = 64;
  public static final int DEFAULT_BUFFER_RESIZE_FACTOR = 2;
  public static final int DEFAULT_BUFFER_SIZE = 8192;
  /**
   * Mask used to extract 8 bits, used in decoding bytes
   */
  protected static final int MASK_8BITS = 0xff;
  /**
   * Byte used to pad output.
   */
  protected static final byte PAD_DEFAULT = '='; // Allow static access to
                                                 // default
  protected final byte PAD = PAD_DEFAULT; // instance variable just in case it
                                          // needs to vary later
  /**
   * Number of bytes in each full block of unencoded data, e.g. 4 for Base64 and 5 for Base32
   */
  private final int unencodedBlockSize;
  /**
   * Number of bytes in each full block of encoded data, e.g. 3 for Base64 and 8 for Base32
   */
  private final int encodedBlockSize;
  /**
   * Chunksize for encoding. Not used when decoding. A value of zero or less implies no chunking of
   * the encoded data. Rounded down to nearest multiple of encodedBlockSize.
   */
  protected final int lineLength;
  /**
   * Size of chunk separator. Not used unless {@link #lineLength} > 0.
   */
  private final int chunkSeparatorLength;

  /**
   * BASE32 characters are 6 bits in length. They are formed by taking a block of 3 octets to form a
   * 24-bit string, which is converted into 4 BASE64 characters.
   */
  private static final int BITS_PER_ENCODED_BYTE = 6;
  private static final int BYTES_PER_UNENCODED_BLOCK = 3;
  private static final int BYTES_PER_ENCODED_BLOCK = 4;

  /**
   * Chunk separator per RFC 2045 section 2.1.
   *
   * <p>
   * N.B. The next major release may break compatibility and make this field private.
   * </p>
   *
   * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045 section 2.1</a>
   */
  static final byte[] CHUNK_SEPARATOR = {'\r', '\n'};

  /**
   * This array is a lookup table that translates 6-bit positive integer index values into their
   * "Base64 Alphabet" equivalents as specified in Table 1 of RFC 2045.
   *
   * Thanks to "commons" project in ws.apache.org for this code.
   * http://svn.apache.org/repos/asf/webservices/commons/trunk/modules/util/
   */
  private static final byte[] STANDARD_ENCODE_TABLE = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
      'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b',
      'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
      'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};

  /**
   * This is a copy of the STANDARD_ENCODE_TABLE above, but with + and / changed to - and _ to make
   * the encoded Base64 results more URL-SAFE. This table is only used when the Base64's mode is set
   * to URL-SAFE.
   */
  private static final byte[] URL_SAFE_ENCODE_TABLE = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
      'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b',
      'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
      'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};

  /**
   * This array is a lookup table that translates Unicode characters drawn from the "Base64
   * Alphabet" (as specified in Table 1 of RFC 2045) into their 6-bit positive integer equivalents.
   * Characters that are not in the Base64 alphabet but fall within the bounds of the array are
   * translated to -1.
   *
   * Note: '+' and '-' both decode to 62. '/' and '_' both decode to 63. This means decoder
   * seamlessly handles both URL_SAFE and STANDARD base64. (The encoder, on the other hand, needs to
   * know ahead of time what to emit).
   *
   * Thanks to "commons" project in ws.apache.org for this code.
   * http://svn.apache.org/repos/asf/webservices/commons/trunk/modules/util/
   */
  private static final byte[] DECODE_TABLE = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      -1, -1, -1, -1, -1, -1, -1, 62, -1, 62, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1,
      -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
      20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36,
      37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51};

  /**
   * Base64 uses 6-bit fields.
   */
  /**
   * Mask used to extract 6 bits, used when encoding
   */
  private static final int MASK_6BITS = 0x3f;

  // The static final fields above are used for the original static byte[]
  // methods on Base64.
  // The private member fields below are used with the new streaming approach,
  // which requires
  // some state be preserved between calls of encode() and decode().
  /**
   * Encode table to use: either STANDARD or URL_SAFE. Note: the DECODE_TABLE above remains static
   * because it is able to decode both STANDARD and URL_SAFE streams, but the encodeTable must be a
   * member variable so we can switch between the two modes.
   */
  private final byte[] encodeTable;

  // Only one decode table currently; keep for consistency with Base32 code
  private final byte[] decodeTable = DECODE_TABLE;

  /**
   * Line separator for encoding. Not used when decoding. Only used if lineLength > 0.
   */
  private final byte[] lineSeparator;

  /**
   * Convenience variable to help us determine when our buffer is going to run out of room and needs
   * resizing. <code>decodeSize = 3 + lineSeparator.length;</code>
   */
  private final int decodeSize;

  /**
   * Convenience variable to help us determine when our buffer is going to run out of room and needs
   * resizing. <code>encodeSize = 4 + lineSeparator.length;</code>
   */
  private final int encodeSize;

  /**
   * Creates a Base64 codec used for decoding (all modes) and encoding in URL-unsafe mode.
   * <p>
   * When encoding the line length is 0 (no chunking), and the encoding table is
   * STANDARD_ENCODE_TABLE.
   * </p>
   *
   * <p>
   * When decoding all variants are supported.
   * </p>
   */
  public Base64() {
    this(0);
  }

  protected Base64(final int lineLength) {
    this(lineLength, CHUNK_SEPARATOR, false);
  }

  /**
   * Creates a Base64 codec used for decoding (all modes) and encoding in URL-unsafe mode.
   * <p>
   * When encoding the line length and line separator are given in the constructor, and the encoding
   * table is STANDARD_ENCODE_TABLE.
   * </p>
   * <p>
   * Line lengths that aren't multiples of 4 will still essentially end up being multiples of 4 in
   * the encoded data.
   * </p>
   * <p>
   * When decoding all variants are supported.
   * </p>
   *
   * @param lineLength Each line of encoded data will be at most of the given length (rounded down
   *        to nearest multiple of 4). If lineLength <= 0, then the output will not be divided into
   *        lines (chunks). Ignored when decoding. @param lineSeparator Each line of encoded data
   *        will end with this sequence of bytes.
   * @param urlSafe Instead of emitting '+' and '/' we emit '-' and '_' respectively. urlSafe is
   *        only applied to encode operations. Decoding seamlessly handles both modes. <b>Note: no
   *        padding is added when using the URL-safe alphabet.</b>
   * @throws IllegalArgumentException The provided lineSeparator included some base64 characters.
   *         That's not going to work!
   * @since 1.4
   */
  public Base64(final int lineLength, final byte[] lineSeparator, final boolean urlSafe) {
    this.unencodedBlockSize = BYTES_PER_UNENCODED_BLOCK;
    this.encodedBlockSize = BYTES_PER_ENCODED_BLOCK;
    this.chunkSeparatorLength = lineSeparator == null ? 0 : lineSeparator.length;
    final boolean useChunking = lineLength > 0 && chunkSeparatorLength > 0;
    this.lineLength = useChunking ? (lineLength / encodedBlockSize) * encodedBlockSize : 0;

    if (lineSeparator != null) {
      if (containsAlphabetOrPad(lineSeparator)) {
        final String sep = new String(lineSeparator, StandardCharsets.UTF_8);
        throw new IllegalArgumentException(
            "lineSeparator must not contain base64 characters: [" + sep + "]");
      }
      if (lineLength > 0) { // null line-sep forces no chunking rather
                            // than throwing IAE
        this.encodeSize = BYTES_PER_ENCODED_BLOCK + lineSeparator.length;
        this.lineSeparator = new byte[lineSeparator.length];
        System.arraycopy(lineSeparator, 0, this.lineSeparator, 0, lineSeparator.length);
      } else {
        this.encodeSize = BYTES_PER_ENCODED_BLOCK;
        this.lineSeparator = null;
      }
    } else {
      this.encodeSize = BYTES_PER_ENCODED_BLOCK;
      this.lineSeparator = null;
    }
    this.decodeSize = this.encodeSize - 1;
    this.encodeTable = urlSafe ? URL_SAFE_ENCODE_TABLE : STANDARD_ENCODE_TABLE;
  }

  /**
   * Tests a given byte array to see if it contains only valid characters within the alphabet. The
   * method optionally treats whitespace and pad as valid.
   *
   * @param arrayOctet byte array to test
   * @param allowWSPad if {@code true}, then whitespace and PAD are also allowed
   *
   * @return {@code true} if all bytes are valid characters in the alphabet or if the byte array is
   *         empty; {@code false}, otherwise
   */
  public boolean isInAlphabet(final byte[] arrayOctet, final boolean allowWSPad) {
    for (int i = 0; i < arrayOctet.length; i++) {
      if (!isInAlphabet(arrayOctet[i])
          && (!allowWSPad || (arrayOctet[i] != PAD) && !isWhiteSpace(arrayOctet[i]))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns whether or not the <code>octet</code> is in the Base64 alphabet.
   *
   * @param octet The value to test
   * @return {@code true} if the value is defined in the the Base64 alphabet {@code false}
   *         otherwise.
   */
  protected boolean isInAlphabet(final byte octet) {
    return octet >= 0 && octet < decodeTable.length && decodeTable[octet] != -1;
  }

  /**
   * Checks if a byte value is whitespace or not. Whitespace is taken to mean: space, tab, CR, LF
   *
   * @param byteToCheck the byte to check
   * @return true if byte is whitespace, false otherwise
   */
  protected static boolean isWhiteSpace(final byte byteToCheck) {
    switch (byteToCheck) {
      case ' ':
      case '\n':
      case '\r':
      case '\t':
        return true;
      default:
        return false;
    }
  }

  /**
   * Tests a given String to see if it contains only valid characters within the alphabet. The
   * method treats whitespace and PAD as valid.
   *
   * @param basen String to test
   * @return {@code true} if all characters in the String are valid characters in the alphabet or if
   *         the String is empty; {@code false}, otherwise
   * @see #isInAlphabet(byte[], boolean)
   */
  public boolean isInAlphabet(final String basen) {
    return isInAlphabet(basen.getBytes(StandardCharsets.UTF_8), true);
  }

  /**
   * Tests a given byte array to see if it contains any characters within the alphabet or PAD.
   *
   * Intended for use in checking line-ending arrays
   *
   * @param arrayOctet byte array to test
   * @return {@code true} if any byte is a valid character in the alphabet or PAD; {@code false}
   *         otherwise
   */
  protected boolean containsAlphabetOrPad(final byte[] arrayOctet) {
    if (arrayOctet == null) {
      return false;
    }
    for (final byte element : arrayOctet) {
      if (PAD == element || isInAlphabet(element)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns our current encode mode. True if we're URL-SAFE, false otherwise.
   *
   * @return true if we're in URL-SAFE mode, false otherwise.
   * @since 1.4
   */
  public boolean isUrlSafe() {
    return this.encodeTable == URL_SAFE_ENCODE_TABLE;
  }

  /**
   * Get the default buffer size. Can be overridden.
   *
   * @return {@link #DEFAULT_BUFFER_SIZE}
   */
  protected int getDefaultBufferSize() {
    return DEFAULT_BUFFER_SIZE;
  }

  /**
   * <b>WARNING: this method has never been even tried, after conversion from Apache Commons</b>
   * <p>
   * Encodes all of the provided data, starting at inPos, for inAvail bytes. Must be called at least
   * twice: once with the data to encode, and once with inAvail set to "-1" to alert encoder that
   * EOF has been reached, to flush last remaining bytes (if not multiple of 3).
   * </p>
   * <p>
   * <b>Note: no padding is added when encoding using the URL-safe alphabet.</b>
   * </p>
   * <p>
   * Thanks to "commons" project in ws.apache.org for the bitwise operations, and general approach.
   * http://svn.apache.org/repos/asf/webservices/commons/trunk/modules/util/
   * </p>
   *
   * @param in byte[] array of binary data to base64 encode.
   * @param offset Position to start reading data from.
   * @param length Amount of bytes available from input for encoding.
   * @param ctx the context to be used
   */
  // void encodeImpl(final byte[] in, int offset, final int length, final
  // Base64Context ctx)
  // throws FileParsingException {
  // try {
  // if (ctx.eof) {
  // return;
  // }
  // // inAvail < 0 is how we're informed of EOF in the underlying data we're
  // encoding.
  // if (length < 0) {
  // ctx.eof = true;
  // if (0 == ctx.modulus && lineLength == 0) {
  // return; // no leftovers to process and not using chunking
  // }
  //
  // int bytesToEncode = encodeSize;
  // byte[] buffer = ctx.ensureBufferHasCapacityLeft(bytesToEncode);
  // final int savedPos = ctx.pos;
  // switch (ctx.modulus) { // 0-2
  // case 0: // nothing to do here
  // break;
  // case 1: // 8 bits = 6 + 2
  // // top 6 bits:
  // buffer[ctx.pos++] = encodeTable[(ctx.ibitWorkArea >> 2) & MASK_6BITS];
  // // remaining 2:
  // buffer[ctx.pos++] = encodeTable[(ctx.ibitWorkArea << 4) & MASK_6BITS];
  // // URL-SAFE skips the padding to further reduce size.
  // if (encodeTable == STANDARD_ENCODE_TABLE) {
  // buffer[ctx.pos++] = PAD;
  // buffer[ctx.pos++] = PAD;
  // }
  // break;
  //
  // case 2: // 16 bits = 6 + 6 + 4
  // buffer[ctx.pos++] = encodeTable[(ctx.ibitWorkArea >> 10) & MASK_6BITS];
  // buffer[ctx.pos++] = encodeTable[(ctx.ibitWorkArea >> 4) & MASK_6BITS];
  // buffer[ctx.pos++] = encodeTable[(ctx.ibitWorkArea << 2) & MASK_6BITS];
  // // URL-SAFE skips the padding to further reduce size.
  // if (encodeTable == STANDARD_ENCODE_TABLE) {
  // buffer[ctx.pos++] = PAD;
  // }
  // break;
  // default:
  // throw new IllegalStateException("Impossible modulus " + ctx.modulus);
  // }
  // ctx.currentLinePos += ctx.pos - savedPos; // keep track of current line
  // position
  // // if currentPos == 0 we are at the start of a line, so don't add CRLF
  // if (lineLength > 0 && ctx.currentLinePos > 0) {
  // System.arraycopy(lineSeparator, 0, buffer, ctx.pos,
  // lineSeparator.length);
  // ctx.pos += lineSeparator.length;
  // }
  // } else {
  // int bytesToEncode = encodeSize * length;
  // byte[] buffer = ctx.ensureBufferHasCapacityLeft(bytesToEncode);
  // for (int i = 0; i < length; i++) {
  // //final byte[] buffer = ensureBufferSize(encodeSize, context);
  // ctx.modulus = (ctx.modulus + 1) % BYTES_PER_UNENCODED_BLOCK;
  // int b = in[offset++];
  // if (b < 0) {
  // b += 256;
  // }
  // ctx.ibitWorkArea = (ctx.ibitWorkArea << 8) + b; // BITS_PER_BYTE
  // if (0 == ctx.modulus) { // 3 bytes = 24 bits = 4 * 6 bits to extract
  // buffer[ctx.pos++] = encodeTable[(ctx.ibitWorkArea >> 18) & MASK_6BITS];
  // buffer[ctx.pos++] = encodeTable[(ctx.ibitWorkArea >> 12) & MASK_6BITS];
  // buffer[ctx.pos++] = encodeTable[(ctx.ibitWorkArea >> 6) & MASK_6BITS];
  // buffer[ctx.pos++] = encodeTable[ctx.ibitWorkArea & MASK_6BITS];
  // ctx.currentLinePos += BYTES_PER_ENCODED_BLOCK;
  // if (lineLength > 0 && lineLength <= ctx.currentLinePos) {
  // System.arraycopy(lineSeparator, 0, buffer, ctx.pos,
  // lineSeparator.length);
  // ctx.pos += lineSeparator.length;
  // ctx.currentLinePos = 0;
  // }
  // }
  // }
  // }
  // } finally {
  // ctx.syncBufferPos();
  // }
  // }

  /**
   * Decodes a byte[] containing characters in the Base-N alphabet.
   *
   * @param pArray A byte array containing Base-N character data
   * @param ctx
   * @return a byte array containing binary data
   * @throws umich.ms.fileio.exceptions.FileParsingException
   */
  public Base64Context decode(final byte[] pArray, Base64Context ctx) throws MSDKException {
    return decode(pArray, 0, pArray.length, ctx);
  }

  /**
   * Decodes a byte[] containing characters in the Base-N alphabet.
   *
   * @param pArray A byte array containing Base-N character data
   * @param offset
   * @param length
   * @param ctx
   * @return a byte array containing binary data
   * @throws umich.ms.fileio.exceptions.FileParsingException
   */
  public Base64Context decode(final byte[] pArray, int offset, int length, Base64Context ctx)
      throws MSDKException {
    if (pArray == null || pArray.length == 0) {
      return null;
    }
    decodeImpl(pArray, offset, length, ctx);
    decodeImpl(pArray, 0, EOF, ctx); // Notify decoder of EOF.
    return ctx;
  }

  /**
   * <p>
   * Decodes all of the provided data, starting at inPos, for inAvail bytes. Should be called at
   * least twice: once with the data to decode, and once with inAvail set to "-1" to alert decoder
   * that EOF has been reached. The "-1" call is not necessary when decoding, but it doesn't hurt,
   * either.
   * </p>
   * <p>
   * Ignores all non-base64 characters. This is how chunked (e.g. 76 character) data is handled,
   * since CR and LF are silently ignored, but has implications for other bytes, too. This method
   * subscribes to the garbage-in, garbage-out philosophy: it will not check the provided data for
   * validity.
   * </p>
   * <p>
   * Thanks to "commons" project in ws.apache.org for the bitwise operations, and general approach.
   * http://svn.apache.org/repos/asf/webservices/commons/trunk/modules/util/
   * </p>
   *
   * @param bytes byte[] array of ascii data to base64 decode.
   * @param offset Position to start reading data from.
   * @param length Amount of bytes available from input for dencoding.
   * @param ctx the context to be used
   */
  void decodeImpl(byte[] bytes, int offset, int length, Base64Context ctx) throws MSDKException {
    try {

      if (ctx.eof) {
        return;
      }
      byte[] buffer = null;
      if (length < 0) {
        ctx.eof = true;
      } else {
        // in general the resulting decoded array should be 3/4 the
        // length of the input. We just leave it at 'length' to be on
        // the safe side.
        buffer = ctx.ensureBufferHasCapacityLeft(length);
      }

      int readPos = offset;
      byte b;
      for (int i = 0; i < length; i++) {
        // I don't know why in Apache impl this has been checked on
        // every iteration
        // final byte[] buffer = ensureBufferSize(decodeSize, context);
        b = bytes[readPos++];
        if (b == PAD) {
          // We're done.
          ctx.eof = true;
          break;
        } else {
          try {
            if (b >= 0 && b < DECODE_TABLE.length) {
              final int result = DECODE_TABLE[b];
              if (result >= 0) {
                ctx.modulus = (ctx.modulus + 1) % BYTES_PER_ENCODED_BLOCK;
                ctx.ibitWorkArea = (ctx.ibitWorkArea << BITS_PER_ENCODED_BYTE) + result;
                if (ctx.modulus == 0) {
                  buffer[ctx.pos] = (byte) ((ctx.ibitWorkArea >> 16) & MASK_8BITS);
                  ctx.pos++;
                  buffer[ctx.pos] = (byte) ((ctx.ibitWorkArea >> 8) & MASK_8BITS);
                  ctx.pos++;
                  buffer[ctx.pos] = (byte) (ctx.ibitWorkArea & MASK_8BITS);
                  ctx.pos++;
                }
              }
            }
          } catch (ArrayIndexOutOfBoundsException e) {
            throw new MSDKException(
                "Something went wrong in Base64 decoder, got out of array bounds" + e);
          }
        }
      }

      // Two forms of EOF as far as base64 decoder is concerned: actual
      // EOF (-1) and first time '=' character is encountered in stream.
      // This approach makes the '=' padding characters completely
      // optional.
      if (ctx.eof && ctx.modulus != 0) {
        buffer = ctx.ensureBufferHasCapacityLeft(decodeSize);

        // We have some spare bits remaining
        // Output all whole multiples of 8 bits and ignore the rest
        switch (ctx.modulus) {
          // case 0 : // impossible, as excluded above
          case 1: // 6 bits - ignore entirely
            // TODO not currently tested; perhaps it is impossible?
            break;
          case 2: // 12 bits = 8 + 4
            ctx.ibitWorkArea = ctx.ibitWorkArea >> 4; // dump the extra
                                                      // 4 bits
            buffer[ctx.pos++] = (byte) ((ctx.ibitWorkArea) & MASK_8BITS);
            break;
          case 3: // 18 bits = 8 + 8 + 2
            ctx.ibitWorkArea = ctx.ibitWorkArea >> 2; // dump 2 bits
            buffer[ctx.pos++] = (byte) ((ctx.ibitWorkArea >> 8) & MASK_8BITS);
            buffer[ctx.pos++] = (byte) ((ctx.ibitWorkArea) & MASK_8BITS);
            break;
          default:
            throw new IllegalStateException("Impossible modulus " + ctx.modulus);
        }
      }

    } finally {
      ctx.syncBufferPos();
    }
  }

  /**
   * Decodes a char[] containing characters in the Base-N alphabet.
   *
   * @param pArray A byte array containing Base-N character data
   * @param ctx
   * @return a byte array containing binary data
   * @throws umich.ms.fileio.exceptions.FileParsingException
   */
  public Base64Context decode(final char[] pArray, Base64Context ctx) throws MSDKException {
    return decode(pArray, 0, pArray.length, ctx);
  }

  /**
   * Decodes a char[] containing characters in the Base-N alphabet.
   *
   * @param pArray A byte array containing Base-N character data
   * @param offset offset in the input array where the data starts
   * @param length length of the data, starting at offset in the input array
   * @param ctx {@link Base64Context} or {@link Base64ContextPooled}, pooled version is preferred
   * @return a byte array containing binary data
   * @throws umich.ms.fileio.exceptions.FileParsingException
   */
  public Base64Context decode(final char[] pArray, final int offset, final int length,
      Base64Context ctx) throws MSDKException {
    if (pArray == null || pArray.length == 0) {
      return null;
    }
    decodeImpl(pArray, offset, length, ctx);
    decodeImpl(pArray, 0, EOF, ctx); // Notify decoder of EOF.
    return ctx;
  }

  /**
   * <p>
   * Decodes all of the provided data, starting at inPos, for inAvail bytes. Should be called at
   * least twice: once with the data to decode, and once with inAvail set to "-1" to alert decoder
   * that EOF has been reached. The "-1" call is not necessary when decoding, but it doesn't hurt,
   * either.
   * </p>
   * <p>
   * Ignores all non-base64 characters. This is how chunked (e.g. 76 character) data is handled,
   * since CR and LF are silently ignored, but has implications for other bytes, too. This method
   * subscribes to the garbage-in, garbage-out philosophy: it will not check the provided data for
   * validity.
   * </p>
   * <p>
   * Thanks to "commons" project in ws.apache.org for the bitwise operations, and general approach.
   * http://svn.apache.org/repos/asf/webservices/commons/trunk/modules/util/
   * </p>
   *
   * @param chars char[] array of ascii data to base64 decode.
   * @param offset offset in the input array where the data starts
   * @param length length of the data, starting at offset in the input array
   * @param ctx the context to be used
   */
  void decodeImpl(final char[] chars, final int offset, final int length, Base64Context ctx)
      throws MSDKException {
    try {

      if (ctx.eof) {
        return;
      }
      byte[] buffer = null;
      if (length < 0) {
        ctx.eof = true;
      } else {
        // in general the resulting decoded array should be 3/4 the
        // length of the input. We double that amount, as consequent
        // scans
        // might have longer base64 strings
        buffer = ctx.ensureBufferHasCapacityLeft(length * 2);
      }

      int readPos = offset;
      byte b;
      for (int i = 0; i < length; i++) {
        // I don't know why in Apache impl this has been checked on
        // every iteration
        // final byte[] buffer = ensureBufferSize(decodeSize, context);
        b = (byte) chars[readPos++];
        if (b == PAD) {
          // We're done.
          ctx.eof = true;
          break;
        } else {
          try {
            if (b >= 0 && b < DECODE_TABLE.length) {
              final int result = DECODE_TABLE[b];
              if (result >= 0) {
                ctx.modulus = (ctx.modulus + 1) % BYTES_PER_ENCODED_BLOCK;
                ctx.ibitWorkArea = (ctx.ibitWorkArea << BITS_PER_ENCODED_BYTE) + result;
                if (ctx.modulus == 0) {
                  buffer[ctx.pos] = (byte) ((ctx.ibitWorkArea >> 16) & MASK_8BITS);
                  ctx.pos++;
                  buffer[ctx.pos] = (byte) ((ctx.ibitWorkArea >> 8) & MASK_8BITS);
                  ctx.pos++;
                  buffer[ctx.pos] = (byte) (ctx.ibitWorkArea & MASK_8BITS);
                  ctx.pos++;
                }
              }
            }
          } catch (ArrayIndexOutOfBoundsException e) {
            throw new MSDKException(
                "Something went wrong in Base64 decoder, got out of array bounds" + e);
          }
        }
      }

      // Two forms of EOF as far as base64 decoder is concerned: actual
      // EOF (-1) and first time '=' character is encountered in stream.
      // This approach makes the '=' padding characters completely
      // optional.
      if (ctx.eof && ctx.modulus != 0) {
        buffer = ctx.ensureBufferHasCapacityLeft(decodeSize);

        // We have some spare bits remaining
        // Output all whole multiples of 8 bits and ignore the rest
        switch (ctx.modulus) {
          // case 0 : // impossible, as excluded above
          case 1: // 6 bits - ignore entirely
            // TODO not currently tested; perhaps it is impossible?
            break;
          case 2: // 12 bits = 8 + 4
            ctx.ibitWorkArea = ctx.ibitWorkArea >> 4; // dump the extra
                                                      // 4 bits
            buffer[ctx.pos++] = (byte) ((ctx.ibitWorkArea) & MASK_8BITS);
            break;
          case 3: // 18 bits = 8 + 8 + 2
            ctx.ibitWorkArea = ctx.ibitWorkArea >> 2; // dump 2 bits
            buffer[ctx.pos++] = (byte) ((ctx.ibitWorkArea >> 8) & MASK_8BITS);
            buffer[ctx.pos++] = (byte) ((ctx.ibitWorkArea) & MASK_8BITS);
            break;
          default:
            throw new IllegalStateException("Impossible modulus " + ctx.modulus);
        }
      }

    } finally {
      ctx.syncBufferPos();
    }
  }

}
