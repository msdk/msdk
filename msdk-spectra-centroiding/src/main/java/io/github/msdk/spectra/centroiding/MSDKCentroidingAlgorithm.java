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

package io.github.msdk.spectra.centroiding;

import javax.annotation.Nonnull;

import io.github.msdk.datamodel.MsScan;

/**
 * <p>
 * MSDKCentroidingAlgorithm interface.
 * </p>
 */
public interface MSDKCentroidingAlgorithm {

  /**
   * <p>
   * centroidScan.
   * </p>
   *
   * @param input a {@link io.github.msdk.datamodel.MsScan} object.
   * @return a {@link io.github.msdk.datamodel.MsScan} object.
   */
  @Nonnull
  MsScan centroidScan(@Nonnull MsScan input);

}
