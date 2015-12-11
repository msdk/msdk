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

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Strings;
import com.google.common.collect.Range;

import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;

/**
 * This class represents scan selection.
 */
@Immutable
public class ScanSelection {

    private final Range<Integer> scanNumberRange;
    private final Range<Double> scanRTRange;
    private final PolarityType polarity;
    private final MsSpectrumType spectrumType;
    private final Integer msLevel;
    private String scanDefinition;

    public ScanSelection() {
        this(1);
    }

    /**
     * <p>Constructor for ScanSelection.</p>
     *
     * @param msLevel an Integer<Double>.
     */
    public ScanSelection(int msLevel) {
        this(null, null, null, null, msLevel, null);
    }

    /**
     * <p>Constructor for ScanSelection.</p>
     *
     * @param scanRTRange a Range<Double>.
     * @param msLevel an Integer<Double>.
     */
    public ScanSelection(Range<Double> scanRTRange, int msLevel) {
        this(null, scanRTRange, null, null, msLevel, null);
    }

    /**
     * <p>Constructor for ScanSelection.</p>
     *
     * @param scanNumberRange a Range<Integer>.
     * @param scanRTRange a Range<Double>.
     * @param polarity a PolarityType<Double>.
     * @param spectrumType a MsSpectrumType<Double>.
     * @param msLevel an Integer<Double>.
     * @param scanDefinition a string<Double>.
     */
    public ScanSelection(Range<Integer> scanNumberRange,
            Range<Double> scanRTRange, PolarityType polarity,
            MsSpectrumType spectrumType, Integer msLevel,
            String scanDefinition) {
        this.scanNumberRange = scanNumberRange;
        this.scanRTRange = scanRTRange;
        this.polarity = polarity;
        this.spectrumType = spectrumType;
        this.msLevel = msLevel;
        this.scanDefinition = scanDefinition;
    }

    public Range<Integer> getScanNumberRange() {
        return scanNumberRange;
    }

    public Range<Double> getScanRTRange() {
        return scanRTRange;
    }

    public PolarityType getPolarity() {
        return polarity;
    }

    public MsSpectrumType getSpectrumType() {
        return spectrumType;
    }

    public Integer getMsLevel() {
        return msLevel;
    }

    public String getScanDefinition() {
        return scanDefinition;
    }

    public List<MsScan> getMatchingScans(RawDataFile dataFile) {

        final List<MsScan> matchingScans = new ArrayList<>();

        for (MsScan scan : dataFile.getScans()) {

            if ((msLevel != null)
                    && (!msLevel.equals(scan.getMsFunction().getMsLevel())))
                continue;

            if ((polarity != null) && (!polarity.equals(scan.getPolarity())))
                continue;

            if ((spectrumType != null)
                    && (!spectrumType.equals(scan.getSpectrumType())))
                continue;

            if ((scanNumberRange != null)
                    && (!scanNumberRange.contains(scan.getScanNumber())))
                continue;

            if ((scanRTRange != null) && (!scanRTRange.contains(scan
                    .getChromatographyInfo().getRetentionTime().doubleValue())))
                continue;

            if (!Strings.isNullOrEmpty(scanDefinition)) {

                final String actualScanDefition = scan.getScanDefinition();

                if (Strings.isNullOrEmpty(actualScanDefition))
                    continue;

                final String regex = TextUtils
                        .createRegexFromWildcards(scanDefinition);

                if (!actualScanDefition.matches(regex))
                    continue;

            }

            matchingScans.add(scan);
        }

        return matchingScans;

    }

}
