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

package io.github.msdk.isotopes.tracing.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.github.msdk.isotopes.tracing.data.ElementFormula;
import io.github.msdk.isotopes.tracing.data.IsotopeFormula;
import io.github.msdk.isotopes.tracing.data.IsotopeList;
import io.github.msdk.isotopes.tracing.data.Partition;
import io.github.msdk.isotopes.tracing.data.constants.Element;
import io.github.msdk.isotopes.tracing.data.constants.Isotope;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class MathUtils {

  public static final String ON = "1";
  public static final String OFF = "0";

  /**
   * @param binary, a String representation of a sequence of bits
   * @return a String representation of the next permutation of the input sequence of bits. The
   *         consecutive application of this method to the string "0011" will produce the following
   *         output:
   * 
   *         0011 0101 0110 1001 1010 1100
   */
  public static String nextPermutation(String binary) {
    /*
     * The algorithm uses the following principle: If the last bit in this sequence is on go to the
     * last off bit and switch it with the following on bit. If the last bit in this sequence is off
     * go to the next to last off bit, switch it with the next on bit and translate all following on
     * bits to the end of this sequence.
     */
    if (binary.lastIndexOf(ON) == binary.length() - 1) {
      return StringUtils.switchLastOffBit(binary);
    } else {
      return StringUtils.switchNextToLastOffBitAndShiftRemainingOnsToTheRight(binary);
    }
  }

  /**
   * This produces a list of all permutations of a binary consisting of n bits, where exactly k ON
   * bits are included. This can be interpreted as a list of binary representations of all
   * k-elementary subsets on an n-elementary set.
   * 
   * @param n, number of elements in the whole set.
   * @param k, number of elements in the subsets.
   * @return a list of binary representations of all k-elementary subsets on an n-elementary set.
   *         e.g permutations(4,2) will return the following list: 0011 0101 0110 1001 1010 1100
   * 
   */
  public static ArrayList<String> permutations(int n, int k) {
    ArrayList<String> permutations = new ArrayList<>();
    StringBuilder binaryBuilder = new StringBuilder();
    for (int i = 0; i < n - k; i++) {
      binaryBuilder.append(OFF);
    }
    for (int i = n - k; i < n; i++) {
      binaryBuilder.append(ON);
    }
    String binary = binaryBuilder.toString();
    for (int i = 1; i < binom(n, k); i++) {
      permutations.add(binary);
      binary = nextPermutation(binary);
    }
    permutations.add(binary);
    return permutations;
  }

  /**
   * 
   * @param n
   * @param k
   * @return the binomial coefficient n over k.
   */
  public static int binom(int n, int k) {
    int binom = 1;
    for (int i = 1; i <= k; i++) {
      binom = binom * (n + 1 - i) / i;
    }
    return binom;
  }

  /**
   * The method does not check if the set contains duplicates. All elements in the set will be
   * considered as different.
   * 
   * @param set, represented as an ArrayList of Integer
   * @return The set of all subsets of set, represented as an ArrayList.
   */
  public static ArrayList<ArrayList<Integer>> powerSet(ArrayList<Integer> set) {
    int n = set.size();
    ArrayList<ArrayList<String>> binarySubsetsList = new ArrayList<>();
    for (int k = 1; k <= n; k++) {
      binarySubsetsList.add(permutations(n, k));
    }
    ArrayList<ArrayList<Integer>> subsets = new ArrayList<>();
    for (ArrayList<String> binarySubsets : binarySubsetsList) {
      ArrayList<ArrayList<Integer>> realSubsets = subsetsFromBinaries(binarySubsets, set);
      subsets.addAll(realSubsets);
    }
    return subsets;
  }

  /**
   * 
   * @param binarySubsetsList
   * @param set
   * @return a list of subsets from set that are represented by the binarySubsets in the
   *         binarySubsetsList.
   */
  private static ArrayList<ArrayList<Integer>> subsetsFromBinaries(
      ArrayList<String> binarySubsetsList, ArrayList<Integer> set) {
    ArrayList<ArrayList<Integer>> subsets = new ArrayList<>();
    String bit;
    for (String binarySubset : binarySubsetsList) {
      ArrayList<Integer> subset = new ArrayList<>();
      for (int index = 0; index < binarySubset.length(); index++) {
        bit = String.valueOf(binarySubset.charAt(index));
        if (bit.equals(ON)) {
          subset.add(set.get(index));
        }
      }
      subsets.add(subset);
    }
    return subsets;
  }

  /**
   * Rounds the given value to the defined precision
   * 
   * @param value
   * @param precision
   * @return the value rounded to the given precision
   */
  public static Double round(Double value, int precision) {
    Double factor = Math.pow(10, precision);
    Double rounded = (double) ((Math.round(value * factor)) / factor);
    return rounded;
  }


  /**
   * 
   * @param lists
   * @return the size of the list with the maximal size.
   */
  @SuppressWarnings("unchecked")
  public static <T> int maxSize(List<T>... lists) {
    int max = 0;
    for (List<T> list : lists) {
      max = Math.max(max, list.size());
    }
    return max;
  }

  /**
   * 
   * @param <K>
   * @param lists
   * @return the size of the map with the maximal size.
   */
  @SafeVarargs
  public static <T, K> int maxSize(Map<K, T>... maps) {
    int max = 0;
    for (Map<K, T> map : maps) {
      max = Math.max(max, map.size());
    }
    return max;
  }

  /**
   * checks if the difference between a and b is not bigger than the allowedError.
   * 
   * @param a
   * @param b
   * @param allowedError
   * @return true if the difference between a and b is not bigger than the allowedError, otherwise
   *         false.
   */
  public static boolean approximatelyEquals(Double a, Double b, Double allowedError) {
    if (Math.abs(a - b) <= allowedError) {
      return true;
    }
    return false;
  }

  /**
   * Each {@link Partition} corresponds to a combination of isotopes that creates a molecule
   * consisting of n elements E (with respect to the isotopes) where n is equals to the
   * numberOfElements. This method creates a list, where each entry represents the abundance of the
   * combination of isotopes in the corresponding allCombinations parameter.
   * 
   * @param allCombinations all possible {@link Partition}s (including their permutations) of the
   *        numberOfElements, where the length of each {@link Partition} is equal to the size of the
   *        {@link IsotopeList}
   * @param isotopes
   * @param numberOfElements
   * @param treshFaktor
   * @return list of abundances of allCombinatins
   */
  public static ArrayList<Double> calculateAbundancies(ArrayList<Partition> allCombinations,
      IsotopeList isotopes, Integer numberOfElements, Double treshFaktor) {
    ArrayList<Double> logFakulty = new ArrayList<>();
    logFakulty.add(0.0);
    for (int i = 1; i <= numberOfElements; i++) {
      logFakulty.add(logFakulty.get(i - 1) + Math.log(i));
    }
    ArrayList<Double> abundancies = new ArrayList<>();
    // isotopes.sort(new IsotopeAbundancyComparator());
    Double treshhold =
        treshFaktor * Math.pow(isotopes.get(isotopes.size() - 1).getAbundance(), numberOfElements);
    for (Partition partition : allCombinations) {
      Double abundancy = 0.0;
      Integer numerator = 0;
      Double denominator = 0.0;
      Double powers = 0.0;
      for (int index = 0; index < partition.size(); index++) {
        Integer summand = partition.get(index);
        numerator = numerator + summand;
        denominator = denominator + logFakulty.get(summand);
        powers = powers + Math.log(Math.pow(isotopes.get(index).getAbundance(), summand));
      }
      abundancy = logFakulty.get(numerator) - denominator + powers;
      abundancy = Math.exp(abundancy);
      if (abundancy > treshhold) {
        abundancies.add(abundancy);
      }
    }
    return abundancies;
  }

  public static Double sum(ArrayList<Double> summands) {
    Double sum = 0.0;
    for (Double summand : summands) {
      sum = sum + summand;
    }
    return sum;
  }

  /**
   * Calculates the natural abundance of an isotopologue I using the concept of partitions. If we
   * split the isotopologue into smaller isotopologues I_0, ... , I_n where each I_k for k in
   * {0,...,n} only consists of isotopes corresponding to the same element E, then the abundance of
   * I is calculated as the product of the abundances of all I_k.
   * 
   * @param isotopologue
   * @return The natural abundance of the isotopologue.
   */
  public static Double naturalAbundance(IsotopeFormula isotopologue) {
    ElementFormula elementFormula = isotopologue.toElementFormula();
    Double totalAbundance = 1.0;
    for (Entry<Element, Integer> element : elementFormula.entrySet()) {
      IsotopeFormula elementIsotopesInIsotopologue = new IsotopeFormula();
      for (Entry<Isotope, Integer> isotopeEntry : isotopologue.entrySet()) {
        if (isotopeEntry.getKey().getElement().equals(element.getKey())) {
          elementIsotopesInIsotopologue.put(isotopeEntry.getKey(), isotopeEntry.getValue());
        }
      }
      Partition isotopeRatiosForCurrentElement = new Partition();
      IsotopeList isotopesForCurrentElemnt = new IsotopeList();
      for (Entry<Isotope, Integer> isotopeEntry : elementIsotopesInIsotopologue.entrySet()) {
        isotopeRatiosForCurrentElement.add(isotopeEntry.getValue());
        isotopesForCurrentElemnt.add(isotopeEntry.getKey());
      }
      ArrayList<Partition> partitions = new ArrayList<>();
      partitions.add(isotopeRatiosForCurrentElement);
      totalAbundance = totalAbundance * MathUtils
          .calculateAbundancies(partitions, isotopesForCurrentElemnt, element.getValue(), 0.0)
          .get(0);
    }
    return totalAbundance;
  }
}
