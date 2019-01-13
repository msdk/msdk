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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.isotopes.tracing.data.constants.Isotope;
import io.github.msdk.isotopes.tracing.util.MathUtils;

/**
 * Each peak in an isotope mass pattern can be described by a sequence of MassShifts from where the
 * peak can be reached starting in the p0 peak. This class can be used for such identifications.
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
@SuppressWarnings("serial")
public class MassShiftList extends ArrayList<MassShift> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MassShiftList.class);

  public MassShiftList() {
    super();
  }

  public MassShiftList(MassShift... massShifts) {
    for (int i = 0; i < massShifts.length; i++) {
      this.add(massShifts[i]);
    }
  }

  /**
   * 
   * @return A list of isotopes that may induce the given mass shifts. These may include
   *         Isotope.NONE for a shiftValue of 0.0 or Isotope.UNDEFINED for a none identifiable
   *         shiftValue.
   */
  public IsotopeListList correspondingIsotopes() {
    IsotopeListList isotopeListList = new IsotopeListList();
    for (MassShift massShift : this) {
      IsotopeList isotopes = Isotope.approximatelyByMassShiftValue(massShift.getShiftValue());
      if (isotopes.equals(new IsotopeList(Isotope.UNDEFINED)) && massShift.getShiftValue() > 2) {
        /*
         * assume this shift was induced by more than one incorporated carbon
         */
        isotopes = new IsotopeList();
        int numberOfCarbons = (int) (massShift.getShiftValue() / Isotope.C_13.getMassShiftValue());
        for (int i = 0; i < numberOfCarbons; i++) {
          isotopes.add(Isotope.C_13);
        }
        Double remainingShift =
            massShift.getShiftValue() - numberOfCarbons * Isotope.C_13.getMassShiftValue();
        if (remainingShift > 0) {
          isotopes.addAll(Isotope.approximatelyByMassShiftValue(remainingShift));
        }
      }
      isotopeListList.add(isotopes);
    }
    return isotopeListList;
  }

  /**
   * If this list is given as sequence of mass shifts MS_0, ..., MS_n, then this method returns a
   * connected subsequence MS_{k_1}, ..., MS_{k_l} of MS, from startPeak to endPeak. If there a more
   * than one possible sequences the first will be returned if all sequences lead to the same set on
   * isotopes. If they lead to different set of isotopes a NotUniqueSeqenceException will be thrown.
   * 
   * @param startPeak
   * @param endPeak
   * @return A connected subsequence from startPeak to endPeak. If there is no connected
   *         subsequence, then an empty list will be returned.
   */
  public MassShiftList findConnectedSubSequence(int startPeak, int endPeak) {
    MassShiftList reducedList = this.reduce(startPeak, endPeak);
    LOGGER.debug("Checking this list of mass shifts for a connected sequence:\n" + reducedList);
    if (reducedList.isConnected(startPeak, endPeak)) {
      return reducedList;
    }
    ArrayList<Integer> indexSet = reducedList.indexSet();
    int n = indexSet.size();
    for (int k = n; k >= 1; k--) {
      LOGGER.debug("Checking all " + k + "- elementary subsets...");
      StringBuilder binaryBuilder = new StringBuilder();
      for (int i = 0; i < n - k; i++) {
        binaryBuilder.append(MathUtils.OFF);
      }
      for (int i = n - k; i < n; i++) {
        binaryBuilder.append(MathUtils.ON);
      }
      String binarySubset = binaryBuilder.toString();
      String bit;
      for (int i = 1; i < MathUtils.binom(n, k); i++) {
        ArrayList<Integer> realIndexSubset = new ArrayList<>();
        for (int index = 0; index < binarySubset.length(); index++) {
          bit = String.valueOf(binarySubset.charAt(index));
          if (bit.equals(MathUtils.ON)) {
            realIndexSubset.add(indexSet.get(index));
          }
        }
        MassShiftList potentialClosedMassShiftList = reducedList.subSublist(realIndexSubset);
        if (potentialClosedMassShiftList.isConnected(startPeak, endPeak)) {
          return potentialClosedMassShiftList;
        }
        binarySubset = MathUtils.nextPermutation(binarySubset);
      }
    }
    LOGGER.warn("Found no possible MassShiftList. Returned an empty list.");
    return new MassShiftList();
  }

  /**
   * @param startPeak
   * @param endPeak
   * @return This list reduced by all mass shifts that start before the startPeak or end after the
   *         endPeak.
   */
  public MassShiftList reduce(int startPeak, int endPeak) {
    MassShiftList reducedList = new MassShiftList();
    for (MassShift massShift : this) {
      if (massShift.getPeak1() >= startPeak && massShift.getPeak2() <= endPeak) {
        reducedList.add(massShift);
      }
    }
    return reducedList;
  }

  /**
   * 
   * @return The index set of this list.
   */
  public ArrayList<Integer> indexSet() {
    ArrayList<Integer> indexSet = new ArrayList<>();
    for (int index = 0; index < this.size(); index++) {
      indexSet.add(index);
    }
    return indexSet;
  }

  /**
   * This method does not check if the indexSubset is really a subset of the index set of this list.
   * 
   * @param indexSubset
   * @return A sublist of this list corresponding to the indexSubset.
   */
  public MassShiftList subSublist(ArrayList<Integer> indexSubset) {
    MassShiftList subList = new MassShiftList();
    for (Integer index : indexSubset) {
      subList.add(this.get(index));
    }
    return subList;
  }


  /**
   * A sequence MS_0,..., MS_n of mass shifts with corresponding peaks peak1_0, peak2_0, ...,
   * peak1_n, peak2_n is connected from startPeak to endPeak if for all i in {0,...,n-1}: peak2_i =
   * peak1_{i+1} and peak1_0 == startPeak, peak2_n == endPeak. That means for two consecutive mass
   * shifts MS_i and MS_{i+1} the end peak of MS_i is the starting peak of MS_{i+1} and the start
   * peak of MS_0 is given by startPeak and the end peak of MS_n is given by endPeak.
   * 
   * @param startPeak
   * @param endPeak
   * @return true if this sequence of mass shifts starts in startPeak, ends in endPeak and is
   *         connected, otherwise false
   */
  private boolean isConnected(int startPeak, int endPeak) {
    if (this.isEmpty()) {
      return false;
    }
    int firstPeak = this.get(0).getPeak1();
    int lastPeak = this.get(this.size() - 1).getPeak2();
    if (firstPeak != startPeak) {
      return false;
    }
    if (lastPeak != endPeak) {
      return false;
    }
    if (!this.isConnected()) {
      return false;
    }
    return true;
  }

  /**
   * A sequence MS_0,..., MS_n of mass shifts with corresponding peaks peak1_0, peak2_0, ...,
   * peak1_n, peak2_n is connected if for all i in {0,...,n-1}: peak2_i = peak1_{i+1}. That means
   * for two consecutive mass shifts MS_i and MS_{i+1} the end peak of MS_i is the starting peak of
   * MS_{i+1}.
   * 
   * @return true if this sequence of mass shifts is connected, otherwise false
   */
  public boolean isConnected() {
    for (int index = 0; index < this.size() - 1; index++) {
      MassShift thisShift = this.get(index);
      MassShift nextShift = this.get(index + 1);
      if (!nextShift.follows(thisShift)) {
        LOGGER.debug(this.toString() + " is not connected.");
        return false;
      }
    }
    LOGGER.debug(this.toString() + " is connected.");
    return true;
  }

  private static final String LIST_SEPARATOR = "_";

  /**
   * @return the string representation of the MassShifts concatenated by "_". E.g.
   *         0.0[0-0]_1.0033[0-1]_2.0[1-2]_0.9996[2-3]
   */
  @Override
  public String toString() {
    String inBrackets = super.toString().replaceAll(",", LIST_SEPARATOR).replaceAll(" ", "");
    return inBrackets.substring(1, inBrackets.length() - 1);
  }

  public static MassShiftList fromString(String listString) {
    String[] massShifts = listString.split(LIST_SEPARATOR);
    MassShiftList massShiftList = new MassShiftList();
    for (int index = 0; index < massShifts.length; index++) {
      massShiftList.add(MassShift.fromString(massShifts[index]));
    }
    return massShiftList;
  }

  /**
   * Two MassShiftLists may refer to different peak sequences (p_k,p_k+1) and (p'_k,p'_k+1). If
   * these sequences lead to the same set of isotopes we will regard the two MassShiftLists as
   * equal. 2.0067[0-1]_1.0033[1-2]_1.0034[2-4]_0.9996[4-5]
   * 2.0067[0-1]_1.0033[1-2]_0.9996[2-3]_1.0034[3-5]
   * 
   * @return true if the peak sequences of both MassShiftLists lead to the same set of isotopes,
   *         otherwise false.
   */
  public boolean equalsUpToPermutationOfIsotopes(MassShiftList other) {
    if (other == null) {
      return false;
    }
    if (this.size() != other.size()) {
      return false;
    }
    IsotopeList thisIsotopes = new IsotopeList();
    IsotopeList otherIsotopes = new IsotopeList();
    for (int index = 0; index < this.size(); index++) {
      thisIsotopes.addAll(Isotope.approximatelyByMassShiftValue(this.get(index).getShiftValue()));
      otherIsotopes.addAll(Isotope.approximatelyByMassShiftValue(other.get(index).getShiftValue()));
    }
    thisIsotopes.sort(new IsotopeMassComparator());
    otherIsotopes.sort(new IsotopeMassComparator());
    if (!thisIsotopes.equals(otherIsotopes)) {
      return false;
    }
    return true;
  }

  public void addAll(List<MassShiftList> lists) {
    if (lists == null) {
      return;
    }
    for (MassShiftList massShiftList : lists) {
      for (MassShift massShift : massShiftList) {
        if (!contains(massShift)) {
          add(massShift);
        }
      }
    }
  }
}
