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
                String[] ionColumns = new String[4];
                ionColumns[0] = "Expected m/z value";
                ionColumns[1] = "Formula";
                ionColumns[2] = "Ion type";
                ionColumns[3] = "SMILES";

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
        String formula = MolecularFormulaManipulator.getString(cdkFormula);
        if (formula != null)
            ionVal2 += formula;
        else
            ionVal2 += "";

        // Ion type
        if (ionVal3 != "")
            ionVal3 = ionVal3 + itemSeparator;
        IonType ionType = ionAnnotation.getIonType();
        if (ionType != null)
            ionVal3 += ionType.getName();
        else
            ionVal3 += "";

        // Chemical structure = SMILES
        if (ionVal4 != "")
            ionVal4 = ionVal4 + itemSeparator;
        IAtomContainer checmicalStructure = ionAnnotation
                .getChemicalStructure();
        SmilesGenerator sg = SmilesGenerator.generic();

        if (checmicalStructure != null) {
            try {
                ionVal4 += sg.create(checmicalStructure);
                System.out.println(sg.create(checmicalStructure));
            } catch (CDKException e) {
                ionVal4 += "?";
            }
        } else
            ionVal4 += "";
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
