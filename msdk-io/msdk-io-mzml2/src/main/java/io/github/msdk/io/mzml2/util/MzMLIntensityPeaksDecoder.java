/*
 * (C) Copyright 2015-2016 by MSDK Development Team
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

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.zip.DataFormatException;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.IOUtils;

import com.google.common.io.LittleEndianDataInputStream;

import io.github.msdk.MSDKException;
import io.github.msdk.io.mzml2.data.MzMLBinaryDataInfo;
import io.github.msdk.io.mzml2.data.MzMLBinaryDataInfo.MzMLCompressionType;
import io.github.msdk.io.mzml2.util.ByteArrayHolder;
import io.github.msdk.io.mzml2.util.MSNumpressFloat;
import io.github.msdk.io.mzml2.util.ZlibInflater;

/**
 * <p>
 * MzMLIntensityPeaksDecoder class.
 * </p>
 *
 * @author plusik
 * @version $Id: $Id
 */
public class MzMLIntensityPeaksDecoder {

  public static class DecodedData {
    private float[] arr;
    float valMax;
    float valMaxPos;
    float valMin;
    int valMinPos;
    float valMinNonZero;
    int valMinNonZeroPos;
    float sum;

    public DecodedData(float[] arr, float valMax, int valMaxPos, float valMin, int valMinPos,
        float valMinNonZero, int valMinNonZeroPos, float sum) {
      this.arr = arr;
      this.valMax = valMax;
      this.valMaxPos = valMaxPos;
      this.valMin = valMin;
      this.valMinPos = valMinPos;
      this.valMinNonZero = valMinNonZero;
      this.valMinNonZeroPos = valMinNonZeroPos;
      this.sum = sum;
    }

    public static DecodedData createEmpty() {
      return new DecodedData(new float[0], 0, -1, 0, -1, 0, -1, 0);
    }

    public float[] getDecodedArray() {
      return arr;
    }

  }

  /**
   * Converts a base64 encoded mz or intensity string used in mzML files to an array of doubles. If
   * the original precision was 32 bit, you still get doubles as output, would be too complicated to
   * provide another method to parseIndexEntries them as floats. Hopefully some day everything will
   * be in 64 bits anyway.
   *
   * @param InputStream, decoded from a base64 encoded string<br>
   *        E.g. like: eNoNxltIkwEYBuAOREZFhrCudGFbbraTU+Zmue...
   * @param lengthIn length of data to be treated as values, i.e. the input array can be longer, the
   *        values to be interpreted must start at offset 0, and this will indicate the length
   * @param precision allowed values: 32 and 64, can be null only if MS-NUMPRESS compression was
   *        applied and is specified in the @{code compressions} enum set.
   * @param numPoints a int.
   * @param compression null or MzMLCompressionType#NO_COMPRESSION have the same effect. Otherwise
   *        the binary data will be inflated according to the compression rules.
   * @throws java.util.zip.DataFormatException if any.
   * @throws java.io.IOException if any.
   * @return a {@link io.github.msdk.io.mzml2.MzMLIntensityPeaksDecoder.DecodedData} object.
   * @throws io.github.msdk.MSDKException if any.
   */
  public static DecodedData decode(InputStream is, int lengthIn, Integer precision, int numPoints,
      MzMLBinaryDataInfo.MzMLCompressionType compression)
      throws DataFormatException, IOException, MSDKException {

    // for some reason there sometimes might be zero length <peaks> tags
    // (ms2 usually)
    // in this case we just return an empty result
    if (lengthIn == 0) {
      return DecodedData.createEmpty();
    }

    EnumSet<MzMLBinaryDataInfo.MzMLCompressionType> compressions =
        EnumSet.noneOf(MzMLBinaryDataInfo.MzMLCompressionType.class);

    if (compression == MzMLCompressionType.NUMPRESS_LINPRED_ZLIB) {
      compressions.add(MzMLCompressionType.NUMPRESS_LINPRED);
      compressions.add(MzMLCompressionType.ZLIB);
    } else if (compression == MzMLCompressionType.NUMPRESS_POSINT_ZLIB) {
      compressions.add(MzMLCompressionType.NUMPRESS_POSINT);
      compressions.add(MzMLCompressionType.ZLIB);
    } else if (compression == MzMLCompressionType.NUMPRESS_SHLOGF_ZLIB) {
      compressions.add(MzMLCompressionType.NUMPRESS_SHLOGF);
      compressions.add(MzMLCompressionType.ZLIB);
    } else if (compression == null) {
      compressions.add(MzMLCompressionType.NO_COMPRESSION);
    } else {
      compressions.add(compression);
    }

    InflaterInputStream iis = null;
    LittleEndianDataInputStream dis = null;
    ByteArrayHolder bytes = null;
    boolean isBytesFromPool = false;

    try { // try/catch to return the byte array, possibly borrowed from a
          // pool

      float[] data = new float[numPoints];

      // first check for zlib compression, inflation must be done before
      // NumPress
      if (compressions.contains(MzMLBinaryDataInfo.MzMLCompressionType.ZLIB)) {
        iis = new InflaterInputStream(is);
        dis = new LittleEndianDataInputStream(iis);
        // bytes = ZlibInflater.zlibUncompressBuffer(bytesIn, lengthIn, null);
        isBytesFromPool = true;
      } else if (compressions.contains(MzMLCompressionType.NO_COMPRESSION)) {
        dis = new LittleEndianDataInputStream(is);
      } else {
        bytes = new ByteArrayHolder(IOUtils.toByteArray(is));
        bytes.setPosition(lengthIn);
      }

      // now can check for NumPress
      if (compressions.contains(MzMLBinaryDataInfo.MzMLCompressionType.NUMPRESS_LINPRED)) {
        int numDecodedDoubles =
            MSNumpressFloat.decodeLinear(bytes.getUnderlyingBytes(), bytes.getPosition(), data);
        if (numDecodedDoubles < 0) {
          throw new MSDKException("MSNumpress linear decoder failed");
        }
        return toDecodedData(data);
      } else if (compressions.contains(MzMLBinaryDataInfo.MzMLCompressionType.NUMPRESS_POSINT)) {
        int numDecodedDoubles =
            MSNumpressFloat.decodePic(bytes.getUnderlyingBytes(), bytes.getPosition(), data);
        if (numDecodedDoubles < 0) {
          throw new MSDKException("MSNumpress positive integer decoder failed");
        }
        return toDecodedData(data);
      } else if (compressions.contains(MzMLBinaryDataInfo.MzMLCompressionType.NUMPRESS_SHLOGF)) {
        int numDecodedDoubles =
            MSNumpressFloat.decodeSlof(bytes.getUnderlyingBytes(), bytes.getPosition(), data);
        if (numDecodedDoubles < 0) {
          throw new MSDKException("MSNumpress short logged float decoder failed");
        }
        return toDecodedData(data);
      }

      if (precision == null) {
        throw new IllegalArgumentException(
            "Precision MUST be specified, if MS-NUMPRESS compression was not used");
      }

      // byte[] decoded = bytes.getUnderlyingBytes();
      // int chunkSize = precision / 8; // in bytes

      // int offset;
      float valMax = Float.NEGATIVE_INFINITY;
      int valMaxPos = 0;
      float valMin = Float.POSITIVE_INFINITY;
      int valMinPos = 0;
      float valMinNonZero = Float.POSITIVE_INFINITY;
      int valMinNonZeroPos = 0;
      float sum = 0f;

      switch (precision) {
        case (32): {
          int asInt;
          float asFloat;

          for (int i = 0; i < numPoints; i++) {
            // offset = i * chunkSize;

            // hopefully this way is faster
            // asInt = ((decoded[offset] & 0xFF)) // zero shift
            // | ((decoded[offset + 1] & 0xFF) << 8) | ((decoded[offset + 2] & 0xFF) << 16)
            // | ((decoded[offset + 3] & 0xFF) << 24);
            asInt = ((dis.readByte() & 0xFF)) // zero shift
                | ((dis.readByte() & 0xFF) << 8) | ((dis.readByte() & 0xFF) << 16)
                | ((dis.readByte() & 0xFF) << 24);
            asFloat = Float.intBitsToFloat(asInt);
            if (asFloat > valMax) {
              valMax = asFloat;
              valMaxPos = i;
            }

            if (asFloat < valMinNonZero) {
              if (asFloat > 0) {
                valMinNonZero = asFloat;
                valMinNonZeroPos = i;
              }
              if (asFloat < valMin) {
                valMin = asFloat;
                valMinPos = i;
              }
            }

            sum = sum + asFloat;
            data[i] = asFloat;
          }
          break;
        }
        case (64): {
          long asLong;
          float asDouble;

          for (int i = 0; i < numPoints; i++) {
            // offset = i * chunkSize;

            // asLong = ((long) (decoded[offset] & 0xFF)) // zero shift
            // | ((long) (decoded[offset + 1] & 0xFF) << 8)
            // | ((long) (decoded[offset + 2] & 0xFF) << 16)
            // | ((long) (decoded[offset + 3] & 0xFF) << 24)
            // | ((long) (decoded[offset + 4] & 0xFF) << 32)
            // | ((long) (decoded[offset + 5] & 0xFF) << 40)
            // | ((long) (decoded[offset + 6] & 0xFF) << 48)
            // | ((long) (decoded[offset + 7] & 0xFF) << 56);
            asLong = ((long) (dis.readByte() & 0xFF)) // zero shift
                | ((long) (dis.readByte() & 0xFF) << 8) | ((long) (dis.readByte() & 0xFF) << 16)
                | ((long) (dis.readByte() & 0xFF) << 24) | ((long) (dis.readByte() & 0xFF) << 32)
                | ((long) (dis.readByte() & 0xFF) << 40) | ((long) (dis.readByte() & 0xFF) << 48)
                | ((long) (dis.readByte() & 0xFF) << 56);
            asDouble = (float) Double.longBitsToDouble(asLong);

            if (asDouble > valMax) {
              valMax = asDouble;
              valMaxPos = i;
            }

            if (asDouble < valMinNonZero) {
              if (asDouble > 0) {
                valMinNonZero = asDouble;
                valMinNonZeroPos = i;
              }
              if (asDouble < valMin) {
                valMin = asDouble;
                valMinPos = i;
              }
            }

            sum = sum + asDouble;
            data[i] = asDouble;
          }
          break;
        }
        default: {
          throw new IllegalArgumentException(
              "Precision can only be 32/64 bits, other values are not valid.");
        }
      }

      return new DecodedData(data, valMax, valMaxPos, valMin, valMinPos, valMinNonZero,
          valMinNonZeroPos, sum);

    } catch (OutOfMemoryError oom) {
      throw new MSDKException("Could not allocate arrays during spectra decoding step\n" + oom);
    } finally {
      // return ByteArrayHolder to the pool
      if (isBytesFromPool && bytes != null) {
        try {
          ZlibInflater.getPool().returnObject(bytes);
        } catch (Exception e) {
          throw new MSDKException("Could not return ByteArrayHolder to the pool.\n" + e);
        }
      }
    }
  }

  /**
   * <p>
   * toDecodedData.
   * </p>
   *
   * @param arr an array of float.
   * @return a {@link io.github.msdk.io.mzml2.MzMLIntensityPeaksDecoder.DecodedData} object.
   */
  protected static DecodedData toDecodedData(float[] arr) {
    if (arr.length == 0) {
      throw new IllegalArgumentException("Array length of zero is not allowed here");
    }
    float valMax = Float.NEGATIVE_INFINITY;
    int valMaxPos = 0;
    float valMin = Float.POSITIVE_INFINITY;
    int valMinPos = 0;
    float valMinNonZero = Float.POSITIVE_INFINITY;
    int valMinNonZeroPos = 0;
    float sum = 0f;

    float val;
    for (int i = 0; i < arr.length; i++) {
      val = arr[i];
      if (val > valMax) {
        valMax = val;
        valMaxPos = i;
      }

      if (val < valMinNonZero) {
        if (val > 0) {
          valMinNonZero = val;
          valMinNonZeroPos = i;
        }
        if (val < valMin) {
          valMin = val;
          valMinPos = i;
        }
      }

      sum = sum + val;
      arr[i] = val;
    }

    return new DecodedData(arr, valMax, valMaxPos, valMin, valMinPos, valMinNonZero,
        valMinNonZeroPos, sum);
  }
}
