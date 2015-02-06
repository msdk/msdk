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

package com.github.msdevkit.io.rawdataimport;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.msdevkit.MSDKAlgorithm;
import com.github.msdevkit.MSDKException;
import com.github.msdevkit.datamodel.RawDataFile;
import com.github.msdevkit.datamodel.RawDataFileType;
import com.github.msdevkit.io.rawdataimport.xmlbased.XMLFileImportAlgorithm;

/**
 * This class detects the type of the given data file using the
 * RawDataFileTypeDetectionAlgorithm and then imports the raw data by performing
 * the right import algorithm.
 */
public class RawDataFileImportAlgorithm implements MSDKAlgorithm<RawDataFile> {

    private final @Nonnull File sourceFile;
    private boolean canceled = false;
    MSDKAlgorithm<RawDataFile> parser = null;

    /**
     * 
     * @param sourceFile
     */
    public RawDataFileImportAlgorithm(@Nonnull File sourceFile) {
	this.sourceFile = sourceFile;
    }

    /**
     * @throws MSDKException
     */
    @Override
    public void execute() throws MSDKException {

	FileTypeDetectionAlgorithm typeDetector = new FileTypeDetectionAlgorithm(
		sourceFile);
	typeDetector.execute();
	RawDataFileType fileType = typeDetector.getResult();

	if (fileType == null)
	    throw new MSDKException("Unsupported file type of file "
		    + sourceFile);

	if (canceled)
	    return;
	switch (fileType) {
	case MZML:
	case MZXML:
	case MZDATA:
	    parser = new XMLFileImportAlgorithm(sourceFile, fileType);
	    break;
	default:
	    throw new MSDKException("Unsupported file type of file "
		    + sourceFile);
	}

	parser.execute();
    }

    @Override
    public double getFinishedPercentage() {
	if (parser == null)
	    return 0.0;
	return parser.getFinishedPercentage();
    }

    @Override
    @Nullable
    public RawDataFile getResult() {
	if (parser == null)
	    return null;
	return parser.getResult();
    }

    @Override
    public void cancel() {
	canceled = true;
	if (parser != null)
	    parser.cancel();
    }

}