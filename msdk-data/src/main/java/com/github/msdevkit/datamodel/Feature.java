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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A detected feature, characterized mainly by its m/z value, retention time,
 * height and area. The term "feature" is synonymous to "chromatographic peak",
 * or "isotope trace". A single compound analyzed by MS can produce many
 * features in the data set (isotopes, adducts, fragments etc.). The feature can
 * be bound to raw data file, if the raw data is available.
 * 
 * One Feature belongs to exactly one PeakListRow.
 * 
 */
public interface Feature {

    /**
     * @return
     */
    @Nonnull
    PeakListRow getParentPeakListRow();

    /**
     * @return The status of this feature.
     */
    @Nonnull
    FeatureType getFeatureType();

    /**
     * Sets a new status of this feature.
     */
    void setFeatureType(@Nonnull FeatureType newStatus);

    /**
     * @return m/z value of this feature. The m/z value might be different from
     *         the raw m/z data points.
     */
    @Nonnull
    Double getMZ();

    /**
     * Sets new m/z value of this feature.
     */
    void setMZ(@Nonnull Double newMZ);

    /**
     * @return The retention time of this feature.
     */
    @Nullable
    ChromatographyData getChromatographyData();

    /**
     * Sets new retention time to this feature.
     */
    void setChromatographyData(@Nullable ChromatographyData chromData);

    /**
     * @return The height of this feature. The height might be different from
     *         the raw data point intensities (e.g. normalized).
     */
    @Nonnull
    Double getHeight();

    /**
     * Sets new height to this feature.
     */
    void setHeight(@Nonnull Double newHeight);

    /**
     * @return The area of this feature. The area might be different from the
     *         area of the raw data points (e.g. normalized). Area may be null
     *         in case data has no RT dimension.
     */
    @Nullable
    Double getArea();

    /**
     * Sets new area to this feature.
     */
    void setArea(@Nullable Double newArea);

    /**
     * @return Raw data file where this peak is present, or null if this peak is
     *         not connected to any raw data.
     */
    @Nullable
    FeatureShape getFeatureShape();

    /**
     * Assigns a raw data file to this feature.
     */
    void setFeatureShape(@Nullable FeatureShape rawData);

    /**
     * Returns the isotope pattern of this peak or null if no pattern is
     * attached
     */
    @Nullable
    IsotopePattern getIsotopePattern();

    /**
     * Sets the isotope pattern of this peak
     */
    void setIsotopePattern(@Nullable IsotopePattern isotopePattern);

    /**
     * Returns the charge of this feature. If the charge is unknown, returns
     * null. Note that charge is represented by absolute value (negative ions
     * still have a positive value of charge).
     */
    @Nullable
    Integer getCharge();

    /**
     * Sets the charge of this feature. Unknown charge is represented by null.
     * Note that charge is represented by absolute value (negative ions still
     * have a positive value of charge).
     */
    void setCharge(@Nullable Integer charge);

}
