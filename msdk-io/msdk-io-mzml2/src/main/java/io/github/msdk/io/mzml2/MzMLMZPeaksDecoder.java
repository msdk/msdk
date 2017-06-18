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
import java.util.EnumSet;
import java.util.zip.DataFormatException;

import io.github.msdk.MSDKException;
import io.github.msdk.io.mzml2.util.ByteArrayHolder;
import io.github.msdk.io.mzml2.util.MSNumpressDouble;
import io.github.msdk.io.mzml2.util.ZlibInflater;

/**
 * <p>MzMLMZPeaksDecoder class.</p>
 *
 * @author Dmitry Avtonomov
 * @version $Id: $Id
 */
public class MzMLMZPeaksDecoder {

  public static class DecodedData {
    double[] arr;
    double valMax;
    int valMaxPos;
    double valMin;
    int valMinPos;
    double valMinNonZero;
    int valMinNonZeroPos;
    double sum;

    public DecodedData(double[] arr, double valMax, int valMaxPos, double valMin, int valMinPos,
        double valMinNonZero, int valMinNonZeroPos, double sum) {
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
      return new DecodedData(new double[0], 0, -1, 0, -1, 0, -1, 0);
    }
  }

  /**
   * Converts a base64 encoded mz or intensity string used in mzML files to an array of doubles. If
   * the original precision was 32 bit, you still get doubles as output, would be too complicated to
   * provide another method to parseIndexEntries them as floats. Hopefully some day everything will
   * be in 64 bits anyway.
   *
   * @param bytesIn Byte array, decoded from a base64 encoded string<br>
   *        E.g. like: eNoNxltIkwEYBuAOREZFhrCudGFbbraTU+Zmue...
   * @param lengthIn length of data to be treated as values, i.e. the input array can be longer, the
   *        values to be interpreted must start at offset 0, and this will indicate the length
   * @param precision allowed values: 32 and 64, can be null only if MS-NUMPRESS compression was
   *        applied and is specified in the @{code compressions} enum set.
   * @param numPoints a int.
   * @param compressions null or MzMLCompressionType#NONE have the same
   *        effect. Otherwise the binary data will be inflated according to the compression rules.
   * @throws java.util.zip.DataFormatException if any.
   * @throws java.io.IOException if any.
   * @return a 
   * @throws io.github.msdk.MSDKException if any.
   */
  public static DecodedData decode(byte[] bytesIn, int lengthIn, Integer precision, int numPoints,
      EnumSet<MzMLBinaryDataInfo.MzMLCompressionType> compressions)
      throws DataFormatException, IOException, MSDKException {

    // for some reason there sometimes might be zero length <peaks> tags
    // (ms2 usually)
    // in this case we just return an empty result
    if (bytesIn.length == 0 || lengthIn == 0) {
      return DecodedData.createEmpty();
    }
    if (compressions == null) {
      compressions = EnumSet.noneOf(MzMLBinaryDataInfo.MzMLCompressionType.class);
    }

    /////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////
    ////// //////
    ////// //////
    ////// CRITICAL SPOT //////
    ////// //////
    ////// We might not have enough memory //////
    ////// for the data array //////
    ////// //////
    /////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////
    ByteArrayHolder bytes = null;
    boolean isBytesFromPool = false;

    try { // try/catch to return the byte array, possibly borrowed from a
          // pool

      double[] data = new double[numPoints];

      // first check for zlib compression, inflation must be done before
      // NumPress
      if (compressions.contains(MzMLBinaryDataInfo.MzMLCompressionType.ZLIB)) {
        bytes = ZlibInflater.zlibUncompressBuffer(bytesIn, lengthIn, null);
        isBytesFromPool = true;
      } else {
        bytes = new ByteArrayHolder(bytesIn);
        bytes.setPosition(lengthIn);
      }

      // now can check for NumPress
      if (compressions.contains(MzMLBinaryDataInfo.MzMLCompressionType.NUMPRESS_LINPRED)) {
        int numDecodedDoubles =
            MSNumpressDouble.decodeLinear(bytes.getUnderlyingBytes(), bytes.getPosition(), data);
        if (numDecodedDoubles < 0) {
          throw new MSDKException("MSNumpress linear decoder failed");
        }
        return toDecodedData(data);
      } else if (compressions.contains(MzMLBinaryDataInfo.MzMLCompressionType.NUMPRESS_POSINT)) {
        int numDecodedDoubles =
            MSNumpressDouble.decodePic(bytes.getUnderlyingBytes(), bytes.getPosition(), data);
        if (numDecodedDoubles < 0) {
          throw new MSDKException("MSNumpress positive integer decoder failed");
        }
        return toDecodedData(data);
      } else if (compressions.contains(MzMLBinaryDataInfo.MzMLCompressionType.NUMPRESS_SHLOGF)) {
        int numDecodedDoubles =
            MSNumpressDouble.decodeSlof(bytes.getUnderlyingBytes(), bytes.getPosition(), data);
        if (numDecodedDoubles < 0) {
          throw new MSDKException("MSNumpress short logged float decoder failed");
        }
        return toDecodedData(data);
      }

      if (precision == null) {
        throw new IllegalArgumentException(
            "Precision MUST be specified, if MS-NUMPRESS compression was not used");
      }
      int decodedLen = bytes.getPosition(); // in bytes
      byte[] decoded = bytes.getUnderlyingBytes();
      int chunkSize = precision / 8; // in bytes

      int offset;
      double valMax = Double.NEGATIVE_INFINITY;
      int valMaxPos = 0;
      double valMin = Double.POSITIVE_INFINITY;
      int valMinPos = 0;
      double valMinNonZero = Double.POSITIVE_INFINITY;
      int valMinNonZeroPos = 0;
      double sum = 0d;

      switch (precision) {
        case (32): {
          int asInt;
          float asFloat;

          for (int i = 0; i < numPoints; i++) {
            offset = i * chunkSize;

            // hopefully this way is faster
            asInt = ((decoded[offset] & 0xFF)) // zero shift
                | ((decoded[offset + 1] & 0xFF) << 8) | ((decoded[offset + 2] & 0xFF) << 16)
                | ((decoded[offset + 3] & 0xFF) << 24);
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
          double asDouble;

          for (int i = 0; i < numPoints; i++) {
            offset = i * chunkSize;

            asLong = ((long) (decoded[offset] & 0xFF)) // zero shift
                | ((long) (decoded[offset + 1] & 0xFF) << 8)
                | ((long) (decoded[offset + 2] & 0xFF) << 16)
                | ((long) (decoded[offset + 3] & 0xFF) << 24)
                | ((long) (decoded[offset + 4] & 0xFF) << 32)
                | ((long) (decoded[offset + 5] & 0xFF) << 40)
                | ((long) (decoded[offset + 6] & 0xFF) << 48)
                | ((long) (decoded[offset + 7] & 0xFF) << 56);
            asDouble = Double.longBitsToDouble(asLong);

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
   * <p>toDecodedData.</p>
   *
   * @param arr an array of double.
   * @return a {@link io.github.msdk.io.mzml2.MzMLMZPeaksDecoder.DecodedData} object.
   */
  protected static DecodedData toDecodedData(double[] arr) {
    if (arr.length == 0) {
      throw new IllegalArgumentException("Array length of zero is not allowed here");
    }
    double valMax = Double.NEGATIVE_INFINITY;
    int valMaxPos = 0;
    double valMin = Double.POSITIVE_INFINITY;
    int valMinPos = 0;
    double valMinNonZero = Double.POSITIVE_INFINITY;
    int valMinNonZeroPos = 0;
    double sum = 0d;

    double val;
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
