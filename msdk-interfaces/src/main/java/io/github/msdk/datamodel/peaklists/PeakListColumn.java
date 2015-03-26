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

import javax.annotation.Nonnull;
import javax.swing.table.TableCellRenderer;

/* 
 * WARNING: the interfaces in this package are still under construction
 */

/**
 * 
 *
 * @param <DataType>
 */
public interface PeakListColumn<DataType> {

    /**
     * @return Short descriptive name for the peak list column
     */
    String getName();

    /**
     * Change the name of this peak list column
     */
    void setName(String name);

    /**
     * @return
     */
    @Nonnull
    Class<DataType> getDataTypeClass();

    /**
     * 
     */
    @Nonnull
    TableCellRenderer getTableCellRenderer();

}
