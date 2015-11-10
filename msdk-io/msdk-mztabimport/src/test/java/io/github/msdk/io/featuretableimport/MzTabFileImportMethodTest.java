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

package io.github.msdk.io.featuretableimport;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.datapointstore.DataPointStoreFactory;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.io.featuretableimport.mztab.MzTabFileImportMethod;

public class MzTabFileImportMethodTest {

	private static final String TEST_DATA_PATH = "src/test/resources/";

	@SuppressWarnings({ "null", "unchecked" })
	@Test
	public void testMzTab_Sample() throws MSDKException {

		// Create the data structures
		DataPointStore dataStore = DataPointStoreFactory.getTmpFileDataPointStore();

		// Import the file
		File inputFile = new File(TEST_DATA_PATH + "Sample-2.3.mzTab");
		Assert.assertTrue(inputFile.canRead());
		MzTabFileImportMethod importer = new MzTabFileImportMethod(inputFile, dataStore);
		FeatureTable featureTable = importer.execute();
		Assert.assertNotNull(featureTable);
		Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

		// The table has 7 samples
		List<Sample> samples = featureTable.getSamples();
		Assert.assertNotNull(samples);
		Assert.assertEquals(7, samples.size());

		// The table has columns
		Assert.assertFalse(featureTable.getColumns().isEmpty());

		// The table has 298 rows
		Assert.assertFalse(featureTable.getRows().isEmpty());
		Assert.assertEquals(298, featureTable.getRows().size());

		// Annotation 7 - HEPES
		FeatureTableRow row = featureTable.getRows().get(7);
		FeatureTableColumn column = featureTable.getColumn("Ion Annotation", null);
		IonAnnotation ionAnnotation = (IonAnnotation) row.getData(column, IonAnnotation.class);
		Assert.assertEquals("HEPES", ionAnnotation.getDescription());
		IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
		IMolecularFormula cdkFormula = MolecularFormulaManipulator.getMolecularFormula("C8H18N2O4S", builder);
		String formula = MolecularFormulaManipulator.getString(ionAnnotation.getFormula());
		String formula2 = MolecularFormulaManipulator.getString(cdkFormula);
		Assert.assertTrue(formula.equals(formula2));

		// Row 5
		row = featureTable.getRows().get(5);
		column = featureTable.getColumn(ColumnName.MZ.getName(), null);
		Double averageMZ = (Double) row.getData(column, Double.class);
		Assert.assertEquals(520.334738595145, averageMZ, 0.0000001);
		Sample sample = featureTable.getSamples().get(5);
		Assert.assertEquals("36C sample 2", sample.getName());

		// Last row
		row = featureTable.getRows().get(297);
		sample = featureTable.getSamples().get(5);
		// Area
		column = featureTable.getColumn(ColumnName.AREA.getName(), sample);
		Double area = (Double) row.getData(column, Double.class);
		Assert.assertEquals(11480605.3259747, area, 0.0000001);
		// RT
		column = featureTable.getColumn(ColumnName.RT.getName(), sample);
		ChromatographyInfo chromatographyInfo = (ChromatographyInfo) row.getData(column, ChromatographyInfo.class);
		Float rt = chromatographyInfo.getRetentionTime();
		Assert.assertEquals(30.2350216666666, rt, 0.0000001);
		// Height
		column = featureTable.getColumn(ColumnName.HEIGHT.getName(), sample);
		Double height = (Double) row.getData(column, Double.class);
		Assert.assertEquals(312942.149147727, height, 0.0000001);
		// m/z
		column = featureTable.getColumn(ColumnName.MZ.getName(), sample);
		Double mz = (Double) row.getData(column, Double.class);
		Assert.assertEquals(144.927825927734, mz, 0.0000001);

		featureTable.dispose();
	}

	@SuppressWarnings("null")
	@Test
	public void testMzTab_Lipidomics() throws MSDKException {

		// Create the data structures
		DataPointStore dataStore = DataPointStoreFactory.getTmpFileDataPointStore();

		// Import the file
		File inputFile = new File(TEST_DATA_PATH + "lipidomics-HFD-LD-study-TG.mzTab");
		Assert.assertTrue(inputFile.canRead());
		MzTabFileImportMethod importer = new MzTabFileImportMethod(inputFile, dataStore);
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
		importer = new MzTabFileImportMethod(inputFile, dataStore);
		featureTable = importer.execute();
		Assert.assertNotNull(featureTable);
		Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

		// The table has 109 rows
		Assert.assertFalse(featureTable.getRows().isEmpty());
		Assert.assertEquals(109, featureTable.getRows().size());

		// Annotation 27 - PC32:1
		FeatureTableRow row = featureTable.getRows().get(27);
		FeatureTableColumn column = featureTable.getColumn("Ion Annotation", null);
		IonAnnotation ionAnnotation = (IonAnnotation) row.getData(column, IonAnnotation.class);
		Assert.assertEquals("PC32:1", ionAnnotation.getAnnotationId());
		IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
		IMolecularFormula cdkFormula = MolecularFormulaManipulator.getMolecularFormula("C40H78P1O8N1", builder);
		String formula = MolecularFormulaManipulator.getString(ionAnnotation.getFormula());
		String formula2 = MolecularFormulaManipulator.getString(cdkFormula);
		Assert.assertTrue(formula.equals(formula2));

		featureTable.dispose();
	}
}
