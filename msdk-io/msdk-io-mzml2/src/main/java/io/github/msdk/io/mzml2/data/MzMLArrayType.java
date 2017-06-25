package io.github.msdk.io.mzml2.data;

public enum MzMLArrayType {
  MZ("MS:1000514"), INTENSITY("MS:1000515"), TIME("MS:1000595");

  private String accession;

  private MzMLArrayType(String accession) {
    this.accession = accession;
  }

  public String getValue() {
    return accession;
  }
}
