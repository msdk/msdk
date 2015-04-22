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

import io.github.msdk.datamodel.rawdata.MsFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Simple implementation of the MsFunction interface.
 */
@Immutable
class SimpleMsFunction implements MsFunction {

    private final @Nonnull String name;
    private final @Nullable Integer msLevel;

    SimpleMsFunction(@Nonnull String name, @Nullable Integer msLevel) {
        this.name = name;
        this.msLevel = msLevel;
    }

    SimpleMsFunction(@Nonnull String name) {
        this.name = name;
        msLevel = null;
    }

    SimpleMsFunction(@Nullable Integer msLevel) {
        this.name = MsFunction.DEFAULT_MS_FUNCTION_NAME;
        this.msLevel = msLevel;
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    @Nullable
    public Integer getMsLevel() {
        return msLevel;
    }

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
        if (msLevel != null) {
            if (f.getMsLevel() == null)
                return false;
            if (!msLevel.equals(f.getMsLevel()))
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = name.hashCode();
        if (msLevel != null)
            hash += msLevel.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        String str = name;
        if (msLevel != null)
            str += " (MS" + msLevel + ")";
        return str;
    }

}
