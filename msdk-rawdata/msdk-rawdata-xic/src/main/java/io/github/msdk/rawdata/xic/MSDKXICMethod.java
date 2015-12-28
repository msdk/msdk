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

package io.github.msdk.rawdata.xic;

import java.util.List;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.chromatograms.ChromatogramType;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;
import io.github.msdk.util.MsSpectrumUtil;

/**
 * <p>
 * MSDKXICMethod class.
 * </p>
 */
public class MSDKXICMethod implements MSDKMethod<Chromatogram> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final @Nonnull RawDataFile rawDataFile;
    private final @Nonnull List<MsScan> scans;
    private final @Nonnull Range<Double> mzRange;
    private final @Nonnull ChromatogramType chromatogramType;
    private final @Nonnull DataPointStore store;

    private int processedScans = 0, totalScans = 0;
    private Chromatogram result;
    private boolean canceled = false;

    /**
     * <p>
     * Constructor for MSDKXICMethod.
     * </p>
     *
     * @param rawDataFile
     *            a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
     * @param scans
     *            a {@link java.util.List} object.
     * @param mzRange
     *            a {@link com.google.common.collect.Range} object.
     * @param chromatogramType
     *            a
     *            {@link io.github.msdk.datamodel.chromatograms.ChromatogramType}
     *            object.
     * @param store
     *            a
     *            {@link io.github.msdk.datamodel.datapointstore.DataPointStore}
     *            object.
     */
    public MSDKXICMethod(@Nonnull RawDataFile rawDataFile,
            @Nonnull List<MsScan> scans, @Nonnull Range<Double> mzRange,
            @Nonnull ChromatogramType chromatogramType,
            @Nonnull DataPointStore store) {
        this.rawDataFile = rawDataFile;
        this.scans = scans;
        this.mzRange = mzRange;
        this.chromatogramType = chromatogramType;
        this.store = store;
    }

    /** {@inheritDoc} */
    @Override
    public Float getFinishedPercentage() {
        if (totalScans == 0) {
            return null;
        } else {
            return (float) processedScans / totalScans;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Chromatogram execute() throws MSDKException {

        logger.info(
                "Started extracting XIC from file " + rawDataFile.getName());

        totalScans = scans.size();

        // Data
        double scanMzValues[] = new double[1000];
        float scanIntensityValues[] = new float[1000];

        ChromatographyInfo chromRtValues[] = new ChromatographyInfo[totalScans];
        double chromMzValues[] = new double[totalScans];
        float chromIntensityValues[] = new float[totalScans];
        int numOfDataPoints;
        for (processedScans = 0; processedScans < totalScans; processedScans++) {

            if (canceled)
                return null;

            MsScan scan = scans.get(processedScans);
            chromRtValues[processedScans] = scan.getChromatographyInfo();
            if (chromRtValues[processedScans] == null)
                throw new MSDKException("Cannot extract chromatogram: scan #"
                        + scan.getScanNumber() + " has no chromatography data");

            scanMzValues = scan.getMzValues(scanMzValues);
            scanIntensityValues = scan.getIntensityValues(scanIntensityValues);
            numOfDataPoints = scan.getNumberOfDataPoints();

            Integer topPeakIndex = MsSpectrumUtil.getBasePeakIndex(scanMzValues,
                    scanIntensityValues, numOfDataPoints, mzRange);
            if (topPeakIndex != null)
                chromMzValues[processedScans] = scanMzValues[topPeakIndex];
            else
                chromMzValues[processedScans] = (mzRange.lowerEndpoint()
                        + mzRange.upperEndpoint()) / 2.0;

            switch (chromatogramType) {
            case BPC:
                if (topPeakIndex != null)
                    chromIntensityValues[processedScans] = scanIntensityValues[topPeakIndex];
                break;
            case TIC:
            case XIC:
            case SIC:
                chromIntensityValues[processedScans] = MsSpectrumUtil.getTIC(
                        scanMzValues, scanIntensityValues, numOfDataPoints,
                        mzRange);
                break;
            default:
                throw new MSDKException(
                        "Invalid chromatogram type: " + chromatogramType);
            }

        }

        // Create a new Chromatogram
        result = MSDKObjectBuilder.getChromatogram(store, 0, chromatogramType,
                SeparationType.UNKNOWN);
        result.setDataPoints(chromRtValues, chromMzValues, chromIntensityValues,
                totalScans);

        logger.info(
                "Finished extracting XIC from file " + rawDataFile.getName());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public Chromatogram getResult() {
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() {
        this.canceled = true;
    }

}
