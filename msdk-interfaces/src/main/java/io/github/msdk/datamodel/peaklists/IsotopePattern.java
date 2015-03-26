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

import io.github.msdk.datamodel.rawdata.MassSpectrum;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openscience.cdk.interfaces.IMolecularFormula;

/* 
 * WARNING: the interfaces in this package are still under construction
 */

/**
 * This interface defines an isotope pattern which can be attached to a feature
 */
public interface IsotopePattern extends MassSpectrum {

    /**
     * Returns the isotope pattern type.
     */
    @Nonnull
    IsotopePatternType getType();

    /**
     * Returns a description of this isotope pattern (formula, etc.)
     */
    @Nonnull
    String getDescription();

    void setDescription(@Nonnull String description);

    /**
     * 
     */
    @Nullable
    IMolecularFormula getChemicalFormula();

    void setChemicalFormula(@Nullable IMolecularFormula formula);

}