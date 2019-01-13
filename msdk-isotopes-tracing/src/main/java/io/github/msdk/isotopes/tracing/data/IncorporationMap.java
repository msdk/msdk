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

import io.github.msdk.isotopes.tracing.data.constants.Element;
import io.github.msdk.isotopes.tracing.data.constants.Isotope;
import io.github.msdk.isotopes.tracing.util.MathUtils;


@SuppressWarnings("serial")
/**
 * Imagine to have a mass spectrum corresponding to a fragment measured in a isotope labeling
 * experiment, where all isotopologues are identified. Let t_0,...,t_n be the used tracers,
 * max_0,...,max_n the corresponding maximal numbers of elements that can be marked in the fragment
 * by a tracer. Each map m: t_k --> v_k, with v_k in {0,...,max_k} and k in {0,...,n} represents a
 * possible tracer incorporation. An IncorporationMap is a map of each tracer incorporation to the
 * sum of the intensities of all isotopologues that represent such a tracer incorporation.
 * 
 * For example, if you used 13C and 15N as tracer an IncorporationMap can be seen as a matrix
 * CN=[(cn)_ij], with i in {0,...,C_max} and j in {0,...,N_max}, where the entries (cn)_ij represent
 * the sum of the intensities of all isotopologues that contain i 13C and j 15N.
 * 
 * The correction algorithms are based on: A Computational Framework for High-Throughput Isotopic
 * Natural Abundance Correction of Omics-Level Ultra-High Resolution FT-MS Datasets. Metabolites.
 * 2013;3(4):853–866.
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class IncorporationMap extends LinkedHashMap<IsotopeFormula, Double> {

  private static final Logger LOG = LoggerFactory.getLogger(IncorporationMap.class);
  Isotope[] incorporatedIsotopes;

  public IncorporationMap() {

  }

  /**
   * Create an uncorrected {@link IncorporationMap} from spectrum, massShiftDataset and tracer
   * 
   * @param spectrum
   * @param massShiftDataset
   * @param tracer the tracer used in the experiment
   */
  public IncorporationMap(MassSpectrum spectrum, MassShiftDataSet massShiftDataset,
      IsotopeList tracer) {
    List<Entry<Double, Double>> spectrumEntries = spectrum.toEntryList();
    List<Entry<MassShiftList, IsotopeListList>> shiftEntries = massShiftDataset.toEntryList();
    for (int index = 0; index < spectrumEntries.size(); index++) {
      Double intensity = spectrumEntries.get(index).getValue();
      IsotopeFormula isotopeFormula = shiftEntries.get(index).getValue().countIsotopes(tracer);
      if (this.get(isotopeFormula) == null) {
        this.put(isotopeFormula, intensity);
      } else {
        this.put(isotopeFormula, this.get(isotopeFormula) + intensity);
      }
    }
  }

  /**
   * An IncorporationMap is a map of each tracer incorporation to the sum of the intensities of all
   * isotopologues that represent such a tracer incorporation.
   * 
   * @param isotopeFormulas, represents the tracer incorporation. For example if 15N and 13C have
   *        been used as tracer within an experiment then isotopologues that contain one 15N and two
   *        13C are described by {15N:1, 13C:2}
   * @param intensities, array of the sums of the intensities of all isotopologues that represent a
   *        certain tracer incorporation
   */
  public IncorporationMap(IsotopeFormula[] isotopeFormulas, Double[] intensities) {
    if (isotopeFormulas.length != intensities.length) {
      throw new IndexOutOfBoundsException(
          "Isotope formulas and intensities must have the same size!");
    }
    for (int i = 0; i < isotopeFormulas.length; i++) {
      this.put(isotopeFormulas[i], intensities[i]);
    }
  }

  /**
   * Correct this {@link IncorporationMap} for natural abundance of the tracers corresponding to the
   * elements in maxTracerFormula.
   * 
   * @param maxTracerFormula contains the info about used tracer and the maximal number of those
   *        elements in the fragment
   * @return a new {@link IncorporationMap} corrected for the natural abundance of the tracers,
   *         corresponding to the elements in maxElementsFormula.
   */
  public IncorporationMap correctForNaturalAbundance(ElementFormula maxTracerFormula) {
    Double targetIntensitySum = this.sumIntensities();
    IncorporationMap naSubstractedIncorporation = this.substractNaturalAbundance(maxTracerFormula);
    Double substractedIntensitySum = naSubstractedIncorporation.sumIntensities();
    IncorporationMap naAddedIncorporation = naSubstractedIncorporation
        .addNaturalAbundance(maxTracerFormula, targetIntensitySum / substractedIntensitySum);
    targetIntensitySum = this.replaceNegatives(naAddedIncorporation).sumIntensities();
    Double currentIntensitySumDifference = this.sumDifference(naAddedIncorporation);
    Double lastIntensitySumDifference = currentIntensitySumDifference * 2;
    int iterations = 0;
    while (currentIntensitySumDifference < lastIntensitySumDifference) {
      lastIntensitySumDifference = Double.valueOf(currentIntensitySumDifference);
      IncorporationMap nextIncorporationMap = this.replaceNegatives(naAddedIncorporation);
      Double nextIntensitySum = nextIncorporationMap.sumIntensities();
      naSubstractedIncorporation = nextIncorporationMap.substractNaturalAbundance(maxTracerFormula,
          targetIntensitySum / nextIntensitySum);
      substractedIntensitySum = naSubstractedIncorporation.sumIntensities();
      naAddedIncorporation = naSubstractedIncorporation.addNaturalAbundance(maxTracerFormula,
          targetIntensitySum / substractedIntensitySum);
      targetIntensitySum = this.replaceNegatives(naAddedIncorporation).sumIntensities();
      currentIntensitySumDifference = this.sumDifference(naAddedIncorporation);
      iterations++;
    }
    LOG.debug("iterations" + iterations);
    LOG.debug("naAddedIncorporation" + naAddedIncorporation.asTable());
    return naSubstractedIncorporation;
  }

  /**
   * Subtract the natural isotopologue abundance corresponding to the tracers in maxTracerFormula
   * from this {@link IncorporationMap}
   * 
   * @param maxTracerFormula contains the info about used tracer and the maximal number of those
   *        elements in the fragment
   * @param scaleFactor, for normalization
   * @return a new {@link IncorporationMap} with substracted natural abundance of the tracers,
   *         corresponding to the elements in maxElementsFormula. Normalized according to
   *         scaleFactor
   */
  private IncorporationMap substractNaturalAbundance(ElementFormula maxTracerFormula,
      double scaleFactor) {
    return this.scale(scaleFactor).substractNaturalAbundance(maxTracerFormula);
  }

  /**
   * 
   * @param naAddedIncorporation
   * @return the sum of all the differences of all corresponding entries in this
   *         {@link IncorporationMap} and naAddedIncorporation
   */
  private Double sumDifference(IncorporationMap naAddedIncorporation) {
    Double sum = 0.0;
    List<Entry<IsotopeFormula, Double>> thisEntries = this.toEntryList();
    List<Entry<IsotopeFormula, Double>> addedEntries = naAddedIncorporation.toEntryList();
    for (int index = 0; index < thisEntries.size(); index++) {
      sum = sum + Math.abs(thisEntries.get(index).getValue() - addedEntries.get(index).getValue());
    }
    return sum;
  }

  /**
   * Determines all negative entries in this {@link IncorporationMap} and returns a map like this
   * {@link IncorporationMap} where all negative entries are replaced by the corresponding entries
   * in naAddedIncorporation.
   * 
   * @param naAddedIncorporation
   * @return a map like this {@link IncorporationMap} where all negative entries are replaced by the
   *         corresponding entries in naAddedIncorporation
   */
  private IncorporationMap replaceNegatives(IncorporationMap naAddedIncorporation) {
    IncorporationMap replaced = this.copy();
    List<Entry<IsotopeFormula, Double>> thisEntries = this.toEntryList();
    List<Entry<IsotopeFormula, Double>> addedEntries = naAddedIncorporation.toEntryList();
    for (int index = 0; index < thisEntries.size(); index++) {
      if (thisEntries.get(index).getValue() <= 0) {
        replaced.put(thisEntries.get(index).getKey(), addedEntries.get(index).getValue());
      }
    }
    return replaced;
  }

  /**
   * Add the natural isotopologue abundance corresponding to the tracers in maxTracerFormula from
   * this {@link IncorporationMap}
   * 
   * @param maxTracerFormula contains the info about used tracer and the maximal number of those
   *        elements in the fragment
   * @param scaleFactor, for normalization
   * @return a new {@link IncorporationMap} with added natural abundance of the tracers,
   *         corresponding to the elements in maxElementsFormula. Normalized according to
   *         scaleFactor
   */
  private IncorporationMap addNaturalAbundance(ElementFormula maxTracerFormula,
      double scaleFactor) {
    return this.scale(scaleFactor).addNaturalAbundance(maxTracerFormula);
  }

  /**
   * Add the natural isotopologue abundance corresponding to the tracers in maxTracerFormula from
   * this {@link IncorporationMap}
   * 
   * @param maxTracerFormula contains the info about used tracer and the maximal number of those
   *        elements in the fragment
   * @return a new {@link IncorporationMap} with added natural abundance of the tracers,
   *         corresponding to the elements in maxElementsFormula.
   */
  private IncorporationMap addNaturalAbundance(ElementFormula maxElements) {
    List<Entry<IsotopeFormula, Double>> thisMapEntries = this.toEntryList();
    IncorporationMap naAddedIncorporation = new IncorporationMap();
    for (int index = thisMapEntries.size() - 1; index >= 0; index--) {
      IsotopeFormula currentCorrectionIndex = thisMapEntries.get(index).getKey();
      Double newValue = thisMapEntries.get(index).getValue();
      newValue = newValue * productOfCorrectionSums(currentCorrectionIndex, maxElements);
      for (int i = 0; i < index; i++) {
        IsotopeFormula notYetCorrectedIndex = thisMapEntries.get(i).getKey();
        if (notYetCorrectedIndex.mattersForCorrectionOf(currentCorrectionIndex)) {
          Double notYetCorrectedIntensity = thisMapEntries.get(i).getValue();
          newValue = newValue
              + notYetCorrectedIntensity * incorporationProbabilityProduct(currentCorrectionIndex,
                  notYetCorrectedIndex, maxElements);
        }
      }
      naAddedIncorporation.put(0, currentCorrectionIndex, newValue);
    }
    return naAddedIncorporation;
  }

  /**
   * @param scaleFactor
   * @return a copy of this map normalized by the scaleFactor
   */
  private IncorporationMap scale(double scaleFactor) {
    IncorporationMap scaled = new IncorporationMap();
    for (Entry<IsotopeFormula, Double> entry : this.entrySet()) {
      scaled.put(entry.getKey(), entry.getValue() * scaleFactor);
    }
    return scaled;
  }

  /**
   * 
   * @return a copy of this map
   */
  private IncorporationMap copy() {
    IncorporationMap copy = new IncorporationMap();
    for (Entry<IsotopeFormula, Double> entry : this.entrySet()) {
      copy.put(entry.getKey(), entry.getValue());
    }
    return copy;
  }

  /**
   * Subtract the natural isotopologue abundance corresponding to the tracers in maxTracerFormula
   * from this {@link IncorporationMap}
   * 
   * @param maxTracerFormula contains the info about used tracer and the maximal number of those
   *        elements in the fragment
   * @return a new {@link IncorporationMap} with substracted natural abundance of the tracers,
   *         corresponding to the elements in maxElementsFormula.
   */
  private IncorporationMap substractNaturalAbundance(ElementFormula maxTracerFormula) {
    // assuming this list is ordered by number of incorporated isotopoes
    List<Entry<IsotopeFormula, Double>> thisMapEntries = this.toEntryList();
    IncorporationMap naSubstractedIncorporation = new IncorporationMap();
    for (int index = 0; index < thisMapEntries.size(); index++) {
      IsotopeFormula currentCorrectionIndex = thisMapEntries.get(index).getKey();
      Double newValue = thisMapEntries.get(index).getValue();
      for (int i = 0; i < index; i++) {
        List<Entry<IsotopeFormula, Double>> alreadyCorrectedMapEntries =
            naSubstractedIncorporation.toEntryList();
        IsotopeFormula alreadyCorrectedIndex = alreadyCorrectedMapEntries.get(i).getKey();
        if (alreadyCorrectedIndex.mattersForCorrectionOf(currentCorrectionIndex)) {
          Double alreadyCorrectedIntensity = alreadyCorrectedMapEntries.get(i).getValue();
          newValue = newValue
              - alreadyCorrectedIntensity * incorporationProbabilityProduct(currentCorrectionIndex,
                  alreadyCorrectedIndex, maxTracerFormula);
        }
      }
      newValue = newValue / productOfCorrectionSums(currentCorrectionIndex, maxTracerFormula);
      if (newValue < 0) {
        newValue = 0.0;
      }
      naSubstractedIncorporation.put(currentCorrectionIndex, newValue);
    }
    return naSubstractedIncorporation;
  }

  /**
   * 
   * @return the sum of all intensities of this map
   */
  private Double sumIntensities() {
    Double sum = 0.0;
    for (Entry<IsotopeFormula, Double> entry : this.entrySet()) {
      sum = sum + entry.getValue();
    }
    return sum;
  }

  /**
   * 
   * @param currentCorrectionIndex when this method is involved in the correction of an data
   *        point/Intensity I_J with index set J = {j_1, ..., j_k} where k stands for the number of
   *        used tracer and I_J stands for the sum of all intensities that refer to an isotopologue
   *        with j_1 of the first tracer, j_2 of the second tracer, ..., j_k of the k-th tracer
   *        incorporated, then the entries of currentCorrectionIndex are the used tracers and their
   *        values are the corresponding elements in J.
   * @param maxTracerFormula contains the info about used tracer and the maximal number of those
   *        elements in the fragment
   * @return the "correction sum" according to [Hunter N. B. Moseley et al. 2013]
   */
  private Double productOfCorrectionSums(IsotopeFormula currentCorrectionIndex,
      ElementFormula maxTracerFormula) {
    Double product = 1.0;
    for (Entry<Element, Integer> elementEntry : maxTracerFormula.entrySet()) {
      Double sum = 0.0;
      Isotope tracer = elementEntry.getKey().getTracer();
      Integer maxElements = elementEntry.getValue();
      Integer markedElements = currentCorrectionIndex.get(tracer);
      for (int k = markedElements + 1; k <= maxElements; k++) {
        sum = sum + correctionProduct(tracer, maxElements, markedElements, k);
      }
      product = product * (1 - sum);
    }
    return product;
  }

  /**
   * 
   * @param tracer the tracer of interest
   * @param maxElements
   * @param markedElements
   * @param additionalElements
   * @return let I be the sum of all intensities that originate from isotopologues with k tracer
   *         incorporated, the this method returns the proportion of I that arises from the natural
   *         incorporation of k-n additional tracer in molecules where n tracer are already labeled.
   *         In terms of the parameters n stands for markedElements and k for additionalElements
   */
  private Double correctionProduct(Isotope tracer, Integer maxElements, Integer markedElements,
      int additionalElements) {
    Integer n = maxElements - markedElements;
    Integer i = additionalElements - markedElements;
    Double correctionProduct = MathUtils.binom(n, i) * Math.pow(tracer.getAbundance(), i)
        * Math.pow(1 - tracer.getAbundance(), n - i);
    return correctionProduct;
  }

  /**
   * For each element in maxElementsFormula multiply the probability that k elements out of m are
   * marked under the condition that n are already marked. Here k refers to the value of the element
   * tracer isotope in currentCorrectionIndex, n refers to the value of the element tracer isotope
   * in alreadyCorrectedIndex and m refers to the value of the element in maxElementsFormula.
   * 
   * @param currentCorrectionIndex includes the information on how many further elements in the
   *        fragment shall be marked marked
   * @param alreadyCorrectedIndex includes the information on how many elements in the fragment are
   *        already marked
   * @param maxElementsFormula a map of the used tracers to the maximal number of elements in a
   *        fragment, that can be marked by this tracers
   * @return
   */
  private Double incorporationProbabilityProduct(IsotopeFormula currentCorrectionIndex,
      IsotopeFormula alreadyCorrectedIndex, ElementFormula maxElementsFormula) {
    Double correctionProduct = 1.0;
    for (Entry<Element, Integer> elementEntry : maxElementsFormula.entrySet()) {
      Isotope tracer = elementEntry.getKey().getTracer();
      Integer maxElements = elementEntry.getValue();
      Integer numberOfMarkedElements = alreadyCorrectedIndex.get(tracer);
      Integer additionalElements = currentCorrectionIndex.get(tracer);
      Integer n = maxElements - numberOfMarkedElements;
      Integer k = additionalElements - numberOfMarkedElements;
      correctionProduct = correctionProduct * incorporationProbability(tracer, n, k);
    }
    return correctionProduct;
  }

  /**
   * @param tracer
   * @param n
   * @param k
   * @return the probability that k out of n elements are marked by the tracer under the assumption
   *         that the tracer refers to an element with no more than two isotopes.
   */
  private Double incorporationProbability(Isotope tracer, Integer n, Integer k) {
    Double probability = MathUtils.binom(n, k) * Math.pow(tracer.getAbundance(), k)
        * Math.pow(1 - tracer.getAbundance(), n - k);
    return probability;
  }

  public List<Entry<IsotopeFormula, Double>> toEntryList() {
    List<Entry<IsotopeFormula, Double>> entryList = new ArrayList<>(this.entrySet());
    return entryList;
  }

  public String asTable() {
    IsotopeList isotopeList = new IsotopeList();
    for (Entry<IsotopeFormula, Double> entry : this.entrySet()) {
      isotopeList = entry.getKey().toIsotopeList();
      break;
    }
    ArrayList<String> header = new ArrayList<>();
    for (Isotope isotope : isotopeList) {
      header.add(isotope.getElement().toString());
    }
    header.add("Intensity");
    DataTable dataTable = new DataTable(header);
    for (Isotope isotope : isotopeList) {
      ArrayList<Integer> isotopeCountColumn = new ArrayList<>();
      for (Entry<IsotopeFormula, Double> entry : this.entrySet()) {
        isotopeCountColumn.add(entry.getKey().get(isotope));
      }
      dataTable.addColumn(isotopeCountColumn);
    }
    ArrayList<Double> intensityColumn = new ArrayList<>();
    for (Entry<IsotopeFormula, Double> entry : this.entrySet()) {
      intensityColumn.add(entry.getValue());
    }
    dataTable.addColumn(intensityColumn);
    return dataTable.toString("NA", true);
  }

  public IncorporationMap normalize() {
    Double sum = this.sumIntensities();
    IncorporationMap normalized = new IncorporationMap();
    for (Entry<IsotopeFormula, Double> entry : this.entrySet()) {
      normalized.put(entry.getKey(), entry.getValue() / sum);
    }
    return normalized;
  }

  public IncorporationMap normalize(int precision) {
    Double sum = this.sumIntensities();
    IncorporationMap normalized = new IncorporationMap();
    for (Entry<IsotopeFormula, Double> entry : this.entrySet()) {
      normalized.put(entry.getKey(), MathUtils.round(entry.getValue() / sum, precision));
    }
    return normalized;
  }


  public void put(int index, IsotopeFormula formula, double intensity) {
    IncorporationMap copy = this.copy();
    this.clear();
    int entryCount = 0;
    for (Entry<IsotopeFormula, Double> entry : copy.entrySet()) {
      if (entryCount == index) {
        this.put(formula, intensity);
      }
      entryCount++;
      this.put(entry.getKey(), entry.getValue());
    }
    if (entryCount == index) {
      this.put(formula, intensity);
    }
  }

  /**
   * Uses the Isotopes this map refers to in a determined order and assumes the same order for the
   * entries in combinationOfIsotopes. That means if this map refers to entries for 13C and 15N (in
   * this order) a parameter combinationOfIsotopes = {0,1} would return the sum of all the
   * intensities of isotopomers that incorporate 0 13C and 1 15N.
   * 
   * @param combinationOfIsotopes
   * @return the sum of all the intensities tracer in the parameter combination of isotopomers that
   *         incorporate.
   */
  public Double get(int... combinationOfIsotopes) {
    if (incorporatedIsotopes == null) {
      incorporatedIsotopes = new Isotope[combinationOfIsotopes.length];
      for (IsotopeFormula formula : keySet()) {
        int counter = 0;
        for (Isotope isotope : formula.keySet()) {
          incorporatedIsotopes[counter] = isotope;
          counter++;
        }
        break;
      }
    }
    IsotopeFormula isotopeFormula = new IsotopeFormula();
    for (int i = 0; i < incorporatedIsotopes.length; i++) {
      isotopeFormula.put(incorporatedIsotopes[i], combinationOfIsotopes[i]);
    }
    return get(isotopeFormula);
  }

}
