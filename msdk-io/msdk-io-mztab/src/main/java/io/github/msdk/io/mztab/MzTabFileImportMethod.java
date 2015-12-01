/* 
 * (C) Copyright 2015 by MSDK Development Team
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

package io.github.msdk.io.mztab;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;
import com.google.common.math.DoubleMath;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.SeparationType;
import uk.ac.ebi.pride.jmztab.model.Assay;
import uk.ac.ebi.pride.jmztab.model.MZTabColumn;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.model.MsRun;
import uk.ac.ebi.pride.jmztab.model.SmallMolecule;
import uk.ac.ebi.pride.jmztab.model.SplitList;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;

public class MzTabFileImportMethod implements MSDKMethod<FeatureTable> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private int parsedRows, totalRows = 0, samples;

    private final @Nonnull File sourceFile;
    private final @Nonnull DataPointStore dataStore;

    private FeatureTable newFeatureTable;
    private boolean canceled = false;
    private Map<String, FeatureTableColumn<?>> tableColumns = new HashMap<String, FeatureTableColumn<?>>();
    private ArrayList<String> columnNameArray = new ArrayList<String>();

    public MzTabFileImportMethod(@Nonnull File sourceFile,
            @Nonnull DataPointStore dataStore) {
        this.sourceFile = sourceFile;
        this.dataStore = dataStore;
    }

    @SuppressWarnings("null")
    @Override
    public FeatureTable execute() throws MSDKException {

        logger.info("Started parsing file " + sourceFile);

        // Check if the file is readable
        if (!sourceFile.canRead()) {
            throw new MSDKException("Cannot read file " + sourceFile);
        }

        String fileName = sourceFile.getName();
        newFeatureTable = MSDKObjectBuilder
                .getFeatureTable(fileName, dataStore);

        try {
            // Prevent MZTabFileParser from writing to console
            OutputStream logStream = ByteStreams.nullOutputStream();

            // Load mzTab file
            MZTabFileParser mzTabFileParser = new MZTabFileParser(sourceFile,
                    logStream);

            MZTabFile mzTabFile = mzTabFileParser.getMZTabFile();

            // Let's say the initial parsing took 10% of the time
            totalRows = mzTabFile.getSmallMolecules().size();
            samples = mzTabFile.getMetadata().getMsRunMap().size();

            // Check if cancel is requested
            if (canceled) {
                return null;
            }

            // Add the columns to the table
            addColumns(newFeatureTable, mzTabFile);

            // Check if cancel is requested
            if (canceled) {
                return null;
            }

            // Add the rows to the table (= import small molecules)
            addRows(newFeatureTable, mzTabFile);

            // Check if cancel is requested
            if (canceled) {
                return null;
            }

        } catch (Exception e) {
            throw new MSDKException(e);
        }

        logger.info("Finished parsing " + sourceFile + ", parsed " + samples
                + " samples and " + totalRows + " features.");

        return newFeatureTable;

    }

    @Override
    @Nullable
    public FeatureTable getResult() {
        return newFeatureTable;
    }

    @Override
    public Float getFinishedPercentage() {
        return totalRows == 0 ? null : (float) parsedRows / totalRows;
    }

    @Override
    public void cancel() {
        this.canceled = true;
    }

    @SuppressWarnings("null")
    private void addColumns(@Nonnull FeatureTable featureTable,
            @Nonnull MZTabFile mzTabFile) {
        // Common columns
        FeatureTableColumn<Integer> idColumn = MSDKObjectBuilder
                .getIdFeatureTableColumn();
        FeatureTableColumn<Double> mzColumn = MSDKObjectBuilder
                .getMzFeatureTableColumn();
        FeatureTableColumn<ChromatographyInfo> chromatographyInfoColumn = MSDKObjectBuilder
                .getChromatographyInfoFeatureTableColumn();
        FeatureTableColumn<IonAnnotation> ionAnnotationColumn = MSDKObjectBuilder
                .getIonAnnotationFeatureTableColumn();
        FeatureTableColumn<Integer> chargeColumn = MSDKObjectBuilder
                .getChargeFeatureTableColumn();
        newFeatureTable.addColumn(idColumn);
        newFeatureTable.addColumn(mzColumn);
        newFeatureTable.addColumn(chromatographyInfoColumn);
        newFeatureTable.addColumn(ionAnnotationColumn);
        newFeatureTable.addColumn(chargeColumn);

        // Sample specific columns
        FeatureTableColumn<?> column;
        SortedMap<Integer, MsRun> msrun = mzTabFile.getMetadata().getMsRunMap();
        for (Entry<Integer, MsRun> entry : msrun.entrySet()) {

            // Sample
            File file = new File(entry.getValue().getLocation().getPath());
            String fileName = file.getName();
            Sample sample = MSDKObjectBuilder.getSimpleSample(fileName);
            int msRunKey = entry.getKey();

            // Abundance (= Area) column
            column = MSDKObjectBuilder.getFeatureTableColumn(ColumnName.AREA,
                    sample);
            newFeatureTable.addColumn(column);
            tableColumns.put(
                    "[" + entry.getKey() + "]_" + ColumnName.AREA.getName(),
                    column);

            // Optional columns
            String mzTabColumnName;
            SortedMap<String, MZTabColumn> columnsMap = mzTabFile
                    .getSmallMoleculeColumnFactory().getOptionalColumnMapping();
            for (Entry<String, MZTabColumn> entry2 : columnsMap.entrySet()) {
                mzTabColumnName = entry2.getValue().getName();
                column = getFeatureTableColumn(mzTabColumnName, sample,
                        msRunKey);

                if (column != null) {
                    newFeatureTable.addColumn(column);
                    tableColumns.put(mzTabColumnName, column);

                    // Add column name to array
                    addNameToColumnArray(mzTabColumnName);
                }
            }

            // Check if cancel is requested
            if (canceled) {
                return;
            }

        }
    }

    @SuppressWarnings({ "unchecked", "null" })
    private void addRows(@Nonnull FeatureTable featureTable,
            @Nonnull MZTabFile mzTabFile) {

        String formula, smile, inchiKey, description, database, identifier;
        // String dbVersion, reliability, url;
        Double mzCalc = null, featureArea = null;
        @Nonnull
        Double mzExp;
        Float rtAverageValue = null;
        Integer charge = null;

        // Loop through small molecules data
        Collection<SmallMolecule> smallMolecules = mzTabFile
                .getSmallMolecules();
        for (SmallMolecule smallMolecule : smallMolecules) {
            parsedRows++;
            FeatureTableColumn column;
            FeatureTableRow currentRow = MSDKObjectBuilder.getFeatureTableRow(
                    featureTable, parsedRows);

            formula = smallMolecule.getChemicalFormula();
            smile = smallMolecule.getSmiles().toString();
            inchiKey = smallMolecule.getInchiKey().toString();
            description = smallMolecule.getDescription();
            database = smallMolecule.getDatabase();
            // dbVersion = smallMolecule.getDatabaseVersion();
            identifier = smallMolecule.getIdentifier().toString();
            SplitList<Double> rt = smallMolecule.getRetentionTime();
            mzExp = smallMolecule.getExpMassToCharge();

            // if (smallMolecule.getReliability() != null)
            // reliability = smallMolecule.getReliability().toString();
            // if (smallMolecule.getURI() != null)
            // url = smallMolecule.getURI().toString();
            if (smallMolecule.getCalcMassToCharge() != null)
                mzCalc = smallMolecule.getCalcMassToCharge();
            if (smallMolecule.getCharge() != null)
                charge = smallMolecule.getCharge();

            // Calculate average RT if multiple values are available
            if (rt != null && !rt.isEmpty())
                rtAverageValue = (float) DoubleMath.mean(rt);

            // Chromatography Info
            ChromatographyInfo chromatographyInfo = MSDKObjectBuilder
                    .getChromatographyInfo1D(SeparationType.UNKNOWN,
                            rtAverageValue);

            // Ion Annotation
            IonAnnotation ionAnnotation = MSDKObjectBuilder
                    .getSimpleIonAnnotation();
            ionAnnotation.setAnnotationId(database);
            ionAnnotation.setDescription(description);
            ionAnnotation.setExpectedMz(mzCalc);
            ionAnnotation.setAnnotationId(identifier);

            // Convert formula to IMolecularFormula using CDK
            if (formula != null) {
                IChemObjectBuilder builder = DefaultChemObjectBuilder
                        .getInstance();
                IMolecularFormula cdkFormula = MolecularFormulaManipulator
                        .getMolecularFormula(formula, builder);
                ionAnnotation.setFormula(cdkFormula);
            }

            // Convert smile & INCHIkey to IAtomContainer using CDK
            if ((smile != null && !smile.equals(""))
                    || (inchiKey != null && !inchiKey.equals(""))) {
                /*
                 * TODO: Add checmical structure
                 */
                // ionAnnotation.setChemicalStructure();
            }

            // Add common data to columns
            // Common column: Id
            column = featureTable.getColumn(ColumnName.ID, null);
            currentRow.setData(column, parsedRows);

            // Common column: m/z
            column = featureTable.getColumn(ColumnName.MZ, null);
            currentRow.setData(column, mzExp);

            // Common column: Chromatography Info
            column = featureTable.getColumn("Chromatography Info", null,
                    ChromatographyInfo.class);
            currentRow.setData(column, chromatographyInfo);

            // Common column: Ion Annotation
            column = featureTable.getColumn("Ion Annotation", null,
                    IonAnnotation.class);
            currentRow.setData(column, ionAnnotation);

            // Common column: Charge
            column = featureTable.getColumn(ColumnName.CHARGE, null);
            if (charge != null)
                currentRow.setData(column, charge);

            // Add data to sample specific columns
            SortedMap<Integer, Assay> assayMap = mzTabFile.getMetadata()
                    .getAssayMap();
            for (Entry<Integer, Assay> entry : assayMap.entrySet()) {
                Assay sampleAssay = assayMap.get(entry.getKey());
                int sampleKey = sampleAssay.getId();

                // Abundance - Area
                if (smallMolecule.getAbundanceColumnValue(sampleAssay) != null) {
                    featureArea = Double.parseDouble(smallMolecule
                            .getAbundanceColumnValue(sampleAssay).toString());
                    column = tableColumns.get("[" + sampleKey + "]_"
                            + ColumnName.AREA.getName());
                    currentRow.setData(column, featureArea);
                }

                // Optional columns
                for (String columnName : columnNameArray) {
                    if (smallMolecule.getOptionColumnValue(sampleAssay,
                            columnName) != null) {

                        column = tableColumns.get("opt_assay[" + sampleKey
                                + "]_" + columnName);
                        String classType = getDataTypeClass(columnName)
                                .getSimpleName();
                        String orgValue = smallMolecule.getOptionColumnValue(
                                sampleAssay, columnName);
                        switch (classType) {
                        case "Float":
                            Float floatValue = Float.parseFloat(orgValue);
                            currentRow.setData(column, floatValue);
                            break;
                        case "Double":
                            Double doubleValue = Double.parseDouble(orgValue);
                            currentRow.setData(column, doubleValue);

                            break;
                        case "Integer":
                            Integer integerValue = Integer.parseInt(orgValue);
                            currentRow.setData(column, integerValue);
                            break;
                        case "ChromatographyInfo":
                            Float rtValue = Float.parseFloat(orgValue);
                            ChromatographyInfo ciValue = MSDKObjectBuilder
                                    .getChromatographyInfo1D(
                                            SeparationType.UNKNOWN, rtValue);
                            currentRow.setData(column, ciValue);
                            break;
                        }

                    }

                }

            }

            // Add row to feature table
            newFeatureTable.addRow(currentRow);

            // Check if cancel is requested
            if (canceled) {
                return;
            }

        }
    }

    @Nullable
    @SuppressWarnings("null")
    private FeatureTableColumn<?> getFeatureTableColumn(
            @Nonnull String mzTabColumnName, @Nonnull Sample sample,
            int msRunKey) {
        if (mzTabColumnName.contains("[" + msRunKey + "]_")) {
            String[] nameArray = mzTabColumnName.split("]_");
            @Nonnull
            String name = nameArray[1];
            ColumnName columnName = mzTabNameToColumnName(name);
            if (columnName == null) {
                return MSDKObjectBuilder.getFeatureTableColumn(name,
                        String.class, sample);
            } else {
                return MSDKObjectBuilder.getFeatureTableColumn(columnName,
                        sample);
            }
        } else {
            return null;
        }
    }

    private void addNameToColumnArray(String columnName) {
        // Convert names from opt_assay[2]_XYZ to XYZ
        if (columnName.contains("]_")) {
            String[] nameArray = columnName.split("]_");
            String name = nameArray[1];

            if (!columnNameArray.contains(name))
                columnNameArray.add(name);
        }
    }

    @Nullable
    private ColumnName mzTabNameToColumnName(String mzTabColumnName) {
        // Only compare letters in name and ignore case
        String compareName = mzTabColumnName.replaceAll("[^A-Za-z]", "");
        for (ColumnName columnName : ColumnName.values()) {
            if (compareName.toUpperCase().equals(
                    columnName.toString().toUpperCase()))
                return columnName;
        }
        return null;
    }

    @Nonnull
    private Class<?> getDataTypeClass(String mzTabColumnName) {
        return mzTabNameToColumnName(mzTabColumnName).getDataTypeClass();
    }

}
