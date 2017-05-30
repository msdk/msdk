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

package io.github.msdk.features.rowfilter;

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
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;
import io.github.msdk.featdet.chromatogramtofeaturetable.ChromatogramToFeatureTableMethod;
import io.github.msdk.featdet.targeteddetection.TargetedDetectionMethod;
import io.github.msdk.features.rowfilter.RowFilterMethod;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import io.github.msdk.util.tolerances.MaximumMzTolerance;
import io.github.msdk.util.tolerances.MzTolerance;
import io.github.msdk.util.tolerances.RTTolerance;

public class RowFilterMethodTest {

  private static final String TEST_DATA_PATH = "src/test/resources/";


  @Test
  public void testOrbitrap() throws MSDKException {

    // Create the data structures
    final DataPointStore dataStore = DataPointStoreFactory.getMemoryDataStore();

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "orbitrap_300-600mz.mzML");
    Assert.assertTrue("Cannot read test data", inputFile.canRead());
    MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();
    Assert.assertNotNull(rawFile);
    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

    // Ion 1
    List<IonAnnotation> ionAnnotations = new ArrayList<IonAnnotation>();
    IonAnnotation ion1 = MSDKObjectBuilder.getIonAnnotation();
    ion1.setExpectedMz(332.56);
    ion1.setAnnotationId("Feature 332.56");
    ion1.setExpectedRetentionTime(772.8f);
    ionAnnotations.add(ion1);

    // Ion 2
    IonAnnotation ion2 = MSDKObjectBuilder.getIonAnnotation();
    ion2.setExpectedMz(508.004);
    ion2.setAnnotationId("Feature 508.004");
    ion2.setExpectedRetentionTime(868.8f);
    ionAnnotations.add(ion2);

    // Ion 3
    IonAnnotation ion3 = MSDKObjectBuilder.getIonAnnotation();
    ion3.setExpectedMz(362.102);
    ion3.setAnnotationId("Feature 362.102");
    ion3.setExpectedRetentionTime(643.2f);
    ionAnnotations.add(ion3);

    // Variables
    final MzTolerance mzTolerance = new MaximumMzTolerance(0.003, 5.0);
    final RTTolerance rtTolerance = new RTTolerance(3, false);
    final Double intensityTolerance = 0.10d;
    final Double noiseLevel = 5000d;

    TargetedDetectionMethod chromBuilder = new TargetedDetectionMethod(ionAnnotations, rawFile,
        dataStore, mzTolerance, rtTolerance, intensityTolerance, noiseLevel);
    final List<Chromatogram> chromatograms = chromBuilder.execute();
    Assert.assertEquals(1.0, chromBuilder.getFinishedPercentage(), 0.0001);
    Assert.assertNotNull(chromatograms);
    Assert.assertEquals(3, chromatograms.size());

    // Create a new feature table
    FeatureTable featureTable = MSDKObjectBuilder.getFeatureTable("orbitrap_300-600mz", dataStore);
    Sample sample = MSDKObjectBuilder.getSample("orbitrap_300-600mz");

    ChromatogramToFeatureTableMethod tableBuilder =
        new ChromatogramToFeatureTableMethod(chromatograms, featureTable, sample);
    tableBuilder.execute();

    // 1. Filter parameters
    boolean filterByMz = true;
    boolean filterByRt = true;
    boolean filterByDuration = true;
    boolean filterByCount = true;
    boolean filterByIsotopes = true;
    boolean filterByIonAnnotation = true;
    boolean requireAnnotation = false;
    boolean removeDuplicates = false;
    boolean duplicateRequireSameID = true;
    Range<Double> mzRange = Range.closed(300.0, 600.0);
    Range<Float> rtRange = Range.closed(600.0f, 900.0f); // Seconds
    Range<Double> durationRange = Range.closed(0.0, 47.0); // Seconds
    final MzTolerance duplicateMzTolerance = new MaximumMzTolerance(0.003, 5.0);
    final RTTolerance duplicateRtTolerance = new RTTolerance(0.2f, false);
    int minCount = 1;
    int minIsotopes = 1;
    String ionAnnotation = null;
    String nameSuffix = "-rowFiltered";

    // 1. Filter the rows
    RowFilterMethod filterMethod = new RowFilterMethod(featureTable, dataStore, nameSuffix,
        filterByMz, filterByRt, filterByDuration, filterByCount, filterByIsotopes,
        filterByIonAnnotation, requireAnnotation, mzRange, rtRange, durationRange, minCount,
        minIsotopes, ionAnnotation, removeDuplicates, duplicateMzTolerance, duplicateRtTolerance,
        duplicateRequireSameID);
    filterMethod.execute();
    Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);

    // 1. Verify data
    FeatureTable filteredTable = filterMethod.getResult();
    Assert.assertNotNull(filteredTable);
    Assert.assertEquals(3, filteredTable.getRows().size());

    // 2. Filter parameters
    mzRange = Range.closed(350.0, 600.0);
    durationRange = Range.closed(46.0, 47.0); // Seconds

    // 2. Filter the features
    filterMethod = new RowFilterMethod(featureTable, dataStore, nameSuffix, filterByMz, filterByRt,
        filterByDuration, filterByCount, filterByIsotopes, filterByIonAnnotation, requireAnnotation,
        mzRange, rtRange, durationRange, minCount, minIsotopes, ionAnnotation, removeDuplicates,
        duplicateMzTolerance, duplicateRtTolerance, duplicateRequireSameID);
    filterMethod.execute();
    Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);

    // 2. Verify data
    filteredTable = filterMethod.getResult();
    Assert.assertNotNull(filteredTable);
    Assert.assertEquals(1, filteredTable.getRows().size());

    // 3. Filter parameters
    minCount = 2;

    // 3. Filter the features
    filterMethod = new RowFilterMethod(filteredTable, dataStore, nameSuffix, filterByMz, filterByRt,
        filterByDuration, filterByCount, filterByIsotopes, filterByIonAnnotation, requireAnnotation,
        mzRange, rtRange, durationRange, minCount, minIsotopes, ionAnnotation, removeDuplicates,
        duplicateMzTolerance, duplicateRtTolerance, duplicateRequireSameID);
    filterMethod.execute();
    Assert.assertEquals(1.0, filterMethod.getFinishedPercentage(), 0.0001);

    // 3. Verify data
    filteredTable = filterMethod.getResult();
    Assert.assertNotNull(filteredTable);
    Assert.assertEquals(0, filteredTable.getRows().size());

    featureTable.dispose();
    filteredTable.dispose();
  }

}
