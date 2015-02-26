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
import io.github.msdk.datamodel.peaklists.IPeakListRow;
import io.github.msdk.datamodel.rawdata.IRawDataFile;

import java.util.List;

/**
 * Simple implementation of the PeakList interface.
 */
public class PeakListImpl extends DataPointStoreImpl implements IPeakList {

    @Override
    public String getName() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setName(String name) {
	// TODO Auto-generated method stub

    }

    @Override
    public int getNumberOfRawDataFiles() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public List<IRawDataFile> getRawDataFiles() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean hasRawDataFile(IRawDataFile file) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public IRawDataFile getRawDataFile(int position) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public int getNumberOfRows() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public List<IPeakListRow> getRows() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void addRow(IPeakListRow row) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void removeRow(IPeakListRow row) {
	// TODO Auto-generated method stub
	
    }


}
