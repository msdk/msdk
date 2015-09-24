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

package io.github.msdk.io.filetypedetection;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.files.FileType;

public class FileTypeDetectionMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @Test
    public void testNetCDF() throws MSDKException {
        File fileName = new File(TEST_DATA_PATH + "wt15.CDF");
        FileTypeDetectionMethod method = new FileTypeDetectionMethod(fileName);
        FileType fileType = method.execute();
        Assert.assertEquals(FileType.NETCDF, fileType);
    }

    @Test
    public void testMzXML() throws MSDKException {
        File fileName = new File(TEST_DATA_PATH + "A1-0_A2.mzXML");
        FileTypeDetectionMethod method = new FileTypeDetectionMethod(fileName);
        FileType fileType = method.execute();
        Assert.assertEquals(FileType.MZXML, fileType);
    }

    @Test
    public void testMzML() throws MSDKException {
        File fileName = new File(TEST_DATA_PATH
                + "RawCentriodCidWithMsLevelInRefParamGroup.mzML");
        FileTypeDetectionMethod method = new FileTypeDetectionMethod(fileName);
        FileType fileType = method.execute();
        Assert.assertEquals(FileType.MZML, fileType);
    }

    @Test
    public void testMzData() throws MSDKException {
        File fileName = new File(TEST_DATA_PATH + "test.mzData");
        FileTypeDetectionMethod method = new FileTypeDetectionMethod(fileName);
        FileType fileType = method.execute();
        Assert.assertEquals(FileType.MZDATA, fileType);
    }

    @Test
    public void testThermoRaw() throws MSDKException {
        File fileName = new File(TEST_DATA_PATH + "RP240K_01.raw");
        FileTypeDetectionMethod method = new FileTypeDetectionMethod(fileName);
        FileType fileType = method.execute();
        Assert.assertEquals(FileType.THERMO_RAW, fileType);
    }

    @Test
    public void testWatersRaw() throws MSDKException {
        File fileName = new File(TEST_DATA_PATH + "20150813-63.raw");
        FileTypeDetectionMethod method = new FileTypeDetectionMethod(fileName);
        FileType fileType = method.execute();
        Assert.assertEquals(FileType.WATERS_RAW, fileType);
    }

    @Test
    public void testMzTAB() throws MSDKException {
        File fileName = new File(
                TEST_DATA_PATH + "lipidomics-HFD-LD-study-PL-DG-SM.mzTab");
        FileTypeDetectionMethod method = new FileTypeDetectionMethod(fileName);
        FileType fileType = method.execute();
        Assert.assertEquals(FileType.MZTAB, fileType);
    }

}
