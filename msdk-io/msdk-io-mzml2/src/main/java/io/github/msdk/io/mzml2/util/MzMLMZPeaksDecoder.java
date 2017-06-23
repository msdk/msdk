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

/**
 * <p>
 * MzMLMZPeaksDecoder class.
 * </p>
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

    public double[] getDecodedArray() {
      return arr;
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
   * @return a {@link io.github.msdk.io.mzml2.MzMLMzPeaksDecoder.DecodedData} object.
   * @throws io.github.msdk.MSDKException if any.
   */
  public static DecodedData decode(InputStream is, int lengthIn, Integer precision, int numPoints,
      MzMLBinaryDataInfo.MzMLCompressionType compression)
      throws DataFormatException, IOException, MSDKException {

    if (lengthIn == 0) {
      return DecodedData.createEmpty();
    }

    EnumSet<MzMLBinaryDataInfo.MzMLCompressionType> compressions =
        MzMLCompressionsHelper.getCompressions(compression);

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
    InflaterInputStream iis = null;
    LittleEndianDataInputStream dis = null;
    ByteArrayHolder bytes = null;

    double[] data = new double[numPoints];

    // first check for zlib compression, inflation must be done before
    // NumPress
    if (compressions.contains(MzMLBinaryDataInfo.MzMLCompressionType.ZLIB)) {
      iis = new InflaterInputStream(is);
      dis = new LittleEndianDataInputStream(iis);
    } else if (compressions.contains(MzMLCompressionType.NO_COMPRESSION)) {
      dis = new LittleEndianDataInputStream(is);
    } else {
      bytes = new ByteArrayHolder(IOUtils.toByteArray(is));
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
          asInt = dis.readInt();
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
          asLong = dis.readLong();
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

  }

  /**
   * <p>
   * toDecodedData.
   * </p>
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
