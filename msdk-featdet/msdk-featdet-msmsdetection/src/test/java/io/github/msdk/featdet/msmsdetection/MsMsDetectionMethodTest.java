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

package io.github.msdk.featdet.msmsdetection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.datapointstore.DataPointStoreFactory;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import io.github.msdk.util.MZTolerance;
import io.github.msdk.util.RTTolerance;

public class MsMsDetectionMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @Test
    public void testOrbitrap() throws MSDKException {

        // Create the data structures
        final DataPointStore dataStore = DataPointStoreFactory
                .getMemoryDataStore();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "msms.mzML");
        Assert.assertTrue("Cannot read test data", inputFile.canRead());
        MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
        RawDataFile rawFile = importer.execute();
        Assert.assertNotNull(rawFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // MS2 scans
        List<MsScan> msScans = new ArrayList<MsScan>();
        for (MsScan scan : rawFile.getScans()) {
            if (scan.getMsFunction().getMsLevel().equals(2))
                msScans.add(scan);
        }

        // Parameters
        final MZTolerance mzTolerance = new MZTolerance(0.003, 5.0);
        final RTTolerance rtTolerance = new RTTolerance(0.2, false);
        final Double intensityTolerance = 0.10d;

        // MS/MS detection method
        MsMsDetectionMethod msMethod = new MsMsDetectionMethod(rawFile, msScans,
                dataStore, mzTolerance, rtTolerance, intensityTolerance);
        final List<IonAnnotation> ionAnnotations = msMethod.execute();
        Assert.assertEquals(1.0, msMethod.getFinishedPercentage(), 0.0001);

        // Verify ions
        Assert.assertEquals(13, ionAnnotations.size());
        Assert.assertEquals(809.6484375, ionAnnotations.get(0).getExpectedMz(),
                0.0001);
        Assert.assertEquals(435.9, ionAnnotations.get(0).getChromatographyInfo()
                .getRetentionTime(), 0.1);
        Assert.assertEquals(534.5249023, ionAnnotations.get(1).getExpectedMz(),
                0.0001);
        Assert.assertEquals(453.6, ionAnnotations.get(1).getChromatographyInfo()
                .getRetentionTime(), 0.1);
        Assert.assertEquals(797.6456299, ionAnnotations.get(2).getExpectedMz(),
                0.0001);
        Assert.assertEquals(449.2, ionAnnotations.get(2).getChromatographyInfo()
                .getRetentionTime(), 0.1);
        Assert.assertEquals(806.5674438, ionAnnotations.get(3).getExpectedMz(),
                0.0001);
        Assert.assertEquals(420.1, ionAnnotations.get(3).getChromatographyInfo()
                .getRetentionTime(), 0.1);
        Assert.assertEquals(750.5430298, ionAnnotations.get(4).getExpectedMz(),
                0.0001);
        Assert.assertEquals(427.4, ionAnnotations.get(4).getChromatographyInfo()
                .getRetentionTime(), 0.1);
        Assert.assertEquals(796.6193848, ionAnnotations.get(5).getExpectedMz(),
                0.0001);
        Assert.assertEquals(435.0, ionAnnotations.get(5).getChromatographyInfo()
                .getRetentionTime(), 0.1);
        Assert.assertEquals(753.5879517, ionAnnotations.get(6).getExpectedMz(),
                0.0001);
        Assert.assertEquals(444.3, ionAnnotations.get(6).getChromatographyInfo()
                .getRetentionTime(), 0.1);
        Assert.assertEquals(776.5801392, ionAnnotations.get(7).getExpectedMz(),
                0.0001);
        Assert.assertEquals(409.0, ionAnnotations.get(7).getChromatographyInfo()
                .getRetentionTime(), 0.1);
        Assert.assertEquals(816.5888062, ionAnnotations.get(8).getExpectedMz(),
                0.0001);
        Assert.assertEquals(422.7, ionAnnotations.get(8).getChromatographyInfo()
                .getRetentionTime(), 0.1);
        Assert.assertEquals(756.5533447, ionAnnotations.get(9).getExpectedMz(),
                0.0001);
        Assert.assertEquals(443.3, ionAnnotations.get(9).getChromatographyInfo()
                .getRetentionTime(), 0.1);
        Assert.assertEquals(774.5436401, ionAnnotations.get(10).getExpectedMz(),
                0.0001);
        Assert.assertEquals(416.1, ionAnnotations.get(10)
                .getChromatographyInfo().getRetentionTime(), 0.1);
        Assert.assertEquals(754.5394897, ionAnnotations.get(11).getExpectedMz(),
                0.0001);
        Assert.assertEquals(409.8, ionAnnotations.get(11)
                .getChromatographyInfo().getRetentionTime(), 0.1);
        Assert.assertEquals(715.5751953, ionAnnotations.get(12).getExpectedMz(),
                0.0001);
        Assert.assertEquals(402.7, ionAnnotations.get(12)
                .getChromatographyInfo().getRetentionTime(), 0.1);

    }

}
