/*
 * (C) Copyright 2015-2017 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1 as published by the Free
 * Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation.
 */

package io.github.msdk.datamodel.datastore;

import javax.annotation.Nonnull;

/**
 * Data store provider
 */
public class DataPointStoreFactory {

  /**
   * <p>
   * getMemoryDataStore.
   * </p>
   *
   * @return a {@link io.github.msdk.datamodel.datastore.DataPointStore} object.
   */
  public static final @Nonnull DataPointStore getMemoryDataStore() {
    return new MemoryDataStore();
  }

  /**
   * <p>
   * getTmpFileDataPointStore.
   * </p>
   *
   * @return a {@link io.github.msdk.datamodel.datastore.DataPointStore} object.
   */
  public static final @Nonnull DataPointStore getTmpFileDataStore() {
    return new TmpFileDataStore();
  }

}
