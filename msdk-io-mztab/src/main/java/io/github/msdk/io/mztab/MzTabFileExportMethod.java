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

package io.github.msdk.io.mztab;

import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;
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

import com.google.common.base.Strings;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.MSDKVersion;
import io.github.msdk.datamodel.Feature;
import io.github.msdk.datamodel.FeatureTable;
import io.github.msdk.datamodel.FeatureTableRow;
import io.github.msdk.datamodel.IonAnnotation;
import io.github.msdk.datamodel.Sample;
import uk.ac.ebi.pride.jmztab.model.Assay;
import uk.ac.ebi.pride.jmztab.model.CVParam;
import uk.ac.ebi.pride.jmztab.model.MZTabColumnFactory;
import uk.ac.ebi.pride.jmztab.model.MZTabDescription;
import uk.ac.ebi.pride.jmztab.model.Metadata;
import uk.ac.ebi.pride.jmztab.model.MsRun;
import uk.ac.ebi.pride.jmztab.model.Section;
import uk.ac.ebi.pride.jmztab.model.SmallMolecule;
import uk.ac.ebi.pride.jmztab.model.SmallMoleculeColumn;

/**
 * <p>
 * CsvFileExportMethod class.
 * </p>
 */
public class MzTabFileExportMethod implements MSDKMethod<File> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  // Input variables
  private @Nonnull FeatureTable featureTable;
  private @Nonnull File mzTabFile;
  private @Nonnull Boolean exportAllFeatures;
  private String newLine = System.lineSeparator();
  private String itemSeparator = "|";

  // Other variables
  private int parsedRows, totalRows = 0;
  private boolean canceled = false;

  /**
   * <p>
   * Constructor for MzTabFileExportMethod.
   * </p>
   *
   * @param featureTable a {@link io.github.msdk.datamodel.FeatureTable} object.
   * @param mzTabFile a {@link java.io.File} object.
   * @param exportAllFeatures a {@link java.lang.Boolean} object.
   */
  public MzTabFileExportMethod(@Nonnull FeatureTable featureTable, @Nonnull File mzTabFile,
      @Nonnull Boolean exportAllFeatures) {
    this.featureTable = featureTable;
    this.mzTabFile = mzTabFile;
    this.exportAllFeatures = exportAllFeatures;
  }

  /** {@inheritDoc} */
  @Override
  public File execute() throws MSDKException {

    logger.info("Started exporting feature table to " + mzTabFile);

    // Get number of rows
    totalRows = featureTable.getRows().size();

    // Open file
    FileWriter writer;
    try {
      writer = new FileWriter(mzTabFile);
    } catch (Exception e) {
      logger.info("Could not open file " + mzTabFile + " for writing.");
      return null;
    }

    // jmztab data holders
    Metadata mtd = new Metadata();
    MZTabColumnFactory factory = MZTabColumnFactory.getInstance(Section.Small_Molecule);

    // Write meta data to mzTab file
    writeMetaData(featureTable, writer, mtd, factory);

    // Write sample data to mzTab file
    writeSampleData(featureTable, writer, mtd, factory);

    // Close file
    try {
      writer.close();
    } catch (Exception e) {
      logger.info("Could not close file " + mzTabFile);
      return null;
    }

    return mzTabFile;
  }

  private void writeMetaData(FeatureTable featureTable, FileWriter writer, Metadata mtd,
      MZTabColumnFactory factory) {

    // Meta data
    mtd.setMZTabMode(MZTabDescription.Mode.Summary);
    mtd.setMZTabType(MZTabDescription.Type.Quantification);
    // mtd.setDescription(featureTable.getName());
    mtd.addSoftwareParam(1, new CVParam("MS", "MS:1002342", "MSDK", MSDKVersion.getMSDKVersion()));
    mtd.setSmallMoleculeQuantificationUnit(
        new CVParam("PRIDE", "PRIDE:0000330", "Arbitrary quantification unit", null));
    mtd.addSmallMoleculeSearchEngineScoreParam(1,
        new CVParam("MS", "MS:1001153", "search engine specific score", null));
    mtd.addFixedModParam(1,
        new CVParam("MS", "MS:1002453", "No fixed modifications searched", null));
    mtd.addVariableModParam(1,
        new CVParam("MS", "MS:1002454", "No variable modifications searched", null));

    // Create stable columns - only available in jmztab 3.0.2 and later
    factory.addDefaultStableColumns();

    // Add optional columns which have stable order
    factory.addURIOptionalColumn();
    factory.addBestSearchEngineScoreOptionalColumn(SmallMoleculeColumn.BEST_SEARCH_ENGINE_SCORE, 1);

    // Add sample columns
    List<Sample> samples = featureTable.getSamples();
    int sampleCounter = 0;
    for (Sample sample : samples) {
      sampleCounter++;
      File originalFile = sample.getOriginalFile();

      // MS run location
      MsRun msRun = new MsRun(sampleCounter);
      String filePath = sample.getName();
      if (originalFile != null) {
        filePath = originalFile.getAbsolutePath();
      }

      URL fileURL = null;
      try {
        fileURL = new URL("file:///" + filePath);
      } catch (MalformedURLException e) {
      }

      msRun.setLocation(fileURL);
      mtd.addMsRun(msRun);
      mtd.addAssayMsRun(sampleCounter, msRun);

      // Additional columns
      factory.addAbundanceOptionalColumn(new Assay(sampleCounter));
      factory.addOptionalColumn(new Assay(sampleCounter), "mz", String.class);
      factory.addOptionalColumn(new Assay(sampleCounter), "rt", String.class);
      factory.addOptionalColumn(new Assay(sampleCounter), "height", String.class);
    }

    // Write to file
    try {
      writer.write(mtd.toString());
      writer.write(newLine);
      writer.write(factory.toString());
      writer.write(newLine);
    } catch (Exception e) {
      logger.info("Could not write to file " + mzTabFile);
      return;
    }

    // Cancel?
    if (canceled)
      return;
  }

  private void writeSampleData(FeatureTable featureTable, FileWriter writer, Metadata mtd,
      MZTabColumnFactory factory) {

    // Write data rows
    for (FeatureTableRow row : featureTable.getRows()) {

      // Get ion annotation column

      SmallMolecule sm = new SmallMolecule(factory, mtd);
      Boolean writeFeature = false;

      // Ion annotation variables
      String identifier = "";
      String formula = "";
      String smiles = "";
      String inchiKey = "";
      String description = "";
      String url = "";
      // String database = "";

      // Get ion annotation
      /*
       * for (IonAnnotation ionAnnotation : ionAnnotations) { // Annotation ID String
       * ionAnnotationId = ionAnnotation.getAnnotationId(); if
       * (!Strings.isNullOrEmpty(ionAnnotationId)) { identifier = identifier + itemSeparator +
       * escapeString(ionAnnotationId); writeFeature = true; }
       * 
       * // Formula IMolecularFormula ionFormula = ionAnnotation.getFormula(); if (ionFormula !=
       * null) { formula = formula + itemSeparator +
       * escapeString(MolecularFormulaManipulator.getString(ionFormula)); writeFeature = true; }
       * 
       * // Chemical structure = SMILES IAtomContainer chemicalStructure =
       * ionAnnotation.getChemicalStructure(); if (chemicalStructure != null) { try {
       * SmilesGenerator sg = SmilesGenerator.generic(); smiles = smiles + itemSeparator +
       * sg.create(chemicalStructure); } catch (CDKException e) {
       * logger.info("Could not create SMILE for " + ionAnnotation.getDescription()); } }
       * 
       * // InchiKey String ik = ionAnnotation.getInchiKey(); if (!Strings.isNullOrEmpty(ik)) {
       * inchiKey += itemSeparator + escapeString(ik); writeFeature = true; }
       * 
       * // Description String ionDescription = ionAnnotation.getDescription(); if
       * (!Strings.isNullOrEmpty(ionDescription)) { description = description + itemSeparator +
       * escapeString(ionDescription); writeFeature = true; }
       * 
       * // URL URL ionUrl = ionAnnotation.getAccessionURL(); if (ionUrl != null) { url = url +
       * itemSeparator + escapeString(ionUrl.toString()); writeFeature = true; }
       * 
       * }
       */

      // Write feature to file?
      if (exportAllFeatures || writeFeature) {
        sm.setIdentifier(removeFirstCharacter(identifier));
        sm.setChemicalFormula(removeFirstCharacter(formula));
        sm.setSmiles(removeFirstCharacter(smiles));
        sm.setInchiKey(removeFirstCharacter(inchiKey));
        sm.setDescription(removeFirstCharacter(description));
        sm.setURI(removeFirstCharacter(url));
        // sm.setDatabase(database);

        // Common feature m/z value
        Double rowMZ = row.getMz();
        if (rowMZ != null)
          sm.setExpMassToCharge(rowMZ);

        // Sample specific data
        List<Sample> samples = featureTable.getSamples();
        int sampleCounter = 0;
        Float rt;
        for (Sample sample : samples) {
          sampleCounter++;

          Feature feature = row.getFeature(sample);

          // m/z
          String peakMZ = feature.getMz().toString();
          sm.setOptionColumnValue(new Assay(sampleCounter), "mz", peakMZ);

          // RT
          sm.setRetentionTime(feature.getRetentionTime().toString());
          sm.setOptionColumnValue(new Assay(sampleCounter), "rt", row.getRT().toString());

          // Height
          if (feature.getHeight() != null) {
            String peakHeight = feature.getHeight().toString();
            sm.setOptionColumnValue(new Assay(sampleCounter), "height", peakHeight);
          }

          // Area
          if (feature.getArea() != null) {
            Float peakArea = feature.getArea();
            sm.setAbundanceColumnValue(new Assay(sampleCounter), peakArea.doubleValue());
          }

        }

        // Write to file
        try {
          writer.write(sm.toString());
          writer.write(newLine);
        } catch (Exception e) {
          logger.info("Could not write to file " + mzTabFile);
          return;
        }
      }

      // Cancel?
      if (canceled)
        return;

      parsedRows++;
    }

  }

  private String escapeString(final String inputString) {
    if (inputString == null)
      return "";

    // Remove all special characters e.g. \n \t
    return inputString.replaceAll("[\\p{Cntrl}]", " ");
  }

  /**
   * <p>
   * removeFirstCharacter.
   * </p>
   *
   * @param str a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  private String removeFirstCharacter(String str) {
    if (str.length() > 0) {
      str = str.substring(1, str.length());
    }
    return str;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public File getResult() {
    return mzTabFile;
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
