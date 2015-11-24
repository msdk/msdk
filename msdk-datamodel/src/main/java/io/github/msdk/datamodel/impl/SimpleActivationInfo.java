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
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import io.github.msdk.datamodel.rawdata.ActivationInfo;
import io.github.msdk.datamodel.rawdata.ActivationType;

/**
 * Implementation of FragmentationInfo
 */
class SimpleActivationInfo implements ActivationInfo {

    private @Nonnull ActivationType fragmentationType = ActivationType.UNKNOWN;
    private @Nullable Double activationEnergy;

    SimpleActivationInfo(@Nullable Double activationEnergy) {
        this.activationEnergy = activationEnergy;
    }

    SimpleActivationInfo(@Nullable Double activationEnergy,
            @Nonnull ActivationType fragmentationType) {
        Preconditions.checkNotNull(fragmentationType);
        this.activationEnergy = activationEnergy;
        this.fragmentationType = fragmentationType;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public ActivationType getActivationType() {
        return fragmentationType;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Double getActivationEnergy() {
        return activationEnergy;
    }

}
