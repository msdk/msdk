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

package io.github.msdk.io;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.FileType;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.io.filetypedetection.FileTypeDetectionAlgorithm;
import io.github.msdk.io.mzdata.MzDataFileImportMethod;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import io.github.msdk.io.mzxml.MzXMLFileImportMethod;
import io.github.msdk.io.nativeformats.ThermoRawImportMethod;
import io.github.msdk.io.nativeformats.WatersRawImportMethod;
import io.github.msdk.io.netcdf.NetCDFFileImportMethod;

/**
 * This class detects the type of the given data file using the FileTypeDetectionAlgorithm and then
 * imports the raw data by performing the right import algorithm.
 */
public class RawDataFileImportMethod implements MSDKMethod<RawDataFile> {

  private final @Nonnull File sourceFile;

  private RawDataFile result;
  private boolean canceled = false;
  MSDKMethod<RawDataFile> parser = null;

  /**
   * <p>
   * Constructor for RawDataFileImportMethod.
   * </p>
   *
   * @param sourceFile a {@link java.io.File} object.
   */
  public RawDataFileImportMethod(@Nonnull File sourceFile) {
    this.sourceFile = sourceFile;
  }

  /** {@inheritDoc} */
  @Override
  public RawDataFile execute() throws MSDKException {

    FileType fileType;
    try {
      fileType = FileTypeDetectionAlgorithm.detectDataFileType(sourceFile);
    } catch (IOException e) {
      throw new MSDKException(e);
    }

    if (fileType == null)
      throw new MSDKException("Unknown file type of file " + sourceFile);

    if (canceled)
      return null;

    switch (fileType) {
      case MZML:
        parser = new MzMLFileImportMethod(sourceFile);
        break;
      case MZXML:
        parser = new MzXMLFileImportMethod(sourceFile);
        break;
      case MZDATA:
        parser = new MzDataFileImportMethod(sourceFile);
        break;
      case NETCDF:
        parser = new NetCDFFileImportMethod(sourceFile);
        break;
      case THERMO_RAW:
        parser = new ThermoRawImportMethod(sourceFile);
        break;
      case WATERS_RAW:
        parser = new WatersRawImportMethod(sourceFile);
        break;
      default:
        throw new MSDKException("Unsupported file type (" + fileType + ") of file " + sourceFile);
    }

    result = parser.execute();
    return result;

  }

  /** {@inheritDoc} */
  @Override
  public Float getFinishedPercentage() {
    if (parser == null)
      return null;
    return parser.getFinishedPercentage();
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public RawDataFile getResult() {
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    canceled = true;
    if (parser != null)
      parser.cancel();
  }

}
