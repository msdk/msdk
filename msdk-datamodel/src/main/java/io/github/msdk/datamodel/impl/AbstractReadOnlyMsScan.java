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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

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

/**
 * <p>Abstract AbstractReadOnlyMsScan class.</p>
 *
 */
public abstract class AbstractReadOnlyMsScan implements MsScan {

    private final @Nonnull RawDataFile dataFile;
    private final @Nonnull Integer scanNumber;
    private final @Nullable String scanDefinition;

    private final @Nonnull MsSpectrumType spectrumType;
    private final @Nonnull MsFunction msFunction;
    private final @Nonnull ChromatographyInfo chromatographyInfo;
    private final @Nonnull MsScanType scanType;
    private final @Nullable Range<Double> mzRange, scanningRange;
    private final @Nonnull Float tic;
    private final @Nonnull PolarityType polarity;
    private final @Nullable ActivationInfo sourceFragmentation;
    private final @Nonnull List<IsolationInfo> isolations;

    /**
     * <p>Constructor for AbstractReadOnlyMsScan.</p>
     *
     * @param dataFile a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
     * @param spectrumType a {@link io.github.msdk.datamodel.msspectra.MsSpectrumType} object.
     * @param msFunction a {@link io.github.msdk.datamodel.rawdata.MsFunction} object.
     * @param chromatographyInfo a {@link io.github.msdk.datamodel.rawdata.ChromatographyInfo} object.
     * @param scanType a {@link io.github.msdk.datamodel.rawdata.MsScanType} object.
     * @param mzRange a {@link com.google.common.collect.Range} object.
     * @param scanningRange a {@link com.google.common.collect.Range} object.
     * @param scanNumber a {@link java.lang.Integer} object.
     * @param scanDefinition a {@link java.lang.String} object.
     * @param tic a {@link java.lang.Float} object.
     * @param polarity a {@link io.github.msdk.datamodel.rawdata.PolarityType} object.
     * @param sourceFragmentation a {@link io.github.msdk.datamodel.rawdata.ActivationInfo} object.
     * @param isolations a {@link java.util.List} object.
     */
    public AbstractReadOnlyMsScan(@Nonnull RawDataFile dataFile,
            @Nonnull MsSpectrumType spectrumType,
            @Nonnull MsFunction msFunction,
            @Nonnull ChromatographyInfo chromatographyInfo,
            @Nonnull MsScanType scanType, @Nullable Range<Double> mzRange,
            @Nullable Range<Double> scanningRange, @Nonnull Integer scanNumber,
            @Nullable String scanDefinition, @Nonnull Float tic,
            @Nonnull PolarityType polarity,
            @Nullable ActivationInfo sourceFragmentation,
            @Nonnull List<IsolationInfo> isolations) {
        this.dataFile = dataFile;
        this.spectrumType = spectrumType;
        this.msFunction = msFunction;
        this.chromatographyInfo = chromatographyInfo;
        this.scanType = scanType;
        this.mzRange = mzRange;
        this.scanningRange = scanningRange;
        this.scanNumber = scanNumber;
        this.scanDefinition = scanDefinition;
        this.tic = tic;
        this.polarity = polarity;
        this.sourceFragmentation = sourceFragmentation;
        this.isolations = isolations;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public MsSpectrumType getSpectrumType() {
        return spectrumType;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Range<Double> getMzRange() {
        return mzRange;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Float getTIC() {
        return tic;
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
    public Integer getScanNumber() {
        return scanNumber;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public String getScanDefinition() {
        return scanDefinition;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public MsFunction getMsFunction() {
        return msFunction;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public MsScanType getMsScanType() {
        return scanType;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public ChromatographyInfo getChromatographyInfo() {
        return chromatographyInfo;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Range<Double> getScanningRange() {
        return scanningRange;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public PolarityType getPolarity() {
        return polarity;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public ActivationInfo getSourceInducedFragmentation() {
        return sourceFragmentation;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<IsolationInfo> getIsolations() {
        return isolations;
    }

    /*
     * Unsupported set-operations
     */

    /** {@inheritDoc} */
    @Override
    public void setDataPoints(@Nonnull MsSpectrumDataPointList newDataPoints) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void setSpectrumType(@Nonnull MsSpectrumType spectrumType) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void setRawDataFile(@Nonnull RawDataFile newDataFile) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void setScanNumber(@Nonnull Integer scanNumber) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void setScanDefinition(@Nullable String scanDefinition) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void setMsFunction(@Nonnull MsFunction newFunction) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void setMsScanType(@Nonnull MsScanType newType) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void setChromatographyInfo(@Nullable ChromatographyInfo chromData) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void setScanningRange(@Nullable Range<Double> newScanRange) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void setPolarity(@Nonnull PolarityType newPolarity) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void setSourceInducedFragmentation(
            @Nullable ActivationInfo newFragmentationInfo) {
        throw new UnsupportedOperationException();
    }

}
