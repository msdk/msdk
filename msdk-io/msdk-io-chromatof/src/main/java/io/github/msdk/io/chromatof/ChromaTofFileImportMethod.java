/*
 * (C) Copyright 2015-2016 by MSDK Development Team
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.io.chromatof.ChromaTofParser.Mode;
import io.github.msdk.io.chromatof.ChromaTofParser.TableColumn;
import io.github.msdk.util.FeatureTableUtil;

/**
 * <p>
 * ChromaTofFileImportMethod class.
 * </p>
 */
public class ChromaTofFileImportMethod implements MSDKMethod<FeatureTable> {

  private final Logger logger = LoggerFactory.getLogger(ChromaTofFileImportMethod.class);

  private int parsedLines, totalLines = 0;

  private final @Nonnull File sourceFile;
  private final @Nonnull DataPointStore dataStore;
  private final @Nonnull Locale locale;
  private String fieldSeparator = ChromaTofParser.FIELD_SEPARATOR_TAB;
  private String quotationCharacter = ChromaTofParser.QUOTATION_CHARACTER_NONE;

  private Map<Integer, FeatureTableColumn<?>> columns =
      new HashMap<Integer, FeatureTableColumn<?>>();

  private FeatureTable newFeatureTable;
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
    this.fileSample = MSDKObjectBuilder.getSample(sourceFile.getName());
    this.locale = locale;
    this.fieldSeparator = fieldSeparator;
    this.quotationCharacter = quotationCharacter;
  }

  /**
   * <p>
   * Constructor for ChromaTofFileImportMethod. Uses {@link Locale#US} for number parsing.
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
    newFeatureTable = MSDKObjectBuilder.getFeatureTable(fileName, dataStore);
    boolean normalizeColumnNames = false;

    ChromaTofParser parser = new ChromaTofParser(fieldSeparator, quotationCharacter, Locale.US);
    LinkedHashSet<TableColumn> header = parser.parseHeader(sourceFile, normalizeColumnNames);
    int tcIndex = 0;
    for (TableColumn tc : header) {
      // Map the column name to the MSDK ColumnName
      FeatureTableColumn<?> column = createNewColumn(tc.getColumnName(), fileSample);

      // Make sure that there is only one ion annotation column
      FeatureTableColumn<?> ionAnnotationColumn =
          newFeatureTable.getColumn(ColumnName.IONANNOTATION, null);
      if (column.getName().equals(ColumnName.IONANNOTATION.getName())) {
        if (ionAnnotationColumn != null) {
          column = ionAnnotationColumn;
        } else // Add the column to the feature table
        {
          newFeatureTable.addColumn(column);
        }
      } else {
        // Add the column to the feature table
        newFeatureTable.addColumn(column);
      }

      // Add the column to the map
      columns.put(tcIndex++, column);
    }
    // Read all lines from the CSV file into an array
    final List<TableRow> lines = parser.parseBody(header, sourceFile, normalizeColumnNames);

    final Mode mode = parser.getMode(lines);

    // Update total lines
    totalLines = lines.size();

    int rowId = 1;

    for (TableRow tableRow : lines) {

      // Feature table row
      rowId++;
      FeatureTableRow row = MSDKObjectBuilder.getFeatureTableRow(newFeatureTable, rowId);
      newFeatureTable.addRow(row);

      // Loop through all the data and add it to the row
      int i = 0;
      for (TableColumn tableColumn : tableRow.keySet()) {
        String value = tableRow.getValueForName(tableColumn.getColumnName());
        // Ignore null values
        if (value == null || value.equals("") || value.equals("null")) {
          continue;
        }

        Object objectData = null;
        FeatureTableColumn currentColumn = columns.get(i);
        Class<?> currentClass = currentColumn.getDataTypeClass();

        if (currentClass.getSimpleName().equals("List")) {
          FeatureTableColumn<List<IonAnnotation>> ionAnnotationColumn =
              newFeatureTable.getColumn(ColumnName.IONANNOTATION, null);
          if (ionAnnotationColumn == null) {
            newFeatureTable.addColumn(MSDKObjectBuilder.getIonAnnotationFeatureTableColumn());
            ionAnnotationColumn = newFeatureTable.getColumn(ColumnName.IONANNOTATION, null);
          }
          List<IonAnnotation> ionAnnotations = row.getData(ionAnnotationColumn);
          IonAnnotation ionAnnotation;

          // Get ion annotation or create a new
          if (ionAnnotations != null) {
            ionAnnotation = ionAnnotations.get(0);
          } else {
            ionAnnotation = MSDKObjectBuilder.getIonAnnotation();
          }

          switch (tableColumn.getColumnName()) {
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

          // Add the data
          List<IonAnnotation> newIonAnnotations = new ArrayList<IonAnnotation>();
          newIonAnnotations.add(ionAnnotation);
          row.setData(currentColumn, newIonAnnotations);

        } else {
          switch (currentClass.getSimpleName()) {
            case "Integer":
              objectData = Integer.parseInt(value);
              break;
            case "Double":
              objectData = Double.parseDouble(value);
              break;
            default:
              objectData = value;
              break;
          }
          // Add the data
          row.setData(currentColumn, objectData);

        }

      }

      parsedLines++;

      // Check if cancel is requested
      if (canceled) {
        return null;
      }

    }

    // Update average row m/z and RT values. This will also create the
    // columns if they are missing.
    FeatureTableUtil.recalculateAverages(newFeatureTable);

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

  private FeatureTableColumn<?> createNewColumn(
      io.github.msdk.io.chromatof.ChromaTofParser.ColumnName columnName, Sample sample) {

    ColumnName newColumnName = null;

    // If no match, then check common matches
    if (newColumnName == null) {
      switch (columnName) {
        case RETENTION_TIME_SECONDS:
        case FIRST_DIMENSION_TIME_SECONDS:
        case SECOND_DIMENSION_TIME_SECONDS:
          newColumnName = ColumnName.RT;
          break;
        case NAME:
        case FORMULA:
          newColumnName = ColumnName.IONANNOTATION;
          break;
      }
    }

    // If no samples were found, then assume that all data in the file is
    // from one sample
    // if (newColumnName != ColumnName.IONANNOTATION) {
    sample = fileSample;
    // }

    // If still no match, then create a new column with String.class
    FeatureTableColumn<?> column = null;

    if (newColumnName == null) {
      column = MSDKObjectBuilder.getFeatureTableColumn(columnName.name(), String.class, sample);
    } else // Use special columns for Id, m/z, rt and ion annotation
    {
      if (sample == null) {
        if (newColumnName.equals(ColumnName.ID)) {
          column = MSDKObjectBuilder.getIdFeatureTableColumn();
        }
        if (newColumnName.equals(ColumnName.MZ)) {
          column = MSDKObjectBuilder.getMzFeatureTableColumn();
        }
        if (newColumnName.equals(ColumnName.RT)) {
          column = MSDKObjectBuilder.getRetentionTimeFeatureTableColumn();
        }
        if (newColumnName.equals(ColumnName.IONANNOTATION)) {
          column = MSDKObjectBuilder.getIonAnnotationFeatureTableColumn();
        }
      } else {
        column = MSDKObjectBuilder.getFeatureTableColumn(newColumnName, sample);
      }
    }

    return column;
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
