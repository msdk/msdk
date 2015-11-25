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

package io.github.msdk.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Range;

import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;

/**
 * <p>RawDataFileUtil class.</p>
 *
 */
public class RawDataFileUtil {

    /**
     * <p>getScans.</p>
     *
     * @param rawDataFile a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
     * @param msFunction a {@link io.github.msdk.datamodel.rawdata.MsFunction} object.
     * @return a {@link java.util.List} object.
     */
    @Nonnull
    static public List<MsScan> getScans(RawDataFile rawDataFile,
            MsFunction msFunction) {
        ArrayList<MsScan> msScanList = new ArrayList<MsScan>();
        List<MsScan> scans = rawDataFile.getScans();
        synchronized (scans) {
            for (MsScan scan : scans) {
                if (scan.getMsFunction().equals(msFunction))
                    msScanList.add(scan);
            }
        }
        return msScanList;
    }

    /**
     * <p>getScans.</p>
     *
     * @param rawDataFile a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
     * @param rtRange a {@link com.google.common.collect.Range} object.
     * @return a {@link java.util.List} object.
     */
    @Nonnull
    static public List<MsScan> getScans(RawDataFile rawDataFile,
            Range<ChromatographyInfo> rtRange) {
        ArrayList<MsScan> msScanList = new ArrayList<MsScan>();
        List<MsScan> scans = rawDataFile.getScans();
        synchronized (scans) {
            for (MsScan scan : scans) {
                ChromatographyInfo scanRT = scan.getChromatographyInfo();
                if (scanRT != null) {
                    if (rtRange.contains(scanRT))
                        msScanList.add(scan);
                }
            }
        }
        return new ArrayList<MsScan>();
    }

    /**
     * <p>getScans.</p>
     *
     * @param rawDataFile a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
     * @param msFunction a {@link io.github.msdk.datamodel.rawdata.MsFunction} object.
     * @param rtRange a {@link com.google.common.collect.Range} object.
     * @return a {@link java.util.List} object.
     */
    @Nonnull
    static public List<MsScan> getScans(RawDataFile rawDataFile,
            MsFunction msFunction, Range<ChromatographyInfo> rtRange) {
        ArrayList<MsScan> msScanList = new ArrayList<MsScan>();
        List<MsScan> scans = rawDataFile.getScans();
        synchronized (scans) {
            for (MsScan scan : scans) {
                ChromatographyInfo scanRT = scan.getChromatographyInfo();
                if (scanRT != null) {
                    if (scan.getMsFunction().equals(msFunction)
                            && rtRange.contains(scanRT))
                        msScanList.add(scan);
                }
            }
        }
        return new ArrayList<MsScan>();
    }

    /**
     * <p>getNextChromatogramNumber.</p>
     *
     * @param rawDataFile a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
     * @return a {@link java.lang.Integer} object.
     */
    @Nonnull
    static public Integer getNextChromatogramNumber(RawDataFile rawDataFile){
        int chromatogramNumber = 1;
        List<Chromatogram> chromatograms = rawDataFile.getChromatograms();
        for (Chromatogram chromatogram : chromatograms) {
            int currentChromatogramNumber = chromatogram.getChromatogramNumber();
            if (currentChromatogramNumber > chromatogramNumber)
                chromatogramNumber = currentChromatogramNumber;
        }
        return chromatogramNumber;
    }
}
