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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.isotopes.tracing.data.constants.PathConstants;
import junit.framework.TestCase;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class AnalyseMassShiftsTest extends TestCase {

  private static final Logger LOGGER = LoggerFactory.getLogger(AnalyseMassShiftsTest.class);
  private File[] testFiles = new File(PathConstants.TEST_RESOURCES.toAbsolutePath()).listFiles();

  public void testAnalyseNaturalMassShifts() {
    for (File file : testFiles) {
      if (file.getName().contains(this.getClass().getSimpleName())) {
        LOGGER.info("Checking natural MSD for file " + file.getName());
        MSShiftDatabase msShiftDatabase = new MSShiftDatabase(file.getAbsolutePath());
        MassSpectrum naturalSpectrum = msShiftDatabase.getNaturalSpectrum();
        MassShiftDataSet expectedMassShiftDataset = msShiftDatabase.getNaturalMassShifts();
        ElementList availableElements =
            ElementList.fromFormula(msShiftDatabase.getCompoundFormula());
        MassShiftDataSet actualMassShiftDataset =
            naturalSpectrum.analyseMassShifts(availableElements);
        LOGGER.info("actualMassShiftDataset:\t" + actualMassShiftDataset);
        LOGGER.info("expectedMassShiftDataset:\t" + expectedMassShiftDataset);
        assertTrue(
            expectedMassShiftDataset.equalsUpToPermutationOfIsotopes(actualMassShiftDataset));
      }
    }
  }

  public void testAnalyseMarkedMassShifts() {
    for (File file : testFiles) {
      if (file.getName().contains(this.getClass().getSimpleName())) {
        LOGGER.info("Checking marked MSD for file" + file.getName());
        MSShiftDatabase msShiftDatabase = new MSShiftDatabase(file.getAbsolutePath());
        MassSpectrum markedSpectrum = msShiftDatabase.getMarkedSpectrum();
        MassShiftDataSet expectedMassShiftDataset = msShiftDatabase.getMarkedMassShifts();
        ElementList availableElements =
            ElementList.fromFormula(msShiftDatabase.getCompoundFormula());
        MassShiftDataSet actualMassShiftDataset =
            markedSpectrum.analyseMassShifts(availableElements);
        LOGGER.info("actualMassShiftDataset:\t" + actualMassShiftDataset);
        LOGGER.info("expectedMassShiftDataset:\t" + expectedMassShiftDataset);
        assertTrue(
            expectedMassShiftDataset.equalsUpToPermutationOfIsotopes(actualMassShiftDataset));
      }
    }
  }

  public void testAnalyseMixedMassShifts() {
    for (File file : testFiles) {
      if (file.getName().contains(this.getClass().getSimpleName())) {
        LOGGER.info("Checking mixed MSD for file" + file.getName());
        MSShiftDatabase msShiftDatabase = new MSShiftDatabase(file.getAbsolutePath());
        MassSpectrum mixedSpectrum = msShiftDatabase.getMixedSpectrum();
        MassShiftDataSet expectedMassShiftDataset = msShiftDatabase.getMixedMassShifts();
        ElementList availableElements =
            ElementList.fromFormula(msShiftDatabase.getCompoundFormula());
        MassShiftDataSet actualMassShiftDataset =
            mixedSpectrum.analyseMassShifts(availableElements);
        LOGGER.info("actualMassShiftDataset:\t" + actualMassShiftDataset);
        LOGGER.info("expectedMassShiftDataset:\t" + expectedMassShiftDataset);
        assertTrue(
            expectedMassShiftDataset.equalsUpToPermutationOfIsotopes(actualMassShiftDataset));
      }
    }
  }
}
