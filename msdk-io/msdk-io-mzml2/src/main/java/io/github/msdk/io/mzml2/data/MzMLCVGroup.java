package io.github.msdk.io.mzml2.data;

import java.util.ArrayList;

public class MzMLCVGroup {
  private ArrayList<MzMLCVParam> cvParams;

  public MzMLCVGroup() {
    this.cvParams = new ArrayList<>();
  }

  public ArrayList<MzMLCVParam> getReferenceableCvParams() {
    return cvParams;
  }

  public void addCVParam(MzMLCVParam cvParam) {
    cvParams.add(cvParam);
  }
}
