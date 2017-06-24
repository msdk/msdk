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
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;

public class MzTabFileImportMethodTest {

  private static final String TEST_DATA_PATH = "src/test/resources/";

  @Ignore
  @Test
  public void testMzTab_Sample() throws MSDKException {

    // Create the data structures
    DataPointStore dataStore = DataPointStoreFactory.getTmpFileDataStore();

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "Sample-2.3.mzTab");
    Assert.assertTrue(inputFile.canRead());
    MzTabFileImportMethod importer = new MzTabFileImportMethod(inputFile);
    FeatureTable featureTable = importer.execute();
    Assert.assertNotNull(featureTable);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // The table has 7 samples
    List<Sample> samples = featureTable.getSamples();
    Assert.assertNotNull(samples);
    Assert.assertEquals(7, samples.size());

    // The table has 298 rows
    Assert.assertFalse(featureTable.getRows().isEmpty());
    Assert.assertEquals(298, featureTable.getRows().size());

    // ********************
    // Annotation 7 - HEPES
    // ********************
    FeatureTableRow row = featureTable.getRows().get(7);
    IonAnnotation ionAnnotation = row.getFeature(0).getIonAnnotation();
    Assert.assertEquals("HEPES", ionAnnotation.getDescription());
    IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
    IMolecularFormula cdkFormula =
        MolecularFormulaManipulator.getMolecularFormula("C8H18N2O4S", builder);
    String formula = MolecularFormulaManipulator.getString(ionAnnotation.getFormula());
    String formula2 = MolecularFormulaManipulator.getString(cdkFormula);
    Assert.assertTrue(formula.equals(formula2));

    // ********************
    // Row 5
    // ********************
    row = featureTable.getRows().get(5);
    Double averageMZ = row.getMz();
    Assert.assertNotNull(averageMZ);
    Assert.assertEquals(520.334738595145, averageMZ, 0.0000001);
    Sample sample = featureTable.getSamples().get(5);
    Assert.assertEquals("36C sample 2", sample.getName());

    // ********************
    // Last row
    // ********************
    row = featureTable.getRows().get(297);
    sample = featureTable.getSamples().get(5);

    // Area
    Float area = row.getFeature(0).getArea();
    Assert.assertNotNull(area);
    Assert.assertEquals(11480605.3259747, area, 0.0000001);

    // RT
    Float rt = row.getFeature(0).getRetentionTime();
    Assert.assertEquals(30.324697494506836, rt, 0.0000001);

    // Height
    Float height = row.getFeature(0).getHeight();
    Assert.assertNull(height);

    // m/z
    Double mz = row.getFeature(0).getMz();
    Assert.assertNotNull(mz);
    Assert.assertEquals(144.927825927734, mz, 0.0000001);

    featureTable.dispose();
  }

  @Ignore
  @Test
  public void testMzTab_Lipidomics() throws MSDKException {

    // Create the data structures
    DataPointStore dataStore = DataPointStoreFactory.getTmpFileDataStore();

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "lipidomics-HFD-LD-study-TG.mzTab");
    Assert.assertTrue(inputFile.canRead());
    MzTabFileImportMethod importer = new MzTabFileImportMethod(inputFile);
    FeatureTable featureTable = importer.execute();
    Assert.assertNotNull(featureTable);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // The table has 18 samples
    List<Sample> samples = featureTable.getSamples();
    Assert.assertNotNull(samples);
    Assert.assertEquals(18, samples.size());

    featureTable.dispose();

    // Import the file
    inputFile = new File(TEST_DATA_PATH + "lipidomics-HFD-LD-study-PL-DG-SM.mzTab");
    Assert.assertTrue(inputFile.canRead());
    importer = new MzTabFileImportMethod(inputFile);
    featureTable = importer.execute();
    Assert.assertNotNull(featureTable);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // The table has 109 rows
    Assert.assertFalse(featureTable.getRows().isEmpty());
    Assert.assertEquals(109, featureTable.getRows().size());

    // Annotation 27 - PC32:1
    FeatureTableRow row = featureTable.getRows().get(27);
    IonAnnotation ionAnnotation = row.getFeature(0).getIonAnnotation();
    Assert.assertEquals("PC32:1", ionAnnotation.getDescription());
    IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
    IMolecularFormula cdkFormula =
        MolecularFormulaManipulator.getMolecularFormula("C40H78P1O8N1", builder);
    String formula = MolecularFormulaManipulator.getString(ionAnnotation.getFormula());
    String formula2 = MolecularFormulaManipulator.getString(cdkFormula);
    Assert.assertTrue(formula.equals(formula2));

    featureTable.dispose();
  }
}
