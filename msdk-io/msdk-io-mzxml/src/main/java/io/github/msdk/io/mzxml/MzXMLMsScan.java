/* 
 * (C) Copyright 2015-2016 by MSDK Development Team
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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.impl.AbstractReadOnlyMsScan;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.ActivationInfo;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScanType;
import io.github.msdk.datamodel.rawdata.PolarityType;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;

class MzXMLMsScan extends AbstractReadOnlyMsScan {

    private final @Nonnull MzXMLRawDataFile dataFile;
    private final @Nonnull String spectrumId;

    MzXMLMsScan(@Nonnull MzXMLRawDataFile dataFile, @Nonnull String spectrumId,
            @Nonnull MsSpectrumType spectrumType,
            @Nonnull MsFunction msFunction,
            @Nonnull ChromatographyInfo chromatographyInfo,
            @Nonnull MsScanType scanType, @Nullable Range<Double> mzRange,
            @Nullable Range<Double> scanningRange, @Nonnull Integer scanNumber,
            @Nullable String scanDefinition, @Nonnull Float tic,
            @Nonnull PolarityType polarity,
            @Nullable ActivationInfo sourceFragmentation,
            @Nonnull List<IsolationInfo> isolations,
            @Nonnull Integer numOfDataPoints) {

        super(dataFile, spectrumType, msFunction, chromatographyInfo, scanType,
                mzRange, scanningRange, scanNumber, scanDefinition, tic,
                polarity, sourceFragmentation, isolations, numOfDataPoints);

        this.dataFile = dataFile;
        this.spectrumId = spectrumId;
    }

    /** {@inheritDoc} */
    @Override
    public @Nonnull double[] getMzValues(@Nullable double[] array) {
        try {
            MzXMLFile parser = dataFile.getParser();
            if (parser == null) {
                throw new MSDKRuntimeException(
                        "The raw data file object has been disposed");
            }
            Spectrum jmzSpectrum = parser.getSpectrumById(spectrumId);
            return MzXMLConverter.extractMzValues(jmzSpectrum, array);
        } catch (JMzReaderException e) {
            throw (new MSDKRuntimeException(e));
        }
    }

    /** {@inheritDoc} */
    @Override
    public @Nonnull float[] getIntensityValues(@Nullable float[] array) {
        try {
            MzXMLFile parser = dataFile.getParser();
            if (parser == null) {
                throw new MSDKRuntimeException(
                        "The raw data file object has been disposed");
            }
            Spectrum jmzSpectrum = parser.getSpectrumById(spectrumId);
            return MzXMLConverter.extractIntensityValues(jmzSpectrum, array);
        } catch (JMzReaderException e) {
            throw (new MSDKRuntimeException(e));
        }
    }

}
