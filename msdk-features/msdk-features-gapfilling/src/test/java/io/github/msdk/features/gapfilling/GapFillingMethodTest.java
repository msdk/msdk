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

package io.github.msdk.features.gapfilling;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;
import io.github.msdk.featdet.chromatogramtofeaturetable.ChromatogramToFeatureTableMethod;
import io.github.msdk.featdet.targeteddetection.TargetedDetectionMethod;
import io.github.msdk.features.joinaligner.JoinAlignerMethod;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import io.github.msdk.util.MZTolerance;
import io.github.msdk.util.RTTolerance;

public class GapFillingMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @Test
    public void testOrbitrap() throws MSDKException {

        // Create the data structures
        final DataPointStore dataStore = DataPointStoreFactory
                .getMemoryDataStore();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "orbitrap_300-600mz.mzML");
        Assert.assertTrue("Cannot read test data", inputFile.canRead());
        MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
        RawDataFile rawFile = importer.execute();
        Assert.assertNotNull(rawFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        //
        // Run the targeted detection module
        //

        // Ion 1
        List<IonAnnotation> ionAnnotations = new ArrayList<>();
        IonAnnotation ion1 = MSDKObjectBuilder.getSimpleIonAnnotation();
        ion1.setExpectedMz(332.56);
        ion1.setAnnotationId("Feature 332.56");
        ion1.setChromatographyInfo(MSDKObjectBuilder
                .getChromatographyInfo1D(SeparationType.LC, (float) 772.8));
        ionAnnotations.add(ion1);

        // Ion 2
        IonAnnotation ion2 = MSDKObjectBuilder.getSimpleIonAnnotation();
        ion2.setExpectedMz(508.004);
        ion2.setAnnotationId("Feature 508.004");
        ion2.setChromatographyInfo(MSDKObjectBuilder
                .getChromatographyInfo1D(SeparationType.LC, (float) 868.8));
        ionAnnotations.add(ion2);

        // Ion 3
        IonAnnotation ion3 = MSDKObjectBuilder.getSimpleIonAnnotation();
        ion3.setExpectedMz(362.102);
        ion3.setAnnotationId("Feature 362.102");
        ion3.setChromatographyInfo(MSDKObjectBuilder
                .getChromatographyInfo1D(SeparationType.LC, (float) 643.2));
        ionAnnotations.add(ion3);

        // Variables
        MZTolerance mzTolerance = new MZTolerance(0.003, 5.0);
        RTTolerance rtTolerance = new RTTolerance(0.2, false);
        Double intensityTolerance = 0.10d;
        Double noiseLevel = 5000d;

        TargetedDetectionMethod chromBuilder = new TargetedDetectionMethod(
                ionAnnotations, rawFile, dataStore, mzTolerance, rtTolerance,
                intensityTolerance, noiseLevel);
        chromBuilder.execute();
        Assert.assertEquals(1.0, chromBuilder.getFinishedPercentage(), 0.0001);
        List<Chromatogram> chromatograms = chromBuilder.getResult();
        Assert.assertNotNull(chromatograms);
        Assert.assertEquals(3, chromatograms.size());

        //
        // 1st feature table with all ions
        //

        FeatureTable featureTable1 = MSDKObjectBuilder
                .getFeatureTable("orbitrap_300-600mz-1", dataStore);
        Sample sample = MSDKObjectBuilder
                .getSimpleSample("orbitrap_300-600mz-1");
        sample.setRawDataFile(rawFile);

        ChromatogramToFeatureTableMethod tableBuilder = new ChromatogramToFeatureTableMethod(
                chromatograms, featureTable1, sample);
        tableBuilder.execute();
        Assert.assertEquals(1.0, tableBuilder.getFinishedPercentage(), 0.0001);
        Assert.assertEquals(3, featureTable1.getRows().size());

        //
        // 2nd feature table with only the first two ions
        //

        chromatograms.remove(2);
        FeatureTable featureTable2 = MSDKObjectBuilder
                .getFeatureTable("orbitrap_300-600mz-2", dataStore);
        Sample sample2 = MSDKObjectBuilder
                .getSimpleSample("orbitrap_300-600mz-2");
        sample2.setRawDataFile(rawFile);
        tableBuilder = new ChromatogramToFeatureTableMethod(chromatograms,
                featureTable2, sample2);
        tableBuilder.execute();
        Assert.assertEquals(1.0, tableBuilder.getFinishedPercentage(), 0.0001);
        Assert.assertEquals(2, featureTable2.getRows().size());

        //
        // Run the alignment method on the two feature tables
        //

        // Variables
        mzTolerance = new MZTolerance(0.003, 5.0);
        rtTolerance = new RTTolerance(0.1, false);
        int mzWeight = 10;
        int rtWeight = 10;
        boolean requireSameCharge = false;
        boolean requireSameAnnotation = false;
        String featureTableName = "Aligned Feature Table";
        List<FeatureTable> featureTables = new ArrayList<FeatureTable>();
        featureTables.add(featureTable1);
        featureTables.add(featureTable2);

        // Perform alignment
        JoinAlignerMethod alignMethod = new JoinAlignerMethod(featureTables,
                dataStore, mzTolerance, rtTolerance, mzWeight, rtWeight,
                requireSameCharge, requireSameAnnotation, featureTableName);
        FeatureTable featureTable = alignMethod.execute();
        Assert.assertEquals(1.0, alignMethod.getFinishedPercentage(), 0.0001);

        // Verify the data
        Assert.assertEquals(3, featureTable.getRows().size());
        Assert.assertEquals(29, featureTable.getColumns().size());

        //
        // Run the gap filling method
        //

        // Variables
        mzTolerance = new MZTolerance(0.003, 5.0);
        rtTolerance = new RTTolerance(0.2, false);
        intensityTolerance = 0.10d;
        boolean sameRT = true;
        boolean sameMZ = true;
        String nameSuffix = " gapFilled";

        GapFillingMethod gapFillMethod = new GapFillingMethod(featureTable,
                dataStore, mzTolerance, rtTolerance, intensityTolerance, sameRT,
                sameMZ, nameSuffix);
        featureTable = gapFillMethod.execute();
        Assert.assertEquals(1.0, gapFillMethod.getFinishedPercentage(), 0.0001);

        //
        // Verify the data of sample 2, feature 3
        //

        FeatureTableRow row = featureTable.getRows().get(2);
        sample = featureTable.getSamples().get(1);
        Sample sample0 = featureTable.getSamples().get(0);

        // Area
        FeatureTableColumn<Double> columnArea = featureTable
                .getColumn(ColumnName.AREA, sample);
        Assert.assertNotNull(columnArea);
        Double area = row.getData(columnArea);
        Assert.assertNotNull(area);
        Assert.assertEquals(3.415448809043382E7, area, 0.0001);

        // The area of sample 1 and 2 should be identical
        FeatureTableColumn<Double> columnArea0 = featureTable
                .getColumn(ColumnName.AREA, sample0);
        Assert.assertNotNull(columnArea0);
        Double area0 = row.getData(columnArea0);
        Assert.assertNotNull(area0);
        Assert.assertEquals(area0, area, 0.0001);

        // Height
        FeatureTableColumn<Float> columnHeight = featureTable
                .getColumn(ColumnName.HEIGHT, sample);
        Assert.assertNotNull(columnHeight);
        Float height = row.getData(columnHeight);
        Assert.assertNotNull(height);
        Assert.assertEquals(2609394.5, height, 0.0001);

        // RT
        FeatureTableColumn<ChromatographyInfo> columnRt = featureTable
                .getColumn(ColumnName.RT, sample);
        Assert.assertNotNull(columnRt);
        ChromatographyInfo rt = row.getData(columnRt);
        Assert.assertNotNull(rt);
        Assert.assertNotNull(rt.getRetentionTime());
        Assert.assertEquals(643.3532, rt.getRetentionTime(), 0.0001);

        // m/z
        FeatureTableColumn<Double> columnMz = featureTable
                .getColumn(ColumnName.MZ, sample);
        Assert.assertNotNull(columnMz);
        Double mz = row.getData(columnMz);
        Assert.assertNotNull(mz);
        Assert.assertEquals(362.1021303449358, mz, 0.0001);

    }

}
