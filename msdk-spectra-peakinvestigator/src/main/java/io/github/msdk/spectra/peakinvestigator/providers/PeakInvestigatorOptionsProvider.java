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

package io.github.msdk.spectra.peakinvestigator.providers;

/**
 * An interface to provide the following options to PeakInvestigator:
 * <ul>
 * <li>The desired version (e.g. 1.3)</li>
 * <li>The desired start mass</li>
 * <li>The desired end mass</li>
 * </ul>
 */
public interface PeakInvestigatorOptionsProvider extends PeakInvestigatorProvider {
  /**
   * <p>
   * getVersion.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  String getVersion();

  /**
   * <p>
   * getStartMass.
   * </p>
   *
   * @return a int.
   */
  int getStartMass();

  /**
   * <p>
   * getEndMass.
   * </p>
   *
   * @return a int.
   */
  int getEndMass();
}
