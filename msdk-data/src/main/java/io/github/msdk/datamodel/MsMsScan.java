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

import javax.annotation.Nullable;

import com.google.common.collect.Range;

/**
 * Represent one MS/MS spectrum in a raw data file.
 */
public interface MsMsScan extends MsScan {

    /**
     * 
     * @return parent scan or null if there is no parent scan
     */
    @Nullable
    MsScan getParentScan();

    void setParentScan(@Nullable MsScan parentScan);

    /**
     * Null means the precursor is not specified in the data
     * 
     * @return Precursor m/z
     */
    @Nullable
    Double getPrecursorMz();

    @Nullable
    Range<Double> getIsolationWidth();

    double getActivationEnergy();

    void setPrecursorMz(@Nullable Double precursorMZ);

    void setActivationEnergy(double activationEnergy);

    /**
     * 
     * @return Precursor charge or 0 if charge is unknown
     */
    int getPrecursorCharge();

    void setPrecursorCharge(int charge);

}
