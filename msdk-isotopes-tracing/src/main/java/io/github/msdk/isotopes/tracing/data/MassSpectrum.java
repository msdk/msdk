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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.isotopes.tracing.data.constants.ErrorMessage;
import io.github.msdk.isotopes.tracing.data.constants.IntensityType;
import io.github.msdk.isotopes.tracing.data.constants.Isotope;
import io.github.msdk.isotopes.tracing.data.constants.NaturalConstants;
import io.github.msdk.isotopes.tracing.data.exception.IntensityTypeMismatchException;
import io.github.msdk.isotopes.tracing.util.MathUtils;


/**
 * A map, used to identify a mass (the map key) to its frequency (the map value)
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
@SuppressWarnings("serial")
public class MassSpectrum extends LinkedHashMap<Double, Double> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MassSpectrum.class);
  private IntensityType intensityType;

  public MassSpectrum(IntensityType intensityType) {
    this.intensityType = intensityType;
  }

  /**
   * Merges this map with an other map by adding frequencies of masses that are contained in both
   * maps and adding new entries from the other map that are not contained in this map. This only
   * makes sense if both have IntensityType.ABSOLUTE.
   * 
   * @param otherSpectrum
   * @return a new merged MassSpectrum
   * @throws IntensityTypeMismatchException
   */
  public MassSpectrum merge(MassSpectrum otherSpectrum) throws IntensityTypeMismatchException {
    if (this.intensityType.equals(IntensityType.MID)
        || otherSpectrum.intensityType.equals(IntensityType.MID)) {
      throw new IntensityTypeMismatchException(ErrorMessage.INTENSITY_TYPE_MISMATCH.getMessage());
    }
    if (this.isEmpty()) {
      return otherSpectrum;
    }
    if (otherSpectrum.isEmpty()) {
      return this;
    }
    MassSpectrum newSpectrum = new MassSpectrum(IntensityType.ABSOLUTE);
    for (Entry<Double, Double> entry : this.entrySet()) {
      Double thisMass = entry.getKey();
      Double thisFrequency = entry.getValue();
      double thisOldFrequency = newSpectrum.get(thisMass) != null ? newSpectrum.get(thisMass) : 0;
      newSpectrum.put(thisMass, thisOldFrequency + thisFrequency);
    }
    for (Entry<Double, Double> otherEntry : otherSpectrum.entrySet()) {
      Double otherMass = otherEntry.getKey();
      Double otherFrequency = otherEntry.getValue();
      double otherOldFrequency =
          newSpectrum.get(otherMass) != null ? newSpectrum.get(otherMass) : 0;
      newSpectrum.put(otherMass, otherOldFrequency + otherFrequency);
    }
    return newSpectrum;
  }

  /**
   * 
   * @return A copy of this Spectrum with frequencies converted to MIDs (mass isotopomer
   *         distribution).
   */
  public MassSpectrum toMIDFrequency() {
    if (this.intensityType.equals(IntensityType.MID)) {
      return this;
    } else {
      MassSpectrum newSpectrum = new MassSpectrum(IntensityType.MID);
      Double absoluteNumberOfFragments = 0.0;
      for (Entry<Double, Double> entry : this.entrySet()) {
        absoluteNumberOfFragments = absoluteNumberOfFragments + entry.getValue();
      }
      for (Entry<Double, Double> entry : this.entrySet()) {
        Double relativeFrequency = entry.getValue() / absoluteNumberOfFragments;
        newSpectrum.put(entry.getKey(), relativeFrequency);
      }
      return newSpectrum;
    }
  }

  /**
   * 
   * @return A copy of this Spectrum with frequencies converted to relative ones.
   */
  public MassSpectrum toRelativeFrequency() {
    if (this.intensityType.equals(IntensityType.RELATIVE)) {
      return this;
    } else {
      MassSpectrum newSpectrum = new MassSpectrum(IntensityType.RELATIVE);
      Double highestFrequency = getHighestFrequency();
      if (highestFrequency == 0.0) {
        return this;
      }
      for (Entry<Double, Double> entry : this.entrySet()) {
        Double relativeFrequency = (entry.getValue() / highestFrequency) * 100;
        newSpectrum.put(entry.getKey(), relativeFrequency);
      }
      return newSpectrum;
    }
  }

  /**
   * 
   * @param minimalValue
   * @return A copy of this map without masses that have an abundance under the minimalValue
   */
  public MassSpectrum skipLowFrequency(Double minimalValue) {
    MassSpectrum newSpectrum = new MassSpectrum(this.getIntensityType());
    for (Entry<Double, Double> entry : this.entrySet()) {
      if (entry.getValue() >= minimalValue) {
        newSpectrum.put(entry.getKey(), entry.getValue());
      }
    }
    return newSpectrum;
  }

  /**
   * This returns a new map without masses of this map, that are greater than the highest mass
   * parameter.
   * 
   * @param highestMass
   * @return a new map with masses lower than upper bound
   */
  public MassSpectrum skipHighMasses(Double highestMass) {
    MassSpectrum newSpectrum = new MassSpectrum(this.intensityType);
    for (Entry<Double, Double> entry : this.entrySet()) {
      if (entry.getKey() <= highestMass) {
        newSpectrum.put(entry.getKey(), entry.getValue());
      }
    }
    return newSpectrum;
  }

  /**
   * This method rounds all masses to the given precision. If this causes doubled masses they will
   * be saved as one mass with their frequencies added.
   * 
   * @param precision
   * @return a new map with masses rounded to the given precision.
   */
  public MassSpectrum roundMasses(int precision) {
    MassSpectrum newSpectrum = new MassSpectrum(this.intensityType);
    for (Entry<Double, Double> currentEntry : this.entrySet()) {
      Double roundedMass = MathUtils.round(currentEntry.getKey(), precision);
      if (newSpectrum.containsKey(roundedMass)) {
        Double oldFrequency = newSpectrum.get(roundedMass);
        newSpectrum.put(roundedMass, currentEntry.getValue() + oldFrequency);

      } else {
        newSpectrum.put(roundedMass, currentEntry.getValue());
      }
    }
    return newSpectrum;
  }

  /**
   * This method rounds the frequencies of this map to the given precision.
   * 
   * @param precision
   * @return a new map with frequencies rounded to the given precision.
   */
  public MassSpectrum roundFrequencies(int precision) {
    MassSpectrum newSpectrum = new MassSpectrum(this.intensityType);
    for (Entry<Double, Double> currentEntry : this.entrySet()) {
      Double roundedFrequency = MathUtils.round(currentEntry.getValue(), precision);
      newSpectrum.put(currentEntry.getKey(), roundedFrequency);
    }
    return newSpectrum;

  }

  /**
   * 
   * @return a new sorted spectrum
   */
  public MassSpectrum sortDescendingByMass() {
    List<Entry<Double, Double>> entryList = new ArrayList<>(this.entrySet());
    entryList.sort(Entry.comparingByKey());
    MassSpectrum sortedList = new MassSpectrum(this.intensityType);
    for (int index = entryList.size() - 1; index >= 0; index--) {
      Entry<Double, Double> entry = entryList.get(index);
      sortedList.put(entry.getKey(), entry.getValue());
    }
    return sortedList;
  }

  /**
   * 
   * @return a new sorted spectrum
   */
  public MassSpectrum sortDescendingByFrequency() {
    List<Entry<Double, Double>> entryList = new ArrayList<>(this.entrySet());
    entryList.sort(Entry.comparingByValue());
    MassSpectrum sortedList = new MassSpectrum(this.intensityType);
    for (int index = entryList.size() - 1; index >= 0; index--) {
      Entry<Double, Double> entry = entryList.get(index);
      sortedList.put(entry.getKey(), entry.getValue());
    }
    return sortedList;
  }

  /**
   * 
   * @return a new sorted spectrum
   */
  public MassSpectrum sortAscendingByFrequency() {
    List<Entry<Double, Double>> entryList = new ArrayList<>(this.entrySet());
    entryList.sort(Entry.comparingByValue());
    MassSpectrum sortedList = new MassSpectrum(this.intensityType);
    for (Entry<Double, Double> entry : entryList) {
      sortedList.put(entry.getKey(), entry.getValue());
    }
    return sortedList;
  }

  /**
   * 
   * @return a new sorted spectrum
   */
  public MassSpectrum sortAscendingByMass() {
    List<Entry<Double, Double>> entryList = new ArrayList<>(this.entrySet());
    entryList.sort(Entry.comparingByKey());
    MassSpectrum sortedList = new MassSpectrum(this.intensityType);
    for (Entry<Double, Double> entry : entryList) {
      sortedList.put(entry.getKey(), entry.getValue());
    }
    return sortedList;
  }

  public IntensityType getIntensityType() {
    return intensityType;
  }

  /**
   * @param elements elements that may induce a shift
   * @return the MassShiftDataSet corresponding to the isotope pattern represented by this
   *         MassSpectrum, under the assumption that each mass gap between two consecutive peaks is
   *         induced by only up to one isotope.
   * 
   */
  public MassShiftDataSet analyseMassShifts(ElementList elements) {
    MassShiftDataSet massShiftData = new MassShiftDataSet();
    ArrayList<MassShiftList> massShiftLists = new ArrayList<>();
    massShiftLists.add(new MassShiftList(new MassShift(0, 0, 0.0)));
    List<Entry<Double, Double>> massEntries = new ArrayList<>(this.entrySet());
    int numberOfMassEntries = massEntries.size();
    LOGGER.debug("Analyse total peaks", this.size());
    for (int leadingIndex = 1; leadingIndex < numberOfMassEntries; leadingIndex++) {
      MassShiftList massShiftList = new MassShiftList();
      if (leadingIndex > 1) {
        massShiftList.addAll(massShiftLists.subList(1, leadingIndex));
      }
      LOGGER.debug("Analyse peak number", leadingIndex);
      int peak1 = leadingIndex - 1;
      int peak2 = leadingIndex;
      Double shiftValue =
          massEntries.get(leadingIndex).getKey() - massEntries.get(leadingIndex - 1).getKey();
      Double roundedShiftValue = MathUtils.round(shiftValue, 6);
      /*
       * as long as the roundedShiftValue cannot be connected with an isotope check if an peak
       * overleaping mass shift should be considered. That means if there is no isotope that can
       * induce a shift from p_2 to p_3 there may be an isotope that induces a shift from p_0 to
       * p_3.
       */
      int peakBackShift = 2;
      boolean backShiftPossible = leadingIndex - peakBackShift >= 0;
      boolean isotopeNotIdentified =
          Isotope.approximatelyByMassShiftValueAndAvailableElements(roundedShiftValue, elements)
              .equals(new IsotopeList(Isotope.UNDEFINED));
      while (isotopeNotIdentified && backShiftPossible) {
        shiftValue = massEntries.get(leadingIndex).getKey()
            - massEntries.get(leadingIndex - peakBackShift).getKey();
        roundedShiftValue = MathUtils.round(shiftValue, 6);
        peak1 = leadingIndex - peakBackShift;
        isotopeNotIdentified =
            Isotope.approximatelyByMassShiftValueAndAvailableElements(roundedShiftValue, elements)
                .equals(new IsotopeList(Isotope.UNDEFINED));
        peakBackShift++;
        backShiftPossible = leadingIndex - peakBackShift >= 0;
      }
      massShiftList.add(new MassShift(peak1, peak2, roundedShiftValue));
      massShiftList = massShiftList.findConnectedSubSequence(0, leadingIndex);
      massShiftLists.add(massShiftList);
    }
    for (MassShiftList massShiftList : massShiftLists) {
      IsotopeListList correspondingIsotopes = massShiftList.correspondingIsotopes();
      massShiftData.put(massShiftList, correspondingIsotopes);
    }
    return massShiftData;
  }

  @Override
  public String toString() {
    DataTable dataTable = new DataTable("Mass", "Frequency");
    dataTable.addColumn(this);
    return dataTable.toString("NA", true);
  }

  public List<Entry<Double, Double>> toEntryList() {
    List<Entry<Double, Double>> entryList = new ArrayList<>(this.entrySet());
    return entryList;
  }

  /**
   * Creates a MassSpectrum with frequencies absolute to the number of elements in the
   * fragmentMasses list.
   * 
   * @param fragmentMasses
   * @return A MassSpectrum with ABSOLUTE frequencies.
   */
  public static MassSpectrum createSpectrumFromMasses(ArrayList<Double> fragmentMasses) {
    MassSpectrum spectrum = new MassSpectrum(IntensityType.ABSOLUTE);
    for (Double mass : fragmentMasses) {
      if (spectrum.get(mass) == null /* count each mass only once */) {
        int index = fragmentMasses.indexOf(mass);
        int massCount = 1;
        for (Double otherMass : fragmentMasses.subList(index + 1, fragmentMasses.size())) {
          if (mass.equals(otherMass)) {
            massCount++;
          }
        }
        spectrum.put(mass, Double.valueOf(massCount));
      }
    }
    return spectrum;
  }

  /**
   * 
   * @param charge, charge of the molecule
   * @return This spectrum where the masses are reduced by charge * electron mass.
   */
  public MassSpectrum adjustToCharge(int charge) {
    if (charge == 0) {
      return this;
    }
    MassSpectrum adjusted = new MassSpectrum(this.getIntensityType());
    for (Entry<Double, Double> entry : this.entrySet()) {
      adjusted.put(entry.getKey() - charge * NaturalConstants.ELECTRON_MASS.getValue(),
          entry.getValue());
    }
    return adjusted;
  }

  public Double getHighestMass() {
    List<Entry<Double, Double>> entryList = new ArrayList<>(this.entrySet());
    entryList.sort(Entry.comparingByKey());
    return entryList.get(entryList.size() - 1).getKey();
  }

  public Double getLowestMass() {
    List<Entry<Double, Double>> entryList = new ArrayList<>(this.entrySet());
    entryList.sort(Entry.comparingByKey());
    return entryList.get(0).getKey();
  }

  public Double getHighestFrequency() {
    if (this.size() == 0) {
      return 0.0;
    }
    List<Entry<Double, Double>> entryList = new ArrayList<>(this.entrySet());
    entryList.sort(Entry.comparingByValue());
    return entryList.get(entryList.size() - 1).getValue();
  }

  public Double getLowestFrequency() {
    List<Entry<Double, Double>> entryList = new ArrayList<>(this.entrySet());
    entryList.sort(Entry.comparingByValue());
    return entryList.get(0).getValue();
  }

  public Double getMostAbundandMass() {
    List<Entry<Double, Double>> entryList = new ArrayList<>(this.entrySet());
    entryList.sort(Entry.comparingByValue());
    return entryList.get(entryList.size() - 1).getKey();
  }

}
