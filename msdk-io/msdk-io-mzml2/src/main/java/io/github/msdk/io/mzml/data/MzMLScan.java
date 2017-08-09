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

package io.github.msdk.io.mzml.data;

import java.util.Optional;

public class MzMLScan extends MzMLCVGroup {

  Optional<MzMLScanWindowList> scanWindowList;

  public MzMLScan() {
    this.scanWindowList = Optional.ofNullable(null);
  }

  public Optional<MzMLScanWindowList> getScanWindowList() {
    return scanWindowList;
  }

  public void setScanWindowList(MzMLScanWindowList scanWindowList) {
    this.scanWindowList = Optional.ofNullable(scanWindowList);
  }

}
