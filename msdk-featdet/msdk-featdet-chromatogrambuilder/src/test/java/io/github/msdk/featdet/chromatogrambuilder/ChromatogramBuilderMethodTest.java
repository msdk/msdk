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

package io.github.msdk.featdet.chromatogrambuilder;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.featdet.chromatogrambuilder.ChromatogramBuilderMethod;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import io.github.msdk.util.CombinedMZTolerance;
import io.github.msdk.util.MZTolerance;

public class ChromatogramBuilderMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    private static DataPointStore dataStore;
    private static RawDataFile rawFile;

    @SuppressWarnings("null")
    @BeforeClass
    public static void loadData() throws MSDKException {

        // Create the data structures
        dataStore = DataPointStoreFactory.getMemoryDataStore();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "orbitrap_300-600mz.mzML");
        Assert.assertTrue("Cannot read test data", inputFile.canRead());
        MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
        rawFile = importer.execute();
        Assert.assertNotNull(rawFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);
    }

    @SuppressWarnings("null")
    @Test
    public void testOrbitrap() throws MSDKException {

        double noiseLevel = 0;
        double minimumTimeSpan = 6; // 6s
        double minimumHeight = 1E4;
        MZTolerance mzTolerance = new CombinedMZTolerance(0.001, 5.0);
        ChromatogramBuilderMethod chromBuilder = new ChromatogramBuilderMethod(
                dataStore, rawFile, noiseLevel, minimumTimeSpan, minimumHeight,
                mzTolerance);
        List<Chromatogram> detectedFeatures = chromBuilder.execute();
        Assert.assertEquals(1.0, chromBuilder.getFinishedPercentage(), 0.0001);

        Assert.assertTrue(detectedFeatures.size() > 10);

    }

    @SuppressWarnings("null")
    @Test
    public void testBigTimeSpan() throws MSDKException {

        // Testing a big timeSpan
        double noiseLevel = 0;
        double minimumTimeSpan = 1000000;
        double minimumHeight = 1E4;
        MZTolerance mzTolerance = new CombinedMZTolerance(0.001, 5.0);
        ChromatogramBuilderMethod chromBuilder = new ChromatogramBuilderMethod(
                dataStore, rawFile, noiseLevel, minimumTimeSpan, minimumHeight,
                mzTolerance);
        List<Chromatogram> detectedFeatures = chromBuilder.execute();
        Assert.assertEquals(1.0, chromBuilder.getFinishedPercentage(), 0.0001);

        // Nothing should be recognized as a chromatogram
        Assert.assertEquals(0, detectedFeatures.size());

    }

    @SuppressWarnings("null")
    @Test
    public void testBigMinHeight() throws MSDKException {

        // Testing a big minimum height
        double noiseLevel = 0f;
        double minimumTimeSpan = 6; // 6s
        double minimumHeight = 10000000;
        MZTolerance mzTolerance = new CombinedMZTolerance(0.001, 5.0);
        ChromatogramBuilderMethod chromBuilder = new ChromatogramBuilderMethod(
                dataStore, rawFile, noiseLevel, minimumTimeSpan, minimumHeight,
                mzTolerance);
        List<Chromatogram> detectedFeatures = chromBuilder.execute();
        Assert.assertEquals(1.0, chromBuilder.getFinishedPercentage(), 0.0001);

        // There are no so big picks in the data so there should be no
        // chromatograms
        Assert.assertEquals(0, detectedFeatures.size());

    }

}
