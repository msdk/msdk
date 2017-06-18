package io.github.msdk.io.mzml2;

import java.util.ArrayList;

class MzMLReferenceableParamGroup {

  private String paramGroupName;
  private ArrayList<MzMLCVParam> cvParams;

  public MzMLReferenceableParamGroup(String paramGroupName) {
    this.paramGroupName = paramGroupName;
    this.cvParams = new ArrayList<>();
  }

  public String getParamGroupName() {
    return paramGroupName;
  }

  public void setParamGroupName(String paramGroupName) {
    this.paramGroupName = paramGroupName;
  }

  public ArrayList<MzMLCVParam> getReferenceableCvParams() {
    return cvParams;
  }

  public void addReferenceableCvParam(MzMLCVParam cvParam) {
    cvParams.add(cvParam);
  }

}
