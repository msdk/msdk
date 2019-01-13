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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import io.github.msdk.isotopes.tracing.data.constants.IncorporationType;
import io.github.msdk.isotopes.tracing.data.constants.IntensityType;
import io.github.msdk.isotopes.tracing.data.constants.MSDatabaseColKey;
import io.github.msdk.isotopes.tracing.util.FileWriterUtils;
import io.github.msdk.isotopes.tracing.util.ParserUtils;

/**
 * A mass spectra database, containing natural, marked and mixed spectra of a fragment. Information
 * like incorporation rate, metabolite key and incorporated tracers is also included.
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class MSDatabase {

  public static final String NA_VALUE = "NA";
  public static final int JPEG_WIDTH_PER_CATEGORY = 200;
  public static final int JPEG_HEIGHT_PER_CATEGORY = 100;
  public static final int JPEG_MIN_WIDTH = 800;
  public static final int JPEG_MIN_HEIGHT = 400;

  private Double incorporationRate;
  private MassSpectrum naturalSpectrum = new MassSpectrum(IntensityType.MID);
  private MassSpectrum markedSpectrum = new MassSpectrum(IntensityType.MID);
  private MassSpectrum mixedSpectrum = new MassSpectrum(IntensityType.MID);
  private String compoundFormula;
  private String incorporatedTracers;

  public MSDatabase() {

  }

  public MSDatabase(String absoluteFilePath) {
    this.parseCsv(absoluteFilePath);
  }

  /**
   * parse this object from a csv file with information according to the MSDatabaseColKey enum.
   * 
   * @param absoluteFilePath
   */
  public void parseCsv(String absoluteFilePath) {
    File csvData = new File(absoluteFilePath);
    CSVParser parser;
    try {
      parser = CSVParser.parse(csvData, Charset.defaultCharset(), CSVFormat.RFC4180);
      List<CSVRecord> records = parser.getRecords();
      naturalSpectrum =
          ParserUtils.parseSpectrum(records, MSDatabaseColKey.NATURAL_MASS.getColumnIndex(),
              MSDatabaseColKey.NATURAL_FREQUENCY.getColumnIndex(), IntensityType.MID, 1);
      markedSpectrum =
          ParserUtils.parseSpectrum(records, MSDatabaseColKey.MARKED_MASS.getColumnIndex(),
              MSDatabaseColKey.MARKED_FREQUENCY.getColumnIndex(), IntensityType.MID, 1);
      mixedSpectrum =
          ParserUtils.parseSpectrum(records, MSDatabaseColKey.MIXED_MASS.getColumnIndex(),
              MSDatabaseColKey.MIXED_FREQUENCY.getColumnIndex(), IntensityType.MID, 1);
      incorporationRate =
          Double.parseDouble(records.get(1).get(MSDatabaseColKey.INC_RATE.getColumnIndex()));
      compoundFormula = records.get(1).get(MSDatabaseColKey.COMPOUND_FORMULA.getColumnIndex());
      incorporatedTracers =
          records.get(1).get(MSDatabaseColKey.INCORPORATED_TRACERS.getColumnIndex());
    } catch (Exception e) {
      System.out.println(e.getStackTrace());
      e.printStackTrace();
    }
  }

  /**
   * write this object to csv using the headers defined in MSDatabaseColKey enum
   * 
   * @param outputFolderPath
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  public void writeCsv(String outputFolderPath) throws IOException {
    File folder = new File(outputFolderPath);
    if (!folder.exists()) {
      folder.mkdir();
    }
    String filePath = outputFolderPath + createFilename() + FileWriterUtils.CSV_EXTENSION;
    filePath = FileWriterUtils.checkFilePath(filePath, FileWriterUtils.CSV_EXTENSION);
    DataTable dataTable = new DataTable(MSDatabaseColKey.toHeaderList());
    dataTable.addColumns(naturalSpectrum, markedSpectrum, mixedSpectrum);
    dataTable.addConstantValueColumn(0, incorporationRate);
    dataTable.addConstantValueColumn(compoundFormula);
    dataTable.addConstantValueColumn(incorporatedTracers);
    dataTable.writeToCsv(NA_VALUE, true, filePath);
  }

  public Double getIncorporationRate() {
    return incorporationRate;
  }

  public void setIncorporationRate(Double incorporationRate) {
    this.incorporationRate = incorporationRate;
  }

  public MassSpectrum getNaturalSpectrum() {
    return naturalSpectrum;
  }

  public void setNaturalSpectrum(MassSpectrum naturalSpectrum) {
    this.naturalSpectrum = naturalSpectrum;
  }

  public MassSpectrum getMarkedSpectrum() {
    return markedSpectrum;
  }

  public void setMarkedSpectrum(MassSpectrum markedSpectrum) {
    this.markedSpectrum = markedSpectrum;
  }

  public MassSpectrum getMixedSpectrum() {
    return mixedSpectrum;
  }

  public void setMixedSpectrum(MassSpectrum mixedSpectrum) {
    this.mixedSpectrum = mixedSpectrum;
  }

  public String getCompoundFormula() {
    return compoundFormula;
  }

  public void setCompoundFormula(String compoundFormula) {
    this.compoundFormula = compoundFormula;
  }

  public String getIncorporatedTracers() {
    return incorporatedTracers;
  }

  public void setIncorporatedTracers(String incorporatedTracers) {
    this.incorporatedTracers = incorporatedTracers;
  }

  /**
   * 
   * @return string containing FragmentKey, incorporated tracer and incorporation percent
   */
  public String createFilename() {
    int incorporationPerCent = (int) (incorporationRate * 100);
    return compoundFormula + "_" + incorporatedTracers + "_" + incorporationPerCent;
  }

  /**
   * 
   * @return the incorporation rate as percent value
   */
  public int incorporationPerCent() {
    return (int) (incorporationRate * 100);
  }

  /**
   * creates a table string representation of this {@link MSDatabase}
   */
  @Override
  public String toString() {
    DataTable dataTable = new DataTable(MSDatabaseColKey.toHeaderList());
    dataTable.addColumn(getNaturalSpectrum());
    dataTable.addColumn(getMarkedSpectrum());
    dataTable.addColumn(getMixedSpectrum());
    dataTable.addConstantValueColumn(0, getIncorporationRate());
    dataTable.addConstantValueColumn(compoundFormula);
    dataTable.addConstantValueColumn(getIncorporatedTracers());
    return dataTable.toString(NA_VALUE, true);
  }

  public MassSpectrum getSpectrum(IncorporationType type) {
    if (IncorporationType.NATURAL.equals(type)) {
      return getNaturalSpectrum();
    }
    if (IncorporationType.MIXED.equals(type)) {
      return getMixedSpectrum();
    }
    if (IncorporationType.MARKED.equals(type)) {
      return getMarkedSpectrum();
    }
    return null;
  }

}
