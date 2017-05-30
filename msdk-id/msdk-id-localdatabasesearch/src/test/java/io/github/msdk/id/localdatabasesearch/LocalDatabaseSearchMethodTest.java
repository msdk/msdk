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

package io.github.msdk.id.localdatabasesearch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import com.google.common.base.Strings;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.impl.SimpleIonAnnotation;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.ionannotations.IonType;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.io.mztab.MzTabFileImportMethod;
import io.github.msdk.util.IonTypeUtil;
import io.github.msdk.util.tolerances.MaximumMzTolerance;
import io.github.msdk.util.tolerances.MzTolerance;
import io.github.msdk.util.tolerances.RTTolerance;

public class LocalDatabaseSearchMethodTest {

  private static final String TEST_DATA_PATH = "src/test/resources/";


  @Test
  public void identificationTest() throws MSDKException {

    // Create the data structures
    DataPointStore dataStore = DataPointStoreFactory.getMemoryDataStore();

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "Sample-2.3_Small.mzTab");
    Assert.assertTrue("Cannot read test data", inputFile.canRead());
    MzTabFileImportMethod importer = new MzTabFileImportMethod(inputFile, dataStore);
    FeatureTable featureTable = importer.execute();
    Assert.assertNotNull(featureTable);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // Verify that none of the features have an ion annotation
    FeatureTableColumn<List<SimpleIonAnnotation>> column =
        featureTable.getColumn(ColumnName.IONANNOTATION, null);
    int annotatedFeatures = 0;
    for (FeatureTableRow row : featureTable.getRows()) {
      List<SimpleIonAnnotation> ionAnnotations = row.getData(column);
      if (ionAnnotations != null)
        for (IonAnnotation annot : ionAnnotations)
          if (!Strings.isNullOrEmpty(annot.getDescription()))
            annotatedFeatures++;
    }
    Assert.assertEquals(0, annotatedFeatures);

    // Variables
    final MzTolerance mzTolerance = new MaximumMzTolerance(0.003, 5.0);
    final RTTolerance rtTolerance = new RTTolerance(0.1f, false);
    List<SimpleIonAnnotation> ionAnnotations = new ArrayList<SimpleIonAnnotation>();
    IMolecularFormula formula;

    // Ion 1
    SimpleIonAnnotation ion1 = new SimpleIonAnnotation();
    ion1.setAnnotationId("1");
    ion1.setExpectedMz(508.0047259);
    ion1.setDescription("ATP");
    ion1.setExpectedRetentionTime(814.4f);
    formula = MolecularFormulaManipulator.getMolecularFormula("C10H16N5O13P3",
        DefaultChemObjectBuilder.getInstance());
    ion1.setFormula(formula);
    ion1.setIonType(IonTypeUtil.createIonType("[M+2H]2+"));
    ionAnnotations.add(ion1);

    // Ion 2
    SimpleIonAnnotation ion2 = new SimpleIonAnnotation();
    ion2.setAnnotationId("2");
    ion2.setExpectedMz(613.1625235);
    ion2.setDescription("Glutathione disulfide");
    ion2.setExpectedRetentionTime(878.1f);
    formula = MolecularFormulaManipulator.getMolecularFormula("C20H32N6O12S2",
        DefaultChemObjectBuilder.getInstance());
    ion2.setFormula(formula);
    ion2.setIonType(IonTypeUtil.createIonType("[M+NH4]+"));
    ionAnnotations.add(ion2);

    // Ion 3
    SimpleIonAnnotation ion3 = new SimpleIonAnnotation();
    ion3.setAnnotationId("3");
    ion3.setExpectedMz(239.1068954);
    ion3.setDescription("HEPES");
    ion3.setExpectedRetentionTime(409.0f);
    ionAnnotations.add(ion3);

    // Ion 4
    SimpleIonAnnotation ion4 = new SimpleIonAnnotation();
    ion4.setAnnotationId("4");
    ion4.setExpectedMz(175.1194502);
    ion4.setDescription("L-Arginine");
    ion4.setExpectedRetentionTime(1368.9f);
    formula = MolecularFormulaManipulator.getMolecularFormula("C6H14N4O2",
        DefaultChemObjectBuilder.getInstance());
    ion4.setFormula(formula);
    ionAnnotations.add(ion4);

    // Ion 5
    SimpleIonAnnotation ion5 = new SimpleIonAnnotation();
    ion5.setAnnotationId("5");
    ion5.setExpectedMz(664.118587);
    ion5.setDescription("NAD+");
    ion5.setExpectedRetentionTime(680.8f);
    formula = MolecularFormulaManipulator.getMolecularFormula("C21H28N7O14P2",
        DefaultChemObjectBuilder.getInstance());
    ion5.setFormula(formula);
    ionAnnotations.add(ion5);

    // Ion 6
    SimpleIonAnnotation ion6 = new SimpleIonAnnotation();
    ion6.setAnnotationId("6");
    ion6.setExpectedMz(666.1328387);
    ion6.setDescription("NADH");
    ion6.setExpectedRetentionTime(633.5f);
    formula = MolecularFormulaManipulator.getMolecularFormula("C21H29N7O14P2",
        DefaultChemObjectBuilder.getInstance());
    ion6.setFormula(formula);
    ionAnnotations.add(ion6);

    // Ion 7
    SimpleIonAnnotation ion7 = new SimpleIonAnnotation();
    ion7.setAnnotationId("7");
    ion7.setExpectedMz(303.0693468);
    ion7.setDescription("PIPES");
    ion7.setExpectedRetentionTime(661.7f);
    ionAnnotations.add(ion7);

    // Run identification using the local database search
    LocalDatabaseSearchMethod method =
        new LocalDatabaseSearchMethod(featureTable, ionAnnotations, mzTolerance, rtTolerance);
    method.execute();
    Assert.assertEquals(1.0, method.getFinishedPercentage(), 0.0001);
    Assert.assertNotNull(featureTable);

    // Verify that 7 of the features have an ion annotation
    for (FeatureTableRow row : featureTable.getRows()) {
      ionAnnotations = (List<SimpleIonAnnotation>) row.getData(column);
      if (ionAnnotations != null)
        for (IonAnnotation annot : ionAnnotations)
          if (!Strings.isNullOrEmpty(annot.getDescription()))
            annotatedFeatures++;
    }
    Assert.assertEquals(7, annotatedFeatures);

    // Verify ion 2
    ionAnnotations = featureTable.getRows().get(1).getData(column);
    Assert.assertNotNull(ionAnnotations);
    IonAnnotation ionAnnotation = ionAnnotations.get(0);

    Assert.assertEquals("3", ionAnnotation.getAnnotationId());
    Assert.assertEquals("HEPES", ionAnnotation.getDescription());
    Double mz = ionAnnotation.getExpectedMz();
    Assert.assertNotNull(mz);
    Assert.assertEquals(239.1068954, mz, 0.0001);
    IonType ionType = ionAnnotation.getIonType();
    Assert.assertNull(ionType);

    // Verify ion 8
    ionAnnotations = featureTable.getRows().get(7).getData(column);
    Assert.assertNotNull(ionAnnotations);
    ionAnnotation = ionAnnotations.get(0);
    Assert.assertEquals("2", ionAnnotation.getAnnotationId());
    Assert.assertEquals("Glutathione disulfide", ionAnnotation.getDescription());
    mz = ionAnnotation.getExpectedMz();
    Assert.assertNotNull(mz);
    Assert.assertEquals(613.1625235, mz, 0.0001);
    ionType = ionAnnotation.getIonType();
    Assert.assertNotNull(ionType);
    Assert.assertEquals("H4N", ionType.getAdductFormula());
    Assert.assertEquals("[M+NH4]+", ionType.getName());
    Assert.assertEquals(1, ionType.getNumberOfMolecules());
    Assert.assertEquals(PolarityType.POSITIVE, ionType.getPolarity());
    IMolecularFormula ionFormula = ionAnnotation.getFormula();
    Assert.assertEquals("C20H32N6O12S2", MolecularFormulaManipulator.getString(ionFormula));

    featureTable.dispose();
  }

}
