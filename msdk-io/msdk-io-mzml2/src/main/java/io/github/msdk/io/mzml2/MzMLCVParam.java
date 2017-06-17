package io.github.msdk.io.mzml2;

class MzMLCVParam {

  private String accession;
  private String value;
  private String unitAccession;

  public MzMLCVParam(String accession, String value, String unitAccession) {
    this.setAccession(accession);
    this.setValue(value);
    this.setUnitAccession(unitAccession);
  }

  public String getAccession() {
    return accession;
  }

  public void setAccession(String accession) {
    this.accession = accession;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getUnitAccession() {
    return unitAccession;
  }

  public void setUnitAccession(String unitAccession) {
    this.unitAccession = unitAccession;
  }
}
