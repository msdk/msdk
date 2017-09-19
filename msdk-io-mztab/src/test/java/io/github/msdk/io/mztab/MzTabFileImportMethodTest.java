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

public class MzTabFileImportMethodTest {

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
    Assert.assertNotNull(ionAnnotation);
    Assert.assertNotNull(ionAnnotation.getDescription());
    Assert.assertNotNull(ionAnnotation.getFormula());
    Assert.assertEquals("HEPES", ionAnnotation.getDescription());
    IMolecularFormula cdkFormula = MolecularFormulaManipulator.getMolecularFormula("C8H18N2O4S",
        SilentChemObjectBuilder.getInstance());
    Assert.assertTrue(MolecularFormulaManipulator.compare(cdkFormula, ionAnnotation.getFormula()));

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

    // Area
    Float area = row.getFeature(sample).getArea();
    Assert.assertNotNull(area);
    Assert.assertEquals(1.1480605E7, area, 0.1);

    // RT
    Float rt = row.getFeature(0).getRetentionTime();
    Assert.assertEquals(30.324697494506836, rt, 0.0000001);

    // Height
    Float height = row.getFeature(sample).getHeight();
    Assert.assertNull(height);

    // m/z
    Double mz = row.getFeature(sample).getMz();
    Assert.assertNotNull(mz);
    Assert.assertEquals(144.92782592773438, mz, 0.0000001);

    featureTable.dispose();
  }

  @Test
  public void testMzTab_Lipidomics() throws Exception {

    // Import the file
    File inputFile = new File(
        this.getClass().getClassLoader().getResource("lipidomics-HFD-LD-study-TG.mzTab").toURI());
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
  }

  @Test
  public void testMzTab_Lipidomics2() throws Exception {


    // Import the file
    File inputFile = new File(this.getClass().getClassLoader()
        .getResource("lipidomics-HFD-LD-study-PL-DG-SM.mzTab").toURI());
    Assert.assertTrue(inputFile.canRead());
    MzTabFileImportMethod importer = new MzTabFileImportMethod(inputFile);
    FeatureTable featureTable = importer.execute();
    Assert.assertNotNull(featureTable);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // The table has 109 rows
    Assert.assertFalse(featureTable.getRows().isEmpty());
    Assert.assertEquals(109, featureTable.getRows().size());

    // Annotation 27 - PC32:1
    FeatureTableRow row = featureTable.getRows().get(27);
    IonAnnotation ionAnnotation = row.getFeature(0).getIonAnnotation();
    Assert.assertNotNull(ionAnnotation);
    Assert.assertNotNull(ionAnnotation.getFormula());
    Assert.assertEquals("PC32:1", ionAnnotation.getDescription());
    IMolecularFormula cdkFormula = MolecularFormulaManipulator.getMolecularFormula("C40H78P1O8N1",
        SilentChemObjectBuilder.getInstance());
    Assert.assertTrue(MolecularFormulaManipulator.compare(cdkFormula, ionAnnotation.getFormula()));

    featureTable.dispose();
  }
}
