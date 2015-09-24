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

package io.github.msdk.io.rawdataimport;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.chromatograms.ChromatogramDataPointList;
import io.github.msdk.datamodel.chromatograms.ChromatogramType;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.ActivationType;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.rawdataimport.mzml.MzMLFileImportMethod;
import io.github.msdk.util.MsSpectrumUtil;

public class MzMLFileImportMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @SuppressWarnings("null")
    @Test
    public void test5peptideFT() throws MSDKException {

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

        // The file has 7 scans
        List<MsScan> scans = rawFile.getScans();
        Assert.assertNotNull(scans);
        Assert.assertEquals(7, scans.size());

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

        rawFile.dispose();

    }

    @SuppressWarnings("null")
    @Test
    public void testPwizTiny() throws MSDKException {

        // Create the data structures
        MsSpectrumDataPointList dataPoints = MSDKObjectBuilder
                .getMsSpectrumDataPointList();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "tiny.pwiz.idx.mzML");
        Assert.assertTrue(inputFile.canRead());
        MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
        RawDataFile rawFile = importer.execute();
        Assert.assertNotNull(rawFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // The file has 4 scans
        List<MsScan> scans = rawFile.getScans();
        Assert.assertNotNull(scans);
        Assert.assertEquals(4, scans.size());

        // 2nd scan, #20
        MsScan scan2 = scans.get(1);
        Assert.assertEquals(new Integer(20), scan2.getScanNumber());
        Assert.assertEquals(MsSpectrumType.CENTROIDED, scan2.getSpectrumType());
        Assert.assertEquals(new Integer(2), scan2.getMsFunction().getMsLevel());
        Assert.assertEquals(359.43f,
                scan2.getChromatographyInfo().getRetentionTime(), 0.01f);
        Assert.assertEquals(PolarityType.POSITIVE, scan2.getPolarity());
        scan2.getDataPoints(dataPoints);
        Assert.assertEquals(10, dataPoints.getSize());
        Float scan2maxInt = MsSpectrumUtil.getMaxIntensity(dataPoints);
        Assert.assertEquals(20f, scan2maxInt, 0.001f);

        List<IsolationInfo> scan2Isolations = scan2.getIsolations();
        Assert.assertNotNull(scan2Isolations);
        Assert.assertEquals(1, scan2Isolations.size());

        IsolationInfo scan2Isolation = scan2Isolations.get(0);
        Assert.assertEquals(445.34, scan2Isolation.getPrecursorMz(), 0.001);
        Assert.assertEquals(new Integer(2),
                scan2Isolation.getPrecursorCharge());

        rawFile.dispose();

    }

    @SuppressWarnings("null")
    @Test
    public void testParamGroup() throws MSDKException {

        // Create the data structures
        MsSpectrumDataPointList dataPoints = MSDKObjectBuilder
                .getMsSpectrumDataPointList();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH
                + "RawCentriodCidWithMsLevelInRefParamGroup.mzML");
        Assert.assertTrue(inputFile.canRead());
        MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
        RawDataFile rawFile = importer.execute();
        Assert.assertNotNull(rawFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // The file has 102 scans
        List<MsScan> scans = rawFile.getScans();
        Assert.assertNotNull(scans);
        Assert.assertEquals(102, scans.size());

        // 2nd scan, #1001
        MsScan scan2 = scans.get(1);
        Assert.assertEquals(new Integer(1001), scan2.getScanNumber());
        Assert.assertEquals(MsSpectrumType.CENTROIDED, scan2.getSpectrumType());
        Assert.assertEquals(new Integer(2), scan2.getMsFunction().getMsLevel());
        Assert.assertEquals(100.002f,
                scan2.getChromatographyInfo().getRetentionTime(), 0.01f);
        Assert.assertEquals(PolarityType.POSITIVE, scan2.getPolarity());
        scan2.getDataPoints(dataPoints);
        Assert.assertEquals(33, dataPoints.getSize());
        Float scan2maxInt = MsSpectrumUtil.getMaxIntensity(dataPoints);
        Assert.assertEquals(6.8E3f, scan2maxInt, 1E2f);

        // 101th scan, #1100
        MsScan scan101 = scans.get(100);
        Assert.assertEquals(new Integer(1100), scan101.getScanNumber());
        Assert.assertEquals(MsSpectrumType.CENTROIDED,
                scan101.getSpectrumType());
        Assert.assertEquals(new Integer(1),
                scan101.getMsFunction().getMsLevel());
        Assert.assertEquals(109.998f,
                scan101.getChromatographyInfo().getRetentionTime(), 0.01f);
        scan101.getDataPoints(dataPoints);
        Assert.assertEquals(21, dataPoints.getSize());
        Float scan5maxInt = MsSpectrumUtil.getMaxIntensity(dataPoints);
        Assert.assertEquals(1.8E4f, scan5maxInt, 1E2f);

        rawFile.dispose();

    }

    @Test
    public void testCompressedAndUncompressed() throws MSDKException {

        // Create the data structures
        MsSpectrumDataPointList dataPoints1 = MSDKObjectBuilder
                .getMsSpectrumDataPointList();
        MsSpectrumDataPointList dataPoints2 = MSDKObjectBuilder
                .getMsSpectrumDataPointList();

        // Import the compressed file
        File compressedFile = new File(
                TEST_DATA_PATH + "MzMLFile_7_compressed.mzML");
        Assert.assertTrue(compressedFile.canRead());
        MzMLFileImportMethod importer = new MzMLFileImportMethod(
                compressedFile);
        RawDataFile compressedRaw = importer.execute();
        Assert.assertNotNull(compressedRaw);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // Import the uncompressed file
        File unCompressedFile = new File(
                TEST_DATA_PATH + "MzMLFile_7_uncompressed.mzML");
        Assert.assertTrue(unCompressedFile.canRead());
        importer = new MzMLFileImportMethod(unCompressedFile);
        RawDataFile uncompressedRaw = importer.execute();
        Assert.assertNotNull(uncompressedRaw);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // These files have 3 scans
        List<MsScan> compressedScans = compressedRaw.getScans();
        List<MsScan> unCompressedScans = uncompressedRaw.getScans();
        Assert.assertEquals(3, compressedScans.size());
        Assert.assertEquals(3, unCompressedScans.size());

        for (int i = 0; i < 3; i++) {
            MsScan compressedScan = compressedScans.get(i);
            MsScan unCompressedScan = unCompressedScans.get(i);

            compressedScan.getDataPoints(dataPoints1);
            unCompressedScan.getDataPoints(dataPoints2);

            Assert.assertTrue(dataPoints1.equals(dataPoints2));
        }

        compressedRaw.dispose();
        uncompressedRaw.dispose();

    }

    @SuppressWarnings("null")
    @Test
    public void testSRM() throws MSDKException {

        // Create the data structures
        ChromatogramDataPointList dataPoints = MSDKObjectBuilder
                .getChromatogramDataPointList();

        // Import the file
        File inputFile = new File(TEST_DATA_PATH + "SRM.mzML");
        Assert.assertTrue(inputFile.canRead());
        MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
        RawDataFile rawFile = importer.execute();
        Assert.assertNotNull(rawFile);
        Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

        // The file has 37 chromatograms
        List<Chromatogram> chromatograms = rawFile.getChromatograms();
        Assert.assertNotNull(chromatograms);
        Assert.assertEquals(37, chromatograms.size());

        // 4th chromatogram
        Chromatogram chromatogram = chromatograms.get(3);
        Assert.assertEquals(new Integer(4),
                chromatogram.getChromatogramNumber());
        Assert.assertEquals(ChromatogramType.MRM_SRM,
                chromatogram.getChromatogramType());
        chromatogram.getDataPoints(dataPoints);
        Assert.assertEquals(new Integer(1608), (Integer) dataPoints.getSize());
        Assert.assertEquals(new Integer(2),
                (Integer) chromatogram.getIsolations().size());
        Assert.assertEquals(new Double(440.706),
                chromatogram.getIsolations().get(1).getPrecursorMz());
        Assert.assertEquals(ActivationType.CID, chromatogram.getIsolations()
                .get(0).getActivationInfo().getActivationType());

        // 1st chromatogram
        chromatogram = chromatograms.get(0);
        Assert.assertEquals(ChromatogramType.TIC,
                chromatogram.getChromatogramType());
        Assert.assertEquals(0, chromatogram.getIsolations().size());

        rawFile.dispose();
    }

}
