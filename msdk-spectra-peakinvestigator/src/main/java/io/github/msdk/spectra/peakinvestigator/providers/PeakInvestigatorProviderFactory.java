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

import com.veritomyx.actions.InitAction;
import com.veritomyx.actions.PiVersionsAction;

/**
 * <p>
 * PeakInvestigatorProviderFactory interface.
 * </p>
 */
public interface PeakInvestigatorProviderFactory {
  /**
   * <p>
   * createProjectProvider.
   * </p>
   *
   * @return a
   *         {@link io.github.msdk.spectra.peakinvestigator.providers.PeakInvestigatorProjectProvider}
   *         object.
   */
  PeakInvestigatorProjectProvider createProjectProvider();

  /**
   * <p>
   * createOptionsProvider.
   * </p>
   *
   * @param action a {@link com.veritomyx.actions.PiVersionsAction} object.
   * @param dataStart a int.
   * @param dataEnd a int.
   * @return a
   *         {@link io.github.msdk.spectra.peakinvestigator.providers.PeakInvestigatorOptionsProvider}
   *         object.
   */
  PeakInvestigatorOptionsProvider createOptionsProvider(PiVersionsAction action, int dataStart,
      int dataEnd);

  /**
   * <p>
   * createInitProvider.
   * </p>
   *
   * @param action a {@link com.veritomyx.actions.InitAction} object.
   * @return a
   *         {@link io.github.msdk.spectra.peakinvestigator.providers.PeakInvestigatorInitProvider}
   *         object.
   */
  PeakInvestigatorInitProvider createInitProvider(InitAction action);
}
