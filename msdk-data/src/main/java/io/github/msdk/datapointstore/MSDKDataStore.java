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

package io.github.msdk.datapointstore;

import io.github.msdk.MSDKException;
import io.github.msdk.datapointstore.DataPointStore;

import javax.annotation.Nonnull;

/**
 * Data store provider
 */
public class MSDKDataStore {

    private static final MemoryDataPointStore memoryDataStore = new MemoryDataPointStore();

    public static final @Nonnull DataPointStore getMemoryDataStore() {
        return memoryDataStore;
    }

    public static final @Nonnull DataPointStore getTmpFileDataPointStore()
            throws MSDKException {
        return new TmpFileDataPointStore();
    }

}
