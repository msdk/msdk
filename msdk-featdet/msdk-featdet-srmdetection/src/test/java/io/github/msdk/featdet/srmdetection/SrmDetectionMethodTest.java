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

package io.github.msdk.featdet.srmdetection;

import java.io.File;
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
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.featdet.chromatogramtofeaturetable.ChromatogramToFeatureTableMethod;
import io.github.msdk.io.mzml.MzMLFileImportMethod;

public class SrmDetectionMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @SuppressWarnings("null")
    @Test
    public void testSRM() throws MSDKException {

        // Create the data structures
        final DataPointStore dataStore = DataPointStoreFactory
                .getMemoryDataStore();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "SRM.mzML");
        Assert.assertTrue("Cannot read test data", inputFile.canRead());
        MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
        RawDataFile rawFile = importer.execute();
        Assert.assertNotNull(rawFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // SRM detection method
        SrmDetectionMethod srmMethod = new SrmDetectionMethod(rawFile,
                dataStore);
        final List<Chromatogram> chromatograms = srmMethod.execute();
        Assert.assertEquals(1.0, srmMethod.getFinishedPercentage(), 0.0001);

        // Verify data
        Assert.assertEquals(36, chromatograms.size());

        // Build feature table from chromatograms
        FeatureTable featureTable = MSDKObjectBuilder
                .getFeatureTable("SRM srmDetection", dataStore);
        Sample sample = MSDKObjectBuilder.getSimpleSample("SRM");
        ChromatogramToFeatureTableMethod chromMethod = new ChromatogramToFeatureTableMethod(
                chromatograms, featureTable, sample);
        featureTable = chromMethod.execute();
        Assert.assertEquals(1.0, chromMethod.getFinishedPercentage(), 0.0001);

        // Verify data
        List<FeatureTableRow> rows = featureTable.getRows();
        Assert.assertEquals(36, rows.size());

        // Group ID
        FeatureTableColumn<Integer> column = featureTable
                .getColumn(ColumnName.GROUPID, null);
        Assert.assertEquals(0, rows.get(0).getData(column), 0.0001);
        Assert.assertEquals(1, rows.get(1).getData(column), 0.0001);
        Assert.assertEquals(1, rows.get(2).getData(column), 0.0001);
        Assert.assertEquals(19, rows.get(19).getData(column), 0.0001);
        Assert.assertEquals(19, rows.get(20).getData(column), 0.0001);

        // m/z
        FeatureTableColumn<Double> columnMz = featureTable
                .getColumn(ColumnName.MZ, null);
        Assert.assertEquals(481.9, rows.get(0).getData(columnMz), 0.0001);
        Assert.assertEquals(722.35, rows.get(18).getData(columnMz), 0.0001);
    }

}
