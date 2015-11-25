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
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;

import io.github.msdk.datamodel.rawdata.MsFunction;

/**
 * Simple implementation of the MsFunction interface.
 */
@Immutable
class SimpleMsFunction implements MsFunction {

    private final @Nonnull String name;
    private final @Nullable Integer msLevel;

    SimpleMsFunction(@Nonnull String name) {
        Preconditions.checkNotNull(name);
        this.name = name;
        msLevel = null;
    }

    @SuppressWarnings("null")
    SimpleMsFunction(@Nullable Integer msLevel) {
        this.name = MsFunction.DEFAULT_MS_FUNCTION_NAME;
        this.msLevel = msLevel;
    }

    SimpleMsFunction(@Nonnull String name, @Nullable Integer msLevel) {
        Preconditions.checkNotNull(name);
        this.name = name;
        this.msLevel = msLevel;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Integer getMsLevel() {
        return msLevel;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MsFunction))
            return false;
        MsFunction f = (MsFunction) obj;
        if (!name.equals(f.getName()))
            return false;
        if (msLevel == null) {
            if (f.getMsLevel() != null)
                return false;
        }
        final Integer msLevel2 = msLevel;
        if (msLevel2 != null) {
            if (f.getMsLevel() == null)
                return false;
            if (!msLevel2.equals(f.getMsLevel()))
                return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = name.hashCode();
        final Integer msLevel2 = msLevel;
        if (msLevel2 != null) {
            hash += msLevel2.hashCode();
        }
        return hash;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        String str = name;
        if (msLevel != null)
            if (!name.toUpperCase().equals("MS" + msLevel))
                str += " (MS" + msLevel + ")";
        return str;
    }

}
