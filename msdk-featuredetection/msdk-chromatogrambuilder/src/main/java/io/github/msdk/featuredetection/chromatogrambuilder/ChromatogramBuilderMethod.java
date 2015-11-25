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

package io.github.msdk.featuredetection.chromatogrambuilder;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.util.MZTolerance;

/**
 * <p>ChromatogramBuilderMethod class.</p>
 *
 */
public class ChromatogramBuilderMethod
        implements MSDKMethod<List<Chromatogram>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final @Nonnull DataPointStore dataPointStore;
    private final @Nonnull RawDataFile inputFile;
    private final @Nonnull List<MsScan> inputScans;
    private final @Nonnull Double minimumTimeSpan, minimumHeight;
    private final @Nonnull MZTolerance mzTolerance;

    private int processedScans = 0, totalScans = 0;
    private boolean canceled = false;
    private List<Chromatogram> result;

    /**
     * <p>Constructor for ChromatogramBuilderMethod.</p>
     *
     * @param dataPointStore a {@link io.github.msdk.datamodel.datapointstore.DataPointStore} object.
     * @param inputFile a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
     * @param minimumTimeSpan a {@link java.lang.Double} object.
     * @param minimumHeight a {@link java.lang.Double} object.
     * @param mzTolerance a {@link io.github.msdk.util.MZTolerance} object.
     */
    public ChromatogramBuilderMethod(@Nonnull DataPointStore dataPointStore,
            @Nonnull RawDataFile inputFile, @Nonnull Double minimumTimeSpan,
            @Nonnull Double minimumHeight, @Nonnull MZTolerance mzTolerance) {
        this(dataPointStore, inputFile, inputFile.getScans(), minimumTimeSpan,
                minimumHeight, mzTolerance);
    }

    /**
     * <p>Constructor for ChromatogramBuilderMethod.</p>
     *
     * @param dataPointStore a {@link io.github.msdk.datamodel.datapointstore.DataPointStore} object.
     * @param inputFile a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
     * @param inputScans a {@link java.util.List} object.
     * @param minimumTimeSpan a {@link java.lang.Double} object.
     * @param minimumHeight a {@link java.lang.Double} object.
     * @param mzTolerance a {@link io.github.msdk.util.MZTolerance} object.
     */
    public ChromatogramBuilderMethod(@Nonnull DataPointStore dataPointStore,
            @Nonnull RawDataFile inputFile, @Nonnull List<MsScan> inputScans,
            @Nonnull Double minimumTimeSpan, @Nonnull Double minimumHeight,
            @Nonnull MZTolerance mzTolerance) {
        this.dataPointStore = dataPointStore;
        this.inputFile = inputFile;
        this.inputScans = inputScans;
        this.minimumTimeSpan = minimumTimeSpan;
        this.minimumHeight = minimumHeight;
        this.mzTolerance = mzTolerance;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public List<Chromatogram> execute() throws MSDKException {

        logger.info(
                "Started chromatogram builder on file " + inputFile.getName());

        // Check if we have any scans
        totalScans = inputScans.size();
        if (totalScans == 0) {
            throw new MSDKException(
                    "No scans provided for Chromatogram Builder");
        }

        // Check if the scans are properly ordered by RT
        ChromatographyInfo prevRT = null;
        for (MsScan s : inputScans) {
            if (s.getChromatographyInfo() == null)
                continue;
            if (prevRT == null) {
                prevRT = s.getChromatographyInfo();
                continue;
            }
            if (prevRT.compareTo(s.getChromatographyInfo()) > 0) {
                final String msg = "Retention time of scan #"
                        + s.getScanNumber()
                        + " is smaller then the retention time of the previous scan."
                        + " Please make sure you only use scans with increasing retention times.";
                throw new MSDKException(msg);
            }
            prevRT = s.getChromatographyInfo();
        }

        // Create a new feature table

        HighestDataPointConnector massConnector = new HighestDataPointConnector(
                minimumTimeSpan, minimumHeight, mzTolerance);

        for (MsScan scan : inputScans) {

            if (canceled)
                return null;

            massConnector.addScan(inputFile, scan);
            processedScans++;
        }

        result = new ArrayList<>();
        massConnector.finishChromatograms(inputFile, dataPointStore, result);

        logger.info(
                "Finished chromatogram builder on file " + inputFile.getName());

        return result;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Float getFinishedPercentage() {
        if (totalScans == 0)
            return null;
        else
            return (float) processedScans / totalScans;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public List<Chromatogram> getResult() {
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() {
        this.canceled = true;
    }

}
