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

import java.util.ArrayList;

public class MzMLScanList extends MzMLCVGroup {
  private ArrayList<MzMLScan> scans;

  public MzMLScanList() {
    this.scans = new ArrayList<>();
  }

  public ArrayList<MzMLScan> getScans() {
    return scans;
  }

  public void addScan(MzMLScan scan) {
    scans.add(scan);
  }

}
