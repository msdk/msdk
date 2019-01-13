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

package io.github.msdk.isotopes.tracing.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.isotopes.tracing.data.constants.Element;
import io.github.msdk.isotopes.tracing.data.constants.IntensityType;
import io.github.msdk.isotopes.tracing.data.constants.Isotope;
import io.github.msdk.isotopes.tracing.data.exception.IntensityTypeMismatchException;
import io.github.msdk.isotopes.tracing.simulation.IsotopePatternSimulator;
import io.github.msdk.isotopes.tracing.simulation.IsotopePatternSimulatorRequest;
import io.github.msdk.isotopes.tracing.simulation.IsotopePatternSimulatorResponse;
import junit.framework.TestCase;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class IncorporationMapTest extends TestCase {

  private static final Logger LOG = LoggerFactory.getLogger(IncorporationMapTest.class);

  public void testConstructor() throws IntensityTypeMismatchException {
    final double INC_CN = 0.1;
    final double INC_C = 0.5;
    final double INC_N = 0.2;
    final Integer PRECISION = 4;
    final double MIN_FREQUENCY = 0.003;
    IsotopePatternSimulatorRequest request = new IsotopePatternSimulatorRequest();
    request.setAnalyzeMassShifts(true);
    request.setCharge(1);
    request.setFragments(new FragmentList(new Fragment("C10H26NO2Si3", "CN")));
    request.setMinimalFrequency(MIN_FREQUENCY);
    request.setRoundedFrequenciesPrecision(PRECISION);
    request.setRoundedMassPrecision(PRECISION);
    request.setTargetIntensityType(IntensityType.MID);
    request.setTracer1(Element.C);
    request.setTracer1Inc(new IncorporationRate(INC_C));
    request.setTracer2(Element.N);
    request.setTracer2Inc(new IncorporationRate(INC_N));
    request.setTracerAllInc(new IncorporationRate(INC_CN));
    IsotopePatternSimulatorResponse response =
        IsotopePatternSimulator.simulateIndependentTracerIncorporation(request);
    MSShiftDatabase msShiftDatabase = (MSShiftDatabase) response.getMsDatabaseList().get(0);
    LOG.info(msShiftDatabase.toString());
    IncorporationMap incorporationMap = new IncorporationMap(msShiftDatabase.getMixedSpectrum(),
        msShiftDatabase.getMixedMassShifts(), new IsotopeList(Isotope.C_13, Isotope.N_15));
    LOG.info(incorporationMap.asTable());
  }

  public void testCorrection() {
    IncorporationMap incorporationMap = new IncorporationMap();
    IsotopeFormula _00 = new IsotopeFormula();
    _00.put(Isotope.C_13, 0);
    _00.put(Isotope.N_15, 0);
    incorporationMap.put(_00, 2736425697.0);

    IsotopeFormula _01 = new IsotopeFormula();
    _01.put(Isotope.C_13, 0);
    _01.put(Isotope.N_15, 1);
    incorporationMap.put(_01, 11895695.5);

    IsotopeFormula _10 = new IsotopeFormula();
    _10.put(Isotope.C_13, 1);
    _10.put(Isotope.N_15, 0);
    incorporationMap.put(_10, 210763094.5);

    IsotopeFormula _20 = new IsotopeFormula();
    _20.put(Isotope.C_13, 2);
    _20.put(Isotope.N_15, 0);
    incorporationMap.put(_20, 4886826.5);

    LOG.info(incorporationMap.asTable());

    ElementFormula maxElementsFormular = new ElementFormula();
    maxElementsFormular.put(Element.C, 7);
    maxElementsFormular.put(Element.N, 1);

    incorporationMap = incorporationMap.correctForNaturalAbundance(maxElementsFormular);

    LOG.info(incorporationMap.asTable());

  }

  public void testGetValueByKey() {
    IncorporationMap incorporationMap = new IncorporationMap();
    IsotopeFormula formula = new IsotopeFormula();
    formula.put(Isotope.C_13, 2);
    IsotopeFormula formula2 = new IsotopeFormula();
    formula2.put(Isotope.C_13, 2);
    incorporationMap.put(formula, 1.0);
    assertTrue(formula.equals(formula2));
    assertEquals(1.0, incorporationMap.get(formula2));
  }

  public void testAdditionalGetterMethod() {
    IncorporationMap incorporationMap = new IncorporationMap();
    IsotopeFormula _00 = new IsotopeFormula();
    _00.put(Isotope.C_13, 0);
    _00.put(Isotope.N_15, 0);
    incorporationMap.put(_00, 2736425697.0);

    IsotopeFormula _01 = new IsotopeFormula();
    _01.put(Isotope.C_13, 0);
    _01.put(Isotope.N_15, 1);
    incorporationMap.put(_01, 11895695.5);

    IsotopeFormula _10 = new IsotopeFormula();
    _10.put(Isotope.C_13, 1);
    _10.put(Isotope.N_15, 0);
    incorporationMap.put(_10, 210763094.5);

    IsotopeFormula _20 = new IsotopeFormula();
    _20.put(Isotope.C_13, 2);
    _20.put(Isotope.N_15, 0);
    incorporationMap.put(_20, 4886826.5);
    assertEquals(incorporationMap.get(0, 0), 2736425697.0);
    assertEquals(incorporationMap.get(0, 1), 11895695.5);
    assertEquals(incorporationMap.get(1, 0), 210763094.5);
    assertEquals(incorporationMap.get(2, 0), 4886826.5);
  }

}
