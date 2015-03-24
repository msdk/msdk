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

package io.github.msdk.datamodel.peaklists;

import io.github.msdk.datamodel.rawdata.ChromatographyInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/* 
 * WARNING: the interfaces in this package are still under construction
 */

/**
 * 
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
    Double getMz();

    /**
     * Sets new m/z value of this feature.
     */
    void setMz(@Nonnull Double newMZ);

    /**
     * @return The retention time of this feature.
     */
    @Nullable
    ChromatographyInfo getChromatographyInfo();

    /**
     * Sets new retention time to this feature.
     */
    void setChromatographyInfo(@Nullable ChromatographyInfo chromData);

    /**
     * @return The height of this feature. The height might be different from
     *         the raw data point intensities (e.g., normalized).
     */
    @Nonnull
    Float getHeight();

    /**
     * Sets new height to this feature.
     */
    void setHeight(@Nonnull Float newHeight);

    /**
     * @return The area of this feature. The area might be different from the
     *         area of the raw data points (e.g. normalized). Area may be null
     *         in case data has no RT dimension.
     */
    @Nullable
    Float getArea();

    /**
     * Sets new area to this feature.
     */
    void setArea(@Nullable Float newArea);

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
