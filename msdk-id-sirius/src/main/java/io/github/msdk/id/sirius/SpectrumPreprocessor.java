/*
 * Copyright 2006-2018 The MZmine 2 Development Team
 *
 * This file is part of MZmine 2.
 *
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package io.github.msdk.id.sirius;

import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.SimpleMsSpectrum;
import io.github.msdk.util.DataPointSorter;
import io.github.msdk.util.DataPointSorter.SortingDirection;
import io.github.msdk.util.DataPointSorter.SortingProperty;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

public class SpectrumPreprocessor {
  private SpectrumPreprocessor() {}

  /**
   * Method preprocesses list of spectra, limits its amount and amount of mz/intensity pairs
   * Filtering of Spectrum objects is done by retrieving top N Spectrum objects with largest Intensity values
   * Filtering of mz/intensity pairs is done by retrieving top M amount of pairs by Intensity values
   * @param spectra - list of spectrum to be preprocessed
   * @param listLimit - maximum amount of items to be in a new list
   * @param pairsLimit - maximum amount of mz/intensity pairs in a MsSpectrum
   * @return preprocessed filtered lists
   */
  public static List<MsSpectrum> preprocessSpectra(List<MsSpectrum> spectra, int listLimit, int pairsLimit) {
    if (spectra == null)
      return null;

    if (spectra.size() > listLimit) {
      spectra = retrieveMostIntense(spectra, listLimit);
    }

    return filterSpectra(spectra, pairsLimit);
  }

  /**
   * Method orders MsSpectra by its largest Intensity values and returns top N
   * @param spectra - list to be ordered
   * @param limit - amount of largest MsSpectra to return
   * @return top N MsSpectra ordered by Intensity
   */
  private static List<MsSpectrum> retrieveMostIntense(List<MsSpectrum> spectra, int limit) {
    TreeMap<Float, MsSpectrum> orderedSpectra = new TreeMap<>();
    for (MsSpectrum ms: spectra) {
      float biggest = 0;
      for (float temp: ms.getIntensityValues()) {
        if (temp > biggest)
          biggest = temp;
      }

      orderedSpectra.put(biggest, ms);
    }

    List<MsSpectrum> ordered = new LinkedList<>();
    for (int i = 0; i < limit; i++) {
      Entry<Float, MsSpectrum> pair = orderedSpectra.firstEntry();
      orderedSpectra.remove(pair.getKey());
      MsSpectrum ms = pair.getValue();
      ordered.add(ms);
    }

    return ordered;
  }

  /**
   * Processes each spectrum in a list, filters with only N most intensive elements.
   * @param spectra - list of spectrums
   * @param pairsLimit - maximum amount of items in a new Spectrum
   * @return
   */
  private static List<MsSpectrum> filterSpectra(List<MsSpectrum> spectra, int pairsLimit) {
    List<MsSpectrum> filtered = new LinkedList<>();
    TreeMap<Float, Double> intesityMzSorted = new TreeMap<>();

    /* Purify each spectrum */
    for (MsSpectrum ms: spectra) {
      double mz[] = ms.getMzValues();
      float intensity[] = ms.getIntensityValues();
      if (mz.length > pairsLimit) {
        double[] newMz = new double[pairsLimit];
        float[] newIntensity = new float[pairsLimit];

        /* Sort intensity pairs */
        for (int i = 0; i < mz.length; i++) {
          intesityMzSorted.put(intensity[i], mz[i]);
        }

        /* Create new arrays with filtered intensity pairs */
        for (int i = 0; i < pairsLimit; i++) {
          Entry<Float, Double> pair = intesityMzSorted.firstEntry();
          intesityMzSorted.remove(pair.getKey());
          newMz[i] = pair.getValue();
          newIntensity[i] = pair.getKey();
        }
        intesityMzSorted.clear();

        /* Sort ascending by mz */
        DataPointSorter.sortDataPoints(newMz, newIntensity, pairsLimit, SortingProperty.MZ,
            SortingDirection.ASCENDING);

        /* Create new Spectrum object */
        MsSpectrumType type = ms.getSpectrumType();
        SimpleMsSpectrum filteredMs = new SimpleMsSpectrum(newMz, newIntensity, pairsLimit, type);
        filtered.add(filteredMs);
      } else {
        filtered.add(ms);
      }
    }
    return filtered;
  }

}
