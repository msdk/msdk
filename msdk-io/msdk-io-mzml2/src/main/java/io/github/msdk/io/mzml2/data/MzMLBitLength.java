package io.github.msdk.io.mzml2.data;

public enum MzMLBitLength {
  THIRTY_TWO_BIT_INTEGER("MS:1000519"), // 32-bit integer
  SIXTEEN_BIT_FLOAT("MS:1000520"), // 16-bit float
  THIRTY_TWO_BIT_FLOAT("MS:1000521"), // 32-bit float
  SIXTY_FOUR_BIT_INTEGER("MS:1000522"), // 64-bit integer
  SIXTY_FOUR_BIT_FLOAT("MS:1000523"); // 64-bit float

  private String accession;

  private MzMLBitLength(String accession) {
    this.accession = accession;
  }

  public String getValue() {
    return accession;
  }

}
