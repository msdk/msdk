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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.FeatureTable;
import io.github.msdk.datamodel.Sample;
import io.github.msdk.datamodel.SimpleFeatureTable;
import io.github.msdk.datamodel.SimpleFeatureTableRow;
import io.github.msdk.datamodel.SimpleSample;
import uk.ac.ebi.pride.jmztab.model.Assay;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.model.MsRun;
import uk.ac.ebi.pride.jmztab.model.SmallMolecule;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;

/**
 * <p>
 * MzTabFileImportMethod class.
 * </p>
 */
public class MzTabFileImportMethod implements MSDKMethod<FeatureTable> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private int parsedRows, totalRows = 0, samples;

  private final @Nonnull File sourceFile;

  private SimpleFeatureTable newFeatureTable;
  private boolean canceled = false;

  private final Map<MsRun, Sample> sampleMap = new Hashtable<>();

  /**
   * <p>
   * Constructor for MzTabFileImportMethod.
   * </p>
   *
   * @param sourceFile a {@link java.io.File} object.
   */
  public MzTabFileImportMethod(@Nonnull File sourceFile) {
    this.sourceFile = sourceFile;
  }

  /** {@inheritDoc} */
  @Override
  public FeatureTable execute() throws MSDKException {

    logger.info("Started parsing file " + sourceFile);

    // Check if the file is readable
    if (!sourceFile.canRead()) {
      throw new MSDKException("Cannot read file " + sourceFile);
    }

    newFeatureTable = new SimpleFeatureTable();

    try {
      // Prevent MZTabFileParser from writing to console
      OutputStream logStream = ByteStreams.nullOutputStream();

      // Load mzTab file
      MZTabFileParser mzTabFileParser = new MZTabFileParser(sourceFile, logStream);

      MZTabFile mzTabFile = mzTabFileParser.getMZTabFile();

      if (mzTabFile == null) {
        return null;
      }

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

    logger.info("Finished parsing " + sourceFile + ", parsed " + samples + " samples and "
        + totalRows + " features.");

    return newFeatureTable;

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
    return totalRows == 0 ? null : (float) parsedRows / totalRows;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    this.canceled = true;
  }

  private void addColumns(@Nonnull FeatureTable featureTable, @Nonnull MZTabFile mzTabFile) {

    // Sample specific columns
    SortedMap<Integer, MsRun> msrun = mzTabFile.getMetadata().getMsRunMap();
    List<Sample> allSamples = new ArrayList<>();
    for (Entry<Integer, MsRun> entry : msrun.entrySet()) {

      // Sample
      File file = new File(entry.getValue().getLocation().getPath());
      String fileName = file.getName();
      Sample sample = new SimpleSample(fileName);
      allSamples.add(sample);
      sampleMap.put(entry.getValue(), sample);
    }
    newFeatureTable.setSamples(allSamples);
  }

  private void addRows(@Nonnull FeatureTable featureTable, @Nonnull MZTabFile mzTabFile) {

    // Loop through small molecules data
    Collection<SmallMolecule> smallMolecules = mzTabFile.getSmallMolecules();
    for (SmallMolecule smallMolecule : smallMolecules) {
      parsedRows++;
      SimpleFeatureTableRow currentRow = new SimpleFeatureTableRow(featureTable);

      currentRow.setCharge(smallMolecule.getCharge());

      // Add data to sample specific columns
      SortedMap<Integer, Assay> assayMap = mzTabFile.getMetadata().getAssayMap();
      for (Entry<Integer, Assay> entry : assayMap.entrySet()) {
        Assay sampleAssay = assayMap.get(entry.getKey());

        Sample sample = sampleMap.get(sampleAssay.getMsRun());
        MzTabFeature newFeature = new MzTabFeature(smallMolecule, sampleAssay);
        currentRow.setFeature(sample, newFeature);

      }

      // Add row to feature table
      newFeatureTable.addRow(currentRow);

      // Check if cancel is requested
      if (canceled)
        return;


    }
  }

}
