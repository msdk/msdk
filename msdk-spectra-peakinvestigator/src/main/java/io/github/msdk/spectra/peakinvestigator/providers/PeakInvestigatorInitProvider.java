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
 * An interface to (optionally) show estimated costs and ask the user for the desired response time
 * objective (RTO) of a PeakInvestigator job.
 */
public interface PeakInvestigatorInitProvider extends PeakInvestigatorProvider {
  /**
   * <p>
   * getRto.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  String getRto();
}
