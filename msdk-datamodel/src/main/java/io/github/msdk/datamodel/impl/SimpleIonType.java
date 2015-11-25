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

package io.github.msdk.datamodel.impl;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import io.github.msdk.datamodel.ionannotations.IonType;
import io.github.msdk.datamodel.rawdata.PolarityType;

/**
 * Implementation of IonType
 */
class SimpleIonType implements IonType {

    private @Nonnull String name;
    private @Nonnull PolarityType polarity;
    private int numberOfMolecules;
    private @Nonnull String adductFormula;
    private int charge;

    SimpleIonType(@Nonnull String name, @Nonnull PolarityType polarity, 
            int numberOfMolecules, @Nonnull String adductFormula, int charge) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(polarity);
        Preconditions.checkNotNull(adductFormula);
        this.name = name;
        this.polarity = polarity;
        this.numberOfMolecules = numberOfMolecules;
        this.adductFormula = adductFormula;
        this.charge = charge;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public PolarityType getPolarity() {
        return polarity;
    }

    /** {@inheritDoc} */
    @Override
    public int getNumberOfMolecules() {
        return numberOfMolecules;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getAdductFormula() {
        return adductFormula;
    }

    /** {@inheritDoc} */
    @Override
    public int getCharge() {
        return charge;
    }

}
