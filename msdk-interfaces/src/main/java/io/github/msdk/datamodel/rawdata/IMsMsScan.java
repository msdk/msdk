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
 * Represents a single fragmentation (MS/MS) scan in a raw data file.
 */
public interface IMsMsScan extends IMsScan, Cloneable {

    /**
     * Returns the precursor m/z range, that is the m/z range of ions that were
     * fragmented. Null is returned if the precursor information is not
     * specified in the data.
     * 
     * @return Precursor m/z range, or null.
     */
    @Nullable
    Range<Double> getPrecursorMzRange();

    /**
     * Updates the precursor m/z range.
     * 
     * @param precursorMzRange
     *            New precursor m/z range.
     */
    void setPrecursorMzRange(@Nullable Range<Double> precursorMzRange);

    /**
     * Returns the precursor charge. This only makes sense for MS/MS scans that
     * fragment a single precursor ion. Null is returned if the charge is
     * unknown or if the MS/MS scan targets multiple ions (such as DIA scan).
     * Charge is always represented by a positive integer.
     * 
     * @return Precursor charge, or null.
     */
    @Nullable
    Integer getPrecursorCharge();

    /**
     * Updates the precursor charge.
     * 
     * @param charge
     *            New charge (must be positive integer or null).
     */
    void setPrecursorCharge(@Nullable Integer charge);

    /**
     * Returns the type of the MS/MS experiment for this scan. If unknown,
     * MsMsExperimentType.UNKNOWN is returned.
     * 
     * @return MS/MS experiment type
     */
    @Nonnull
    MsMsExperimentType getMsMsExperimentType();

    /**
     * Updates the MS/MS experiment type.
     * 
     * @param newType
     *            New MS/MS experiment type.
     */
    void setMsMsExperimentType(@Nonnull MsMsExperimentType newType);

    /**
     * Returns the activation energy applied for this MS/MS scan. This value has
     * no dimension and its meaning depends on instrument. Null is returned if
     * unknown.
     * 
     * @return MS/MS activation energy, or null.
     */
    @Nullable
    Double getActivationEnergy();

    /**
     * Updates the MS/MS activation energy.
     * 
     * @param activationEnergy
     *            New activation energy.
     */
    void setActivationEnergy(@Nullable Double activationEnergy);

    /**
     * Returns a deep clone of this object.
     * 
     * @return A clone.
     */
    IMsMsScan clone();

}
