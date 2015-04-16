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

import io.github.msdk.MSDKMethod;
import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.rawdata.RawDataFileType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Detector of raw data file format
 */
public class FileTypeDetectionMethod implements MSDKMethod<RawDataFileType> {

    /*
     * See
     * "http://www.unidata.ucar.edu/software/netcdf/docs/netcdf/File-Format-Specification.html"
     */
    private static final String CDF_HEADER = "CDF";

    /*
     * mzML files with index start with <indexedmzML><mzML>tags, but files with
     * no index contain only the <mzML> tag. See
     * "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/schema/mzML1.1.0.xsd"
     */
    private static final String MZML_HEADER = "<mzML";

    /*
     * mzXML files with index start with <mzXML><msRun> tags, but files with no
     * index contain only the <msRun> tag. See
     * "http://sashimi.sourceforge.net/schema_revision/mzXML_3.2/mzXML_3.2.xsd"
     */
    private static final String MZXML_HEADER = "<msRun";

    // See "http://www.psidev.info/sites/default/files/mzdata.xsd.txt"
    private static final String MZDATA_HEADER = "<mzData";

    // See "https://code.google.com/p/unfinnigan/wiki/FileHeader"
    private static final String THERMO_HEADER = String.valueOf(new char[] {
            0x01, 0xA1, 'F', 0, 'i', 0, 'n', 0, 'n', 0, 'i', 0, 'g', 0, 'a', 0,
            'n', 0 });

    private @Nonnull File fileName;
    private @Nullable RawDataFileType result = null;
    private Float finishedPercentage = null;

    /**
     * 
     * @return Detected file type or null if the file is not of any supported
     *         type
     */
    public FileTypeDetectionMethod(@Nonnull File fileName) {
        this.fileName = fileName;
    }

    @Override
    public RawDataFileType execute() throws MSDKException {

        try {
            result = detectDataFileType(fileName);
        } catch (IOException e) {
            throw new MSDKException(e);
        }
        finishedPercentage = 1f;
        return result;

    }

    private RawDataFileType detectDataFileType(File fileName)
            throws IOException {

        if (fileName.isDirectory()) {
            // To check for Waters .raw directory, we look for _FUNC[0-9]{3}.DAT
            for (File f : fileName.listFiles()) {
                if (f.isFile() && f.getName().matches("_FUNC[0-9]{3}.DAT"))
                    return RawDataFileType.WATERS_RAW;
            }
            // We don't recognize any other directory type than Waters
            return null;
        }

        // Read the first 1kB of the file into a String
        InputStreamReader reader = new InputStreamReader(new FileInputStream(
                fileName), "ISO-8859-1");
        char buffer[] = new char[1024];
        reader.read(buffer);
        reader.close();
        String fileHeader = new String(buffer);

        if (fileHeader.startsWith(THERMO_HEADER)) {
            return RawDataFileType.THERMO_RAW;
        }

        if (fileHeader.startsWith(CDF_HEADER)) {
            return RawDataFileType.NETCDF;
        }

        if (fileHeader.contains(MZML_HEADER))
            return RawDataFileType.MZML;

        if (fileHeader.contains(MZDATA_HEADER))
            return RawDataFileType.MZDATA;

        if (fileHeader.contains(MZXML_HEADER))
            return RawDataFileType.MZXML;

        return null;

    }

    @Override
    public Float getFinishedPercentage() {
        return finishedPercentage;
    }

    @Override
    @Nullable
    public RawDataFileType getResult() {
        return result;
    }

    @Override
    public void cancel() {
        // This method is too fast to be canceled
    }

}
