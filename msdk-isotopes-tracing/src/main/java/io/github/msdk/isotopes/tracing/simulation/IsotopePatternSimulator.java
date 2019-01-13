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

import java.util.InputMismatchException;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.isotopes.tracing.data.ElementFormula;
import io.github.msdk.isotopes.tracing.data.Fragment;
import io.github.msdk.isotopes.tracing.data.IncorporationRate;
import io.github.msdk.isotopes.tracing.data.IsotopeSet;
import io.github.msdk.isotopes.tracing.data.MSDatabase;
import io.github.msdk.isotopes.tracing.data.MSDatabaseList;
import io.github.msdk.isotopes.tracing.data.MSShiftDatabase;
import io.github.msdk.isotopes.tracing.data.MassSpectrum;
import io.github.msdk.isotopes.tracing.data.constants.Element;
import io.github.msdk.isotopes.tracing.data.constants.IncorporationType;
import io.github.msdk.isotopes.tracing.data.constants.IntensityType;
import io.github.msdk.isotopes.tracing.data.exception.IntensityTypeMismatchException;

/**
 * Class to simulate isotope patterns for tracing experiments. Parameters have to be set in the
 * {@link IsotopePatternSimulatorRequest}. Assume you labeled with (15N-amino)(13C)5-glutamine and
 * you want to simulate the isotope pattern of the glutamate pool where 60% of the pool consists of
 * (15N)(13C)5-glutamate. Then your {@link Fragment} is given by the formula C5H9NO4 and the
 * capacity C5N. Set the {@link IncorporationRate} to 0.6 and use the #simulate method for
 * simulation.
 * 
 * If you want to simulate a glutamate pool where 10% consist of 15N-glutamate, 20% consist of
 * (13C)5-glutamate and 50% consist of (15N)(13C)5-glutamate use the
 * #simulateIndependentTracerIncorporation method.
 * 
 * When you set analyseMassShifts = true in the {@link IsotopePatternSimulatorRequest} you will get
 * the information which isotopes induced certain peaks, but this could cost a lot.
 * 
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class IsotopePatternSimulator {

  private static final Logger LOGGER = LoggerFactory.getLogger(IsotopePatternSimulator.class);

  public static IsotopePatternSimulatorResponse simulate(IsotopePatternSimulatorRequest request)
      throws IntensityTypeMismatchException {
    final double incRate = request.getIncorporationRate().getRateValue();
    final double naturalFragments = request.getTotalNumberOfFragments() * (1 - incRate);
    final double experimentalFragments = request.getTotalNumberOfFragments() * incRate;
    final boolean analyzeMassShifts = request.getAnalyzeMassShifts();
    final int charge = request.getCharge();
    final IntensityType intensityType = request.getTargetIntensityType();
    final Integer roundMassesPrecision = request.getRoundedMassPrecision();
    final Integer roundFrequenciesPrecision = request.getRoundedFrequenciesPrecision();
    final Double minimalRelativeFrequency = request.getMinimalFrequency();
    MSDatabaseList msDatabaseList = new MSDatabaseList();
    for (Fragment fragment : request.getFragments()) {
      IsotopeSet naturalSet = new IsotopeSet(fragment, naturalFragments, IncorporationType.NATURAL);
      IsotopeSet markedSet =
          new IsotopeSet(fragment, experimentalFragments, IncorporationType.MARKED);
      MassSpectrum naturalSpectrum = naturalSet.simulateSpectrum(charge);
      MassSpectrum markedSpectrum = markedSet.simulateSpectrum(charge);
      MassSpectrum mixedSpectrum = naturalSpectrum.merge(markedSpectrum);
      naturalSpectrum = prepareSpectrum(naturalSpectrum, roundMassesPrecision,
          roundFrequenciesPrecision, minimalRelativeFrequency, intensityType);
      markedSpectrum = prepareSpectrum(markedSpectrum, roundMassesPrecision,
          roundFrequenciesPrecision, minimalRelativeFrequency, intensityType);
      mixedSpectrum = prepareSpectrum(mixedSpectrum, roundMassesPrecision,
          roundFrequenciesPrecision, minimalRelativeFrequency, intensityType);
      if (analyzeMassShifts) {
        MSShiftDatabase msShiftDatabase = new MSShiftDatabase();
        msShiftDatabase.setIncorporatedTracers(fragment.getTracerCapacity().toSimpleString());
        msShiftDatabase.setIncorporationRate(incRate);
        msShiftDatabase.setCompoundFormula(fragment.getFormula().toSimpleString());
        msShiftDatabase.setNaturalSpectrum(naturalSpectrum);
        msShiftDatabase.setMarkedSpectrum(markedSpectrum);
        msShiftDatabase.setMixedSpectrum(mixedSpectrum);
        msShiftDatabase.analyseAllShifts();
        LOGGER.debug("\n" + msShiftDatabase);
        msDatabaseList.add(msShiftDatabase);
      } else {
        MSDatabase msDatabase = new MSDatabase();
        msDatabase.setIncorporatedTracers(fragment.getTracerCapacity().toSimpleString());
        msDatabase.setIncorporationRate(incRate);
        msDatabase.setCompoundFormula(fragment.getFormula().toSimpleString());
        msDatabase.setNaturalSpectrum(naturalSpectrum);
        msDatabase.setMarkedSpectrum(markedSpectrum);
        msDatabase.setMixedSpectrum(mixedSpectrum);
        LOGGER.debug("\n" + msDatabase);
        msDatabaseList.add(msDatabase);
      }
    }
    IsotopePatternSimulatorResponse simulationResponse = new IsotopePatternSimulatorResponse();
    simulationResponse.setMsDatabaseList(msDatabaseList);
    return simulationResponse;
  }

  public static IsotopePatternSimulatorResponse simulateIndependentTracerIncorporation(
      IsotopePatternSimulatorRequest request) throws IntensityTypeMismatchException {
    final double tracer1Inc = request.getTracer1Inc().getRateValue();
    final double tracer2Inc = request.getTracer2Inc().getRateValue();
    final double tracerAllInc = request.getTracerAllInc().getRateValue();
    final double incRate = tracer1Inc + tracer2Inc + tracerAllInc;
    if (incRate > 1) {
      throw new InputMismatchException(
          "Total incorporation rate value " + incRate + " is not greater than 1!");
    }
    final boolean analyzeMassShifts = request.getAnalyzeMassShifts();
    final int charge = request.getCharge();
    final IntensityType intensityType = request.getTargetIntensityType();
    final Element tracer1 = request.getTracer1();
    final Element tracer2 = request.getTracer2();
    final double numberOfFragments = request.getTotalNumberOfFragments();
    final Integer roundMassesPrecision = request.getRoundedMassPrecision();
    final Integer roundFrequenciesPrecision = request.getRoundedFrequenciesPrecision();
    final Double minimalFrequency = request.getMinimalFrequency();
    MSDatabaseList msDatabaseList = new MSDatabaseList();

    for (Fragment fragment : request.getFragments()) {
      ElementFormula capacity = fragment.getTracerCapacity();
      String capacity1 = tracer1.name() + capacity.get(tracer1);
      String capacity2 = tracer2.name() + capacity.get(tracer2);
      Fragment fragmentAll = fragment.copy();
      Fragment fragment1 = fragment.copy();
      fragment1.setCapacity(capacity1);
      Fragment fragment2 = fragment.copy();
      fragment2.setCapacity(capacity2);

      IsotopeSet naturalSet =
          new IsotopeSet(fragmentAll, numberOfFragments * (1 - incRate), IncorporationType.NATURAL);
      IsotopeSet markedSetTracerALL =
          new IsotopeSet(fragmentAll, numberOfFragments * (tracerAllInc), IncorporationType.MARKED);
      IsotopeSet markedSetTracer1 =
          new IsotopeSet(fragment1, numberOfFragments * (tracer1Inc), IncorporationType.MARKED);
      IsotopeSet markedSetTracer2 =
          new IsotopeSet(fragment2, numberOfFragments * (tracer2Inc), IncorporationType.MARKED);

      MassSpectrum naturalSpectrum = naturalSet.simulateSpectrum(charge);
      MassSpectrum markedSpectrumTracerAll = markedSetTracerALL.simulateSpectrum(charge);
      MassSpectrum markedSpectrumTracer1 = markedSetTracer1.simulateSpectrum(charge);
      MassSpectrum markedSpectrumTracer2 = markedSetTracer2.simulateSpectrum(charge);
      MassSpectrum mixedSpectrum = naturalSpectrum.merge(markedSpectrumTracerAll);
      mixedSpectrum = mixedSpectrum.merge(markedSpectrumTracer1);
      mixedSpectrum = mixedSpectrum.merge(markedSpectrumTracer2);

      naturalSpectrum = IsotopePatternSimulator.prepareSpectrum(naturalSpectrum,
          roundMassesPrecision, roundFrequenciesPrecision, minimalFrequency, intensityType);
      markedSpectrumTracerAll = IsotopePatternSimulator.prepareSpectrum(markedSpectrumTracerAll,
          roundMassesPrecision, roundFrequenciesPrecision, minimalFrequency, intensityType);
      markedSpectrumTracer1 = IsotopePatternSimulator.prepareSpectrum(markedSpectrumTracer1,
          roundMassesPrecision, roundFrequenciesPrecision, minimalFrequency, intensityType);
      markedSpectrumTracer2 = IsotopePatternSimulator.prepareSpectrum(markedSpectrumTracer2,
          roundMassesPrecision, roundFrequenciesPrecision, minimalFrequency, intensityType);
      mixedSpectrum = IsotopePatternSimulator.prepareSpectrum(mixedSpectrum, roundMassesPrecision,
          roundFrequenciesPrecision, minimalFrequency, intensityType);

      MSShiftDatabase msShiftDatabase = new MSShiftDatabase();
      msShiftDatabase.setIncorporatedTracers(capacity1 + ", " + capacity2 + " independently");
      msShiftDatabase.setIncorporationRate(incRate);
      msShiftDatabase.setCompoundFormula(fragment.getFormula().toSimpleString());
      msShiftDatabase.setNaturalSpectrum(naturalSpectrum);
      msShiftDatabase.setMarkedSpectrum(markedSpectrumTracer1);
      msShiftDatabase.setMixedSpectrum(mixedSpectrum);
      if (analyzeMassShifts) {
        msShiftDatabase.analyseAllShifts();
      }
      msDatabaseList.add(msShiftDatabase);
    }
    IsotopePatternSimulatorResponse simulationResponse = new IsotopePatternSimulatorResponse();
    simulationResponse.setMsDatabaseList(msDatabaseList);
    return simulationResponse;

  }

  /**
   * Returns a new spectrum that resulted from a manipulation of the parameter spectrum.
   * 
   * @param spectrum, spectrum to be prepared
   * @param roundMassesPrecision,
   * @param roundFrequenciesPrecision,
   * @param minimaFrequency,
   * @param intensityType,
   * @return
   */
  public static MassSpectrum prepareSpectrum(MassSpectrum spectrum, Integer roundMassesPrecision,
      Integer roundFrequenciesPrecision, Double minimalFrequency, IntensityType intensityType) {
    if (roundMassesPrecision != null) {
      spectrum = spectrum.roundMasses(roundMassesPrecision);
    }
    if (intensityType.equals(IntensityType.MID)) {
      spectrum = spectrum.toMIDFrequency();
    } else if (intensityType.equals(IntensityType.RELATIVE)) {
      spectrum = spectrum.toRelativeFrequency();
    }
    if (roundFrequenciesPrecision != null) {
      spectrum = spectrum.roundFrequencies(roundFrequenciesPrecision);
    }
    if (minimalFrequency != null) {
      spectrum = spectrum.skipLowFrequency(minimalFrequency);
    }
    Double sumOfFrequencies = 0.0;
    for (Entry<Double, Double> entry : spectrum.entrySet()) {
      sumOfFrequencies = sumOfFrequencies + entry.getValue();
    }
    return spectrum.sortAscendingByMass();
  }
}
