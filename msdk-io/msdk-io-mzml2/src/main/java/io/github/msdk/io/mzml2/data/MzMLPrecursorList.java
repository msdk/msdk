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


public class MzMLPrecursorList {

  private ArrayList<MzMLPrecursorElement> precursorElements;

  public MzMLPrecursorList() {
    this.precursorElements = new ArrayList<>();
  }

  public ArrayList<MzMLPrecursorElement> getPrecursorElements() {
    return precursorElements;
  }

  public void addPrecursor(MzMLPrecursorElement precursor) {
    precursorElements.add(precursor);
  }
}
