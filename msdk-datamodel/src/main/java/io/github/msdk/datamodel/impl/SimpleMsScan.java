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

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.rawdata.ActivationInfo;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.MsScanType;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;

/**
 * Simple implementation of the Scan interface.
 */
class SimpleMsScan extends AbstractSpectrum implements MsScan {

    private @Nullable RawDataFile dataFile;
    private @Nonnull Integer scanNumber;
    private @Nullable String scanDefinition;
    private @Nonnull MsFunction msFunction;
    private @Nonnull PolarityType polarity = PolarityType.UNKNOWN;
    private @Nonnull MsScanType msScanType = MsScanType.UNKNOWN;
    private @Nullable Range<Double> scanningRange;
    private @Nullable ChromatographyInfo chromInfo;
    private @Nullable ActivationInfo sourceInducedFragInfo;

    private final @Nonnull List<IsolationInfo> isolations = new LinkedList<>();

    /**
     * <p>Constructor for SimpleMsScan.</p>
     *
     * @param dataPointStore a {@link io.github.msdk.datamodel.datapointstore.DataPointStore} object.
     * @param scanNumber a {@link java.lang.Integer} object.
     * @param msFunction a {@link io.github.msdk.datamodel.rawdata.MsFunction} object.
     */
    public SimpleMsScan(@Nonnull DataPointStore dataPointStore,
            @Nonnull Integer scanNumber, @Nonnull MsFunction msFunction) {
        super(dataPointStore);
        Preconditions.checkNotNull(scanNumber);
        Preconditions.checkNotNull(msFunction);
        this.scanNumber = scanNumber;
        this.msFunction = msFunction;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public RawDataFile getRawDataFile() {
        return dataFile;
    }

    /** {@inheritDoc} */
    @Override
    public void setRawDataFile(@Nonnull RawDataFile newRawDataFile) {
        this.dataFile = newRawDataFile;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Integer getScanNumber() {
        return scanNumber;
    }

    /** {@inheritDoc} */
    @Override
    public void setScanNumber(@Nonnull Integer scanNumber) {
        Preconditions.checkNotNull(scanNumber);
        this.scanNumber = scanNumber;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public String getScanDefinition() {
        return scanDefinition;
    }

    /** {@inheritDoc} */
    @Override
    public void setScanDefinition(@Nullable String scanDefinition) {
        this.scanDefinition = scanDefinition;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public MsFunction getMsFunction() {
        return msFunction;
    }

    /** {@inheritDoc} */
    @Override
    public void setMsFunction(@Nonnull MsFunction newFunction) {
        Preconditions.checkNotNull(newFunction);
        this.msFunction = newFunction;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Range<Double> getScanningRange() {
        return scanningRange;
    }

    /** {@inheritDoc} */
    @Override
    public void setScanningRange(@Nullable Range<Double> newScanRange) {
        this.scanningRange = newScanRange;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public PolarityType getPolarity() {
        return polarity;
    }

    /** {@inheritDoc} */
    @Override
    public void setPolarity(@Nonnull PolarityType newPolarity) {
        Preconditions.checkNotNull(newPolarity);
        this.polarity = newPolarity;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public MsScanType getMsScanType() {
        return msScanType;
    }

    /** {@inheritDoc} */
    @Override
    public void setMsScanType(@Nonnull MsScanType newMsScanType) {
        Preconditions.checkNotNull(newMsScanType);
        this.msScanType = newMsScanType;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public ChromatographyInfo getChromatographyInfo() {
        return chromInfo;
    }

    /** {@inheritDoc} */
    @Override
    public void setChromatographyInfo(
            @Nullable ChromatographyInfo chromatographyInfo) {
        this.chromInfo = chromatographyInfo;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public ActivationInfo getSourceInducedFragmentation() {
        return sourceInducedFragInfo;
    }

    /** {@inheritDoc} */
    @Override
    public void setSourceInducedFragmentation(
            @Nullable ActivationInfo newFragmentationInfo) {
        this.sourceInducedFragInfo = newFragmentationInfo;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<IsolationInfo> getIsolations() {
        return isolations;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Scan ");
        final RawDataFile rawDataFile2 = dataFile;
        if (rawDataFile2 != null) {
            buf.append(rawDataFile2.getName());
            buf.append(" ");
        }
        buf.append(msFunction.getName());
        buf.append(" #");
        buf.append(getScanNumber());
        return buf.toString();
    }

}
