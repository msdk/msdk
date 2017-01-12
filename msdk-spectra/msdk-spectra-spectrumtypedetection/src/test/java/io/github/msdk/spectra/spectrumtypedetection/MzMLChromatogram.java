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

package io.github.msdk.spectra.spectrumtypedetection;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.chromatograms.ChromatogramType;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

class MzMLChromatogram implements Chromatogram {

    private final @Nonnull MzMLRawDataFile dataFile;
    private final @Nonnull String chromatogramId;
    private final @Nonnull Integer chromatogramNumber;
    private final @Nonnull ChromatogramType chromatogramType;
    private final @Nullable Double mz;
    private final @Nonnull SeparationType separationType;
    private final @Nonnull List<IsolationInfo> isolations;
    private final @Nonnull Integer numOfDataPoints;
    private final Range<ChromatographyInfo> rtRange;

    MzMLChromatogram(@Nonnull MzMLRawDataFile dataFile,
            @Nonnull String chromatogramId, @Nonnull Integer chromatogramNumber,
            @Nonnull SeparationType separationType, @Nullable Double mz,
            @Nonnull ChromatogramType chromatogramType,
            @Nonnull List<IsolationInfo> isolations,
            @Nonnull Integer numOfDataPoints,
            @Nonnull Range<ChromatographyInfo> rtRange) {
        this.dataFile = dataFile;
        this.chromatogramId = chromatogramId;
        this.chromatogramNumber = chromatogramNumber;
        this.separationType = separationType;
        this.mz = mz;
        this.chromatogramType = chromatogramType;
        this.isolations = isolations;
        this.numOfDataPoints = numOfDataPoints;
        this.rtRange = rtRange;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public RawDataFile getRawDataFile() {
        return dataFile;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Integer getChromatogramNumber() {
        return chromatogramNumber;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public ChromatogramType getChromatogramType() {
        return chromatogramType;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Double getMz() {
        return mz;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<IsolationInfo> getIsolations() {
        return isolations;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public SeparationType getSeparationType() {
        return separationType;
    }

    /** {@inheritDoc} */
    @Override
    public IonAnnotation getIonAnnotation() {
        return null;
    }

    /*
     * Unsupported set-operations
     */

    /** {@inheritDoc} */
    @Override
    public void setRawDataFile(@Nonnull RawDataFile newRawDataFile) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void setChromatogramNumber(@Nonnull Integer chromatogramNumber) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void setChromatogramType(
            @Nonnull ChromatogramType newChromatogramType) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void setDataPoints(@Nonnull ChromatographyInfo rtValues[],
            @Nullable double mzValues[], @Nonnull float intensityValues[],
            @Nonnull Integer size) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void setMz(@Nullable Double newMz) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void setSeparationType(@Nonnull SeparationType separationType) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void setIonAnnotation(@Nonnull IonAnnotation ionAnnotation) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Integer getNumberOfDataPoints() {
        return numOfDataPoints;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public ChromatographyInfo[] getRetentionTimes() {
        return getRetentionTimes(null);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public ChromatographyInfo[] getRetentionTimes(
            @Nullable ChromatographyInfo[] array) {
        try {
            MzMLUnmarshaller parser = dataFile.getParser();
            if (parser == null) {
                throw new MSDKRuntimeException(
                        "The raw data file object has been disposed");
            }
            uk.ac.ebi.jmzml.model.mzml.Chromatogram jmzChromatogram = parser
                    .getChromatogramById(chromatogramId);
            return MzMLConverter.extractRtValues(jmzChromatogram, array);
        } catch (MzMLUnmarshallerException e) {
            throw (new MSDKRuntimeException(e));
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public float[] getIntensityValues() {
        return getIntensityValues(null);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public float[] getIntensityValues(@Nullable float[] array) {
        try {
            MzMLUnmarshaller parser = dataFile.getParser();
            if (parser == null) {
                throw new MSDKRuntimeException(
                        "The raw data file object has been disposed");
            }
            uk.ac.ebi.jmzml.model.mzml.Chromatogram jmzChromatogram = parser
                    .getChromatogramById(chromatogramId);
            return MzMLConverter.extractIntensityValues(jmzChromatogram, array);
        } catch (MzMLUnmarshallerException e) {
            throw (new MSDKRuntimeException(e));
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public double[] getMzValues() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public double[] getMzValues(@Nullable double[] array) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Range<ChromatographyInfo> getRtRange() {
        return rtRange;
    }

}
