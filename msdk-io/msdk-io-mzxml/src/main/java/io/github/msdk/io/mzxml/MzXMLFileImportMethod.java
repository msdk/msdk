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

package io.github.msdk.io.mzxml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.files.FileType;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.ActivationInfo;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.MsScanType;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.spectrumtypedetection.SpectrumTypeDetectionMethod;
import io.github.msdk.util.MsSpectrumUtil;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;

/**
 * This class reads XML-based mass spec data formats (mzData, mzXML, and mzML)
 * using the jmzreader library.
 */
public class MzXMLFileImportMethod implements MSDKMethod<RawDataFile> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final @Nonnull File sourceFile;
    private final @Nonnull FileType fileType = FileType.MZXML;

    private boolean canceled = false;

    private MzXMLRawDataFile newRawFile;
    private long totalScans = 0, parsedScans;

    /**
     * <p>Constructor for MzXMLFileImportMethod.</p>
     *
     * @param sourceFile a {@link java.io.File} object.
     */
    public MzXMLFileImportMethod(@Nonnull File sourceFile) {
        this.sourceFile = sourceFile;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("null")
    @Override
    public RawDataFile execute() throws MSDKException {

        logger.info("Started parsing file " + sourceFile);

        try {

            MzXMLFile parser = new MzXMLFile(sourceFile);

            totalScans = parser.getSpectraCount();

            // Prepare data structures
            List<MsFunction> msFunctionsList = new ArrayList<>();
            List<MsScan> scansList = new ArrayList<>();
            List<Chromatogram> chromatogramsList = new ArrayList<>();
            MsSpectrumDataPointList dataPoints = MSDKObjectBuilder
                    .getMsSpectrumDataPointList();

            // Create the XMLBasedRawDataFile object
            newRawFile = new MzXMLRawDataFile(sourceFile, fileType, parser,
                    msFunctionsList, scansList, chromatogramsList);

            // Create the converter from jmzreader data model to our data model
            final MzXMLConverter converter = new MzXMLConverter();

            final List<Long> scanNumbers = parser.getScanNumbers();

            for (int scanIndex = 0; scanIndex < totalScans; scanIndex++) {

                if (canceled)
                    return null;

                // Parse the spectrum
                Spectrum spectrum = parser.getSpectrumByIndex(scanIndex + 1);

                // Get the scan number
                String spectrumId = spectrum.getId();
                Integer scanNumber = scanNumbers.get(scanIndex).intValue();

                // For now, let's use the spectrum id as scan definition
                String scanDefinition = spectrumId;

                // Get the MS function
                MsFunction msFunction = converter.extractMsFunction(spectrum);
                msFunctionsList.add(msFunction);

                // Store the chromatography data
                ChromatographyInfo chromData = converter
                        .extractChromatographyData(spectrum);

                // Extract the scan data points, so we can check the m/z range
                // and detect the spectrum type (profile/centroid)
                MzXMLConverter.extractDataPoints(spectrum, dataPoints);

                // Get the m/z range
                Range<Double> mzRange = MsSpectrumUtil.getMzRange(dataPoints);

                // Get the instrument scanning range
                Range<Double> scanningRange = null;

                // Get the TIC
                Float tic = MsSpectrumUtil.getTIC(dataPoints);

                // Auto-detect whether this scan is centroided
                SpectrumTypeDetectionMethod detector = new SpectrumTypeDetectionMethod(
                        dataPoints);
                MsSpectrumType spectrumType = detector.execute();

                // Get the MS scan type
                MsScanType scanType = converter.extractScanType(spectrum);

                // Get the polarity
                PolarityType polarity = converter.extractPolarity(spectrum);

                // Get the in-source fragmentation
                ActivationInfo sourceFragmentation = converter
                        .extractSourceFragmentation(spectrum);

                // Get the in-source fragmentation
                List<IsolationInfo> isolations = converter
                        .extractIsolations(spectrum);

                // Create a new MsScan instance
                MzXMLMsScan scan = new MzXMLMsScan(newRawFile,
                        spectrumId, spectrumType, msFunction, chromData,
                        scanType, mzRange, scanningRange, scanNumber,
                        scanDefinition, tic, polarity, sourceFragmentation,
                        isolations);

                // Add the scan to the final raw data file
                scansList.add(scan);

                parsedScans++;

            }

        } catch (Exception e) {
            throw new MSDKException(e);
        }

        logger.info("Finished importing " + sourceFile + ", parsed "
                + parsedScans + " scans");

        return newRawFile;

    }

    /** {@inheritDoc} */
    @Override
    public Float getFinishedPercentage() {
        return totalScans == 0 ? null : (float) parsedScans / totalScans;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public RawDataFile getResult() {
        return newRawFile;
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() {
        this.canceled = true;
    }

}
