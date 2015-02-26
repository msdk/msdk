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

package io.github.msdk.io.rawdataimport;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.rawdata.IRawDataFile;
import io.github.msdk.datamodel.rawdata.IRawDataFileType;
import io.github.msdk.io.rawdataimport.xmlbased.XMLFileImportAlgorithm;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class detects the type of the given data file using the
 * RawDataFileTypeDetectionAlgorithm and then imports the raw data by performing
 * the right import algorithm.
 */
public class RawDataFileImportAlgorithm implements MSDKMethod<IRawDataFile> {

    private final @Nonnull File sourceFile;
    private IRawDataFile result;
    private boolean canceled = false;
    MSDKMethod<IRawDataFile> parser = null;

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
    public IRawDataFile execute() throws MSDKException {

	FileTypeDetectionAlgorithm typeDetector = new FileTypeDetectionAlgorithm(
		sourceFile);
	IRawDataFileType fileType = typeDetector.execute();

	if (fileType == null)
	    throw new MSDKException("Unsupported file type of file "
		    + sourceFile);

	if (canceled)
	    return null;
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

	result = parser.execute();
	return result;

    }

    @Override
    public double getFinishedPercentage() {
	if (parser == null)
	    return 0.0;
	return parser.getFinishedPercentage();
    }

    @Override
    @Nullable
    public IRawDataFile getResult() {
	return result;
    }

    @Override
    public void cancel() {
	canceled = true;
	if (parser != null)
	    parser.cancel();
    }

}