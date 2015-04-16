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

import io.github.msdk.datamodel.peaklists.PeakList;
import io.github.msdk.datamodel.peaklists.PeakListColumn;
import io.github.msdk.datamodel.peaklists.PeakListRow;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Implementation of PeakListRow
 */
class SimplePeakListRow implements PeakListRow {

    @Override
    public @Nonnull PeakList getParentPeakList() {
        // TODO Auto-generated method stub
        return new SimplePeakList();
    }

    @Override
    public int getId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getNumberOfColumns() {
        // TODO Auto-generated method stub
        return 0;
    }

    public <DataType> List<PeakListColumn<DataType>> getColumns() {
        // TODO Auto-generated method stub
        return null;
    }

    public <DataType> DataType getData(PeakListColumn<DataType> column) {
        // TODO Auto-generated method stub
        return null;
    }

}
