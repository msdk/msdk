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

package io.github.msdk.datamodel;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

/**
 * Represent one MS spectrum in a raw data file.
 */
public interface MsScan extends MassSpectrum {

    /**
     * 
     * @return RawDataFile containing this Scan
     */
    @Nonnull
    RawDataFile getDataFile();

    @Nullable
    ChromatographyData getChromatographyData();

    void setChromatographyData(@Nullable ChromatographyData chromData);

    /**
     * 
     * @return Scan number
     */
    @Nonnull
    Integer getScanNumber();

    void setScanNumber(@Nonnull Integer scanNumber);

    /**
     * 
     * @return MS level
     */
    @Nullable
    Integer getMSLevel();

    void setMSLevel(@Nullable Integer msLevel);

    /**
     * Returns the sum of intensities of all data points.
     * 
     * @return Total ion current
     */
    double getTIC();

    /**
     * @return the actual scanning range of the instrument
     */
    Range<Double> getScanRange();

    @Nonnull
    PolarityType getPolarity();

    /**
     * 
     * @return array of fragment scans, or null if there are none
     */
    @Nonnull
    List<MsMsScan> getFragmentScans();

}
