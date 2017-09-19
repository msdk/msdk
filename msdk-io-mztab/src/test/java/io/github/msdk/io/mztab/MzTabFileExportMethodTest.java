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
import org.junit.Test;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import io.github.msdk.datamodel.FeatureTable;
import io.github.msdk.datamodel.FeatureTableRow;
import io.github.msdk.datamodel.IonAnnotation;
import io.github.msdk.datamodel.Sample;

public class MzTabFileExportMethodTest {

  @Test
  public void testMzTab_Sample() throws Exception {

    // Import the file
    File inputFile =
        new File(this.getClass().getClassLoader().getResource("Sample-2.3.mzTab").toURI());
    Assert.assertTrue(inputFile.canRead());
    MzTabFileImportMethod importer = new MzTabFileImportMethod(inputFile);
    FeatureTable featureTable = importer.execute();
    Assert.assertNotNull(featureTable);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // Create temp file
    File tempFile = File.createTempFile("MZmine_TestFile_", ".mzTab");

    // Export the file
    Assert.assertNotNull(tempFile);
    MzTabFileExportMethod exporter = new MzTabFileExportMethod(featureTable, tempFile, true);
    exporter.execute();
    Assert.assertEquals(1.0, exporter.getFinishedPercentage(), 0.0001);
    Assert.assertTrue(tempFile.canRead());

    // Clean up
    featureTable.dispose();

    // Import the file again
    importer = new MzTabFileImportMethod(tempFile);
    FeatureTable featureTable2 = importer.execute();
    Assert.assertNotNull(featureTable2);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // The table has 7 samples
    List<Sample> samples = featureTable2.getSamples();
    Assert.assertNotNull(samples);
    Assert.assertEquals(7, samples.size());

    // The table has 298 rows
    Assert.assertFalse(featureTable2.getRows().isEmpty());
    Assert.assertEquals(298, featureTable2.getRows().size());

    // ********************
    // Annotation 7 - HEPES
    // ********************
    FeatureTableRow row = featureTable.getRows().get(7);
    IonAnnotation ionAnnotation = row.getFeature(0).getIonAnnotation();
    Assert.assertEquals("HEPES", ionAnnotation.getDescription());
    IMolecularFormula cdkFormula = MolecularFormulaManipulator.getMolecularFormula("C8H18N2O4S",
        SilentChemObjectBuilder.getInstance());
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
    Float area = row.getFeature(sample).getArea();
    Assert.assertNotNull(area);
    Assert.assertEquals(11480605, area, 0.0000001);

    // RT
    Float rt = row.getFeature(0).getRetentionTime();
    Assert.assertEquals(30.324697494506836, rt, 0.0000001);

    // Height
    Float height = row.getFeature(sample).getHeight();
    Assert.assertNull(height);

    // m/z
    Double mz = row.getFeature(sample).getMz();
    Assert.assertNotNull(mz);
    Assert.assertEquals(144.927825927734, mz, 0.0000001);


    // Clean up
    tempFile.delete();
    featureTable2.dispose();
  }

}
