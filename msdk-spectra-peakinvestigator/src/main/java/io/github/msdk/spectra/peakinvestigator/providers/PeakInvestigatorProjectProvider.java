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
 * An interface to provide the username, password, and project ID to submit PeakInvestigator jobs.
 */
public interface PeakInvestigatorProjectProvider extends PeakInvestigatorProvider {
  /**
   * <p>
   * getUsername.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  String getUsername();

  /**
   * <p>
   * getPassword.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  String getPassword();

  /**
   * <p>
   * getProjectId.
   * </p>
   *
   * @return a int.
   */
  int getProjectId();
}
