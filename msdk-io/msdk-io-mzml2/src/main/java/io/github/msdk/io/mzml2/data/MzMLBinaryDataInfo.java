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

package io.github.msdk.io.mzml2.data;

public class MzMLBinaryDataInfo {

  public static enum MzMLCompressionType {
    NUMPRESS_LINPRED("MS:1002312"), NUMPRESS_POSINT("MS:1002313"), ZLIB(
        "MS:1000574"), NUMPRESS_SHLOGF("MS:1002314"), NO_COMPRESSION("MS:1000576");
    private String accession;

    private MzMLCompressionType(String accession) {
      this.accession = accession;
    }

    public String getValue() {
      return accession;
    }
  }

  public static enum MzMLBitLength {
    THIRTY_TWO_BIT_INTEGER("MS:1000519"), SIXTEEN_BIT_FLOAT("MS:1000520"), THIRTY_TWO_BIT_FLOAT(
        "MS:1000521"), SIXTY_FOUR_BIT_INTEGER("MS:1000522"), SIXTY_FOUR_BIT_FLOAT("MS:1000523");

    private String accession;

    private MzMLBitLength(String accession) {
      this.accession = accession;
    }

    public String getValue() {
      return accession;
    }
  }

  public static enum MzMLArrayType {
    MZ("MS:1000514"), INTENSITY("MS:1000515"), TIME("MS:1000595");

    private String accession;

    private MzMLArrayType(String accession) {
      this.accession = accession;
    }

    public String getValue() {
      return accession;
    }
  }

  private final int encodedLength;
  private final int arrayLength;
  private long position;
  private MzMLCompressionType compressionType;
  private MzMLBitLength bitLength;
  private MzMLArrayType arrayType;

  /**
   * <p>
   * Constructor for MzMLBinaryDataInfo.
   * </p>
   *
   * @param encodedLength a int.
   * @param arrayLength a int.
   */
  public MzMLBinaryDataInfo(int encodedLength, int arrayLength) {
    this.encodedLength = encodedLength;
    this.arrayLength = arrayLength;
  }

  /**
   * <p>
   * Getter for the field <code>bitLength</code>.
   * </p>
   *
   * @return a {@link io.github.msdk.io.mzml2.data.MzMLBinaryDataInfo.MzMLBitLength} object.
   */
  public MzMLBitLength getBitLength() {
    return bitLength;
  }

  /**
   * <p>
   * Setter for the field <code>bitLength</code>.
   * </p>
   *
   * @param bitLengthAccession a {@link java.lang.String} object.
   */
  public void setBitLength(String bitLengthAccession) {
    for (MzMLBitLength bitLength : MzMLBitLength.values()) {
      if (bitLength.getValue().equals(bitLengthAccession))
        this.bitLength = bitLength;
    }
  }

  /**
   * <p>
   * isBitLengthAccession.
   * </p>
   *
   * @param bitLengthAccession a {@link java.lang.String} object.
   * @return a boolean.
   */
  public boolean isBitLengthAccession(String bitLengthAccession) {
    for (MzMLBitLength bitLength : MzMLBitLength.values()) {
      if (bitLength.getValue().equals(bitLengthAccession))
        return true;
    }
    return false;
  }

  /**
   * <p>
   * Getter for the field <code>compressionType</code>.
   * </p>
   *
   * @return a {@link io.github.msdk.io.mzml2.data.MzMLBinaryDataInfo.MzMLCompressionType} object.
   */
  public MzMLCompressionType getCompressionType() {
    return compressionType;
  }

  /**
   * <p>
   * Setter for the field <code>compressionType</code>.
   * </p>
   *
   * @param compressionTypeAccession a {@link java.lang.String} object.
   */
  public void setCompressionType(String compressionTypeAccession) {
    for (MzMLCompressionType compressionType : MzMLCompressionType.values()) {
      if (compressionType.getValue().equals(compressionTypeAccession))
        this.compressionType = compressionType;
    }
  }

  /**
   * <p>
   * isCompressionTypeAccession.
   * </p>
   *
   * @param compressionTypeAccession a {@link java.lang.String} object.
   * @return a boolean.
   */
  public boolean isCompressionTypeAccession(String compressionTypeAccession) {
    for (MzMLCompressionType compressionType : MzMLCompressionType.values()) {
      if (compressionType.getValue().equals(compressionTypeAccession))
        return true;
    }
    return false;
  }

  /**
   * <p>
   * Getter for the field <code>arrayType</code>.
   * </p>
   *
   * @return a {@link io.github.msdk.io.mzml2.data.MzMLBinaryDataInfo.MzMLArrayType} object.
   */
  public MzMLArrayType getArrayType() {
    return arrayType;
  }

  /**
   * <p>
   * Setter for the field <code>arrayType</code>.
   * </p>
   *
   * @param arrayTypeAccession a {@link java.lang.String} object.
   */
  public void setArrayType(String arrayTypeAccession) {
    for (MzMLArrayType arrayType : MzMLArrayType.values()) {
      if (arrayType.getValue().equals(arrayTypeAccession))
        this.arrayType = arrayType;
    }
  }

  /**
   * <p>
   * isArrayTypeAccession.
   * </p>
   *
   * @param arrayTypeAccession a {@link java.lang.String} object.
   * @return a boolean.
   */
  public boolean isArrayTypeAccession(String arrayTypeAccession) {
    for (MzMLArrayType arrayType : MzMLArrayType.values()) {
      if (arrayType.getValue().equals(arrayTypeAccession))
        return true;
    }
    return false;
  }

  /**
   * <p>
   * Getter for the field <code>position</code>.
   * </p>
   *
   * @return a long.
   */
  public long getPosition() {
    return position;
  }

  /**
   * <p>
   * Setter for the field <code>position</code>.
   * </p>
   *
   * @param position a long.
   */
  public void setPosition(long position) {
    this.position = position;
  }

  /**
   * <p>
   * Getter for the field <code>encodedLength</code>.
   * </p>
   *
   * @return a int.
   */
  public int getEncodedLength() {
    return encodedLength;
  }

  /**
   * <p>
   * Getter for the field <code>arrayLength</code>.
   * </p>
   *
   * @return a int.
   */
  public int getArrayLength() {
    return arrayLength;
  }
}
