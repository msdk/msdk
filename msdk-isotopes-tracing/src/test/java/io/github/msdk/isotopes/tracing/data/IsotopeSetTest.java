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

import java.util.ArrayList;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.isotopes.tracing.data.constants.Element;
import io.github.msdk.isotopes.tracing.data.constants.IncorporationType;
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
public class IsotopeSetTest extends TestCase {

  private static final Logger LOGGER = LoggerFactory.getLogger(IsotopeSetTest.class);
  public static final double ALLOWED_FREQUENCY_ERROR = 0.01;

  private void assertEqualsSpectra(MassSpectrum expected, MassSpectrum actual,
      double allowedFrequencyError) {
    for (Entry<Double, Double> expectedEntry : expected.entrySet()) {
      if (actual.get(expectedEntry.getKey()) == null
          && expectedEntry.getValue() <= allowedFrequencyError) {
        actual.put(expectedEntry.getKey(), 0.0);
      }
      Double frequencyDifference =
          Math.abs(expectedEntry.getValue() - actual.get(expectedEntry.getKey()));
      assertTrue(frequencyDifference + "is not smaller than " + allowedFrequencyError,
          frequencyDifference <= allowedFrequencyError);
    }
    for (Entry<Double, Double> actualEntry : actual.entrySet()) {
      if (expected.get(actualEntry.getKey()) == null
          && actualEntry.getValue() <= allowedFrequencyError) {
        expected.put(actualEntry.getKey(), 0.0);
      }
      assertTrue(Math.abs(
          actualEntry.getValue() - expected.get(actualEntry.getKey())) <= allowedFrequencyError);
    }
  }

  /*
   * test the number of isotopes in a set with natural incorporation
   */
  public void testIsotpeSet() {
    String formula = "C5N3H14";
    String capacityFormula = "C2N";
    Fragment fragment = new Fragment(formula, capacityFormula);
    double numberOfFragmentsInTheSet = 100000;
    IsotopeSet isotopeSet =
        new IsotopeSet(fragment, numberOfFragmentsInTheSet, IncorporationType.NATURAL);
    int totalElementNumberInFragment = 0;
    for (Entry<Element, Integer> entry : fragment.getFormula().entrySet()) {
      totalElementNumberInFragment = totalElementNumberInFragment + entry.getValue();
    }
    int expectedTotalElementNumberInSet =
        (int) (totalElementNumberInFragment * numberOfFragmentsInTheSet);
    int totalElementNumberInSet = 0;
    for (Entry<Isotope, Integer> entry : isotopeSet.entrySet()) {
      totalElementNumberInSet = totalElementNumberInSet + entry.getValue();
    }
    assertEquals(expectedTotalElementNumberInSet, totalElementNumberInSet);
    int expectedC12Number = (int) Math.round(numberOfFragmentsInTheSet * 5 * 0.98900);
    int expectedC13Number = (int) Math.round(numberOfFragmentsInTheSet * 5 * 0.01100);
    int expectedH1Number = (int) Math.round(numberOfFragmentsInTheSet * 14 * 0.99985);
    int expectedH2Number = (int) Math.round(numberOfFragmentsInTheSet * 14 * 0.00015);
    int expectedN14Number = (int) Math.round(numberOfFragmentsInTheSet * 3 * 0.99634);
    int expectedN15Number = (int) Math.round(numberOfFragmentsInTheSet * 3 * 0.00366);
    assertEquals(expectedC12Number, (int) isotopeSet.get(Isotope.C_12));
    assertEquals(expectedC13Number, (int) isotopeSet.get(Isotope.C_13));
    assertEquals(expectedH1Number, (int) isotopeSet.get(Isotope.H_1));
    assertEquals(expectedH2Number, (int) isotopeSet.get(Isotope.H_2));
    assertEquals(expectedN14Number, (int) isotopeSet.get(Isotope.N_14));
    assertEquals(expectedN15Number, (int) isotopeSet.get(Isotope.N_15));
  }

  /*
   * test the number of isotopes in a traced set
   */
  public void testIsotpeSet2() {
    String formula = "C5N3H14";
    String capacityFormula = "C2N";
    Fragment fragment = new Fragment(formula, capacityFormula);
    double numberOfFragmentsInTheSet = 100000;
    IsotopeSet isotopeSet =
        new IsotopeSet(fragment, numberOfFragmentsInTheSet, IncorporationType.MARKED);
    int totalElementNumberInFragment = 0;
    for (Entry<Element, Integer> entry : fragment.getFormula().entrySet()) {
      totalElementNumberInFragment = totalElementNumberInFragment + entry.getValue();
    }
    int expectedTotalElementNumberInSet =
        (int) (totalElementNumberInFragment * numberOfFragmentsInTheSet);
    int totalElementNumberInSet = 0;
    for (Entry<Isotope, Integer> entry : isotopeSet.entrySet()) {
      totalElementNumberInSet = totalElementNumberInSet + entry.getValue();
    }
    assertEquals(expectedTotalElementNumberInSet, totalElementNumberInSet);
    int expectedC12Number = (int) Math.round(numberOfFragmentsInTheSet * 3 * 0.98900);
    int expectedC13Number =
        (int) Math.round(numberOfFragmentsInTheSet * 3 * 0.01100 + 2 * numberOfFragmentsInTheSet);
    int expectedH1Number = (int) Math.round(numberOfFragmentsInTheSet * 14 * 0.99985);
    int expectedH2Number = (int) Math.round(numberOfFragmentsInTheSet * 14 * 0.00015);
    int expectedN14Number = (int) Math.round(numberOfFragmentsInTheSet * 2 * 0.99634);
    int expectedN15Number =
        (int) Math.round(numberOfFragmentsInTheSet * 2 * 0.00366 + numberOfFragmentsInTheSet);
    assertEquals(expectedC12Number, (int) isotopeSet.get(Isotope.C_12));
    assertEquals(expectedC13Number, (int) isotopeSet.get(Isotope.C_13));
    assertEquals(expectedH1Number, (int) isotopeSet.get(Isotope.H_1));
    assertEquals(expectedH2Number, (int) isotopeSet.get(Isotope.H_2));
    assertEquals(expectedN14Number, (int) isotopeSet.get(Isotope.N_14));
    assertEquals(expectedN15Number, (int) isotopeSet.get(Isotope.N_15));

  }

  public void testNaturalCarbonShift() {
    String formula = "C";
    String capacityFormula = "C";
    Fragment fragment = new Fragment(formula, capacityFormula);
    double numberOfFragmentsInTheSet = 100000;
    IsotopeSet isotopeSet =
        new IsotopeSet(fragment, numberOfFragmentsInTheSet, IncorporationType.NATURAL);
    MassSpectrum spectrum = isotopeSet.simulateSpectrum(0);
    assertTrue(spectrum.containsKey(Isotope.C_13.getAtomicMass()));
  }

  /*
   * compare the fragment masses of a natural set with existing data
   */
  public void testGetSpectrum() {
    // TODO: compare the results with existing data
  }

  /*
   * compare the fragment masses of a traced set with existing data
   */
  public void testGetSpectrum2() {
    // TODO: compare the results with existing data
  }

  /*
   * compare the fragment masses of a mixed set with existing data
   */
  public void testGetSpectrum3() {
    // TODO: compare the results with existing data
  }

  /*
   * Some quality checks: The spectrum from a natural set containing just one fragment should only
   * contain the lowest mass of the fragment.
   */
  public void testGetSpectrum4() {
    Fragment fragment = new Fragment("C5H7NO2", "C2N");
    IsotopeSet isotopeSet = new IsotopeSet(fragment, 1, IncorporationType.NATURAL);
    MassSpectrum masses = isotopeSet.simulateSpectrum(0);
    LOGGER.info("calculated masses: " + masses);
    LOGGER.info("lowest mass: " + fragment.lowestMass());
    assertEquals(1, masses.size());
    for (Entry<Double, Double> entry : masses.entrySet()) {
      assertTrue(entry.getValue().equals(1.0));
      assertTrue((entry.getKey() - fragment.lowestMass()) <= 0.0000001);
    }
  }

  /*
   * Some quality checks: The spectrum from a traced set containing just one fragment should only
   * contain the lowest mass of the incorporated fragment.
   */
  public void testGetSpectrum5() {
    Fragment fragment = new Fragment("C5H7NO2", "C2N");
    IsotopeSet isotopeSet = new IsotopeSet(fragment, 1, IncorporationType.MARKED);
    MassSpectrum masses = isotopeSet.simulateSpectrum(0);
    LOGGER.info("calculated masses: " + masses);
    LOGGER.info("lowest incorporated mass: " + fragment.lowestFullIncorporatedMass());
    assertEquals(1, masses.size());
    for (Entry<Double, Double> entry : masses.entrySet()) {
      assertTrue(entry.getValue().equals(1.0));
      assertTrue((entry.getKey() - fragment.lowestFullIncorporatedMass()) <= 0.0000001);
    }
  }

  /*
   * Some quality checks: The mass from a natural set of fragments should be in range of the lowest
   * and highest fragment mass.
   */
  public void testGetSpectrum6() {
    Fragment fragment = new Fragment("C5H7NO2", "C2N");
    IsotopeSet isotopeSet = new IsotopeSet(fragment, 1000, IncorporationType.NATURAL);
    for (Entry<Double, Double> entry : isotopeSet.simulateSpectrum(0).entrySet()) {
      Double mass = entry.getKey();
      LOGGER.info("calculated mass: " + mass);
      LOGGER.info("lowest mass: " + fragment.lowestMass());
      LOGGER.info("highest mass: " + fragment.highestMass());
      assertTrue(MathUtils.round(mass, 6) <= MathUtils.round(fragment.highestMass(), 6));
      assertTrue(MathUtils.round(mass, 6) >= MathUtils.round(fragment.lowestMass(), 6));
    }
  }

  /*
   * Some quality checks: The mass from a traced set of fragments should be in range of the lowest
   * incorporated and highest fragment mass.
   */
  public void testGetSpectrum7() {
    Fragment fragment = new Fragment("C5H7NO2", "C2N");
    IsotopeSet isotopeSet = new IsotopeSet(fragment, 1000, IncorporationType.MARKED);
    for (Entry<Double, Double> entry : isotopeSet.simulateSpectrum(0).entrySet()) {
      Double mass = entry.getKey();
      assertTrue(MathUtils.round(mass, 6) <= MathUtils.round(fragment.highestMass(), 6));
      assertTrue(
          MathUtils.round(mass, 6) >= MathUtils.round(fragment.lowestFullIncorporatedMass(), 6));
    }
  }

  /*
   * Some quality checks: If the fragment is just an element the number of isotopes in a natural set
   * of 100000 elements should be the abundance * 100000.
   */
  public void testGetSpectrum8() {
    Fragment fragment = new Fragment("C", "C");
    int numberOfFragments = 100000;
    IsotopeSet isotopeSet = new IsotopeSet(fragment, numberOfFragments, IncorporationType.NATURAL);
    MassSpectrum masses = isotopeSet.simulateSpectrum(0);
    assertEquals(2, masses.size());
    assertTrue(masses.get(Isotope.C_12.getAtomicMass()) == Isotope.C_12.getAbundance()
        * numberOfFragments);
    assertTrue(masses.get(Isotope.C_13.getAtomicMass()) == Isotope.C_13.getAbundance()
        * numberOfFragments);
  }

  /*
   * Some quality checks: If the fragment is just an element the number of isotopes in a traced set
   * of 100000 elements should be 100000.
   */
  public void testGetSpectrum9() {
    Fragment fragment = new Fragment("C", "C");
    int numberOfFragments = 100000;
    IsotopeSet isotopeSet = new IsotopeSet(fragment, numberOfFragments, IncorporationType.MARKED);
    MassSpectrum massAndFrequencyMap = isotopeSet.simulateSpectrum(0);
    assertEquals(1, massAndFrequencyMap.size());
    assertTrue(massAndFrequencyMap.get(Isotope.C_13.getAtomicMass()) == numberOfFragments);
  }

  public void testCreateSpectrumFromMasses() {
    Double[] massArray = {123.012, 123.021, 234.123, 152.331, 123.012, 152.331, 123.012};
    ArrayList<Double> fragmentMasses = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
      fragmentMasses.add(massArray[i]);
    }
    MassSpectrum expectedMap = new MassSpectrum(IntensityType.ABSOLUTE);
    expectedMap.put(123.012, 3.0);
    expectedMap.put(123.021, 1.0);
    expectedMap.put(234.123, 1.0);
    expectedMap.put(152.331, 2.0);
    MassSpectrum calculatedMap = MassSpectrum.createSpectrumFromMasses(fragmentMasses);
    assertEquals(expectedMap, calculatedMap);
  }

  /*
   * Check if the natural spectrum reflects the natural abundance of isotopes.
   */
  public void testSpectraForOneElementaryFragments() throws IntensityTypeMismatchException {
    for (Element element : Element.values()) {
      if (element.equals(Element.UNDEFINED) || element.equals(Element.NONE)) {
        continue;
      }
      LOGGER.info("Checking element" + element);
      Fragment fragment = new Fragment(element.name(), "");
      IsotopePatternSimulatorRequest simulatorRequest = new IsotopePatternSimulatorRequest();
      Integer precision = 4;
      simulatorRequest.setRoundedMassPrecision(precision);
      simulatorRequest.setRoundedFrequenciesPrecision(precision);
      simulatorRequest.setMinimalFrequency(0.0);
      simulatorRequest.setIncorporationRate(new IncorporationRate(0.1));
      simulatorRequest.setFragments(new FragmentList(fragment));
      simulatorRequest.setCharge(0);
      simulatorRequest.setTargetIntensityType(IntensityType.MID);
      IsotopePatternSimulatorResponse response = IsotopePatternSimulator.simulate(simulatorRequest);
      MSDatabase msDatabase = response.getMsDatabaseList().get(0);
      MassSpectrum naturalSpectrum = msDatabase.getNaturalSpectrum();
      LOGGER.info("Natural spectrum" + naturalSpectrum);
      assertEquals(element.getIsotopes().size(), naturalSpectrum.size());
      for (Isotope isotope : element.getIsotopes()) {
        LOGGER.info("Checking simulated abundance of isotope", isotope);
        Double isotopeMass = MathUtils.round(isotope.getAtomicMass(), precision);
        Double isotopeAbundance = MathUtils.round(isotope.getAbundance(), precision);
        Double allowedError = 2 * Math.pow(10, -1 * (precision));
        assertApproximatelySameEntryExists(naturalSpectrum, isotopeMass, isotopeAbundance,
            allowedError);
      }
    }
  }

  /*
   * compare simulated spectra for E2 fragments (where E is any element) using straight forward
   * combinatorics for comparison. That means the expected spectra are created straight forward not
   * using the simulation based on IsotopeSets or the implementation based on partitions.
   */
  public void testSpectraForTwoElementaryFragments() throws IntensityTypeMismatchException {
    for (Element element : Element.values()) {
      if (element.equals(Element.UNDEFINED) || element.equals(Element.NONE)
          || element.getIsotopes().size() != 2) {
        continue;
      }
      Fragment fragment = new Fragment(element.name() + 2, "");
      LOGGER.info("Checking fragment" + fragment.getFormula());
      IsotopePatternSimulatorRequest simulatorRequest = new IsotopePatternSimulatorRequest();
      simulatorRequest.setFragments(new FragmentList(fragment));
      simulatorRequest.setMinimalFrequency(0.0);
      simulatorRequest.setCharge(0);
      simulatorRequest.setTargetIntensityType(IntensityType.MID);
      IsotopePatternSimulatorResponse response = IsotopePatternSimulator.simulate(simulatorRequest);
      MSDatabase msDatabase = response.getMsDatabaseList().get(0);
      MassSpectrum naturalSimulatedSpectrum = msDatabase.getNaturalSpectrum().sortAscendingByMass();
      LOGGER.info("naturalSimulatedSpectrum\n" + naturalSimulatedSpectrum);
      int numberOfIsotopes = element.getIsotopes().size();
      MassSpectrum combinatoricallyExpectedSpectrum = new MassSpectrum(IntensityType.MID);
      IsotopeList isotopes = element.getIsotopes();
      Double mass0 = 2 * isotopes.get(0).getAtomicMass();
      Double abundance0 = isotopes.get(0).getAbundance() * isotopes.get(0).getAbundance();
      combinatoricallyExpectedSpectrum.put(mass0, abundance0);
      Double mass1 = isotopes.get(0).getAtomicMass() + isotopes.get(1).getAtomicMass();
      Double abundance1 = 2 * isotopes.get(0).getAbundance() * isotopes.get(1).getAbundance();
      combinatoricallyExpectedSpectrum.put(mass1, abundance1);
      Double mass2 = 2 * isotopes.get(1).getAtomicMass();
      Double abundance2 = isotopes.get(1).getAbundance() * isotopes.get(1).getAbundance();
      combinatoricallyExpectedSpectrum.put(mass2, abundance2);
      combinatoricallyExpectedSpectrum =
          combinatoricallyExpectedSpectrum.roundMasses(4).roundFrequencies(4).sortAscendingByMass();
      LOGGER.info("combinatoricallyExpectedSpectrum\n" + combinatoricallyExpectedSpectrum);
      // possibleNumberOfMasses = binom(N+n-1,n), where N=numberOfIsotpes,
      // n=numberOfElements
      int possibleNumberOfMasses = MathUtils.binom(numberOfIsotopes + 1, 2);
      LOGGER.info("possibleNumberOfMasses" + possibleNumberOfMasses);
      LOGGER.info("simulatedNumberOfMasses" + naturalSimulatedSpectrum.size());
      LOGGER.info("calculatedNumberOfMasses" + combinatoricallyExpectedSpectrum.size());

      assertEqualsSpectra(combinatoricallyExpectedSpectrum, naturalSimulatedSpectrum, 0.005);
    }
  }

  /*
   * compare simulated spectra for E_n fragments (where E is any element and n a natural number with
   * range defined in the test) using the implementations based on partitions/combinatorics.
   */
  public void testSpectraForNElementaryFragments() throws IntensityTypeMismatchException {
    for (Element element : Element.values()) {
      if (element.equals(Element.UNDEFINED) || element.equals(Element.NONE)) {
        continue;
      }
      for (int numberOfElements = 1; numberOfElements <= 4; numberOfElements++) {
        Fragment fragment = new Fragment(element.name() + numberOfElements, "");
        LOGGER.info("Checking fragment" + fragment.getFormula());
        IsotopePatternSimulatorRequest simulatorRequest = new IsotopePatternSimulatorRequest();
        simulatorRequest.setFragments(new FragmentList(fragment));
        simulatorRequest.setMinimalFrequency(0.0);
        simulatorRequest.setCharge(0);
        simulatorRequest.setTargetIntensityType(IntensityType.MID);
        IsotopePatternSimulatorResponse response =
            IsotopePatternSimulator.simulate(simulatorRequest);
        MSDatabase msDatabase = response.getMsDatabaseList().get(0);
        MassSpectrum naturalSimulatedSpectrum =
            msDatabase.getNaturalSpectrum().sortAscendingByMass();
        LOGGER.info("naturalSimulatedSpectrum\n" + naturalSimulatedSpectrum);
        int numberOfIsotopes = element.getIsotopes().size();
        MassSpectrum naturalCalculatedSpectrum =
            element.multiElementSpectrum(numberOfElements, 0.00);
        naturalCalculatedSpectrum =
            naturalCalculatedSpectrum.roundMasses(4).roundFrequencies(4).sortAscendingByMass();
        LOGGER.info("naturalCalculatedSpectrum\n" + naturalCalculatedSpectrum);
        IsotopeList isotopes = element.getIsotopes();
        if (numberOfElements == 2 && isotopes.size() == 2) {
          MassSpectrum combinatoricallyExpectedSpectrum = new MassSpectrum(IntensityType.MID);
          Double mass0 = 2 * isotopes.get(0).getAtomicMass();
          Double abundance0 = isotopes.get(0).getAbundance() * isotopes.get(0).getAbundance();
          combinatoricallyExpectedSpectrum.put(mass0, abundance0);
          Double mass1 = isotopes.get(0).getAtomicMass() + isotopes.get(1).getAtomicMass();
          Double abundance1 = 2 * isotopes.get(0).getAbundance() * isotopes.get(1).getAbundance();
          combinatoricallyExpectedSpectrum.put(mass1, abundance1);
          Double mass2 = 2 * isotopes.get(1).getAtomicMass();
          Double abundance2 = isotopes.get(1).getAbundance() * isotopes.get(1).getAbundance();
          combinatoricallyExpectedSpectrum.put(mass2, abundance2);
          combinatoricallyExpectedSpectrum = combinatoricallyExpectedSpectrum.roundMasses(4)
              .roundFrequencies(4).sortAscendingByMass();
          LOGGER.info("combinatoricallyExpectedSpectrum\n" + combinatoricallyExpectedSpectrum);
        }
        // possibleNumberOfMasses = binom(N+n-1,n), where
        // N=numberOfIsotpes, n=numberOfElements
        int possibleNumberOfMasses =
            MathUtils.binom(numberOfIsotopes + numberOfElements - 1, numberOfElements);
        LOGGER.info("possibleNumberOfMasses" + possibleNumberOfMasses);
        LOGGER.info("simulatedNumberOfMasses" + naturalSimulatedSpectrum.size());
        LOGGER.info("calculatedNumberOfMasses" + naturalCalculatedSpectrum.size());

        assertEqualsSpectra(naturalCalculatedSpectrum, naturalSimulatedSpectrum, 0.005);
      }
    }
  }

  /*
   * compare simulated spectra for En fragments (where E is any element, n a natural number) using
   * straight forward combinatorics for comparison. That means the expected spectra are created
   * straight forward not using the simulation based on IsotopeSets or the implementation based on
   * partitions.
   */
  public void testSpectraForNElementaryFragments2() throws IntensityTypeMismatchException {
    for (Element element : Element.values()) {
      if (element.equals(Element.UNDEFINED) || element.equals(Element.NONE)) {
        continue;
      }
      for (int numberOfElements = 1; numberOfElements <= 4; numberOfElements++) {
        Fragment fragment = new Fragment(element.name() + numberOfElements, "");
        LOGGER.info("Checking fragment" + fragment.getFormula());
        IsotopePatternSimulatorRequest simulatorRequest = new IsotopePatternSimulatorRequest();
        simulatorRequest.setFragments(new FragmentList(fragment));
        simulatorRequest.setMinimalFrequency(0.0);
        simulatorRequest.setCharge(0);
        simulatorRequest.setTargetIntensityType(IntensityType.MID);
        IsotopePatternSimulatorResponse response =
            IsotopePatternSimulator.simulate(simulatorRequest);
        MSDatabase msDatabase = response.getMsDatabaseList().get(0);
        MassSpectrum naturalSimulatedSpectrum = msDatabase.getNaturalSpectrum();
        LOGGER.info("naturalSimulatedSpectrum\n" + naturalSimulatedSpectrum);
        int numberOfIsotopes = element.getIsotopes().size();
        MassSpectrum naturalCalculatedSpectrum =
            element.multiElementSpectrum(numberOfElements, 0.00);
        naturalCalculatedSpectrum =
            naturalCalculatedSpectrum.roundMasses(4).roundFrequencies(4).sortAscendingByMass();
        LOGGER.info("naturalCalculatedSpectrum\n" + naturalCalculatedSpectrum);
        IsotopeList isotopes = element.getIsotopes();
        if (isotopes.size() == 2) {
          Isotope isotope1 = element.getIsotopes().get(0);
          Isotope isotope2 = element.getIsotopes().get(1);
          MassSpectrum combinatoricallyExpectedSpectrum = new MassSpectrum(IntensityType.MID);
          /*
           * m_k = (n-k)*m_I1 + k*m_I2 a_k = binom(n,k) * a_I1^(n-k) * a_I2^(k)
           */
          for (int k = 0; k <= numberOfElements; k++) {
            Double mass =
                (numberOfElements - k) * isotope1.getAtomicMass() + k * isotope2.getAtomicMass();
            Double abundance1 = Math.pow(isotope1.getAbundance(), numberOfElements - k);
            Double abundance2 = Math.pow(isotope2.getAbundance(), k);
            Double abundance = MathUtils.binom(numberOfElements, k) * abundance1 * abundance2;
            combinatoricallyExpectedSpectrum.put(mass, abundance);
          }
          combinatoricallyExpectedSpectrum = combinatoricallyExpectedSpectrum.roundMasses(4)
              .roundFrequencies(4).sortAscendingByMass();
          LOGGER.info("combinatoricallyExpectedSpectrum\n" + combinatoricallyExpectedSpectrum);
        }
        // possibleNumberOfMasses = binom(N+n-1,n), where
        // N=numberOfIsotpes, n=numberOfElements
        int possibleNumberOfMasses =
            MathUtils.binom(numberOfIsotopes + numberOfElements - 1, numberOfElements);
        LOGGER.info("possibleNumberOfMasses" + possibleNumberOfMasses);
        LOGGER.info("simulatedNumberOfMasses" + naturalSimulatedSpectrum.size());
        LOGGER.info("calculatedNumberOfMasses" + naturalCalculatedSpectrum.size());

        assertEqualsSpectra(naturalCalculatedSpectrum, naturalSimulatedSpectrum, 0.005);
      }
    }
  }

  public void testB2Spectrum() throws IntensityTypeMismatchException {
    Element element = Element.B;
    Fragment fragment = new Fragment(element.name() + 2, "");
    LOGGER.info("Checking fragment" + fragment.getFormula());
    IsotopePatternSimulatorRequest simulatorRequest = new IsotopePatternSimulatorRequest();
    simulatorRequest.setFragments(new FragmentList(fragment));
    simulatorRequest.setMinimalFrequency(0.0);
    simulatorRequest.setIncorporationRate(new IncorporationRate(0.0));
    simulatorRequest.setCharge(0);
    simulatorRequest.setTargetIntensityType(IntensityType.MID);
    IsotopePatternSimulatorResponse response = IsotopePatternSimulator.simulate(simulatorRequest);
    MSDatabase msDatabase = response.getMsDatabaseList().get(0);
    MassSpectrum naturalSimulatedSpectrum = msDatabase.getNaturalSpectrum();
    LOGGER.info("naturalSimulatedSpectrum\n" + naturalSimulatedSpectrum);
    MassSpectrum naturalCalculatedSpectrum = element.multiElementSpectrum(2, 0.00);
    naturalCalculatedSpectrum =
        naturalCalculatedSpectrum.roundMasses(4).roundFrequencies(4).sortAscendingByMass();
    LOGGER.info("naturalCalculatedSpectrum\n" + naturalCalculatedSpectrum);
    IsotopeList isotopes = element.getIsotopes();
    MassSpectrum combinatoricallyExpectedSpectrum = new MassSpectrum(IntensityType.MID);
    Double mass0 = 2 * isotopes.get(0).getAtomicMass();
    Double abundance0 = isotopes.get(0).getAbundance() * isotopes.get(0).getAbundance();
    combinatoricallyExpectedSpectrum.put(mass0, abundance0);
    Double mass1 = isotopes.get(0).getAtomicMass() + isotopes.get(1).getAtomicMass();
    Double abundance1 = 2 * isotopes.get(0).getAbundance() * isotopes.get(1).getAbundance();
    combinatoricallyExpectedSpectrum.put(mass1, abundance1);
    Double mass2 = 2 * isotopes.get(1).getAtomicMass();
    Double abundance2 = isotopes.get(1).getAbundance() * isotopes.get(1).getAbundance();
    combinatoricallyExpectedSpectrum.put(mass2, abundance2);
    combinatoricallyExpectedSpectrum =
        combinatoricallyExpectedSpectrum.roundMasses(4).roundFrequencies(4).sortAscendingByMass();
    LOGGER.info("combinatoricallyExpectedSpectrum\n" + combinatoricallyExpectedSpectrum);

    assertEqualsSpectra(naturalCalculatedSpectrum, naturalSimulatedSpectrum, 0.005);
  }

  public void testCl2Spectrum() throws IntensityTypeMismatchException {
    Element element = Element.Cl;
    Fragment fragment = new Fragment(element.name() + 2, "");
    LOGGER.info("Checking fragment" + fragment.getFormula());
    IsotopePatternSimulatorRequest simulatorRequest = new IsotopePatternSimulatorRequest();
    simulatorRequest.setFragments(new FragmentList(fragment));
    simulatorRequest.setMinimalFrequency(0.0);
    simulatorRequest.setIncorporationRate(new IncorporationRate(0.0));
    simulatorRequest.setCharge(0);
    simulatorRequest.setTargetIntensityType(IntensityType.MID);
    IsotopePatternSimulatorResponse response = IsotopePatternSimulator.simulate(simulatorRequest);
    MSDatabase msDatabase = response.getMsDatabaseList().get(0);
    MassSpectrum naturalSimulatedSpectrum = msDatabase.getNaturalSpectrum();
    LOGGER.info("naturalSimulatedSpectrum\n" + naturalSimulatedSpectrum);
    MassSpectrum naturalCalculatedSpectrum = element.multiElementSpectrum(2, 0.00);
    naturalCalculatedSpectrum =
        naturalCalculatedSpectrum.roundMasses(4).roundFrequencies(4).sortAscendingByMass();
    LOGGER.info("naturalCalculatedSpectrum\n" + naturalCalculatedSpectrum);
    IsotopeList isotopes = element.getIsotopes();
    MassSpectrum combinatoricallyExpectedSpectrum = new MassSpectrum(IntensityType.MID);
    Double mass0 = 2 * isotopes.get(0).getAtomicMass();
    Double abundance0 = isotopes.get(0).getAbundance() * isotopes.get(0).getAbundance();
    combinatoricallyExpectedSpectrum.put(mass0, abundance0);
    Double mass1 = isotopes.get(0).getAtomicMass() + isotopes.get(1).getAtomicMass();
    Double abundance1 = 2 * isotopes.get(0).getAbundance() * isotopes.get(1).getAbundance();
    combinatoricallyExpectedSpectrum.put(mass1, abundance1);
    Double mass2 = 2 * isotopes.get(1).getAtomicMass();
    Double abundance2 = isotopes.get(1).getAbundance() * isotopes.get(1).getAbundance();
    combinatoricallyExpectedSpectrum.put(mass2, abundance2);
    combinatoricallyExpectedSpectrum =
        combinatoricallyExpectedSpectrum.roundMasses(4).roundFrequencies(4).sortAscendingByMass();
    LOGGER.info("combinatoricallyExpectedSpectrum\n" + combinatoricallyExpectedSpectrum);

    assertEqualsSpectra(naturalCalculatedSpectrum, naturalSimulatedSpectrum, 0.005);
  }

  public void testH2Spectrum() throws IntensityTypeMismatchException {
    Element element = Element.H;
    Fragment fragment = new Fragment(element.name() + 2, "");
    LOGGER.info("Checking fragment" + fragment.getFormula());
    IsotopePatternSimulatorRequest simulatorRequest = new IsotopePatternSimulatorRequest();
    simulatorRequest.setFragments(new FragmentList(fragment));
    simulatorRequest.setMinimalFrequency(0.0);
    simulatorRequest.setIncorporationRate(new IncorporationRate(0.0));
    simulatorRequest.setCharge(0);
    simulatorRequest.setTargetIntensityType(IntensityType.MID);
    IsotopePatternSimulatorResponse response = IsotopePatternSimulator.simulate(simulatorRequest);
    MSDatabase msDatabase = response.getMsDatabaseList().get(0);
    MassSpectrum naturalSimulatedSpectrum = msDatabase.getNaturalSpectrum();
    LOGGER.info("naturalSimulatedSpectrum\n" + naturalSimulatedSpectrum);
    MassSpectrum naturalCalculatedSpectrum = element.multiElementSpectrum(2, 0.00);
    naturalCalculatedSpectrum =
        naturalCalculatedSpectrum.roundMasses(4).roundFrequencies(4).sortAscendingByMass();
    LOGGER.info("naturalCalculatedSpectrum\n" + naturalCalculatedSpectrum);
    IsotopeList isotopes = element.getIsotopes();
    MassSpectrum combinatoricallyExpectedSpectrum = new MassSpectrum(IntensityType.MID);
    Double mass0 = 2 * isotopes.get(0).getAtomicMass();
    Double abundance0 = isotopes.get(0).getAbundance() * isotopes.get(0).getAbundance();
    combinatoricallyExpectedSpectrum.put(mass0, abundance0);
    Double mass1 = isotopes.get(0).getAtomicMass() + isotopes.get(1).getAtomicMass();
    Double abundance1 = 2 * isotopes.get(0).getAbundance() * isotopes.get(1).getAbundance();
    combinatoricallyExpectedSpectrum.put(mass1, abundance1);
    Double mass2 = 2 * isotopes.get(1).getAtomicMass();
    Double abundance2 = isotopes.get(1).getAbundance() * isotopes.get(1).getAbundance();
    combinatoricallyExpectedSpectrum.put(mass2, abundance2);
    combinatoricallyExpectedSpectrum =
        combinatoricallyExpectedSpectrum.roundMasses(4).roundFrequencies(4).sortAscendingByMass();
    LOGGER.info("combinatoricallyExpectedSpectrum\n" + combinatoricallyExpectedSpectrum);

    assertEqualsSpectra(naturalCalculatedSpectrum, naturalSimulatedSpectrum, 0.005);
  }

  private void assertApproximatelySameEntryExists(MassSpectrum spectrum, Double isotopeMass,
      Double isotopeAbundance, Double allowedError) {
    boolean entryFound = false;
    for (Entry<Double, Double> entry : spectrum.entrySet()) {
      Double currentMass = entry.getKey();
      Double currentAbundance = entry.getValue();
      if (MathUtils.approximatelyEquals(currentMass, isotopeMass, allowedError)
          && MathUtils.approximatelyEquals(currentAbundance, isotopeAbundance, allowedError)) {
        entryFound = true;
        break;
      }
    }
    if (!entryFound) {
      fail("Found no entry, fitting mass [" + isotopeMass + "] and abundance [" + isotopeAbundance
          + "] " + "with an allowed error of " + allowedError);
    }
  }

}
