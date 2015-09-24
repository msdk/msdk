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

package io.github.msdk.datamodel.impl;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.datapointstore.DataPointStoreFactory;
import io.github.msdk.datamodel.files.FileType;
import io.github.msdk.datamodel.rawdata.ActivationInfo;
import io.github.msdk.datamodel.rawdata.ActivationType;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.MsScanType;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;

/**
 * Tests for SimpleMsScan
 */
public class SimpleMsScanTest {

    private static @Nonnull DataPointStore dataPointStore = DataPointStoreFactory
            .getMemoryDataStore();
    private static @Nonnull MsFunction msFunction = MSDKObjectBuilder
            .getMsFunction("MS", 1);
    private static @Nonnull MsScan msScan1 = MSDKObjectBuilder
            .getMsScan(dataPointStore, 123, msFunction);

    @Test
    public void testScanNumber() throws MSDKException {
        // Verify scan number
        Assert.assertEquals(new Integer(123), msScan1.getScanNumber());
    }

    @Test
    public void testMsFunction() throws MSDKException {
        // Verify ms function
        Assert.assertEquals("MS", msScan1.getMsFunction().getName());
        Assert.assertEquals(new Integer(1),
                msScan1.getMsFunction().getMsLevel());

        // Change ms function
        final @Nonnull MsFunction newMsFunction = MSDKObjectBuilder
                .getMsFunction("MS TOF", 2);
        msScan1.setMsFunction(newMsFunction);

        // Verify ms function
        Assert.assertEquals("MS TOF", msScan1.getMsFunction().getName());
        Assert.assertEquals(new Integer(2),
                msScan1.getMsFunction().getMsLevel());
    }

    @Test
    public void testPolarity() throws MSDKException {
        // Verify polarity
        Assert.assertEquals(PolarityType.UNKNOWN, msScan1.getPolarity());

        // Change polarity
        msScan1.setPolarity(PolarityType.POSITIVE);

        // Verify polarity
        Assert.assertEquals(PolarityType.POSITIVE, msScan1.getPolarity());
    }

    @Test
    public void testMsScanType() throws MSDKException {
        // Verify ms scan type
        Assert.assertEquals(MsScanType.UNKNOWN, msScan1.getMsScanType());

        // Change ms scan type
        msScan1.setMsScanType(MsScanType.MRM_SRM);

        // Verify ms scan type
        Assert.assertEquals(MsScanType.MRM_SRM, msScan1.getMsScanType());
    }

    @SuppressWarnings("null")
    @Test
    public void testRawDataFile() throws MSDKException {
        // Verify raw data file
        Assert.assertEquals(null, msScan1.getRawDataFile());

        // Change raw data file
        final @Nonnull RawDataFile newRawDataFile = MSDKObjectBuilder
                .getRawDataFile("Sample B1", null, FileType.MZML,
                        dataPointStore);
        msScan1.setRawDataFile(newRawDataFile);

        // Verify raw data file
        Assert.assertEquals("Sample B1", msScan1.getRawDataFile().getName());
        Assert.assertEquals(FileType.MZML,
                msScan1.getRawDataFile().getRawDataFileType());
    }

    @Test
    public void testScanningRange() throws MSDKException {
        // Verify scanning range
        Assert.assertEquals(null, msScan1.getScanningRange());

        // Change scanning range
        final Range<Double> newScanRange = Range.closed(0.5, 6.0);
        msScan1.setScanningRange(newScanRange);

        // Verify scanning range
        Assert.assertEquals(newScanRange, msScan1.getScanningRange());
    }

    @SuppressWarnings("null")
    @Test
    public void testChromatographyInfo() throws MSDKException {
        // Verify scanning range
        Assert.assertEquals(null, msScan1.getChromatographyInfo());

        // Change scanning range
        final @Nonnull ChromatographyInfo newChromatographyInfo = MSDKObjectBuilder
                .getChromatographyInfo1D(SeparationType.LC, 1.23f);
        msScan1.setChromatographyInfo(newChromatographyInfo);

        // Verify scanning range
        Assert.assertEquals(SeparationType.LC,
                msScan1.getChromatographyInfo().getSeparationType());
        Assert.assertEquals(new Float(1.23),
                msScan1.getChromatographyInfo().getRetentionTime());
    }

    @SuppressWarnings("null")
    @Test
    public void testSourceInducedFragmentation() throws MSDKException {
        // Verify scanning range
        Assert.assertEquals(null, msScan1.getSourceInducedFragmentation());

        // Change scanning range
        final @Nonnull ActivationInfo newFragmentationInfo = new SimpleActivationInfo(
                25.00, ActivationType.CID);
        msScan1.setSourceInducedFragmentation(newFragmentationInfo);

        // Verify scanning range
        Assert.assertEquals(new Double(25.00),
                msScan1.getSourceInducedFragmentation().getActivationEnergy());
        Assert.assertEquals(ActivationType.CID,
                msScan1.getSourceInducedFragmentation().getActivationType());
    }

    @SuppressWarnings("null")
    @Test
    public void testIsolationInfo() throws MSDKException {
        // Verify isolation info
        Assert.assertEquals(new LinkedList<>(), msScan1.getIsolations());

        // Change isolation info
        final @Nonnull List<IsolationInfo> newIsolations = msScan1
                .getIsolations();
        newIsolations.add(new SimpleIsolationInfo(Range.closed(1.2, 8.9)));
        newIsolations.add(new SimpleIsolationInfo(Range.closed(0.0, 10.0), 0.5f,
                500.123, 1, null));

        // Verify isolation info
        Assert.assertEquals(Range.closed(0.0, 10.0),
                newIsolations.get(1).getIsolationMzRange());
        Assert.assertEquals(new Float(0.5),
                newIsolations.get(1).getIonInjectTime());
        Assert.assertEquals(new Double(500.123),
                newIsolations.get(1).getPrecursorMz());
        Assert.assertEquals(new Integer(1),
                newIsolations.get(1).getPrecursorCharge());
    }

}
