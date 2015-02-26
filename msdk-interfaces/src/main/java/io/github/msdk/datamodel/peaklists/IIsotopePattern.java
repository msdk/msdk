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

import io.github.msdk.datamodel.rawdata.IMassSpectrum;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openscience.cdk.interfaces.IMolecularFormula;

/**
 * This interface defines an isotope pattern which can be attached to a feature
 */
public interface IIsotopePattern extends IMassSpectrum {

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