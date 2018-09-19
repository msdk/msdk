/*
 * Copyright 2006-2018 The MZmine 2 Development Team
 *
 * This file is part of MZmine 2.
 *
 * MZmine 2 is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine 2; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.msdk.id.sirius;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.util.MsSpectrumUtil;

public class SpectrumPreprocessor {
  private SpectrumPreprocessor() {}

  /**
   * Method preprocesses list of spectra, limits its amount and amount of mz/intensity pairs
   * Filtering of Spectrum objects is done by retrieving top N Spectrum objects with largest
   * Intensity values Filtering of mz/intensity pairs is done by retrieving top M amount of pairs by
   * Intensity values
   * 
   * @param spectra - list of spectrum to be preprocessed
   * @param listLimit - maximum amount of items to be in a new list
   * @param pairsLimit - maximum amount of mz/intensity pairs in a MsSpectrum
   * @return preprocessed filtered lists
   */
  public static List<MsSpectrum> preprocessSpectra(List<MsSpectrum> spectra, int listLimit,
      int pairsLimit) {
    if (spectra == null)
      return null;

    /* Small optimization */
    if (spectra.size() > listLimit) {
      spectra = retrieveMostIntense(spectra, listLimit);
    }

    /* Filter pairs of each spectrum */
    List<MsSpectrum> filteredSpectra = new ArrayList<>();
    for (MsSpectrum ms : spectra) {
      MsSpectrum filtered = MsSpectrumUtil.filterMsSpectrum(ms, pairsLimit);
      filteredSpectra.add(filtered);
    }
    return filteredSpectra;
  }

  /**
   * Method orders MsSpectra by its largest Intensity values and returns top N
   * 
   * @param spectra - list to be ordered
   * @param limit - amount of largest MsSpectra to return
   * @return top N MsSpectra ordered by Intensity
   */
  private static List<MsSpectrum> retrieveMostIntense(List<MsSpectrum> spectra, int limit) {
    TreeMap<Float, MsSpectrum> orderedSpectra = new TreeMap<>(Comparator.reverseOrder());

    /* Order by largest intensity value */
    for (MsSpectrum ms : spectra) {
      float biggest = 0;
      for (float temp : ms.getIntensityValues()) {
        if (temp > biggest)
          biggest = temp;
      }

      orderedSpectra.put(biggest, ms);
    }

    /* Retrieve only top N items */
    List<MsSpectrum> ordered = new LinkedList<>();
    for (int i = 0; i < limit; i++) {
      Entry<Float, MsSpectrum> pair = orderedSpectra.firstEntry();
      if (pair == null) break;
      orderedSpectra.remove(pair.getKey());
      MsSpectrum ms = pair.getValue();
      ordered.add(ms);
    }

    return ordered;
  }


}
