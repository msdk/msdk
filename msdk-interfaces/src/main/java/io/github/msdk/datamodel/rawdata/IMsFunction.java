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
import javax.annotation.concurrent.Immutable;

/**
 * Represents a 
 * Is immutable
 */
@Immutable
public interface IMsFunction {

    /**
     * Returns the name of this MS function. In Thermo files, this might be In
     * Waters files, this might be
     * 
     * @return
     */
    @Nonnull
    String getName();

    /**
     * Returns null if unknown.
     * 
     * @return MS level
     */
    @Nullable
    Integer getMsLevel();

}
