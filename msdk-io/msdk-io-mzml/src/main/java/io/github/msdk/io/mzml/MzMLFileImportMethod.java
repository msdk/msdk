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
import io.github.msdk.datamodel.chromatograms.ChromatogramType;
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
import io.github.msdk.datamodel.rawdata.SeparationType;
import io.github.msdk.io.spectrumtypedetection.SpectrumTypeDetectionMethod;
import io.github.msdk.util.MsSpectrumUtil;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;
import uk.ac.ebi.jmzml.xml.io.MzMLObjectIterator;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;

/**
 * This class reads mzML data format using the jmzml library.
 */
public class MzMLFileImportMethod implements MSDKMethod<RawDataFile> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final @Nonnull File sourceFile;

    private boolean canceled = false;

    private MzMLRawDataFile newRawFile;
    private long totalScans = 0, totalChromatograms = 0, parsedScans,
            parsedChromatograms;

    /**
     * <p>Constructor for MzMLFileImportMethod.</p>
     *
     * @param sourceFile a {@link java.io.File} object.
     */
    public MzMLFileImportMethod(@Nonnull File sourceFile) {
        this.sourceFile = sourceFile;
    }

    /** {@inheritDoc} */
    @Override
    public RawDataFile execute() throws MSDKException {

        logger.info("Started parsing file " + sourceFile);

        MzMLUnmarshaller parser = new MzMLUnmarshaller(sourceFile);

        totalScans = parser
                .getObjectCountForXpath("/run/spectrumList/spectrum");
        totalChromatograms = parser
                .getObjectCountForXpath("/run/chromatogramList/chromatogram");

        // Prepare data structures
        List<MsFunction> msFunctionsList = new ArrayList<>();
        List<MsScan> scansList = new ArrayList<>();
        List<Chromatogram> chromatogramsList = new ArrayList<>();
        MsSpectrumDataPointList spectrumDataPoints = MSDKObjectBuilder
                .getMsSpectrumDataPointList();

        // Create the XMLBasedRawDataFile object
        newRawFile = new MzMLRawDataFile(sourceFile, parser, msFunctionsList,
                scansList, chromatogramsList);

        // Create the converter from jmzml data model to our data model
        final MzMLConverter converter = new MzMLConverter();

        if (totalScans > 0) {
            MzMLObjectIterator<Spectrum> iterator = parser
                    .unmarshalCollectionFromXpath("/run/spectrumList/spectrum",
                            Spectrum.class);

            while (iterator.hasNext()) {

                if (canceled)
                    return null;

                Spectrum spectrum = iterator.next();

                // Get spectrum ID
                String spectrumId = spectrum.getId();

                // Get the scan number
                Integer scanNumber = converter.extractScanNumber(spectrum);

                // Get the scan definition
                String scanDefinition = converter
                        .extractScanDefinition(spectrum);

                // Get the MS function
                MsFunction msFunction = converter.extractMsFunction(spectrum);
                msFunctionsList.add(msFunction);

                // Store the chromatography data
                ChromatographyInfo chromData = converter
                        .extractChromatographyData(spectrum);

                // Extract the scan data points, so we can check the m/z range
                // and detect the spectrum type (profile/centroid)
                MzMLConverter.extractDataPoints(spectrum, spectrumDataPoints);

                // Get the m/z range
                Range<Double> mzRange = MsSpectrumUtil
                        .getMzRange(spectrumDataPoints);

                // Get the instrument scanning range
                Range<Double> scanningRange = null;

                // Get the TIC
                Float tic = MsSpectrumUtil.getTIC(spectrumDataPoints);

                // Auto-detect whether this scan is centroided
                SpectrumTypeDetectionMethod detector = new SpectrumTypeDetectionMethod(
                        spectrumDataPoints);
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
                MzMLMsScan scan = new MzMLMsScan(newRawFile, spectrumId,
                        spectrumType, msFunction, chromData, scanType, mzRange,
                        scanningRange, scanNumber, scanDefinition, tic,
                        polarity, sourceFragmentation, isolations);

                // Add the scan to the final raw data file
                scansList.add(scan);

                parsedScans++;

            }
        }

        if (totalChromatograms > 0) {
            MzMLObjectIterator<uk.ac.ebi.jmzml.model.mzml.Chromatogram> iterator = parser
                    .unmarshalCollectionFromXpath(
                            "/run/chromatogramList/chromatogram",
                            uk.ac.ebi.jmzml.model.mzml.Chromatogram.class);

            while (iterator.hasNext()) {

                if (canceled)
                    return null;

                uk.ac.ebi.jmzml.model.mzml.Chromatogram chromatogram = iterator
                        .next();

                // Get the chromatogram id
                String chromatogramId = chromatogram.getId();

                // Get the chromatogram number
                Integer chromatogramNumber = chromatogram.getIndex() + 1;

                // Get the separation type
                SeparationType separationType = converter
                        .extractSeparationType(chromatogram);

                // Get the chromatogram type
                ChromatogramType chromatogramType = converter
                        .extractChromatogramType(chromatogram);

                // Get the chromatogram m/z value
                Double mz = converter.extractMz(chromatogram);

                // Get the in-source fragmentation
                List<IsolationInfo> isolations = converter
                        .extractIsolations(chromatogram);

                // Create a new Chromatogram instance
                MzMLChromatogram chrom = new MzMLChromatogram(newRawFile,
                        chromatogramId, chromatogramNumber, separationType, mz,
                        chromatogramType, isolations);

                // Add the chromatogram to the final raw data file
                chromatogramsList.add(chrom);

                parsedChromatograms++;

            }
        }

        parsedChromatograms = totalChromatograms;
        logger.info("Finished importing " + sourceFile + ", parsed "
                + parsedScans + " scans and " + parsedChromatograms
                + " chromatograms");

        return newRawFile;

    }

    /** {@inheritDoc} */
    @Override
    public Float getFinishedPercentage() {
        return (totalScans + totalChromatograms) == 0 ? null
                : (float) (parsedScans + parsedChromatograms)
                        / (totalScans + totalChromatograms);
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
