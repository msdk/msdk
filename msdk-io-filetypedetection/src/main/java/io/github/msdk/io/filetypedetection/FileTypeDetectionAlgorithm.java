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

package io.github.msdk.io.filetypedetection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import io.github.msdk.datamodel.FileType;

/**
 * Detector of raw data file format
 */
public class FileTypeDetectionAlgorithm {

  /*
   * See "http://www.unidata.ucar.edu/software/netcdf/docs/netcdf/File-Format-Specification.html"
   */
  private static final String CDF_HEADER = "CDF";

  /*
   * mzML files with index start with <indexedmzML><mzML>tags, but files with no index contain only
   * the <mzML> tag. See
   * "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/schema/mzML1.1.0.xsd"
   */
  private static final String MZML_HEADER = "<mzML";

  /*
   * mzDB is a format based on SQLite, see Bouyssié, D. et al. mzDB: a file format using multiple
   * indexing strategies for the efficient analysis of large LC-MS/MS and SWATH-MS data sets. Mol.
   * Cell Proteomics 14, 771–781 (2015).
   */
  private static final String MZDB_HEADER = "SQLite format";
  private static final String MZDB_HEADER2 = "CREATE TABLE mzdb";

  /*
   * mzXML files with index start with <mzXML><msRun> tags, but files with no index contain only the
   * <msRun> tag. See "http://sashimi.sourceforge.net/schema_revision/mzXML_3.2/mzXML_3.2.xsd"
   */
  private static final String MZXML_HEADER = "<msRun";

  /*
   * See "http://www.psidev.info/sites/default/files/mzdata.xsd.txt"
   */
  private static final String MZDATA_HEADER = "<mzData";

  /*
   * See "https://code.google.com/p/unfinnigan/wiki/FileHeader"
   */
  private static final String THERMO_HEADER = String.valueOf(
      new char[] {0x01, 0xA1, 'F', 0, 'i', 0, 'n', 0, 'n', 0, 'i', 0, 'g', 0, 'a', 0, 'n', 0});

  /*
   * See "http://www.psidev.info/mztab#mzTab_1_0"
   */
  private static final String MZTAB_HEADER = "mzTab-version";

  /**
   * <p>
   * detectDataFileType.
   * </p>
   *
   * @param fileName a {@link java.io.File} object.
   * @return a {@link io.github.msdk.datamodel.FileType} object.
   * @throws java.io.IOException if any.
   */
  public static @Nullable FileType detectDataFileType(@Nonnull File fileName) throws IOException {

    // Parameter check
    Preconditions.checkNotNull(fileName);

    if (fileName.isDirectory()) {
      // To check for Waters .raw directory, we look for _FUNC[0-9]{3}.DAT
      for (File f : fileName.listFiles()) {
        if (f.isFile() && f.getName().matches("_FUNC[0-9]{3}.DAT"))
          return FileType.WATERS_RAW;
      }
      // We don't recognize any other directory type than Waters
      return null;
    }

    // Read the first 1kB of the file into a String
    InputStreamReader reader = new InputStreamReader(new FileInputStream(fileName), "ISO-8859-1");
    char buffer[] = new char[1024];
    reader.read(buffer);
    reader.close();
    String fileHeader = new String(buffer);

    if (fileHeader.startsWith(THERMO_HEADER)) {
      return FileType.THERMO_RAW;
    }

    if (fileHeader.startsWith(CDF_HEADER)) {
      return FileType.NETCDF;
    }

    if (fileHeader.contains(MZML_HEADER))
      return FileType.MZML;

    if (fileHeader.contains(MZDATA_HEADER))
      return FileType.MZDATA;

    if (fileHeader.contains(MZXML_HEADER))
      return FileType.MZXML;

    if (fileHeader.contains(MZTAB_HEADER))
      return FileType.MZTAB;

    if (fileHeader.startsWith(MZDB_HEADER) && fileHeader.contains(MZDB_HEADER2))
      return FileType.MZDB;

    return null;

  }

}
