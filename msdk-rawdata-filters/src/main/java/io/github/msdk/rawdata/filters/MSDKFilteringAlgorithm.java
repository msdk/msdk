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

package io.github.msdk.rawdata.filters;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.datamodel.MsScan;

/**
 * <p>
 * MSDKFilteringAlgorithm interface.
 * </p>
 */
public interface MSDKFilteringAlgorithm {

  /**
   * <p>
   * performFilter.
   * </p>
   *
   * @param input a {@link io.github.msdk.datamodel.MsScan} object.
   * @return a {@link io.github.msdk.datamodel.MsScan} object.
   */
  @Nullable
  MsScan performFilter(@Nonnull MsScan input);

}
