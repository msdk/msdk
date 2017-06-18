/*
 * (C) Copyright 2015-2017 by MSDK Development Team
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
