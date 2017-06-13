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

class MzMLBinaryDataInfo {

  static enum MzMLCompressionType {
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

  static enum MzMLBitLength {
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

  static enum MzMLArrayType {
    MZ("MS:1000514"), INTENSITY("MS:1000515");

    private String accession;

    private MzMLArrayType(String accession) {
      this.accession = accession;
    }

    public String getValue() {
      return accession;
    }
  }

  private int position;
  private int encodedLength;
  private int arrayLength;
  private MzMLCompressionType compressionType;
  private MzMLBitLength bitLength;
  private MzMLArrayType arrayType;

  public MzMLBinaryDataInfo(int position, int encodedLength, int arrayLength,
      MzMLCompressionType compressionType, MzMLBitLength bitLength, MzMLArrayType arrayType) {
    this.position = position;
    this.compressionType = compressionType;
    this.bitLength = bitLength;
    this.arrayType = arrayType;
  }

  public MzMLBinaryDataInfo() {

  }

  public MzMLBitLength getBitLength() {
    return bitLength;
  }

  public void setBitLength(String bitLengthAccession) {
    for (MzMLBitLength bitLength : MzMLBitLength.values()) {
      if (bitLength.getValue().equals(bitLengthAccession))
        this.bitLength = bitLength;
    }
  }

  public boolean isBitLengthAccession(String bitLengthAccession) {
    for (MzMLBitLength bitLength : MzMLBitLength.values()) {
      if (bitLength.getValue().equals(bitLengthAccession))
        return true;
    }
    return false;
  }

  public MzMLCompressionType getCompressionType() {
    return compressionType;
  }

  public void setCompressionType(String compressionTypeAccession) {
    for (MzMLCompressionType compressionType : MzMLCompressionType.values()) {
      if (compressionType.getValue().equals(compressionTypeAccession))
        this.compressionType = compressionType;
    }
  }

  public boolean isCompressionTypeAccession(String compressionTypeAccession) {
    for (MzMLCompressionType compressionType : MzMLCompressionType.values()) {
      if (compressionType.getValue().equals(compressionTypeAccession))
        return true;
    }
    return false;
  }

  public MzMLArrayType getArrayType() {
    return arrayType;
  }

  public void setArrayType(String arrayTypeAccession) {
    for (MzMLArrayType arrayType : MzMLArrayType.values()) {
      if (arrayType.getValue().equals(arrayTypeAccession))
        this.arrayType = arrayType;
    }
  }

  public boolean isArrayTypeAccession(String arrayTypeAccession) {
    for (MzMLArrayType arrayType : MzMLArrayType.values()) {
      if (arrayType.getValue().equals(arrayTypeAccession))
        return true;
    }
    return false;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public int getEncodedLength() {
    return encodedLength;
  }

  public void setEncodedLength(int encodedLength) {
    this.encodedLength = encodedLength;
  }

  public int getArrayLength() {
    return arrayLength;
  }

  public void setArrayLength(int arrayLength) {
    this.arrayLength = arrayLength;
  }
}
