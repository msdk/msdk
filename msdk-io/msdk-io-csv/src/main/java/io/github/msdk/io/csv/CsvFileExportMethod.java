/* 
 * (C) Copyright 2015-2016 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */

package io.github.msdk.io.csv;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.ionannotations.IonType;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;

/**
 * <p>
 * CsvFileExportMethod class.
 * </p>
 */
public class CsvFileExportMethod implements MSDKMethod<File> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // Input variables
    private @Nonnull FeatureTable featureTable;
    private @Nonnull File csvFile;
    private @Nonnull String separator;
    private @Nonnull String itemSeparator;
    private @Nonnull Boolean exportAllIds;
    private @Nonnull List<FeatureTableColumn<?>> columns;
    String newLine = System.lineSeparator();

    // Ion Annotation information variables
    String ionVal1;
    String ionVal2;
    String ionVal3;
    String ionVal4;
    String ionVal5;
    String ionVal6;
    String ionVal7;
    String ionVal8;
    String ionVal9;
    String ionVal10;
    String ionVal11;
    String ionVal12;
    String ionVal13;
    String ionVal14;

    // Other variables
    private int parsedRows, totalRows = 0;
    private boolean canceled = false;

    /**
     * <p>
     * Constructor for CsvFileExportMethod.
     * </p>
     *
     * @param featureTable
     *            a {@link io.github.msdk.datamodel.featuretables.FeatureTable}
     *            object.
     * @param csvFile
     *            a {@link java.io.File} object.
     * @param separator
     *            a {@link java.lang.String} object.
     * @param itemSeparator
     *            a {@link java.lang.String} object.
     * @param exportAllIds
     *            a {@link java.lang.Boolean} object.
     */
    public CsvFileExportMethod(@Nonnull FeatureTable featureTable,
            @Nonnull File csvFile, @Nonnull String separator,
            @Nonnull String itemSeparator, @Nonnull Boolean exportAllIds,
            List<FeatureTableColumn<?>> columns) {
        this.featureTable = featureTable;
        this.csvFile = csvFile;
        this.separator = separator;
        this.itemSeparator = itemSeparator;
        this.exportAllIds = exportAllIds;
        this.columns = columns;
    }

    /** {@inheritDoc} */
    @Override
    public File execute() throws MSDKException {

        logger.info("Started exporting " + featureTable.getName() + " to "
                + csvFile);

        // Open file
        FileWriter writer;
        try {
            writer = new FileWriter(csvFile);
        } catch (Exception e) {
            logger.info("Could not open file " + csvFile + " for writing.");
            return null;
        }

        // Get number of rows
        totalRows = featureTable.getRows().size();

        // Write data to CSV file
        writeData(featureTable, writer);

        // Close file
        try {
            writer.close();
        } catch (Exception e) {
            logger.info("Could not close file " + csvFile);
            return null;
        }

        return csvFile;
    }

    private void writeData(FeatureTable featureTable, FileWriter writer) {

        // Buffer for writing
        StringBuffer line = new StringBuffer();

        Boolean writeIonAnnotation = false;

        // Write column headers
        for (FeatureTableColumn<?> column : columns) {
            String columnName = column.getName();
            Sample sample = column.getSample();

            // Add sample name to column name if available
            if (sample != null) {
                columnName = sample.getName() + " " + columnName;
            }

            line.append(escapeStringForCSV(columnName) + separator);

            // Add additional columns related to the IonAnnotation
            if (column == featureTable.getColumn(ColumnName.IONANNOTATION,
                    null)) {
                String[] ionColumns = new String[14];
                ionColumns[0] = "Expected m/z value";
                ionColumns[1] = "Formula";
                ionColumns[2] = "Ion type";
                ionColumns[3] = "Reliability";
                ionColumns[4] = "SMILES";
                ionColumns[5] = "InChI key";
                ionColumns[6] = "Taxonomy id";
                ionColumns[7] = "Species";
                ionColumns[8] = "Database";
                ionColumns[9] = "Database version";
                ionColumns[10] = "SpectraRef";
                ionColumns[11] = "Search engine";
                ionColumns[12] = "Best search engine score";
                ionColumns[13] = "Modifications";

                for (String s : ionColumns)
                    line.append(escapeStringForCSV(s) + separator);

                writeIonAnnotation = true;
            }
        }

        // Remove last separator
        String stringLine = removeLastCharacter(line.toString());

        // Write the line to the CSV file
        try {
            writer.write(stringLine);
        } catch (Exception e) {
            logger.info("Could not write to file " + csvFile);
            return;
        }

        if (canceled)
            return;

        // Write data values
        List<FeatureTableRow> rows = featureTable.getRows();
        for (FeatureTableRow row : rows) {

            // Reset the buffer
            line.setLength(0);

            // Loop through all columns
            for (FeatureTableColumn<?> column : columns) {
                Object object = row.getData(column);
                String strValue = "";
                ionVal1 = "";
                ionVal2 = "";
                ionVal3 = "";
                ionVal4 = "";
                ionVal5 = "";
                ionVal6 = "";
                ionVal7 = "";
                ionVal8 = "";
                ionVal9 = "";
                ionVal10 = "";
                ionVal11 = "";
                ionVal12 = "";
                ionVal13 = "";
                ionVal14 = "";
                Boolean writeIonData = false;

                if (object == null) {
                    strValue = "";
                }
                // Chromatography Info
                else if (object instanceof ChromatographyInfo) {
                    ChromatographyInfo chromatographyInfo = (ChromatographyInfo) object;
                    Float rt1 = chromatographyInfo.getRetentionTime();
                    Float rt2 = chromatographyInfo.getSecondaryRetentionTime();

                    if (rt1 != null) {
                        strValue = rt1.toString();
                    }
                    if (strValue != null && rt2 != null) {
                        strValue = strValue + itemSeparator;
                    }
                    if (rt2 != null) {
                        strValue = strValue + rt2.toString();
                    }
                }
                // List
                else if (object instanceof List<?>) {
                    strValue = "";
                    List<?> list = (List<?>) object;
                    for (Object obj : list) {
                        if (strValue != "")
                            strValue = strValue + itemSeparator;

                        // Ion annotations
                        if (obj instanceof IonAnnotation) {
                            IonAnnotation ionAnnotation = (IonAnnotation) obj;
                            if (ionAnnotation.getDescription() != null)
                                strValue = strValue
                                        + ionAnnotation.getDescription();
                            else
                                strValue = strValue
                                        + ionAnnotation.getAnnotationId();

                            // Add additional data related to the IonAnnotation
                            if (writeIonAnnotation) {
                                getAdditionalIonAnnotationData(ionAnnotation);
                                writeIonData = true;
                            }

                            if (!exportAllIds)
                                break;
                        } else {
                            strValue = strValue + obj.toString();
                        }
                    }

                }
                // Everything else
                else {
                    strValue = object.toString();
                }

                line.append(strValue + separator);

                if (writeIonData) {
                    line.append(ionVal1 + separator);
                    line.append(ionVal2 + separator);
                    line.append(ionVal3 + separator);
                    line.append(ionVal4 + separator);
                    line.append(ionVal5 + separator);
                    line.append(ionVal6 + separator);
                    line.append(ionVal7 + separator);
                    line.append(ionVal8 + separator);
                    line.append(ionVal9 + separator);
                    line.append(ionVal10 + separator);
                    line.append(ionVal11 + separator);
                    line.append(ionVal12 + separator);
                    line.append(ionVal13 + separator);
                    line.append(ionVal14 + separator);
                }
            }

            // Remove last separator
            stringLine = removeLastCharacter(line.toString());

            // Write the line to the CSV file
            try {
                writer.write(newLine + stringLine);
            } catch (Exception e) {
                logger.info("Could not write to file " + csvFile);
                return;
            }

            if (canceled)
                return;

            parsedRows++;
        }

    }

    private void getAdditionalIonAnnotationData(IonAnnotation ionAnnotation) {
        // Expected m/z value
        if (ionVal1 != "")
            ionVal1 = ionVal1 + itemSeparator;
        Double expectedMz = ionAnnotation.getExpectedMz();
        if (expectedMz != null)
            ionVal1 += expectedMz;
        else
            ionVal1 += "";

        // Formula
        if (ionVal2 != "")
            ionVal2 = ionVal2 + itemSeparator;
        IMolecularFormula cdkFormula = ionAnnotation.getFormula();
        if (cdkFormula != null) {
            String formula = MolecularFormulaManipulator.getString(cdkFormula);
            ionVal2 += formula;
        } else
            ionVal2 += "";

        // Ion type
        if (ionVal3 != "")
            ionVal3 = ionVal3 + itemSeparator;
        IonType ionType = ionAnnotation.getIonType();
        if (ionType != null)
            ionVal3 += ionType.getName();
        else
            ionVal3 += "";

        // Reliability
        if (ionVal4 != "")
            ionVal4 = ionVal4 + itemSeparator;
        Integer ionReliability = ionAnnotation.getReliability();
        if (ionReliability != null)
            ionVal4 += ionReliability.toString();
        else
            ionVal4 += "";

        // Chemical structure = SMILES
        if (ionVal5 != "")
            ionVal5 = ionVal5 + itemSeparator;
        IAtomContainer checmicalStructure = ionAnnotation
                .getChemicalStructure();
        SmilesGenerator sg = SmilesGenerator.generic();

        if (checmicalStructure != null) {
            try {
                ionVal5 += sg.create(checmicalStructure);
            } catch (CDKException e) {
                logger.info("Could not create SMILE for "
                        + ionAnnotation.getDescription());
                return;
            }
        } else
            ionVal5 += "";

        // InChI key
        if (ionVal6 != "")
            ionVal6 = ionVal6 + itemSeparator;
        String ionInchiKey = ionAnnotation.getInchiKey();
        if (ionInchiKey != null)
            ionVal6 += ionInchiKey;
        else
            ionVal6 += "";

        // Taxonomy id
        if (ionVal7 != "")
            ionVal7 = ionVal7 + itemSeparator;
        Integer ionTaxonomyId = ionAnnotation.getTaxId();
        if (ionTaxonomyId != null)
            ionVal7 += ionTaxonomyId.toString();
        else
            ionVal7 += "";

        // Species
        if (ionVal8 != "")
            ionVal8 = ionVal8 + itemSeparator;
        String ionSpecies = ionAnnotation.getSpecies();
        if (ionSpecies != null)
            ionVal8 += ionSpecies;
        else
            ionVal8 += "";

        // Database
        if (ionVal9 != "")
            ionVal9 = ionVal9 + itemSeparator;
        String ionDatabase = ionAnnotation.getDatabase();
        if (ionDatabase != null)
            ionVal9 += ionDatabase;
        else
            ionVal9 += "";

        // Database version
        if (ionVal10 != "")
            ionVal10 = ionVal10 + itemSeparator;
        String ionDatabaseVersion = ionAnnotation.getDatabaseVersion();
        if (ionDatabaseVersion != null)
            ionVal10 += ionDatabaseVersion;
        else
            ionVal10 += "";

        // Spectra Ref
        if (ionVal11 != "")
            ionVal11 = ionVal11 + itemSeparator;
        String ionSpectraRef = ionAnnotation.getSpectraRef();
        if (ionSpectraRef != null)
            ionVal11 += ionSpectraRef;
        else
            ionVal11 += "";

        // Search engine
        if (ionVal12 != "")
            ionVal12 = ionVal12 + itemSeparator;
        String ionSearchEngine = ionAnnotation.getSearchEngine();
        if (ionSearchEngine != null)
            ionVal12 += ionSearchEngine;
        else
            ionVal12 += "";

        // Best search engine score
        if (ionVal13 != "")
            ionVal13 = ionVal13 + itemSeparator;
        Double ionSearchEngineScore = ionAnnotation.getBestSearchEngineScore();
        if (ionSearchEngineScore != null)
            ionVal13 += ionSearchEngineScore.toString();
        else
            ionVal13 += "";

        // Modifications
        if (ionVal14 != "")
            ionVal14 = ionVal14 + itemSeparator;
        String ionModifications = ionAnnotation.getModifications();
        if (ionModifications != null)
            ionVal14 += ionModifications;
        else
            ionVal14 += "";

    }

    private String escapeStringForCSV(final String inputString) {
        if (inputString == null)
            return "";

        // Remove all special characters (particularly \n would cause issues)
        String result = inputString.replaceAll("[\\p{Cntrl}]", " ");

        // If the text contains the separator, add parenthesis
        if (result.contains(separator)) {
            result = "\"" + result.replaceAll("\"", "'") + "\"";
        }

        return result;
    }

    public String removeLastCharacter(String str) {
        if (str.length() > 0) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public File getResult() {
        return csvFile;
    }

    /** {@inheritDoc} */
    @Override
    public Float getFinishedPercentage() {
        return totalRows == 0 ? null : (float) parsedRows / totalRows;
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() {
        this.canceled = true;
    }

}
