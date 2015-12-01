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

package io.github.msdk.io.mzml;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzml.MzMLFileExportMethod;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import io.github.msdk.util.MsSpectrumUtil;

public class MzMLFileExportMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @Test
    public void test5peptideFT() throws MSDKException, IOException {

        // Create the data structures
        MsSpectrumDataPointList dataPoints = MSDKObjectBuilder
                .getMsSpectrumDataPointList();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "5peptideFT.mzML");
        Assert.assertTrue(inputFile.canRead());
        MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
        RawDataFile rawFile = importer.execute();
        Assert.assertNotNull(rawFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // Export the file to a new mzML
        File tempFile = File.createTempFile("mzmine", ".mzML");
        MzMLFileExportMethod exporter = new MzMLFileExportMethod(rawFile,
                tempFile);
        exporter.execute();
        Assert.assertEquals(1.0, exporter.getFinishedPercentage(), 0.0001);

        // Import the new mzML
        importer = new MzMLFileImportMethod(tempFile);
        RawDataFile newMzMLFile = importer.execute();
        Assert.assertNotNull(newMzMLFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // CHeck number of scans
        List<MsScan> scans = newMzMLFile.getScans();
        Assert.assertNotNull(scans);
        Assert.assertEquals(rawFile.getScans().size(), scans.size());

        // 2nd scan, #2
        MsScan scan2 = scans.get(1);
        Assert.assertEquals(new Integer(2), scan2.getScanNumber());
        Assert.assertEquals(MsSpectrumType.PROFILE, scan2.getSpectrumType());
        Assert.assertEquals(new Integer(1), scan2.getMsFunction().getMsLevel());
        Assert.assertEquals(0.474f,
                scan2.getChromatographyInfo().getRetentionTime(), 0.01f);
        Assert.assertEquals(PolarityType.POSITIVE, scan2.getPolarity());
        scan2.getDataPoints(dataPoints);
        Assert.assertEquals(19800, dataPoints.getSize());
        Float scan2maxInt = MsSpectrumUtil.getMaxIntensity(dataPoints);
        Assert.assertEquals(1.8E5f, scan2maxInt, 1E4f);

        // 5th scan, #5
        MsScan scan5 = scans.get(4);
        Assert.assertEquals(new Integer(5), scan5.getScanNumber());
        Assert.assertEquals(MsSpectrumType.CENTROIDED, scan5.getSpectrumType());
        Assert.assertEquals(new Integer(2), scan5.getMsFunction().getMsLevel());
        Assert.assertEquals(2.094f,
                scan5.getChromatographyInfo().getRetentionTime(), 0.01f);
        Assert.assertEquals(PolarityType.POSITIVE, scan5.getPolarity());
        scan5.getDataPoints(dataPoints);
        Assert.assertEquals(837, dataPoints.getSize());
        Float scan5maxInt = MsSpectrumUtil.getMaxIntensity(dataPoints);
        Assert.assertEquals(8.6E3f, scan5maxInt, 1E2f);

        // TODO: check chromatogram
        
        // Cleanup
        rawFile.dispose();
        newMzMLFile.dispose();
        // TODO tempFile.delete();

    }

}
