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

import java.util.List;

import org.apache.commons.csv.CSVRecord;

import io.github.msdk.isotopes.tracing.data.IsotopeListList;
import io.github.msdk.isotopes.tracing.data.MSDatabase;
import io.github.msdk.isotopes.tracing.data.MassShiftDataSet;
import io.github.msdk.isotopes.tracing.data.MassShiftList;
import io.github.msdk.isotopes.tracing.data.MassSpectrum;
import io.github.msdk.isotopes.tracing.data.constants.IntensityType;

/**
 * Methods to parse datatypes from csv.
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class ParserUtils {

  /**
   * Parses a MassSpectrum from a list of csv records where masses will be read from the
   * massColumnIndex and the corresponding frequencies from the frequencyColumnIndex.
   * 
   * @param records
   * @param massColumnIndex
   * @param frequencyColumnIndex
   * @return A MassSpectrum from the csv records with 0.0 entries removed.
   */
  public static MassSpectrum parseSpectrum(List<CSVRecord> records, int massColumnIndex,
      int frequencyColumnIndex, IntensityType intensityType, int headerRow) {
    MassSpectrum massSpectrum = new MassSpectrum(intensityType);
    for (CSVRecord csvRecord : records) {
      try {
        if (csvRecord.getRecordNumber() <= headerRow) {
          continue;
        }
        if (csvRecord.get(massColumnIndex).equals(MSDatabase.NA_VALUE)
            || csvRecord.get(massColumnIndex).equals("0.0")) {
          continue;
        }
        if (csvRecord.get(massColumnIndex) == null || csvRecord.get(frequencyColumnIndex) == null) {
          continue;
        }
        Double mass = Double.parseDouble(csvRecord.get(massColumnIndex));
        Double frequency = Double.parseDouble(csvRecord.get(frequencyColumnIndex));
        massSpectrum.put(mass, frequency);
      } catch (ArrayIndexOutOfBoundsException e) {
        // there may be an empty row
        continue;
      }
    }
    massSpectrum.remove(0.0);
    return massSpectrum;
  }

  public static MassShiftDataSet parseMassShiftDataSet(List<CSVRecord> records,
      int shiftValuesColumnIndex, int shifIsotopesColumnIndex) {
    MassShiftDataSet shiftDataSet = new MassShiftDataSet();
    for (CSVRecord csvRecord : records) {
      try {
        if (csvRecord.getRecordNumber() == 1) {
          continue;
        }
        if (csvRecord.get(shiftValuesColumnIndex).equals(MSDatabase.NA_VALUE)) {
          continue;
        }
        String massShiftListString = csvRecord.get(shiftValuesColumnIndex);
        String isotopesListString = csvRecord.get(shifIsotopesColumnIndex);
        MassShiftList massShiftList = MassShiftList.fromString(massShiftListString);
        IsotopeListList isotopeListList = IsotopeListList.fromString(isotopesListString);
        shiftDataSet.put(massShiftList, isotopeListList);
      } catch (ArrayIndexOutOfBoundsException e) {
        // there may be an empty row
        continue;
      }
    }
    return shiftDataSet;
  }

}
