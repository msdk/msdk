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

package io.github.msdk.datamodel.rawdata;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

import io.github.msdk.datamodel.msspectra.MsSpectrum;

/**
 * Represents a single MS scan in a raw data file. This interface extends
 * IMassSpectrum, therefore the actual data points can be accessed through the
 * inherited methods of IMassSpectrum.
 *
 * If the scan is not added to any file, its data points are stored in memory.
 * However, once the scan is added into a raw data file by calling
 * setRawDataFile(), its data points will be stored in a temporary file that
 * belongs to that RawDataFile. When RawDataFile.dispose() is called, the data
 * points are discarded so the MsScan instance cannot be used anymore.
 */
public interface MsScan extends MsSpectrum {

    /**
     * Returns the raw data file that contains this scan. This might return null
     * when the scan is created, but once the scan is added to the raw data file
     * by calling RawDataFile.addScan(), the RawDataFile automatically calls the
     * MsScan.setRawDataFile() method to update this reference.
     *
     * @return RawDataFile containing this MsScan, or null.
     * @see RawDataFile
     */
    @Nullable
    RawDataFile getRawDataFile();

    /**
     * Updates the raw data file reference. This method can be called only once.
     * Any subsequent calls will throw the IllegalOperationException.
     *
     * @param newRawDataFile a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
     */
    void setRawDataFile(@Nonnull RawDataFile newRawDataFile);

    /**
     * Returns the number of this scan, represented by an integer, typically
     * positive. Typically, the scan number will be unique within the file.
     * However, the data model does not guarantee that, and in some cases
     * multiple scans with the same number may be present in the file.
     *
     * @return Scan number
     */
    @Nonnull
    Integer getScanNumber();

    /**
     * Updates the scan number.
     *
     * @param scanNumber
     *            New scan number.
     */
    void setScanNumber(@Nonnull Integer scanNumber);

    /**
     * Returns the instrument-specific textual definition of the scan
     * parameters. For example, in Thermo raw data this may look like:
     *
     * FTMS + p ESI Full ms2 209.09@hcd35.00 [50.00-230.00]
     *
     * The scan definition can be null if not specified in the raw data.
     *
     * @return Scan definition
     */
    @Nullable
    String getScanDefinition();

    /**
     * Updates the scan definition (instrument-specific textual definition of
     * the scan parameters).
     *
     * @param scanDefinition
     *            New scan definition.
     */
    void setScanDefinition(@Nullable String scanDefinition);

    /**
     * Returns the MS function of this scan.
     *
     * @return MS function.
     */
    @Nonnull
    MsFunction getMsFunction();

    /**
     * Updates the MS function of this scan.
     *
     * @param newFunction
     *            New MS function.
     */
    void setMsFunction(@Nonnull MsFunction newFunction);

    /**
     * Returns the type of the MS scan. If unknown, MsScanType.UNKNOWN is
     * returned.
     *
     * @return MS scan type
     */
    @Nonnull
    MsScanType getMsScanType();

    /**
     * Updates the MS scan type.
     *
     * @param newMsScanType a {@link io.github.msdk.datamodel.rawdata.MsScanType} object.
     */
    void setMsScanType(@Nonnull MsScanType newMsScanType);

    /**
     * Returns the chromatography data (retention time, etc.) associated with
     * this scan. Null is returned if no chromatography data is available.
     *
     * @return Associated chromatography data.
     */
    @Nullable
    ChromatographyInfo getChromatographyInfo();

    /**
     * Updates the associated chromatography data.
     *
     * @param chromatographyInfo a {@link io.github.msdk.datamodel.rawdata.ChromatographyInfo} object.
     */
    void setChromatographyInfo(@Nullable ChromatographyInfo chromatographyInfo);

    /**
     * Returns the scanning range of the instrument. Note that this value is
     * different from that returned by getMzRange() from the MassSpectrum
     * interface.
     *
     * getMzRange() returns the range of the actual data points (lowest and
     * highest m/z)
     *
     * getScanningRange() returns the instrument scanning range that was
     * configured in the experiment setup.
     *
     * @return The scanning m/z range of the instrument
     */
    @Nullable
    Range<Double> getScanningRange();

    /**
     * Updates the instrument scanning m/z range.
     *
     * @param newScanRange
     *            New scanning range.
     */
    void setScanningRange(@Nullable Range<Double> newScanRange);

    /**
     * Returns the polarity of this scan. If unknown, PolarityType.UNKNOWN is
     * returned.
     *
     * @return Polarity of this scan.
     */
    @Nonnull
    PolarityType getPolarity();

    /**
     * Updates the polarity of this scan.
     *
     * @param newPolarity
     *            New scan polarity.
     */
    void setPolarity(@Nonnull PolarityType newPolarity);

    /**
     * Returns the fragmentation parameters of ion source-induced fragmentation,
     * or null if no such information is known.
     *
     * @return Fragmentation info of ion source-induced fragmentation, or null.
     */
    @Nullable
    ActivationInfo getSourceInducedFragmentation();

    /**
     * Updates the fragmentation parameters of ion source-induced fragmentation.
     *
     * @param newFragmentationInfo
     *            New fragmentation parameters.
     */
    void setSourceInducedFragmentation(
            @Nullable ActivationInfo newFragmentationInfo);

    /**
     * Returns a list of isolations performed for this scan. These isolations
     * may also include fragmentations (tandem MS).
     *
     * @return A mutable list of isolations. New isolation items can be added to
     *         this list.
     */
    @Nonnull
    List<IsolationInfo> getIsolations();

}
