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
import io.github.msdk.isotopes.tracing.util.MathUtils;
import junit.framework.TestCase;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class IncorporationMapCorrectionTest extends TestCase {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(IncorporationMapCorrectionTest.class);
  private static final Double ALLOWED_INC_ERROR = 0.006;
  // some of these tests take a lot of time so do not run them automatically
  private static final boolean TEST_INCS = false;


  public void testIncorporationRate01() throws IntensityTypeMismatchException {
    if (TEST_INCS) {
      for (int c = 0; c < 10; c++) {
        for (int n = 0; n < 10; n++) {
          for (int cn = 0; cn < 10; cn++) {
            if (cn + c + n >= 10) {
              continue;
            }
            final double INC_CN = 0.0 + cn * 0.1;
            final double INC_C = 0.0 + c * 0.1;
            final double INC_N = 0.0 + n * 0.1;
            final double NUMBER_OF_FRAGMENTS = 100000.0;
            final Integer PRECISION = 4;
            final double MIN_FREQUENCY = 0.001;
            /*
             * simulate the spectrum
             */
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

            LOGGER.debug(msShiftDatabase.toString());
            IncorporationMap incorporationMap = new IncorporationMap(
                msShiftDatabase.getMixedSpectrum(), msShiftDatabase.getMixedMassShifts(),
                new IsotopeList(Isotope.C_13, Isotope.N_15));
            LOGGER.debug("UncorrectedIncorporation " + incorporationMap.asTable());

            ElementFormula fragmentFormula =
                ElementFormula.fromString(msShiftDatabase.getCompoundFormula());
            ElementFormula elementFormula = new ElementFormula();
            elementFormula.put(Element.C, fragmentFormula.get(Element.C));
            elementFormula.put(Element.N, fragmentFormula.get(Element.N));
            /*
             * recalculate the incorporation rates
             */
            IncorporationMap correctedMap =
                incorporationMap.correctForNaturalAbundance(elementFormula);

            LOGGER.debug("correctedMap " + correctedMap.asTable());
            LOGGER.debug("Simulated incorporations ");
            LOGGER.debug("INC_C " + INC_C);
            LOGGER.debug("INC_N " + INC_N);
            LOGGER.debug("INC_CN " + INC_CN);
            LOGGER.debug("Check C incorporation...");
            IsotopeFormula formulaC = new IsotopeFormula();
            formulaC.put(Isotope.C_13, 1);
            formulaC.put(Isotope.N_15, 0);
            LOGGER.debug("calculatedIncC " + correctedMap.get(formulaC));
            LOGGER.debug("expectedIncC " + INC_C);

            LOGGER.debug("Check N incorporation...");
            IsotopeFormula formulaN = new IsotopeFormula();
            formulaN.put(Isotope.C_13, 0);
            formulaN.put(Isotope.N_15, 1);
            LOGGER.debug("calculatedIncN " + correctedMap.get(formulaN));
            LOGGER.debug("expectedIncN " + INC_N);
            LOGGER.debug("Check CN incorporation...");
            IsotopeFormula formulaCN = new IsotopeFormula();
            formulaCN.put(Isotope.C_13, 1);
            formulaCN.put(Isotope.N_15, 1);
            LOGGER.debug("calculatedIncCN " + correctedMap.get(formulaCN));
            LOGGER.debug("expectedIncCN " + INC_CN);
                        Double actualCN = correctedMap.get(formulaCN) != null
                                ? correctedMap.get(formulaCN)
                                : 0.0;
                        Double actualC = correctedMap.get(formulaC) != null
                                ? correctedMap.get(formulaC)
                                : 0.0;
                        Double actualN = correctedMap.get(formulaN) != null
                                ? correctedMap.get(formulaN)
                                : 0.0;
            /*
             * compare the recalculated with the start values
             */
            assertTrue(MathUtils.approximatelyEquals(actualC, INC_C, ALLOWED_INC_ERROR));
            assertTrue(MathUtils.approximatelyEquals(actualN, INC_N, ALLOWED_INC_ERROR));
            assertTrue(MathUtils.approximatelyEquals(actualCN, INC_CN, ALLOWED_INC_ERROR));
            LOGGER.debug("---------------------------- PASSED -----------------------------");
          }
        }
      }
    }
  }

  public void testIncorporationRate02() throws IntensityTypeMismatchException {
    double maxError = 0.0;
    for (int c = 0; c < 10; c++) {
      final double INC_C = 0.0 + c * 0.1;
      final Integer PRECISION = 4;
      final double MIN_FREQUENCY = 0.001;
      /*
       * simulate the spectrum
       */
      IsotopePatternSimulatorRequest request = new IsotopePatternSimulatorRequest();
      request.setAnalyzeMassShifts(true);
      request.setCharge(1);
      request.setIncorporationRate(new IncorporationRate(INC_C));
      request.setFragments(new FragmentList(new Fragment("C10H26NO2Si3", "C")));
      request.setMinimalFrequency(MIN_FREQUENCY);
      request.setRoundedFrequenciesPrecision(PRECISION);
      request.setRoundedMassPrecision(PRECISION);
      request.setTargetIntensityType(IntensityType.MID);
      IsotopePatternSimulatorResponse response = IsotopePatternSimulator.simulate(request);
      MSShiftDatabase msShiftDatabase = (MSShiftDatabase) response.getMsDatabaseList().get(0);

      LOGGER.debug(msShiftDatabase.toString());
      IncorporationMap incorporationMap = new IncorporationMap(msShiftDatabase.getMixedSpectrum(),
          msShiftDatabase.getMixedMassShifts(), new IsotopeList(Isotope.C_13));
      LOGGER.debug("UncorrectedIncorporation " + incorporationMap.asTable());
      /*
       * recalculate the incorporation rates
       */
      ElementFormula fragmentFormula =
          ElementFormula.fromString(msShiftDatabase.getCompoundFormula());
      ElementFormula elementFormula = new ElementFormula();
      elementFormula.put(Element.C, fragmentFormula.get(Element.C));
      IncorporationMap correctedMap = incorporationMap.correctForNaturalAbundance(elementFormula);

      LOGGER.debug("correctedMap " + correctedMap.asTable());
      LOGGER.debug("Simulated incorporation ");
      LOGGER.debug("INC_C " + INC_C);
      LOGGER.debug("Check C incorporation...");
      IsotopeFormula formulaC = new IsotopeFormula();
      formulaC.put(Isotope.C_13, 1);
      LOGGER.debug("calculatedIncC " + correctedMap.get(formulaC));
      LOGGER.debug("expectedIncC " + INC_C);
            Double actualC = correctedMap.get(formulaC) != null
                    ? correctedMap.get(formulaC)
                    : 0.0;
      if (Math.abs(actualC - INC_C) > maxError) {
        maxError = Math.abs(actualC - INC_C);
      }
      /*
       * compare the recalculated with the start values
       */
      assertTrue(MathUtils.approximatelyEquals(actualC, INC_C, ALLOWED_INC_ERROR));
      LOGGER.debug("---------------------------- PASSED -----------------------------");
    }
    LOGGER.debug("maxError " + maxError);
  }

  public void testIncorporationRate03() throws IntensityTypeMismatchException {
    Double maxError = 0.0;
    for (int n = 0; n < 10; n++) {
      final double INC_N = 0.0 + n * 0.1;
      final Integer PRECISION = 4;
      final double MIN_FREQUENCY = 0.001;
      /*
       * simulate the spectrum
       */
      IsotopePatternSimulatorRequest request = new IsotopePatternSimulatorRequest();
      request.setAnalyzeMassShifts(true);
      request.setCharge(1);
      request.setIncorporationRate(new IncorporationRate(INC_N));
      request.setFragments(new FragmentList(new Fragment("C10H26NO2Si3", "N")));
      request.setMinimalFrequency(MIN_FREQUENCY);
      request.setRoundedFrequenciesPrecision(PRECISION);
      request.setRoundedMassPrecision(PRECISION);
      request.setTargetIntensityType(IntensityType.MID);
      IsotopePatternSimulatorResponse response = IsotopePatternSimulator.simulate(request);
      MSShiftDatabase msShiftDatabase = (MSShiftDatabase) response.getMsDatabaseList().get(0);

      LOGGER.debug(msShiftDatabase.toString());

      IncorporationMap incorporationMap = new IncorporationMap(msShiftDatabase.getMixedSpectrum(),
          msShiftDatabase.getMixedMassShifts(), new IsotopeList(Isotope.N_15));
      LOGGER.debug("UncorrectedIncorporation" + incorporationMap.asTable());
      /*
       * recalculate the incorporation rates
       */
      ElementFormula fragmentFormula =
          ElementFormula.fromString(msShiftDatabase.getCompoundFormula());
      ElementFormula elementFormula = new ElementFormula();
      elementFormula.put(Element.N, fragmentFormula.get(Element.N));
      IncorporationMap correctedMap = incorporationMap.correctForNaturalAbundance(elementFormula);

      LOGGER.debug("correctedMap " + correctedMap.asTable());

      LOGGER.debug("Simulated incorporation");
      LOGGER.debug("INC_N " + INC_N);

      LOGGER.debug("Check N incorporation...");
      IsotopeFormula formulaN = new IsotopeFormula();
      formulaN.put(Isotope.N_15, 1);
      LOGGER.debug("calculatedIncN " + correctedMap.get(formulaN));
      LOGGER.debug("expectedIncN " + INC_N);

            Double actualN = correctedMap.get(formulaN) != null
                    ? correctedMap.get(formulaN)
                    : 0.0;
      if (Math.abs(actualN - INC_N) > maxError) {
        maxError = Math.abs(actualN - INC_N);
      }
      /*
       * compare the recalculated with the start values
       */
      assertTrue(MathUtils.approximatelyEquals(actualN, INC_N, ALLOWED_INC_ERROR));
      LOGGER.debug("---------------------------- PASSED -----------------------------");
    }
    LOGGER.debug("maxError " + maxError);
  }

  public void testIncorporationRate04() throws IntensityTypeMismatchException {
    if (TEST_INCS) {
      for (int c2 = 0; c2 < 10; c2++) {
        for (int n = 0; n < 10; n++) {
          if (c2 + n >= 10) {
            continue;
          }
          final double INC_C2 = 0.0 + c2 * 0.1;
          final double INC_N = 0.0 + n * 0.1;
          final double NUMBER_OF_FRAGMENTS = 100000.0;
          final Integer PRECISION = 4;
          final double MIN_FREQUENCY = 0.001;
          /*
           * simulate the spectrum
           */
          IsotopePatternSimulatorRequest request = new IsotopePatternSimulatorRequest();
          request.setAnalyzeMassShifts(true);
          request.setCharge(1);
          request.setFragments(new FragmentList(new Fragment("C10H26NO2Si3", "C2N")));
          request.setTotalNumberOfFragments(NUMBER_OF_FRAGMENTS);
          request.setMinimalFrequency(MIN_FREQUENCY);
          request.setRoundedFrequenciesPrecision(PRECISION);
          request.setRoundedMassPrecision(PRECISION);
          request.setTargetIntensityType(IntensityType.MID);
          request.setTracer1(Element.C);
          request.setTracer1Inc(new IncorporationRate(INC_C2));
          request.setTracer2(Element.N);
          request.setTracer2Inc(new IncorporationRate(INC_N));
          request.setTracerAllInc(new IncorporationRate(0));
          IsotopePatternSimulatorResponse response =
              IsotopePatternSimulator.simulateIndependentTracerIncorporation(request);
          MSShiftDatabase msShiftDatabase = (MSShiftDatabase) response.getMsDatabaseList().get(0);

          LOGGER.debug(msShiftDatabase.toString());

          IncorporationMap incorporationMap = new IncorporationMap(
              msShiftDatabase.getMixedSpectrum(), msShiftDatabase.getMixedMassShifts(),
              new IsotopeList(Isotope.C_13, Isotope.N_15));
          LOGGER.debug("UncorrectedIncorporation" + incorporationMap.asTable());
          /*
           * recalculate the incorporation rates
           */
          ElementFormula fragmentFormula =
              ElementFormula.fromString(msShiftDatabase.getCompoundFormula());
          ElementFormula elementFormula = new ElementFormula();
          elementFormula.put(Element.C, fragmentFormula.get(Element.C));
          elementFormula.put(Element.N, fragmentFormula.get(Element.N));
          IncorporationMap correctedMap =
              incorporationMap.correctForNaturalAbundance(elementFormula);

          LOGGER.debug("correctedMap " + correctedMap.asTable());

          LOGGER.debug("Simulated incorporations");
          LOGGER.debug("INC_C2 " + INC_C2);
          LOGGER.debug("INC_N " + INC_N);

          LOGGER.debug("Check C incorporation...");
          IsotopeFormula formulaC2 = new IsotopeFormula();
          formulaC2.put(Isotope.C_13, 2);
          formulaC2.put(Isotope.N_15, 0);
          LOGGER.debug("calculatedIncC2 " + correctedMap.get(formulaC2));
          LOGGER.debug("expectedIncC2 " + INC_C2);

          LOGGER.debug("Check N incorporation...");
          IsotopeFormula formulaN = new IsotopeFormula();
          formulaN.put(Isotope.C_13, 0);
          formulaN.put(Isotope.N_15, 1);
          LOGGER.debug("calculatedIncN " + correctedMap.get(formulaN));
          LOGGER.debug("expectedIncN " + INC_N);

          LOGGER.debug("Check CN incorporation...");
          IsotopeFormula formulaCN = new IsotopeFormula();
          formulaCN.put(Isotope.C_13, 2);
          formulaCN.put(Isotope.N_15, 1);
          LOGGER.debug("calculatedIncCN " + correctedMap.get(formulaCN));
          LOGGER.debug("expectedIncCN " + 0.0);

                    Double actualCN = correctedMap.get(formulaCN) != null
                            ? correctedMap.get(formulaCN)
                            : 0.0;
                    Double actualC = correctedMap.get(formulaC2) != null
                            ? correctedMap.get(formulaC2)
                            : 0.0;
                    Double actualN = correctedMap.get(formulaN) != null
                            ? correctedMap.get(formulaN)
                            : 0.0;
          /*
           * compare the recalculated with the start values
           */
          assertTrue(MathUtils.approximatelyEquals(actualC, INC_C2, ALLOWED_INC_ERROR));
          assertTrue(MathUtils.approximatelyEquals(actualN, INC_N, ALLOWED_INC_ERROR));
          assertTrue(MathUtils.approximatelyEquals(actualCN, 0.0, ALLOWED_INC_ERROR));
          LOGGER.debug("---------------------------- PASSED -----------------------------");
        }
      }
    }
  }

  public void correctionGlnUnlabeledTest() {
    MassSpectrum measured = new MassSpectrum(IntensityType.ABSOLUTE);
    measured.put(156.083871, 2177824768.0);
    measured.put(157.081106, 3256251.75);
    measured.put(157.083466, 105339544.0);
    measured.put(157.087178, 164780256.0);
    measured.put(158.063075, 5476358.0);
    measured.put(158.080719, 75050424.0);
    measured.put(158.086352, 6758987.5);
    measured.put(158.090634, 3685533.25);
    measured.put(159.083991, 4425675.0);

    MassShiftDataSet shifts = new MassShiftDataSet();
    shifts.put(new MassShiftList(new MassShift(0, 0, null)),
        new IsotopeListList(new IsotopeList(Isotope.NONE)));
    shifts.put(new MassShiftList(new MassShift(0, 1, null)),
        new IsotopeListList(new IsotopeList(Isotope.N_15)));
    shifts.put(new MassShiftList(new MassShift(0, 2, null)),
        new IsotopeListList(new IsotopeList(Isotope.Si_29)));
    shifts.put(new MassShiftList(new MassShift(0, 3, null)),
        new IsotopeListList(new IsotopeList(Isotope.C_13)));
    shifts.put(new MassShiftList(new MassShift(0, 2, null), new MassShift(2, 4, null)),
        new IsotopeListList(new IsotopeList(Isotope.Si_29), new IsotopeList(Isotope.N_15)));
    shifts.put(new MassShiftList(new MassShift(0, 5, null)),
        new IsotopeListList(new IsotopeList(Isotope.Si_30)));
    shifts.put(new MassShiftList(new MassShift(0, 3, null), new MassShift(3, 6, null)),
        new IsotopeListList(new IsotopeList(Isotope.C_13), new IsotopeList(Isotope.Si_29)));
    shifts.put(new MassShiftList(new MassShift(0, 3, null), new MassShift(3, 7, null)),
        new IsotopeListList(new IsotopeList(Isotope.C_13), new IsotopeList(Isotope.C_13)));
    shifts.put(new MassShiftList(new MassShift(0, 5, null), new MassShift(5, 8, null)),
        new IsotopeListList(new IsotopeList(Isotope.Si_30), new IsotopeList(Isotope.C_13)));

    IncorporationMap incorporationMap =
        new IncorporationMap(measured, shifts, new IsotopeList(Isotope.C_13, Isotope.N_15));
    LOGGER.debug("incorporationMap " + incorporationMap.asTable());
    LOGGER.debug("incorporationMap.normalize() " + incorporationMap.normalize(4).asTable());
    ElementFormula fragmentFormula = ElementFormula.fromString("C7H14NOSi");
    ElementFormula elementFormula = new ElementFormula();
    elementFormula.put(Element.C, fragmentFormula.get(Element.C));
    elementFormula.put(Element.N, fragmentFormula.get(Element.N));
    IncorporationMap correctedMap = incorporationMap.correctForNaturalAbundance(elementFormula);
    LOGGER.debug("correctedMap " + correctedMap.asTable());
    LOGGER.debug("correctedMap.normalize() " + correctedMap.normalize(4).asTable());

    IncorporationMap normalizedCorrectedMap = correctedMap.normalize(4);
    IsotopeFormula cn00 = new IsotopeFormula();
    cn00.put(Isotope.C_13, 0);
    cn00.put(Isotope.N_15, 0);
    IsotopeFormula cn01 = new IsotopeFormula();
    cn01.put(Isotope.C_13, 0);
    cn01.put(Isotope.N_15, 1);
    IsotopeFormula cn10 = new IsotopeFormula();
    cn10.put(Isotope.C_13, 1);
    cn10.put(Isotope.N_15, 0);
    IsotopeFormula cn20 = new IsotopeFormula();
    cn20.put(Isotope.C_13, 2);
    cn20.put(Isotope.N_15, 0);
    assertEquals(1.0, normalizedCorrectedMap.get(cn00));
    assertEquals(0.0, normalizedCorrectedMap.get(cn10));
    assertEquals(0.0, normalizedCorrectedMap.get(cn01));
    assertEquals(0.0, normalizedCorrectedMap.get(cn20));
  }

  public void correctionGlnTotallyCNLabeledTest() {
    MassSpectrum measured = new MassSpectrum(IntensityType.ABSOLUTE);
    measured.put(161.094388, 3383957504.000000);
    measured.put(162.093845, 167757680.000000);
    measured.put(162.097430, 96693112.000000);
    measured.put(162.100503, 2915952.500000);
    measured.put(163.091187, 109522104.000000);
    measured.put(163.098605, 6520942.500000);
    measured.put(163.104047, 994677.062500);

    MassShiftDataSet shifts = new MassShiftDataSet();
    shifts.put(new MassShiftList(new MassShift(0, 0, null)), new IsotopeListList(
        new IsotopeList(Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.N_15)));
    shifts.put(new MassShiftList(new MassShift(0, 1, null)),
        new IsotopeListList(new IsotopeList(Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.C_13,
            Isotope.N_15, Isotope.Si_29)));
    shifts.put(new MassShiftList(new MassShift(0, 2, null)),
        new IsotopeListList(new IsotopeList(Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.C_13,
            Isotope.N_15, Isotope.C_13)));
    shifts.put(new MassShiftList(new MassShift(0, 3, null)),
        new IsotopeListList(new IsotopeList(Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.C_13,
            Isotope.N_15, Isotope.H_2)));
    shifts.put(new MassShiftList(new MassShift(0, 4, null)),
        new IsotopeListList(new IsotopeList(Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.C_13,
            Isotope.N_15, Isotope.Si_30)));
    shifts.put(new MassShiftList(new MassShift(0, 2, null), new MassShift(2, 5, null)),
        new IsotopeListList(new IsotopeList(Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.C_13,
            Isotope.N_15, Isotope.C_13), new IsotopeList(Isotope.Si_29)));
    shifts.put(new MassShiftList(new MassShift(0, 6, null)),
        new IsotopeListList(new IsotopeList(Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.C_13,
            Isotope.N_15, Isotope.O_18)));

    IncorporationMap incorporationMap =
        new IncorporationMap(measured, shifts, new IsotopeList(Isotope.C_13, Isotope.N_15));
    LOGGER.debug("incorporationMap " + incorporationMap.asTable());
    LOGGER.debug("incorporationMap.normalize() " + incorporationMap.normalize(4).asTable());
    ElementFormula fragmentFormula = ElementFormula.fromString("C7H14NOSi");
    ElementFormula elementFormula = new ElementFormula();
    elementFormula.put(Element.C, fragmentFormula.get(Element.C));
    elementFormula.put(Element.N, fragmentFormula.get(Element.N));
    IncorporationMap correctedMap = incorporationMap.correctForNaturalAbundance(elementFormula);
    LOGGER.debug("correctedMap " + correctedMap.asTable());
    LOGGER.debug("correctedMap.normalize() " + correctedMap.normalize(4).asTable());

    IncorporationMap normalizedCorrectedMap = correctedMap.normalize(4);
    IsotopeFormula cn41 = new IsotopeFormula();
    cn41.put(Isotope.C_13, 4);
    cn41.put(Isotope.N_15, 1);
    IsotopeFormula cn51 = new IsotopeFormula();
    cn51.put(Isotope.C_13, 5);
    cn51.put(Isotope.N_15, 1);
    assertEquals(1.0, normalizedCorrectedMap.get(cn41));
    assertEquals(0.0, normalizedCorrectedMap.get(cn51));

  }

  public void correctionGlnTotallyCLabeledTest() {
    MassSpectrum measured = new MassSpectrum(IntensityType.ABSOLUTE);
    measured.put(160.097400, 1584645632.000000);
    measured.put(161.094435, 3858969.500000);
    measured.put(161.096895, 75836104.000000);
    measured.put(161.100447, 44920384.000000);
    measured.put(161.103490, 1408230.125000);
    measured.put(162.094165, 51429216.000000);
    measured.put(162.101593, 2085006.750000);
    measured.put(163.097390, 1833341.625000);

    MassShiftDataSet shifts = new MassShiftDataSet();
    shifts.put(new MassShiftList(new MassShift(0, 0, null)), new IsotopeListList(
        new IsotopeList(Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.C_13)));
    shifts.put(new MassShiftList(new MassShift(0, 1, null)), new IsotopeListList(
        new IsotopeList(Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.N_15)));
    shifts.put(new MassShiftList(new MassShift(0, 2, null)), new IsotopeListList(
        new IsotopeList(Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.Si_29)));
    shifts.put(new MassShiftList(new MassShift(0, 3, null)), new IsotopeListList(
        new IsotopeList(Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.C_13)));
    shifts.put(new MassShiftList(new MassShift(0, 4, null)), new IsotopeListList(
        new IsotopeList(Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.H_2)));
    shifts.put(new MassShiftList(new MassShift(0, 5, null)), new IsotopeListList(
        new IsotopeList(Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.Si_30)));
    shifts.put(new MassShiftList(new MassShift(0, 6, null)), new IsotopeListList(
        new IsotopeList(Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.O_18)));
    shifts.put(new MassShiftList(new MassShift(0, 5, null), new MassShift(5, 7, null)),
        new IsotopeListList(
            new IsotopeList(Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.C_13, Isotope.Si_30),
            new IsotopeList(Isotope.C_13)));

    IncorporationMap incorporationMap =
        new IncorporationMap(measured, shifts, new IsotopeList(Isotope.C_13, Isotope.N_15));
    LOGGER.debug("incorporationMap " + incorporationMap.asTable());
    LOGGER.debug("incorporationMap.normalize() " + incorporationMap.normalize(4).asTable());
    ElementFormula fragmentFormula = ElementFormula.fromString("C7H14NOSi");
    ElementFormula elementFormula = new ElementFormula();
    elementFormula.put(Element.C, fragmentFormula.get(Element.C));
    elementFormula.put(Element.N, fragmentFormula.get(Element.N));
    IncorporationMap correctedMap = incorporationMap.correctForNaturalAbundance(elementFormula);
    LOGGER.debug("correctedMap " + correctedMap.asTable());
    LOGGER.debug("correctedMap.normalize() " + correctedMap.normalize(4).asTable());

    IncorporationMap normalizedCorrectedMap = correctedMap.normalize(4);
    IsotopeFormula cn40 = new IsotopeFormula();
    cn40.put(Isotope.C_13, 4);
    cn40.put(Isotope.N_15, 0);
    IsotopeFormula cn41 = new IsotopeFormula();
    cn41.put(Isotope.C_13, 4);
    cn41.put(Isotope.N_15, 1);
    IsotopeFormula cn50 = new IsotopeFormula();
    cn50.put(Isotope.C_13, 5);
    cn50.put(Isotope.N_15, 0);
    assertEquals(1.0, normalizedCorrectedMap.get(cn40));
    assertEquals(0.0, normalizedCorrectedMap.get(cn41));
    assertEquals(0.0, normalizedCorrectedMap.get(cn50));

  }

  public void correctionGlnTotallyNLabeledTest() {
    MassSpectrum measured = new MassSpectrum(IntensityType.ABSOLUTE);
    measured.put(157.081106, 4505609216.000000);
    measured.put(158.080438, 203910720.000000);
    measured.put(158.084197, 329013920.000000);
    measured.put(158.097247, 3530218.250000);
    measured.put(159.077794, 143005824.000000);
    measured.put(159.083474, 14104742.000000);
    measured.put(159.087566, 10400924.000000);
    measured.put(159.090714, 1040433.375000);
    measured.put(160.081109, 11535833.000000);

    MassShiftDataSet shifts = new MassShiftDataSet();
    shifts.put(new MassShiftList(new MassShift(0, 0, null)),
        new IsotopeListList(new IsotopeList(Isotope.N_15)));
    shifts.put(new MassShiftList(new MassShift(0, 1, null)),
        new IsotopeListList(new IsotopeList(Isotope.N_15, Isotope.Si_29)));
    shifts.put(new MassShiftList(new MassShift(0, 2, null)),
        new IsotopeListList(new IsotopeList(Isotope.N_15, Isotope.C_13)));
    shifts.put(new MassShiftList(new MassShift(0, 3, null)),
        new IsotopeListList(new IsotopeList(Isotope.N_15, Isotope.H_2)));
    shifts.put(new MassShiftList(new MassShift(0, 4, null)),
        new IsotopeListList(new IsotopeList(Isotope.N_15, Isotope.Si_30)));
    shifts.put(new MassShiftList(new MassShift(0, 2, null), new MassShift(2, 5, null)),
        new IsotopeListList(new IsotopeList(Isotope.N_15, Isotope.C_13),
            new IsotopeList(Isotope.Si_29)));
    shifts.put(new MassShiftList(new MassShift(0, 6, null)),
        new IsotopeListList(new IsotopeList(Isotope.N_15, Isotope.O_18)));
    shifts.put(new MassShiftList(new MassShift(0, 2, null), new MassShift(2, 7, null)),
        new IsotopeListList(new IsotopeList(Isotope.N_15, Isotope.C_13),
            new IsotopeList(Isotope.C_13)));
    shifts.put(new MassShiftList(new MassShift(0, 4, null), new MassShift(4, 8, null)),
        new IsotopeListList(new IsotopeList(Isotope.N_15, Isotope.Si_30),
            new IsotopeList(Isotope.C_13)));

    IncorporationMap incorporationMap =
        new IncorporationMap(measured, shifts, new IsotopeList(Isotope.C_13, Isotope.N_15));
    LOGGER.debug("incorporationMap " + incorporationMap.asTable());
    LOGGER.debug("incorporationMap.normalize() ", incorporationMap.normalize(4).asTable());
    ElementFormula fragmentFormula = ElementFormula.fromString("C7H14NOSi");
    ElementFormula elementFormula = new ElementFormula();
    elementFormula.put(Element.C, fragmentFormula.get(Element.C));
    elementFormula.put(Element.N, fragmentFormula.get(Element.N));
    IncorporationMap correctedMap = incorporationMap.correctForNaturalAbundance(elementFormula);
    LOGGER.debug("correctedMap " + correctedMap.asTable());
    LOGGER.debug("correctedMap.normalize() " + correctedMap.normalize(4).asTable());

    IncorporationMap normalizedCorrectedMap = correctedMap.normalize(4);
    IsotopeFormula cn01 = new IsotopeFormula();
    cn01.put(Isotope.C_13, 0);
    cn01.put(Isotope.N_15, 1);
    IsotopeFormula cn11 = new IsotopeFormula();
    cn11.put(Isotope.C_13, 1);
    cn11.put(Isotope.N_15, 1);
    IsotopeFormula cn21 = new IsotopeFormula();
    cn21.put(Isotope.C_13, 2);
    cn21.put(Isotope.N_15, 1);
    assertEquals(1.0, normalizedCorrectedMap.get(cn01));
    assertEquals(0.0, normalizedCorrectedMap.get(cn11));
    assertEquals(0.0, normalizedCorrectedMap.get(cn21));

  }



  public void test12CGLN() {
    LOGGER.debug("12CGln");
    IsotopeFormula cn00 = new IsotopeFormula();
    cn00.put(Isotope.C_13, 0);
    cn00.put(Isotope.N_15, 0);
    IsotopeFormula cn01 = new IsotopeFormula();
    cn01.put(Isotope.C_13, 0);
    cn01.put(Isotope.N_15, 1);
    IsotopeFormula cn10 = new IsotopeFormula();
    cn10.put(Isotope.C_13, 1);
    cn10.put(Isotope.N_15, 0);
    IsotopeFormula cn20 = new IsotopeFormula();
    cn20.put(Isotope.C_13, 2);
    cn20.put(Isotope.N_15, 0);
    IsotopeFormula[] isotopeFormulas = {cn00, cn01, cn10, cn20};
    Double[] intensities = {2358214736.0, 8732609.75, 175964918.5, 3685533.25};
    IncorporationMap incorporationMap = new IncorporationMap(isotopeFormulas, intensities);
    LOGGER.debug("uncorrectedMap " + incorporationMap.asTable());
    LOGGER.debug("uncorrectedMap normalized " + incorporationMap.normalize(4).asTable());
    ElementFormula fragmentFormula = ElementFormula.fromString("C7H14NOSi");
    ElementFormula elementFormula = new ElementFormula();
    elementFormula.put(Element.C, fragmentFormula.get(Element.C));
    elementFormula.put(Element.N, fragmentFormula.get(Element.N));
    IncorporationMap correctedMap = incorporationMap.correctForNaturalAbundance(elementFormula);
    LOGGER.debug("correctedMap " + correctedMap.asTable());
    LOGGER.debug("correctedMap normalized " + correctedMap.normalize(4).asTable());
    IncorporationMap normalizedCorrectedMap = correctedMap.normalize(4);
    assertEquals(1.0, normalizedCorrectedMap.get(0, 0));
    assertEquals(0.0, normalizedCorrectedMap.get(0, 1));
    assertEquals(0.0, normalizedCorrectedMap.get(1, 0));
    assertEquals(0.0, normalizedCorrectedMap.get(2, 0));
  }

  public void test13C15NGLN() {
    LOGGER.debug("13C15NGln");
    IsotopeFormula cn41 = new IsotopeFormula();
    cn41.put(Isotope.C_13, 4);
    cn41.put(Isotope.N_15, 1);
    IsotopeFormula cn51 = new IsotopeFormula();
    cn51.put(Isotope.C_13, 5);
    cn51.put(Isotope.N_15, 1);
    IsotopeFormula[] isotopeFormulas = {cn41, cn51};
    Double[] intensities = {3664153240.500000, 103214054.5};
    IncorporationMap incorporationMap = new IncorporationMap(isotopeFormulas, intensities);
    LOGGER.debug("uncorrectedMap " + incorporationMap.asTable());
    LOGGER.debug("uncorrectedMap normalized " + incorporationMap.normalize(4).asTable());
    ElementFormula fragmentFormula = ElementFormula.fromString("C7H14NOSi");
    ElementFormula elementFormula = new ElementFormula();
    elementFormula.put(Element.C, fragmentFormula.get(Element.C));
    elementFormula.put(Element.N, fragmentFormula.get(Element.N));
    IncorporationMap correctedMap = incorporationMap.correctForNaturalAbundance(elementFormula);
    LOGGER.debug("correctedMap " + correctedMap.asTable());
    LOGGER.debug("correctedMap normalized " + correctedMap.normalize(4).asTable());
    IncorporationMap normalizedCorrectedMap = correctedMap.normalize(4);
    assertEquals(1.0, normalizedCorrectedMap.get(4, 1));
    assertEquals(0.0, normalizedCorrectedMap.get(5, 1));
  }

  public void test13CGLN() {
    LOGGER.debug("13CGln");
    IsotopeFormula cn40 = new IsotopeFormula();
    cn40.put(Isotope.C_13, 4);
    cn40.put(Isotope.N_15, 0);
    IsotopeFormula cn41 = new IsotopeFormula();
    cn41.put(Isotope.C_13, 4);
    cn41.put(Isotope.N_15, 1);
    IsotopeFormula cn50 = new IsotopeFormula();
    cn50.put(Isotope.C_13, 5);
    cn50.put(Isotope.N_15, 0);
    IsotopeFormula[] isotopeFormulas = {cn40, cn41, cn50};
    Double[] intensities = {1717237531.0, 3858969.5, 44920384.0};
    IncorporationMap incorporationMap = new IncorporationMap(isotopeFormulas, intensities);
    LOGGER.debug("uncorrectedMap " + incorporationMap.asTable());
    LOGGER.debug("uncorrectedMap normalized " + incorporationMap.normalize(4).asTable());
    ElementFormula fragmentFormula = ElementFormula.fromString("C7H14NOSi");
    ElementFormula elementFormula = new ElementFormula();
    elementFormula.put(Element.C, fragmentFormula.get(Element.C));
    elementFormula.put(Element.N, fragmentFormula.get(Element.N));
    IncorporationMap correctedMap = incorporationMap.correctForNaturalAbundance(elementFormula);
    LOGGER.debug("correctedMap " + correctedMap.asTable());
    LOGGER.debug("correctedMap normalized " + correctedMap.normalize(4).asTable());
    IncorporationMap normalizedCorrectedMap = correctedMap.normalize(4);
    assertEquals(1.0, normalizedCorrectedMap.get(4, 0));
    assertEquals(0.0, normalizedCorrectedMap.get(4, 1));
    assertEquals(0.0, normalizedCorrectedMap.get(5, 0));
  }

}
