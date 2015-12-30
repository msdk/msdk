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

package io.github.msdk.features.filtering;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Range;

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
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;
import io.github.msdk.featdet.chromatogramtofeaturetable.ChromatogramToFeatureTableMethod;
import io.github.msdk.featdet.targeteddetection.TargetedDetectionMethod;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import io.github.msdk.io.mztab.MzTabFileImportMethod;
import io.github.msdk.util.MZTolerance;
import io.github.msdk.util.RTTolerance;

public class FeatureFilterMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @SuppressWarnings("null")
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

        // Ion 1
        List<IonAnnotation> ionAnnotations = new ArrayList<IonAnnotation>();
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
        final MZTolerance mzTolerance = new MZTolerance(0.003, 5.0);
        final RTTolerance rtTolerance = new RTTolerance(3, false);
        final Double intensityTolerance = 0.10d;
        final Double noiseLevel = 5000d;

        TargetedDetectionMethod chromBuilder = new TargetedDetectionMethod(
                ionAnnotations, rawFile, dataStore, mzTolerance, rtTolerance,
                intensityTolerance, noiseLevel);
        final List<Chromatogram> chromatograms = chromBuilder.execute();
        Assert.assertEquals(1.0, chromBuilder.getFinishedPercentage(), 0.0001);
        Assert.assertNotNull(chromatograms);
        Assert.assertEquals(3, chromatograms.size());

        // Create a new feature table
        FeatureTable featureTable = MSDKObjectBuilder
                .getFeatureTable("orbitrap_300-600mz", dataStore);
        Sample sample = MSDKObjectBuilder.getSimpleSample("orbitrap_300-600mz");

        ChromatogramToFeatureTableMethod tableBuilder = new ChromatogramToFeatureTableMethod(
                chromatograms, featureTable, sample);
        tableBuilder.execute();

        // 1. Filter parameters
        boolean filterByDuration = true;
        boolean filterByArea = true;
        boolean filterByHeight = true;
        boolean filterByDataPoints = true;
        boolean filterByFWHM = true;
        boolean filterByTailingFactor = true;
        boolean filterByAsymmetryFactor = true;
        Range<Double> durationRange = Range.closed(10.0, 50.0);
        Range<Double> areaRange = Range.closed(1E5, 1E9);
        Range<Double> heightRange = Range.closed(1E4, 1E7);
        Range<Integer> dataPointsRange = Range.closed(10, 999);
        Range<Double> fwhmRange = Range.closed(0.0, 15.0);
        Range<Double> tailingFactorRange = Range.closed(0.5, 2.0);
        Range<Double> asymmetryFactorRange = Range.closed(0.5, 2.0);
        String nameSuffix = "-Filtered";

        // 1. Filter the features
        FeatureFilterMethod filterMethod = new FeatureFilterMethod(featureTable,
                dataStore, filterByDuration, filterByArea, filterByHeight,
                filterByDataPoints, filterByFWHM, filterByTailingFactor,
                filterByAsymmetryFactor, durationRange, areaRange, heightRange,
                dataPointsRange, fwhmRange, tailingFactorRange,
                asymmetryFactorRange, nameSuffix);
        filterMethod.execute();
        Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);

        // 1. Verify data
        FeatureTable filteredTable = filterMethod.getResult();
        Assert.assertNotNull(filteredTable);
        if (filteredTable != null)
            Assert.assertEquals(3, filteredTable.getRows().size());

        // 2. Filter parameters
        filterByDataPoints = false;
        dataPointsRange = Range.closed(18, 20);

        // 2. Filter the features
        filterMethod = new FeatureFilterMethod(featureTable, dataStore,
                filterByDuration, filterByArea, filterByHeight,
                filterByDataPoints, filterByFWHM, filterByTailingFactor,
                filterByAsymmetryFactor, durationRange, areaRange, heightRange,
                dataPointsRange, fwhmRange, tailingFactorRange,
                asymmetryFactorRange, nameSuffix);
        filterMethod.execute();
        Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);

        // 2. Verify data
        filteredTable = filterMethod.getResult();
        Assert.assertNotNull(filteredTable);
        if (filteredTable != null)
            Assert.assertEquals(3, filteredTable.getRows().size());

        // 3. Filter parameters
        filterByDataPoints = true;
        dataPointsRange = Range.closed(18, 20);
        areaRange = Range.closed(8E7, 1E9);

        // 3. Filter the features
        filterMethod = new FeatureFilterMethod(featureTable, dataStore,
                filterByDuration, filterByArea, filterByHeight,
                filterByDataPoints, filterByFWHM, filterByTailingFactor,
                filterByAsymmetryFactor, durationRange, areaRange, heightRange,
                dataPointsRange, fwhmRange, tailingFactorRange,
                asymmetryFactorRange, nameSuffix);
        filterMethod.execute();
        Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);

        // 3. Verify data
        filteredTable = filterMethod.getResult();
        Assert.assertNotNull(filteredTable);
        if (filteredTable != null)
            Assert.assertEquals(1, filteredTable.getRows().size());

        featureTable.dispose();
    }

    @SuppressWarnings("null")
    @Test
    public void testMzTab_Sample() throws MSDKException {

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
        Assert.assertEquals(298, featureTable.getRows().size());

        // 1. Filter parameters
        boolean filterByDuration = false;
        boolean filterByArea = false;
        boolean filterByHeight = false;
        boolean filterByDataPoints = false;
        boolean filterByFWHM = false;
        boolean filterByTailingFactor = false;
        boolean filterByAsymmetryFactor = false;
        Range<Double> durationRange = Range.closed(0.0, 0.0);
        Range<Double> areaRange = Range.closed(1E7, 1E9);
        Range<Double> heightRange = Range.closed(1E5, 1E9);
        Range<Integer> dataPointsRange = Range.closed(0, 0);
        Range<Double> fwhmRange = Range.closed(0.0, 0.0);
        Range<Double> tailingFactorRange = Range.closed(0.0, 0.0);
        Range<Double> asymmetryFactorRange = Range.closed(0.0, 0.0);
        String nameSuffix = "-Filtered";

        // 1. Filter the features
        FeatureFilterMethod filterMethod = new FeatureFilterMethod(featureTable,
                dataStore, filterByDuration, filterByArea, filterByHeight,
                filterByDataPoints, filterByFWHM, filterByTailingFactor,
                filterByAsymmetryFactor, durationRange, areaRange, heightRange,
                dataPointsRange, fwhmRange, tailingFactorRange,
                asymmetryFactorRange, nameSuffix);
        filterMethod.execute();
        Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);

        // 1. Verify data - no filter was applied
        FeatureTable filteredTable = filterMethod.getResult();
        Assert.assertNotNull(filteredTable);
        if (filteredTable != null)
            Assert.assertEquals(298, filteredTable.getRows().size());

        // 2. Filter parameters
        filterByArea = true;

        // 2. Filter the features
        filterMethod = new FeatureFilterMethod(featureTable, dataStore,
                filterByDuration, filterByArea, filterByHeight,
                filterByDataPoints, filterByFWHM, filterByTailingFactor,
                filterByAsymmetryFactor, durationRange, areaRange, heightRange,
                dataPointsRange, fwhmRange, tailingFactorRange,
                asymmetryFactorRange, nameSuffix);
        filterMethod.execute();
        Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);

        // 2. Verify data
        filteredTable = filterMethod.getResult();
        Assert.assertNotNull(filteredTable);
        if (filteredTable != null)
            Assert.assertEquals(116, filteredTable.getRows().size());

        // 3. Filter parameters
        filterByArea = true;
        filterByHeight = true;

        // 3. Filter the features
        filterMethod = new FeatureFilterMethod(featureTable, dataStore,
                filterByDuration, filterByArea, filterByHeight,
                filterByDataPoints, filterByFWHM, filterByTailingFactor,
                filterByAsymmetryFactor, durationRange, areaRange, heightRange,
                dataPointsRange, fwhmRange, tailingFactorRange,
                asymmetryFactorRange, nameSuffix);
        filterMethod.execute();
        Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);

        // 3. Verify data
        filteredTable = filterMethod.getResult();
        Assert.assertNotNull(filteredTable);
        if (filteredTable != null)
            Assert.assertEquals(115, filteredTable.getRows().size());

        // Row ID 176, ID: L-Arginine
        FeatureTableRow row = filteredTable.getRows().get(110);
        Assert.assertEquals(176, row.getId(), 0.0001);
        FeatureTableColumn<List<IonAnnotation>> ionAnnotationColumn = filteredTable
                .getColumn(ColumnName.IONANNOTATION, null);
        List<IonAnnotation> ionAnnotations = row.getData(ionAnnotationColumn);
        Assert.assertNotNull(ionAnnotations);
        IonAnnotation ionAnnotation = ionAnnotations.get(0);
        Assert.assertEquals("L-Arginine", ionAnnotation.getDescription());

        // BLANK sample
        Sample sample = filteredTable.getSamples().get(0);
        FeatureTableColumn<Double> areaColumn = filteredTable
                .getColumn(ColumnName.AREA, sample);
        Assert.assertNull(row.getData(areaColumn));

        // 26C sample 1
        sample = filteredTable.getSamples().get(1);
        areaColumn = filteredTable.getColumn(ColumnName.AREA, sample);
        Double area = row.getData(areaColumn);
        Assert.assertNotNull(area);
        Assert.assertEquals(2.559630988648635E8, area, 0.00001);

        featureTable.dispose();
    }

}
