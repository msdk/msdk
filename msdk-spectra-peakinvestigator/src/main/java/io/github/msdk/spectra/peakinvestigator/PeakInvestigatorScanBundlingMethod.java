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

package io.github.msdk.spectra.peakinvestigator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.io.txt.TxtExportAlgorithm;

/**
 * <p>
 * This class represents a method for bundling scans for upload to PeakInvestigator software
 * services.
 * </p>
 */
public class PeakInvestigatorScanBundlingMethod implements MSDKMethod<File> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final @Nonnull File file;

  private int processedScans = 0, totalScans = 0;
  private RawDataFile rawDataFile;
  private boolean canceled = false;

  /**
   * <p>
   * Constructor for PeakInvestigatorScanBundlingMethod. A temporary file is created, which will be
   * deleted on JVM exit.
   * </p>
   *
   * @param rawDataFile a {@link io.github.msdk.datamodel.RawDataFile} object.
   * @throws java.io.IOException if any.
   */
  public PeakInvestigatorScanBundlingMethod(@Nonnull RawDataFile rawDataFile) throws IOException {
    this.rawDataFile = rawDataFile;
    this.file = File.createTempFile("PI-", ".tar");
    this.file.deleteOnExit();
  }

  /**
   * <p>
   * Constructor for PeakInvestigatorScanBundlingMethod.
   * </p>
   *
   * @param rawDataFile a {@link io.github.msdk.datamodel.RawDataFile} object.
   * @param file a {@link java.io.File} object.
   */
  public PeakInvestigatorScanBundlingMethod(@Nonnull RawDataFile rawDataFile, @Nonnull File file) {
    this.rawDataFile = rawDataFile;
    this.file = file;
  }

  /** {@inheritDoc} */
  @Override
  public Float getFinishedPercentage() {
    if (totalScans == 0) {
      return null;
    } else {
      return (float) processedScans / totalScans;
    }
  }

  /** {@inheritDoc} */
  @Override
  public File execute() throws MSDKException {

    logger.info("Started bundling scans from file " + rawDataFile.getName());

    List<MsScan> scans = rawDataFile.getScans();
    totalScans = scans.size();

    try (TarArchiveOutputStream tar =
        new TarArchiveOutputStream(new GzipCompressorOutputStream(new FileOutputStream(file)))) {

      for (MsScan scan : scans) {

        if (canceled)
          return null;

        byte[] bytes = scanToBytes(scan);

        TarArchiveEntry entry =
            new TarArchiveEntry(String.format("scan%05d.txt", scan.getScanNumber()));
        entry.setSize(bytes.length);
        entry.setMode(TarArchiveEntry.DEFAULT_FILE_MODE);
        entry.setModTime(new Date());
        tar.putArchiveEntry(entry);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        IOUtils.copy(inputStream, tar);
        // Add the new scan to the created raw data file

        tar.closeArchiveEntry();
        processedScans++;
      }

    } catch (FileNotFoundException e) {
      throw new MSDKException(e);
    } catch (IOException e) {
      throw new MSDKException(e);
    }

    logger.info("Finished bundling scans from file " + rawDataFile.getName());
    return file;
  }

  /** {@inheritDoc} */
  @Override
  public File getResult() {
    return file;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    this.canceled = true;
  }

  /**
   * Helper method to convert a scan to a sequence of bytes.
   * 
   * @param scan An object that implements the {@link MsScan} interface.
   * @return A byte array containing the scan formatted as tab-delimited text.
   * @throws IOException
   * 
   * @see TxtExportAlgoirthm.spectrumToWriter()
   */
  private byte[] scanToBytes(MsScan scan) throws IOException {
    ByteArrayOutputStream outputStream =
        new ByteArrayOutputStream(4 * scan.getNumberOfDataPoints());
    OutputStreamWriter writer = new OutputStreamWriter(outputStream);
    TxtExportAlgorithm.spectrumToWriter(scan, writer, "\t");
    writer.close();
    outputStream.close();
    return outputStream.toByteArray();
  }
}
