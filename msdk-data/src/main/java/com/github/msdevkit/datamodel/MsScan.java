/* 
 * Copyright 2015 MSDK Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.github.msdevkit.datamodel;

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
    @Nonnull
    Integer getMSLevel();

    void setMSLevel(@Nonnull Integer msLevel);

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
