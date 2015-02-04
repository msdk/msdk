/* 
 * Copyright 2015 MSDK Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.github.msdevkit.datamodel.impl;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.github.msdevkit.datamodel.PeakList;
import com.github.msdevkit.datamodel.PeakListColumn;
import com.github.msdevkit.datamodel.PeakListRow;

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
