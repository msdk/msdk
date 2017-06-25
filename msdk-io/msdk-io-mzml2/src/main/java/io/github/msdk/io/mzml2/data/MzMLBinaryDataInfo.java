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
    this.bitLength = getBitLength(bitLengthAccession);
  }

  /**
   * <p>
   * Setter for the field <code>bitLength</code>.
   * </p>
   *
   * @param bitLength a {@link io.github.msdk.io.mzml2.data.MzMLBitLength} object.
   */
  public void setBitLength(MzMLBitLength bitLength) {
    this.bitLength = bitLength;
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
    if (getBitLength(bitLengthAccession) != null)
      return true;
    return false;
  }

  public MzMLBitLength getBitLength(String accession) {
    for (MzMLBitLength bitLength : MzMLBitLength.values()) {
      if (bitLength.getValue().equals(accession))
        return bitLength;
    }
    return null;
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
    this.compressionType = getCompressionType(compressionTypeAccession);
  }

  /**
   * <p>
   * Setter for the field <code>bitLength</code>.
   * </p>
   *
   * @param bitLength a {@link io.github.msdk.io.mzml2.data.MzMLCompressionType} object.
   */
  public void setCompressionType(MzMLCompressionType compressionType) {
    this.compressionType = compressionType;
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
    if (getCompressionType(compressionTypeAccession) != null)
      return true;
    return false;
  }

  public MzMLCompressionType getCompressionType(String accession) {
    for (MzMLCompressionType compressionType : MzMLCompressionType.values()) {
      if (compressionType.getValue().equals(accession))
        return compressionType;
    }
    return null;
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
    this.arrayType = getArrayType(arrayTypeAccession);
  }

  /**
   * <p>
   * Setter for the field <code>bitLength</code>.
   * </p>
   *
   * @param bitLength a {@link io.github.msdk.io.mzml2.data.MzMLArrayType} object.
   */
  public void setArrayType(MzMLArrayType arrayType) {
    this.arrayType = arrayType;
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
    if (getArrayType(arrayTypeAccession) != null)
      return true;
    return false;
  }

  public MzMLArrayType getArrayType(String accession) {
    for (MzMLArrayType arrayType : MzMLArrayType.values()) {
      if (arrayType.getValue().equals(accession))
        return arrayType;
    }
    return null;
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
