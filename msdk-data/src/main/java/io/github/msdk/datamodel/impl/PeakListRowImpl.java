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

import io.github.msdk.datamodel.peaklists.IPeakList;
import io.github.msdk.datamodel.peaklists.IPeakListColumn;
import io.github.msdk.datamodel.peaklists.IPeakListRow;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Implementation of PeakListRow
 */
public class PeakListRowImpl implements IPeakListRow {

    @Override
    public @Nonnull IPeakList getParentPeakList() {
	// TODO Auto-generated method stub
	return null;
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

    public <DataType> List<IPeakListColumn<DataType>> getColumns() {
	// TODO Auto-generated method stub
	return null;
    }

    public <DataType> DataType getData(IPeakListColumn<DataType> column) {
	// TODO Auto-generated method stub
	return null;
    }

}
