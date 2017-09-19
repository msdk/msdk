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

package io.github.msdk.io.nativeformats;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.FileType;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.datamodel.SimpleRawDataFile;

/**
 * <p>
 * WatersRawImportMethod class.
 * </p>
 */
public class WatersRawImportMethod implements MSDKMethod<RawDataFile> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final @Nonnull File sourceFile;

  private SimpleRawDataFile newRawFile;
  private boolean canceled = false;

  private Process dumperProcess = null;
  private RawDumpParser parser = null;

  /**
   * <p>
   * Constructor for WatersRawImportMethod.
   * </p>
   *
   * @param sourceFile a {@link java.io.File} object.
   */
  public WatersRawImportMethod(@Nonnull File sourceFile) {
    this.sourceFile = sourceFile;
  }

  /** {@inheritDoc} */
  @Override
  public RawDataFile execute() throws MSDKException {

    String osName = System.getProperty("os.name").toUpperCase();
    if (!osName.contains("WINDOWS")) {
      throw new MSDKException("Native data format import only works on MS Windows");
    }

    logger.info("Started parsing file " + sourceFile);

    try {

      // Decompress the Waters raw dump executable to a temporary folder
      File tempFolder = Files.createTempDirectory("msdk").toFile();
      tempFolder.deleteOnExit();
      InputStream dumpArchive =
          this.getClass().getClassLoader().getResourceAsStream("watersrawdump.zip");
      if (dumpArchive == null)
        throw new MSDKException("Failed to load the watersrawdump.zip archive from the MSDK jar");
      ZipUtils.extractStreamToFolder(dumpArchive, tempFolder);

      // Path to the rawdump executable
      File rawDumpPath = new File(tempFolder, "WatersRawDump.exe");

      if (!rawDumpPath.canExecute())
        throw new MSDKException("Cannot execute program " + rawDumpPath);

      // Create a separate process and execute RAWdump.exe
      final String cmdLine[] = new String[] {rawDumpPath.getPath(), sourceFile.getPath()};
      dumperProcess = Runtime.getRuntime().exec(cmdLine);

      // Get the stdout of RAWdump process as InputStream
      InputStream dumpStream = dumperProcess.getInputStream();

      // Create the new RawDataFile
      String fileName = sourceFile.getName();
      newRawFile = new SimpleRawDataFile(fileName, Optional.of(sourceFile), FileType.WATERS_RAW);

      // Read the dump data
      parser = new RawDumpParser(newRawFile);
      parser.readRAWDump(dumpStream);

      // Cleanup
      dumpStream.close();
      dumperProcess.destroy();

      try {
        FileUtils.deleteDirectory(tempFolder);
      } catch (IOException e) {
        // Ignore errors while deleting the tmp folder
      }

      if (canceled)
        return null;

    } catch (Throwable e) {
      if (dumperProcess != null)
        dumperProcess.destroy();

      throw new MSDKException(e);
    }

    logger.info("Finished parsing " + sourceFile);

    return newRawFile;

  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public RawDataFile getResult() {
    return newRawFile;
  }

  /** {@inheritDoc} */
  @Override
  public Float getFinishedPercentage() {
    if (parser == null)
      return 0f;
    else
      return parser.getFinishedPercentage();
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    this.canceled = true;
    if (parser != null) {
      parser.cancel();
    }
  }

}
