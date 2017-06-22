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

package io.github.msdk.io.mzml2;

import java.io.IOException;
import java.util.Base64;
import java.util.EnumSet;
import java.util.zip.DataFormatException;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.io.mzml2.data.MzMLBinaryDataInfo;
import io.github.msdk.io.mzml2.util.MzMLMZPeaksDecoder;

/**
 * 
 * Some test data borrowed from the jmzml project. Original author: Florian Reisinger
 */
public class BinaryDataArrayTest {
  ///// ///// ///// ///// ///// ///// ///// ///// ///// /////
  // test values (like the ones in the example XML file) in
  // different numeric types

  private final double[] testData64bitFloat =
      {0.000, 0.001, 0.002, 0.003, 0.004, 0.005, 0.006, 0.007, 0.008, 0.009, 0.010, 0.011, 0.012,
          0.013, 0.014, 0.015, 0.016, 0.017, 0.018, 0.019, 0.020, 0.021, 0.022, 0.023, 0.024, 0.025,
          0.026, 0.027, 0.028, 0.029, 0.030, 0.031, 0.032, 0.033, 0.034, 0.035, 0.036, 0.037, 0.038,
          0.039, 0.040, 0.041, 0.042, 0.043, 0.044, 0.045, 0.046, 0.047, 0.048, 0.049, 0.050, 0.051,
          0.052, 0.053, 0.054, 0.055, 0.056, 0.057, 0.058, 0.059, 0.060, 0.061, 0.062, 0.063, 0.064,
          0.065, 0.066, 0.067, 0.068, 0.069, 0.070, 0.071, 0.072, 0.073, 0.074, 0.075, 0.076, 0.077,
          0.078, 0.079, 0.080, 0.081, 0.082, 0.083, 0.084, 0.085, 0.086, 0.087, 0.088, 0.089, 0.090,
          0.091, 0.092, 0.093, 0.094, 0.095, 0.096, 0.097, 0.098};

  private final float[] testData32bitFloat =
      {00.0F, 01.0F, 02.0F, 03.0F, 04.0F, 05.0F, 06.0F, 07.0F, 08.0F, 09.0F, 10.0F, 11.0F, 12.0F,
          13.0F, 14.0F, 15.0F, 16.0F, 17.0F, 18.0F, 19.0F, 20.0F, 21.0F, 22.0F, 23.0F, 24.0F, 25.0F,
          26.0F, 27.0F, 28.0F, 29.0F, 30.0F, 31.0F, 32.0F, 33.0F, 34.0F, 35.0F, 36.0F, 37.0F, 38.0F,
          39.0F, 40.0F, 41.0F, 42.0F, 43.0F, 44.0F, 45.0F, 46.0F, 47.0F, 48.0F, 49.0F, 50.0F, 51.0F,
          52.0F, 53.0F, 54.0F, 55.0F, 56.0F, 57.0F, 58.0F, 59.0F, 60.0F, 61.0F, 62.0F, 63.0F, 64.0F,
          65.0F, 66.0F, 67.0F, 68.0F, 69.0F, 70.0F, 71.0F, 72.0F, 73.0F, 74.0F, 75.0F, 76.0F, 77.0F,
          78.0F, 79.0F, 80.0F, 81.0F, 82.0F, 83.0F, 84.0F, 85.0F, 86.0F, 87.0F, 88.0F, 89.0F, 90.0F,
          91.0F, 92.0F, 93.0F, 94.0F, 95.0F, 96.0F, 97.0F, 98.0F};

  private final long[] testData64bitInt = {0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L,
      13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L, 31L,
      32L, 33L, 34L, 35L, 36L, 37L, 38L, 39L, 40L, 41L, 42L, 43L, 44L, 45L, 46L, 47L, 48L, 49L, 50L,
      51L, 52L, 53L, 54L, 55L, 56L, 57L, 58L, 59L, 60L, 61L, 62L, 63L, 64L, 65L, 66L, 67L, 68L, 69L,
      70L, 71L, 72L, 73L, 74L, 75L, 76L, 77L, 78L, 79L, 80L, 81L, 82L, 83L, 84L, 85L, 86L, 87L, 88L,
      89L, 90L, 91L, 92L, 93L, 94L, 95L, 96L, 97L, 98L};

  private final int[] testData32bitInt = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
      17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39,
      40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62,
      63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85,
      86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98};


  ///// ///// ///// ///// ///// ///// ///// ///// ///// /////
  // some base64 encoded binary data strings to compare against
  // the binary data produced by the BinaryDataArray object

  // binary test data: compressed, base64 encoded, 64 bit precision
  // extracted from file: MzMLFile_7_compressed.mzML (line 74) "m/z array"
  private final String compressed64bit = "eJwtkWlIVFEAhWUQERGREHFDokQUEQmREIlDoE"
      + "gbaUVI9GOQfgwiIiYxmkRZuVRWluWSmrPoLI7jzJvtJSGhIlYQYYJESERIDCYiJ"
      + "ZEleN995/4Z3ptzz/K9mBj9/HdtL5+prYL+a8TujbrXzU9NfG7E9RRv5bfWZr43"
      + "oyWaeyTe08b/b6Ls69me/oZ26u5g5kSioaCzg/oulAaXrs2+vMd7D6Bm392oVh/"
      + "y/mMMLxz0NZl66fMEX/7Ki/TrQ2bRWk7crWf0fY5LV6SS/gMYGpSHOUP4/OHP+e"
      + "/jL5g3jPTYi0mFyghzR1GjC5k/htNHu48b18fYw4Kf1ZrSwj5W3K//8c+1bWUvG"
      + "wo6U0WEjf3seG+pCP/as7PnOLRWabET7DuBhFVb47EEB3s74NZik53s78RJTZbq"
      + "4g4XZO0sN/e4obX7eGiSuyaRf/nR3E6eh/s8eCtUGUVT3DkFU+9mG0q83OuFMBP"
      + "R09w9jQsCjpjK/T7YRbtVm48cfPi9t2KO5vrJw49yCcBPLn6IcVoQ+SjQ7RRyUl"
      + "AsT4C8Arj95p0gFCC3AD6J9aIL+QVxWAqD5BjEVTkgRJ4hzO/kicUhcg3jgG5Iv"
      + "mFIt6UwOUegjGw5Fk9FyDsCgwyOkLuKczoY8ldh1WJXVH6HV9gH57SBqA==";

  // binary test data: uncompressed, base64 encoded, 64 bit precision
  // extracted from file: MzMLFile_7_uncompressed.mzML (line 74) "m/z array"
  private final String uncompressed64bit = "AAAAAAAAAAD8qfHSTWJQP/yp8dJNYmA/+n5qvH"
      + "STaD/8qfHSTWJwP3sUrkfhenQ/+n5qvHSTeD956SYxCKx8P/yp8dJNYoA/O99Pj"
      + "Zdugj97FK5H4XqEP7pJDAIrh4Y/+n5qvHSTiD85tMh2vp+KP3npJjEIrIw/uB6F"
      + "61G4jj/8qfHSTWKQP5zEILByaJE/O99PjZdukj/b+X5qvHSTP3sUrkfhepQ/Gy/"
      + "dJAaBlT+6SQwCK4eWP1pkO99PjZc/+n5qvHSTmD+amZmZmZmZPzm0yHa+n5o/2c"
      + "73U+Olmz956SYxCKycPxkEVg4tsp0/uB6F61G4nj9YObTIdr6fP/yp8dJNYqA/T"
      + "DeJQWDloD+cxCCwcmihP+xRuB6F66E/O99PjZduoj+LbOf7qfGiP9v5fmq8dKM/"
      + "K4cW2c73oz97FK5H4XqkP8uhRbbz/aQ/Gy/dJAaBpT9qvHSTGASmP7pJDAIrh6Y"
      + "/CtejcD0Kpz9aZDvfT42nP6rx0k1iEKg/+n5qvHSTqD9KDAIrhxapP5qZmZmZma"
      + "k/6SYxCKwcqj85tMh2vp+qP4lBYOXQIqs/2c73U+Olqz8pXI/C9SisP3npJjEIr"
      + "Kw/yXa+nxovrT8ZBFYOLbKtP2iR7Xw/Na4/uB6F61G4rj8IrBxaZDuvP1g5tMh2"
      + "vq8/VOOlm8QgsD/8qfHSTWKwP6RwPQrXo7A/TDeJQWDlsD/0/dR46SaxP5zEILB"
      + "yaLE/RIts5/upsT/sUbgeheuxP5MYBFYOLbI/O99PjZdusj/jpZvEILCyP4ts5/"
      + "up8bI/MzMzMzMzsz/b+X5qvHSzP4PAyqFFtrM/K4cW2c73sz/TTWIQWDm0P3sUr"
      + "kfherQ/I9v5fmq8tD/LoUW28/20P3Noke18P7U/Gy/dJAaBtT/D9Shcj8K1P2q8"
      + "dJMYBLY/EoPAyqFFtj+6SQwCK4e2P2IQWDm0yLY/CtejcD0Ktz+yne+nxku3P1p"
      + "kO99Pjbc/AiuHFtnOtz+q8dJNYhC4P1K4HoXrUbg/+n5qvHSTuD+iRbbz/dS4P0" + "oMAiuHFrk/";

  // binary test data: compressed, base64 encoded, 32 bit precision
  // extracted from file: MzMLFile_7_compressed.mzML (line 80) "intensity array"
  private final String compressed32bit = "eJwVxCFIQ2EAhdE/GAyGhQXDwoLBYFgwGAa+jQ"
      + "WDYcFgMCwYDIYFw4LhITLGGGOIyBAZDxkyhsgQkSFDHrJgNC4uGo1Gj5fv3BD+F"
      + "++6SMQkpCwJpRAy5CkQUaVGnZgWPfokjJgwJeWTLxYs+eaHX0I5hBVWWSNDlnVy"
      + "5Nlgky0KbLNDkYgKe+xT5YBDjqhxzAmn1DmjwTkxF1zSpEWbDl16XHHNDX1uuWN"
      + "Awj1DHhgx5pEnJjzzwitT3pjxTsoH8/IfQP5IBA==";

  // binary test data: uncompressed, base64 encoded, 32 bit precision
  // extracted from file: MzMLFile_7_uncompressed.mzML (line 80) "intensity array"
  private final String uncompressed32bit = "AAAAAAAAgD8AAABAAABAQAAAgEAAAKBAAADAQA"
      + "AA4EAAAABBAAAQQQAAIEEAADBBAABAQQAAUEEAAGBBAABwQQAAgEEAAIhBAACQQ"
      + "QAAmEEAAKBBAACoQQAAsEEAALhBAADAQQAAyEEAANBBAADYQQAA4EEAAOhBAADw"
      + "QQAA+EEAAABCAAAEQgAACEIAAAxCAAAQQgAAFEIAABhCAAAcQgAAIEIAACRCAAA"
      + "oQgAALEIAADBCAAA0QgAAOEIAADxCAABAQgAAREIAAEhCAABMQgAAUEIAAFRCAA"
      + "BYQgAAXEIAAGBCAABkQgAAaEIAAGxCAABwQgAAdEIAAHhCAAB8QgAAgEIAAIJCA"
      + "ACEQgAAhkIAAIhCAACKQgAAjEIAAI5CAACQQgAAkkIAAJRCAACWQgAAmEIAAJpC"
      + "AACcQgAAnkIAAKBCAACiQgAApEIAAKZCAACoQgAAqkIAAKxCAACuQgAAsEIAALJ"
      + "CAAC0QgAAtkIAALhCAAC6QgAAvEIAAL5CAADAQgAAwkIAAMRC";


  @Test
  public void testCompressed64bit() throws MSDKException, DataFormatException, IOException {
    // Get an empty EnumSet to store the compression type
    EnumSet<MzMLBinaryDataInfo.MzMLCompressionType> compressions =
        EnumSet.noneOf(MzMLBinaryDataInfo.MzMLCompressionType.class);
    compressions.add(MzMLBinaryDataInfo.MzMLCompressionType.ZLIB);

    // Base 64 decode the given String and store the result in a byte array
    byte[] decodedArray = Base64.getDecoder().decode(compressed64bit);
    Assert.assertNotNull(decodedArray);

    // Decode the decoded byte array and compare with the expected values
    double[] result = MzMLMZPeaksDecoder
        .decode(decodedArray, decodedArray.length, 64, 99, compressions).getDecodedArray();
    Assert.assertArrayEquals(testData64bitFloat, result, 0.0);

  }

  @Test
  public void testUncompressed64bit() throws MSDKException, DataFormatException, IOException {
    // Get an empty EnumSet to store the compression type
    EnumSet<MzMLBinaryDataInfo.MzMLCompressionType> compressions =
        EnumSet.noneOf(MzMLBinaryDataInfo.MzMLCompressionType.class);
    compressions.add(MzMLBinaryDataInfo.MzMLCompressionType.NO_COMPRESSION);

    // Base 64 decode the given String and store the result in a byte array
    byte[] decodedArray = Base64.getDecoder().decode(uncompressed64bit);
    Assert.assertNotNull(decodedArray);

    // Decode the decoded byte array and compare with the expected values
    double[] result = MzMLMZPeaksDecoder
        .decode(decodedArray, decodedArray.length, 64, 99, compressions).getDecodedArray();
    Assert.assertArrayEquals(testData64bitFloat, result, 0.0);
  }

  @Test
  public void testCompressed32bit() throws MSDKException, DataFormatException, IOException {
    // Get an empty EnumSet to store the compression type
    EnumSet<MzMLBinaryDataInfo.MzMLCompressionType> compressions =
        EnumSet.noneOf(MzMLBinaryDataInfo.MzMLCompressionType.class);
    compressions.add(MzMLBinaryDataInfo.MzMLCompressionType.ZLIB);

    // Base 64 decode the given String and store the result in a byte array
    byte[] decodedArray = Base64.getDecoder().decode(compressed32bit);
    Assert.assertNotNull(decodedArray);

    // Decode the decoded byte array and compare with the expected values
    double[] result = MzMLMZPeaksDecoder
        .decode(decodedArray, decodedArray.length, 32, 99, compressions).getDecodedArray();
    int[] resultToInt = new int[99];
    for (int i = 0; i < resultToInt.length; i++)
      resultToInt[i] = (int) result[i];
    Assert.assertArrayEquals(testData32bitInt, resultToInt);
  }

  @Test
  public void testUncompressed32bit() throws MSDKException, DataFormatException, IOException {
    // Get an empty EnumSet to store the compression type
    EnumSet<MzMLBinaryDataInfo.MzMLCompressionType> compressions =
        EnumSet.noneOf(MzMLBinaryDataInfo.MzMLCompressionType.class);
    compressions.add(MzMLBinaryDataInfo.MzMLCompressionType.NO_COMPRESSION);

    // Base 64 decode the given String and store the result in a byte array
    byte[] decodedArray = Base64.getDecoder().decode(uncompressed32bit);
    Assert.assertNotNull(decodedArray);

    // Decode the decoded byte array and compare with the expected values
    double[] result = MzMLMZPeaksDecoder
        .decode(decodedArray, decodedArray.length, 32, 99, compressions).getDecodedArray();
    int[] resultToInt = new int[99];
    for (int i = 0; i < resultToInt.length; i++)
      resultToInt[i] = (int) result[i];
    Assert.assertArrayEquals(testData32bitInt, resultToInt);
  }

}
