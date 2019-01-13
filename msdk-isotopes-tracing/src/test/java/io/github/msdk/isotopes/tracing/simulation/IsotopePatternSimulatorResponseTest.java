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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.isotopes.tracing.data.Fragment;
import io.github.msdk.isotopes.tracing.data.FragmentList;
import io.github.msdk.isotopes.tracing.data.IncorporationRate;
import io.github.msdk.isotopes.tracing.data.IsotopePattern;
import io.github.msdk.isotopes.tracing.data.constants.IntensityType;
import io.github.msdk.isotopes.tracing.data.exception.IntensityTypeMismatchException;
import junit.framework.TestCase;

public class IsotopePatternSimulatorResponseTest extends TestCase {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(IsotopePatternSimulatorResponseTest.class);

  public void testGetIsotopePattern() throws IntensityTypeMismatchException {
    IsotopePatternSimulatorRequest request = new IsotopePatternSimulatorRequest();
    request.setAnalyzeMassShifts(true);
    request.setCharge(1);
    request.setFragments(new FragmentList(new Fragment("C10H26NO2Si3", "CN")));
    request.setMinimalFrequency(0.001);
    request.setRoundedFrequenciesPrecision(4);
    request.setRoundedMassPrecision(4);
    request.setTargetIntensityType(IntensityType.MID);
    request.setIncorporationRate(new IncorporationRate(0.3));
    IsotopePatternSimulatorResponse response = IsotopePatternSimulator.simulate(request);
    IsotopePattern pattern = response.getIsotopePattern(0);
    LOGGER.info(pattern.toString());
  }

  public void testGetIsotopePatternWithReducedFormulas() throws IntensityTypeMismatchException {
    IsotopePatternSimulatorRequest request = new IsotopePatternSimulatorRequest();
    request.setAnalyzeMassShifts(true);
    request.setCharge(1);
    request.setFragments(new FragmentList(new Fragment("C10H26NO2Si3", "CN")));
    request.setMinimalFrequency(0.001);
    request.setRoundedFrequenciesPrecision(4);
    request.setRoundedMassPrecision(4);
    request.setTargetIntensityType(IntensityType.MID);
    request.setIncorporationRate(new IncorporationRate(0.3));
    IsotopePatternSimulatorResponse response = IsotopePatternSimulator.simulate(request);
    IsotopePattern pattern = response.getIsotopePatternWithReducedFormulas(0);
    LOGGER.info(pattern.toString());
  }

}
