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

package io.github.msdk.isotopes.tracing.simulation;

import io.github.msdk.isotopes.tracing.data.MSDatabase;
import io.github.msdk.isotopes.tracing.data.MSDatabaseList;

/**
 * Includes a list of {@link MSDatabase}s corresponding to the requested fragments and options from
 * a {@link IsotopePatternSimulatorRequest}.
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class IsotopePatternSimulatorResponse {
  // TODO: optimize the IsotopePatternSimulatorResponse and the IsotopePatternSimulator simulation
  // methods to include a (labeled) MassSpectrum as response, where the labels include the
  // information on the isotopes that induced the peaks.
  private MSDatabaseList msDatabaseList;

  /**
   * @return the msDatabaseList
   */
  public MSDatabaseList getMsDatabaseList() {
    return msDatabaseList;
  }

  /**
   * @param msDatabaseList the msDatabaseList to set
   */
  public void setMsDatabaseList(MSDatabaseList msDatabaseList) {
    this.msDatabaseList = msDatabaseList;
  }

}
