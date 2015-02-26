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

import io.github.msdk.datamodel.PeakList;
import io.github.msdk.datamodel.PeakListColumn;
import io.github.msdk.datamodel.PeakListRow;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Implementation of PeakListRow
 */
public class PeakListRowImpl implements PeakListRow {

    @Override
    public @Nonnull PeakList getParentPeakList() {
	// TODO Auto-generated method stub
	return MSDKObjectBuilder.getPeakList();
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

    @Override
    public <DataType> List<PeakListColumn<DataType>> getColumns() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <DataType> Optional<DataType> getData(PeakListColumn<DataType> column) {
	// TODO Auto-generated method stub
	return Optional.empty();
    }

}
