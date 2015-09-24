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
 * Represents one MS function, such as "Full scan", "MS/MS", "MS3", "SIM",
 * "MRM", etc. Typically, a single raw data file may contain scans of different
 * functions. The name of the function is instrument-dependent. For convenience,
 * this interface is immutable, so it can be passed by reference and safely used
 * by multiple threads.
 */
@Immutable
public interface MsFunction {

    static final String DEFAULT_MS_FUNCTION_NAME = "ms";

    /**
     * Returns the name of this MS function. For example, in Thermo raw files,
     * this might look like "Full ms", "ms2", or "sim", etc. In Waters raw
     * files, it might look like "MS", "MRM", "TOFD", etc. The value depends on
     * instrument and experiment configuration.
     * 
     * @return Non-null name of this MS function.
     */
    @Nonnull
    String getName();

    /**
     * Returns the MS level of this function, if it is known. 1 means no
     * fragmentation, 2 means MS/MS, 3 means MS3 and so on. Returns null if the
     * MS level is unknown.
     * 
     * @return MS level, or null.
     */
    @Nullable
    Integer getMsLevel();

}
