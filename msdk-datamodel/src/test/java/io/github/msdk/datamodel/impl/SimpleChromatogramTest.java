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
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.chromatograms.ChromatogramType;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.datapointstore.DataPointStoreFactory;
import io.github.msdk.datamodel.files.FileType;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;

/**
 * Tests for SimpleChromatogram
 */
public class SimpleChromatogramTest {

    private static @Nonnull DataPointStore dataPointStore = DataPointStoreFactory
            .getMemoryDataStore();
    private static @Nonnull Chromatogram chromatogram1 = MSDKObjectBuilder
            .getChromatogram(dataPointStore, 6, ChromatogramType.TIC,
                    SeparationType.LC);

    @Test
    public void testChromatogramNumber() throws MSDKException {
        // Verify chromatogram number
        Assert.assertEquals(new Integer(6),
                chromatogram1.getChromatogramNumber());
    }

    @SuppressWarnings("null")
    @Test
    public void testRawDataFile() throws MSDKException {
        // Verify raw data file
        Assert.assertEquals(null, chromatogram1.getRawDataFile());

        // Change raw data file
        final @Nonnull RawDataFile newRawDataFile = MSDKObjectBuilder
                .getRawDataFile("Sample A1", null, FileType.NETCDF,
                        dataPointStore);
        chromatogram1.setRawDataFile(newRawDataFile);

        // Verify raw data file
        Assert.assertEquals("Sample A1",
                chromatogram1.getRawDataFile().getName());
        Assert.assertEquals(FileType.NETCDF,
                chromatogram1.getRawDataFile().getRawDataFileType());
    }

    @Test
    public void testChromatogramType() throws MSDKException {
        // Verify chromatogram type
        Assert.assertEquals(ChromatogramType.TIC,
                chromatogram1.getChromatogramType());

        // Change chromatogram type
        chromatogram1.setChromatogramType(ChromatogramType.SIC);

        // Verify chromatogram type
        Assert.assertEquals(ChromatogramType.SIC,
                chromatogram1.getChromatogramType());
    }

    @Test
    public void testSeparationType() throws MSDKException {
        // Verify separation type
        Assert.assertEquals(SeparationType.LC,
                chromatogram1.getSeparationType());

        // Change separation type
        chromatogram1.setSeparationType(SeparationType.GCxGC);

        // Verify separation type
        Assert.assertEquals(SeparationType.GCxGC,
                chromatogram1.getSeparationType());
    }

    @SuppressWarnings("null")
    @Test
    public void testIsolationInfo() throws MSDKException {
        // Verify isolation info
        Assert.assertEquals(new LinkedList<>(), chromatogram1.getIsolations());

        // Change isolation info
        final @Nonnull List<IsolationInfo> newIsolations = chromatogram1
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
