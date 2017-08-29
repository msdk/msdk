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

import java.util.ArrayList;

/**
 * <p>
 * A group (or list) of {@link io.github.msdk.io.mzml.data.MzMLCVParam CV Parameter}s
 * </p>
 */
public class MzMLCVGroup {
  private ArrayList<MzMLCVParam> cvParams;

  /**
   * <p>Constructor for MzMLCVGroup.</p>
   */
  public MzMLCVGroup() {
    this.cvParams = new ArrayList<>();
  }

  /**
   * <p>getCVParamsList.</p>
   *
   * @return an {@link java.util.ArrayList ArrayList<MzMLCVParam>} of
   *         {@link io.github.msdk.io.mzml.data.MzMLCVParam CV Parameter}s
   */
  public ArrayList<MzMLCVParam> getCVParamsList() {
    return cvParams;
  }

  /**
   * <p>
   * Adds a {@link io.github.msdk.io.mzml.data.MzMLCVParam CV Parameter} to the
   * {@link io.github.msdk.io.mzml.data.MzMLCVGroup MzMLCVGroup}
   * </p>
   *
   * @param cvParam the {@link io.github.msdk.io.mzml.data.MzMLCVParam CV Parameter} to be added to
   *        the {@link io.github.msdk.io.mzml.data.MzMLCVGroup MzMLCVGroup}
   */
  public void addCVParam(MzMLCVParam cvParam) {
    cvParams.add(cvParam);
  }
}
