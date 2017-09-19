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

package io.github.msdk.io.mzdata;

import java.io.File;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.FileType;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.datamodel.SimpleRawDataFile;

/**
 * This class reads mzData files. Note: we don't use the jmzreader library, because it completely
 * fails to read retention time values from mzData.
 */
public class MzDataFileImportMethod implements MSDKMethod<RawDataFile> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final @Nonnull File sourceFile;

  private MzDataSaxHandler saxHandler;

  private SimpleRawDataFile newRawFile;
  private boolean canceled = false;

  /**
   * <p>
   * Constructor for MzDataFileImportMethod.
   * </p>
   *
   * @param sourceFile a {@link java.io.File} object.
   */
  public MzDataFileImportMethod(@Nonnull File sourceFile) {
    this.sourceFile = sourceFile;
  }

  /** {@inheritDoc} */
  @Override
  public RawDataFile execute() throws MSDKException {

    logger.info("Started parsing file " + sourceFile);

    String fileName = sourceFile.getName();
    newRawFile = new SimpleRawDataFile(fileName, Optional.of(sourceFile), FileType.MZDATA);

    saxHandler = new MzDataSaxHandler(newRawFile);
    SAXParserFactory factory = SAXParserFactory.newInstance();

    try {
      SAXParser saxParser = factory.newSAXParser();
      saxParser.parse(sourceFile, saxHandler);
    } catch (Exception e) {
      if (canceled)
        return null;
      else
        throw new MSDKException(e);
    }

    logger.info("Finished parsing " + sourceFile);

    return newRawFile;

  }

  /** {@inheritDoc} */
  @Override
  public Float getFinishedPercentage() {
    if (saxHandler != null)
      return saxHandler.getFinishedPercentage();
    else
      return null;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public RawDataFile getResult() {
    return newRawFile;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    this.canceled = true;
    if (saxHandler != null)
      saxHandler.cancel();
  }

}
