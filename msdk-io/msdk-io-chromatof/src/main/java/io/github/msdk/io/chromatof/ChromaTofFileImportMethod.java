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

package io.github.msdk.io.chromatof;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.impl.SimpleFeature;
import io.github.msdk.datamodel.impl.SimpleFeatureTable;
import io.github.msdk.datamodel.impl.SimpleFeatureTableRow;
import io.github.msdk.datamodel.impl.SimpleIonAnnotation;
import io.github.msdk.datamodel.impl.SimpleSample;
import io.github.msdk.io.chromatof.ChromaTofParser.Mode;
import io.github.msdk.io.chromatof.ChromaTofParser.TableColumn;

/**
 * <p>
 * ChromaTofFileImportMethod class.
 * </p>
 *
 * @author plusik
 * @version $Id: $Id
 */
public class ChromaTofFileImportMethod implements MSDKMethod<FeatureTable> {

  private final Logger logger = LoggerFactory.getLogger(ChromaTofFileImportMethod.class);

  private int parsedLines, totalLines = 0;

  private final @Nonnull File sourceFile;
  private final @Nonnull DataPointStore dataStore;
  private final @Nonnull Locale locale;
  private String fieldSeparator = ChromaTofParser.FIELD_SEPARATOR_TAB;
  private String quotationCharacter = ChromaTofParser.QUOTATION_CHARACTER_NONE;

  private SimpleFeatureTable newFeatureTable;
  private final Sample fileSample;
  private boolean canceled = false;

  /**
   * <p>
   * Constructor for ChromaTofFileImportMethod.
   * </p>
   *
   * @param sourceFile a {@link java.io.File} object.
   * @param dataStore a {@link io.github.msdk.datamodel.datastore.DataPointStore} object.
   * @param locale the locale and corresponding decimal point format to use for number parsing.
   * @param fieldSeparator the field separator between fields on one line.
   * @param quotationCharacter the quotation character for a field.
   * @see ChromaTofParser
   */
  public ChromaTofFileImportMethod(@Nonnull File sourceFile, @Nonnull DataPointStore dataStore,
      @Nonnull Locale locale, String fieldSeparator, String quotationCharacter) {
    this.sourceFile = sourceFile;
    this.dataStore = dataStore;
    this.fileSample = new SimpleSample(sourceFile.getName());
    this.locale = locale;
    this.fieldSeparator = fieldSeparator;
    this.quotationCharacter = quotationCharacter;
  }

  /**
   * <p>
   * Constructor for ChromaTofFileImportMethod. Uses {@link java.util.Locale#US} for number parsing.
   * </p>
   *
   * @param sourceFile a {@link java.io.File} object.
   * @param dataStore a {@link io.github.msdk.datamodel.datastore.DataPointStore} object.
   */
  public ChromaTofFileImportMethod(@Nonnull File sourceFile, @Nonnull DataPointStore dataStore) {
    this(sourceFile, dataStore, Locale.US, null, null);
  }

  /** {@inheritDoc} */
  @Override
  public FeatureTable execute() throws MSDKException {

    logger.info("Started parsing file " + sourceFile);

    // Check if the file is readable
    if (!sourceFile.canRead()) {
      throw new MSDKException("Cannot read file " + sourceFile);
    }

    logger.info("Using locale: " + locale.getDisplayName());

    if (fieldSeparator == null || quotationCharacter == null) {
      // TODO guess fieldSeparator from first line, guess quotation char
      // from second line?
      throw new MSDKException("Field separator and quotation character must not be null!");
    }

    logger.info("Using field separator: '" + fieldSeparator + "'");
    logger.info("Using quotation character: '" + quotationCharacter + "'");

    String fileName = sourceFile.getName();
    newFeatureTable = new SimpleFeatureTable();
    boolean normalizeColumnNames = false;

    ChromaTofParser parser = new ChromaTofParser(fieldSeparator, quotationCharacter, Locale.US);
    LinkedHashSet<TableColumn> header = parser.parseHeader(sourceFile, normalizeColumnNames);
    int tcIndex = 0;
    // Read all lines from the CSV file into an array
    final List<TableRow> lines = parser.parseBody(header, sourceFile, normalizeColumnNames);

    final Mode mode = parser.getMode(lines);

    // Update total lines
    totalLines = lines.size();

    int rowId = 1;

    for (TableRow tableRow : lines) {

      // Feature table row
      rowId++;
      SimpleFeatureTableRow row = new SimpleFeatureTableRow(newFeatureTable);
      newFeatureTable.addRow(row);
      SimpleFeature feature = new SimpleFeature();
      row.setFeature(fileSample, feature);
      SimpleIonAnnotation ionAnnotation = new SimpleIonAnnotation();;
      feature.setIonAnnotation(ionAnnotation);

      // Loop through all the data and add it to the row
      int i = 0;
      for (TableColumn tableColumn : tableRow.keySet()) {
        String value = tableRow.getValueForName(tableColumn.getColumnName());
        // Ignore null values
        if (value == null || value.equals("") || value.equals("null")) {
          continue;
        }

        switch (tableColumn.getColumnName()) {
          case AREA:
            feature.setArea(Float.parseFloat(value));
            break;
          case NAME:
            ionAnnotation.setDescription(value);
            break;
          case FORMULA:
            // Create chemical structure
            IMolecularFormula formula = MolecularFormulaManipulator.getMolecularFormula(value,
                DefaultChemObjectBuilder.getInstance());
            ionAnnotation.setFormula(formula);
            break;
        }

      }

      parsedLines++;

      // Check if cancel is requested
      if (canceled) {
        return null;
      }

    }

    return newFeatureTable;

  }

  private @Nonnull String findSeparator(String line) {
    // Default is a comma ","
    String result = ",";

    // Tab
    if (line.contains("\t")) {
      result = "\t";
    }

    return result;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public FeatureTable getResult() {
    return newFeatureTable;
  }

  /** {@inheritDoc} */
  @Override
  public Float getFinishedPercentage() {
    if (totalLines == 0) {
      return 0f;
    }

    float lines = 0;
    if (totalLines != 0) {
      lines = (float) parsedLines / (float) totalLines;
    }
    return lines;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    this.canceled = true;
  }

}
