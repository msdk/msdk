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

/**
 * <p>
 * MzMLIntensityPeaksDecoder class.
 * </p>
 *
 * @author plusik
 * @version $Id: $Id
 */
public class MzMLPeaksDecoder {

  /**
   * Converts a base64 encoded mz or intensity string used in mzML files to an array of floats. If
   * the original precision was 64 bit, you still get floats as output.
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
   * @return a float array containing the decoded values
   * @throws io.github.msdk.MSDKException if any.
   */
  public static float[] decodeToFloat(InputStream is, int lengthIn, Integer precision,
      int numPoints, MzMLBinaryDataInfo.MzMLCompressionType compression)
      throws DataFormatException, IOException, MSDKException {

    // for some reason there sometimes might be zero length <peaks> tags
    // (ms2 usually)
    // in this case we just return an empty result
    if (lengthIn == 0) {
      return new float[0];
    }

    EnumSet<MzMLBinaryDataInfo.MzMLCompressionType> compressions =
        MzMLCompressionsHelper.getCompressions(compression);

    InflaterInputStream iis = null;
    LittleEndianDataInputStream dis = null;
    byte[] bytes = null;

    float[] data = new float[numPoints];

    // first check for zlib compression, inflation must be done before
    // NumPress
    if (compressions.contains(MzMLBinaryDataInfo.MzMLCompressionType.ZLIB)) {
      iis = new InflaterInputStream(is);
      dis = new LittleEndianDataInputStream(iis);
    } else if (compressions.contains(MzMLCompressionType.NO_COMPRESSION)) {
      dis = new LittleEndianDataInputStream(is);
    } else {
      bytes = IOUtils.toByteArray(is);
    }

    // now can check for NumPress
    if (compressions.contains(MzMLBinaryDataInfo.MzMLCompressionType.NUMPRESS_LINPRED)) {
      int numDecodedDoubles = MSNumpress.decodeLinear(bytes, bytes.length, data);
      if (numDecodedDoubles < 0) {
        throw new MSDKException("MSNumpress linear decoder failed");
      }
      return data;
    } else if (compressions.contains(MzMLBinaryDataInfo.MzMLCompressionType.NUMPRESS_POSINT)) {
      int numDecodedDoubles = MSNumpress.decodePic(bytes, bytes.length, data);
      if (numDecodedDoubles < 0) {
        throw new MSDKException("MSNumpress positive integer decoder failed");
      }
      return data;
    } else if (compressions.contains(MzMLBinaryDataInfo.MzMLCompressionType.NUMPRESS_SHLOGF)) {
      int numDecodedDoubles = MSNumpress.decodeSlof(bytes, bytes.length, data);
      if (numDecodedDoubles < 0) {
        throw new MSDKException("MSNumpress short logged float decoder failed");
      }
      return data;
    }

    if (precision == null) {
      throw new IllegalArgumentException(
          "Precision MUST be specified, if MS-NUMPRESS compression was not used");
    }

    switch (precision) {
      case (32): {
        int asInt;

        for (int i = 0; i < numPoints; i++) {
          asInt = dis.readInt();
          data[i] = Float.intBitsToFloat(asInt);
        }
        break;
      }
      case (64): {
        long asLong;

        for (int i = 0; i < numPoints; i++) {
          asLong = dis.readLong();
          data[i] = (float) Double.longBitsToDouble(asLong);
        }
        break;
      }
      default: {
        throw new IllegalArgumentException(
            "Precision can only be 32/64 bits, other values are not valid.");
      }
    }

    return data;
  }

  /**
   * Converts a base64 encoded mz or intensity string used in mzML files to an array of doubles. If
   * the original precision was 32 bit, you still get doubles as output.
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
   * @return a double array containing the decoded values
   * @throws io.github.msdk.MSDKException if any.
   */
  public static double[] decodeToDouble(InputStream is, int lengthIn, Integer precision,
      int numPoints, MzMLBinaryDataInfo.MzMLCompressionType compression)
      throws DataFormatException, IOException, MSDKException {

    // for some reason there sometimes might be zero length <peaks> tags
    // (ms2 usually)
    // in this case we just return an empty result
    if (lengthIn == 0) {
      return new double[0];
    }

    EnumSet<MzMLBinaryDataInfo.MzMLCompressionType> compressions =
        MzMLCompressionsHelper.getCompressions(compression);

    InflaterInputStream iis = null;
    LittleEndianDataInputStream dis = null;
    byte[] bytes = null;

    double[] data = new double[numPoints];

    // first check for zlib compression, inflation must be done before
    // NumPress
    if (compressions.contains(MzMLBinaryDataInfo.MzMLCompressionType.ZLIB)) {
      iis = new InflaterInputStream(is);
      dis = new LittleEndianDataInputStream(iis);
    } else if (compressions.contains(MzMLCompressionType.NO_COMPRESSION)) {
      dis = new LittleEndianDataInputStream(is);
    } else {
      bytes = IOUtils.toByteArray(is);
    }

    // now can check for NumPress
    if (compressions.contains(MzMLBinaryDataInfo.MzMLCompressionType.NUMPRESS_LINPRED)) {
      int numDecodedDoubles = MSNumpress.decodeLinear(bytes, bytes.length, data);
      if (numDecodedDoubles < 0) {
        throw new MSDKException("MSNumpress linear decoder failed");
      }
      return data;
    } else if (compressions.contains(MzMLBinaryDataInfo.MzMLCompressionType.NUMPRESS_POSINT)) {
      int numDecodedDoubles = MSNumpress.decodePic(bytes, bytes.length, data);
      if (numDecodedDoubles < 0) {
        throw new MSDKException("MSNumpress positive integer decoder failed");
      }
      return data;
    } else if (compressions.contains(MzMLBinaryDataInfo.MzMLCompressionType.NUMPRESS_SHLOGF)) {
      int numDecodedDoubles = MSNumpress.decodeSlof(bytes, bytes.length, data);
      if (numDecodedDoubles < 0) {
        throw new MSDKException("MSNumpress short logged float decoder failed");
      }
      return data;
    }

    if (precision == null) {
      throw new IllegalArgumentException(
          "Precision MUST be specified, if MS-NUMPRESS compression was not used");
    }

    switch (precision) {
      case (32): {
        int asInt;

        for (int i = 0; i < numPoints; i++) {
          asInt = dis.readInt();
          data[i] = Float.intBitsToFloat(asInt);
        }
        break;
      }
      case (64): {
        long asLong;

        for (int i = 0; i < numPoints; i++) {
          asLong = dis.readLong();
          data[i] = Double.longBitsToDouble(asLong);
        }
        break;
      }
      default: {
        throw new IllegalArgumentException(
            "Precision can only be 32/64 bits, other values are not valid.");
      }
    }

    return data;
  }

}
