package io.github.msdk.io.mzml2.data;


public enum MzMLCompressionType {
  NUMPRESS_LINPRED("MS:1002312"), // MS-Numpress linear prediction compression
  NUMPRESS_POSINT("MS:1002313"), // MS-Numpress positive integer compression
  NUMPRESS_SHLOGF("MS:1002314"), // MS-Numpress short logged float compression
  ZLIB("MS:1000574"), // zlib compression
  NO_COMPRESSION("MS:1000576"), // no compression
  NUMPRESS_LINPRED_ZLIB("MS:1002746"), // MS-Numpress linear prediction compression followed by
                                       // zlib compression
  NUMPRESS_POSINT_ZLIB("MS:1002747"), // MS-Numpress positive integer compression followed by zlib
                                      // compression
  NUMPRESS_SHLOGF_ZLIB("MS:1002748"); // MS-Numpress short logged float compression followed by
                                      // zlib compression

  private String accession;

  private MzMLCompressionType(String accession) {
    this.accession = accession;
  }

  public String getValue() {
    return accession;
  }

}


