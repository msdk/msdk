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
 * Base interface of all providers used to present information or query for user input.
 */
public interface PeakInvestigatorProvider {

  public enum Status {
    ACCEPT, CANCEL
  }

  /**
   * Show the provider to the user.
   *
   * @return Status indicating whether a particular query has been accepted or canceled.
   */
  Status show();
}
