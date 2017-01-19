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

package io.github.msdk.io.mztab;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;

public class MzTabFileExportMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @Test
    public void testMzTab_Sample() throws MSDKException, IOException {

        // Create the data structures
        DataPointStore dataStore = DataPointStoreFactory.getTmpFileDataStore();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "Sample-2.3.mzTab");
        Assert.assertTrue(inputFile.canRead());
        MzTabFileImportMethod importer = new MzTabFileImportMethod(inputFile,
                dataStore);
        FeatureTable featureTable = importer.execute();
        Assert.assertNotNull(featureTable);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // Create temp file
        File tempFile = File.createTempFile("MZmine_TestFile_", ".mzTab");

        // Export the file
        Assert.assertNotNull(tempFile);
        MzTabFileExportMethod exporter = new MzTabFileExportMethod(featureTable,
                tempFile, true);
        exporter.execute();
        Assert.assertEquals(1.0, exporter.getFinishedPercentage(), 0.0001);
        Assert.assertTrue(tempFile.canRead());

        // Clean up
        featureTable.dispose();

        // Import the file again
        importer = new MzTabFileImportMethod(tempFile, dataStore);
        FeatureTable featureTable2 = importer.execute();
        Assert.assertNotNull(featureTable2);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // The table has 7 samples
        List<Sample> samples = featureTable2.getSamples();
        Assert.assertNotNull(samples);
        Assert.assertEquals(7, samples.size());

        // The table has columns
        Assert.assertFalse(featureTable2.getColumns().isEmpty());

        // The table has 298 rows
        Assert.assertFalse(featureTable2.getRows().isEmpty());
        Assert.assertEquals(298, featureTable2.getRows().size());

        // ********************
        // Annotation 7 - HEPES
        // ********************
        FeatureTableRow row = featureTable2.getRows().get(7);
        FeatureTableColumn<List<IonAnnotation>> ionAnnotationColumn = featureTable2
                .getColumn(ColumnName.IONANNOTATION, null);
        List<IonAnnotation> ionAnnotations = row.getData(ionAnnotationColumn);
        Assert.assertNotNull(ionAnnotations);
        IonAnnotation ionAnnotation = ionAnnotations.get(0);
        Assert.assertEquals("HEPES", ionAnnotation.getDescription());
        IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
        IMolecularFormula cdkFormula = MolecularFormulaManipulator
                .getMolecularFormula("C8H18N2O4S", builder);
        String formula = MolecularFormulaManipulator
                .getString(ionAnnotation.getFormula());
        String formula2 = MolecularFormulaManipulator.getString(cdkFormula);
        Assert.assertTrue(formula.equals(formula2));
        String inchiKey = ionAnnotation.getInchiKey();
        Assert.assertEquals("JKMHFZQWWAIEOD-UHFFFAOYSA-N", inchiKey);

        // ********************
        // Row 5
        // ********************
        row = featureTable2.getRows().get(5);
        FeatureTableColumn<Double> mzColumn = featureTable2
                .getColumn(ColumnName.MZ, null);
        Double averageMZ = row.getData(mzColumn);
        Assert.assertNotNull(averageMZ);
        Assert.assertEquals(520.334738595145, averageMZ, 0.0000001);
        Sample sample = featureTable2.getSamples().get(5);
        Assert.assertEquals("36C sample 2", sample.getName());

        // ********************
        // Last row
        // ********************
        row = featureTable2.getRows().get(297);
        sample = featureTable2.getSamples().get(5);

        // Area
        FeatureTableColumn<Double> areaColumn = featureTable2
                .getColumn(ColumnName.AREA, sample);
        Double area = row.getData(areaColumn);
        Assert.assertNotNull(area);
        Assert.assertEquals(11480605.3259747, area, 0.0000001);

        // RT
        FeatureTableColumn<ChromatographyInfo> chromInfoColumn = featureTable2
                .getColumn("Chromatography Info", null,
                        ChromatographyInfo.class);
        ChromatographyInfo chromatographyInfo = row.getData(chromInfoColumn);
        Assert.assertNotNull(chromatographyInfo);
        Float rt = chromatographyInfo.getRetentionTime();
        Assert.assertNotNull(rt);
        Assert.assertEquals(30.324697494506836, rt, 0.0000001);

        // Height
        FeatureTableColumn<Float> heightColumn = featureTable2
                .getColumn(ColumnName.HEIGHT, sample);
        Float height = row.getData(heightColumn);
        Assert.assertNotNull(height);
        Assert.assertEquals(312942.15625, height, 0.0000001);

        // m/z
        mzColumn = featureTable2.getColumn(ColumnName.MZ, sample);
        Double mz = row.getData(mzColumn);
        Assert.assertNotNull(mz);
        Assert.assertEquals(144.927825927734, mz, 0.0000001);
        
        // Clean up
        tempFile.delete();
        featureTable2.dispose();
    }

}
