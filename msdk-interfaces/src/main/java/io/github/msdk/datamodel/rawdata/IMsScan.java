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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

/**
 * Represents a single MS scan in a raw data file. This interface extends
 * IMassSpectrum, therefore the actual data points can be accessed through the
 * inherited methods of IMassSpectrum.
 * 
 * If the scan is not added to any file, its data points are stored in memory.
 * However, once the scan is added into a raw data file by calling
 * setRawDataFile(), its data points will be stored in a temporary file that
 * belongs to that IRawDataFile. When IRawDataFile.dispose() is called, the data
 * points are discarded so the IMsScan instance cannot be used anymore.
 */
public interface IMsScan extends IMassSpectrum, Cloneable {

    /**
     * Returns the raw data file that contains this scan. This might return null
     * when the scan is created, but once the scan is added to the raw data file
     * by calling IRawDataFile.addScan(), the IRawDataFile automatically calls
     * the IMsScan.setRawDataFile() method to update this reference.
     * 
     * @return IRawDataFile containing this MsScan, or null.
     * @see IRawDataFile
     */
    @Nullable
    IRawDataFile getRawDataFile();

    /**
     * Updates the raw data file reference. This method can be called only once.
     * Any subsequent calls will throw the IllegalOperationException.
     * 
     * @param newDataFile
     *            New IRawDataFile reference.
     * @throws IllegalOperationException
     *             If the reference to the raw data file has already been set.
     */
    void setRawDataFile(@Nonnull IRawDataFile newDataFile);

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
     * Returns the MS function of this scan.
     * 
     * @return MS function.
     */
    @Nonnull
    IMsFunction getMsFunction();

    /**
     * Updates the MS function of this scan.
     * 
     * @param newFunction
     *            New MS function.
     */
    void setMsFunction(@Nonnull IMsFunction newFunction);

    /**
     * Returns the chromatography data (retention time, etc.) associated with
     * this scan. Null is returned if no chromatography data is available.
     * 
     * @return Associated chromatography data.
     */
    @Nullable
    IChromatographyData getChromatographyData();

    /**
     * Updates the associated chromatography data.
     * 
     * @param chromData
     */
    void setChromatographyData(@Nullable IChromatographyData chromData);

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
     * Returns a deep clone of this object.
     * 
     * @return A clone.
     */
    IMsScan clone();

}
