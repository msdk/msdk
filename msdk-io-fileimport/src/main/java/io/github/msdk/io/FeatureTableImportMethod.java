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
import io.github.msdk.datamodel.FeatureTable;
import io.github.msdk.datamodel.FileType;
import io.github.msdk.io.filetypedetection.FileTypeDetectionAlgorithm;
import io.github.msdk.io.mztab.MzTabFileImportMethod;

/**
 * This class detects the type of the given data file using the FileTypeDetectionAlgorithm and then
 * imports the feature table using the right import algorithm.
 */
public class FeatureTableImportMethod implements MSDKMethod<FeatureTable> {

  private final @Nonnull File sourceFile;

  private FeatureTable result;
  private boolean canceled = false;
  MSDKMethod<FeatureTable> parser = null;

  /**
   * <p>
   * Constructor for FeatureTableImportMethod.
   * </p>
   *
   * @param sourceFile a {@link java.io.File} object.
   */
  public FeatureTableImportMethod(@Nonnull File sourceFile) {
    this.sourceFile = sourceFile;
  }

  /** {@inheritDoc} */
  @Override
  public FeatureTable execute() throws MSDKException {

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
      case MZTAB:
        parser = new MzTabFileImportMethod(sourceFile);
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
  public FeatureTable getResult() {
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
