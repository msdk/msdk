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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.annotation.Nonnull;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.SimpleMsSpectrum;
import io.github.msdk.util.ArrayUtil;

/**
 * This class extracts scans from the results returned from PeakInvestigator. Internally, it returns
 * a SimpleMsSpectrum object if no error bars are present. Otherwise, it returns a
 * {@link io.github.msdk.spectra.peakinvestigator.PeakInvestigatorMsSpectrum}, which decorates a
 * SimpleMsSpectrum.
 */
public class PeakInvestigatorScanExtractingMethod implements MSDKMethod<List<MsSpectrum>> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final @Nonnull File file;

  private List<MsSpectrum> result;

  private long totalBytes = 0, processedBytes = 0;
  private boolean canceled = false;

  PeakInvestigatorScanExtractingMethod(@Nonnull File file) {
    this.file = file;
  }

  PeakInvestigatorScanExtractingMethod(@Nonnull String filename) {
    this.file = new File(filename);
  }

  /** {@inheritDoc} */
  @Override
  public Float getFinishedPercentage() {
    if (totalBytes == 0) {
      return null;
    } else {
      return ((float) processedBytes) / totalBytes;
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<MsSpectrum> execute() throws MSDKException {
    logger.info("Started extracting scans from file {}.", file);
    try {
      totalBytes = Files.size(file.toPath());
    } catch (IOException e) {
      throw new MSDKException(e);
    }

    result = new ArrayList<>();

    try (BytesReadInputStream stream = new BytesReadInputStream(new FileInputStream(file));
        TarArchiveInputStream tar =
            new TarArchiveInputStream(new GzipCompressorInputStream(stream))) {

      TarArchiveEntry entry;
      while ((entry = tar.getNextTarEntry()) != null) {

        if (canceled) {
          return null;
        }

        byte[] bytes = IOUtils.readFully(tar, (int) entry.getSize());

        BufferedReader reader =
            new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
        MsSpectrum spectrum = parseMsSpectrum(reader);
        if (spectrum != null) {
          result.add(spectrum);
        }

        processedBytes = stream.getBytesRead();
      }

    } catch (FileNotFoundException e) {
      throw new MSDKException(e);
    } catch (IOException e) {
      throw new MSDKException(e);
    }
    logger.info("Finished extracting scans from file {}.", file);
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public List<MsSpectrum> getResult() {
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    this.canceled = true;
  }

  /**
   * This function assumes that the data is either formatted as two columns or five columns,
   * separated by whitespace. If two columns, a MsSpectrum is returned via
   * {@code MSDKObjectBuilder.getMsSpectrum()}. If five columns, the simple MsSpectrum is decorated
   * using the {@link io.github.msdk.spectra.peakinvestigator.PeakInvestigatorMsSpectrum}.
   *
   * @param reader a {@link java.io.BufferedReader} object.
   * @return a {@link io.github.msdk.datamodel.MsSpectrum} object.
   * @throws java.io.IOException if any.
   */
  protected MsSpectrum parseMsSpectrum(BufferedReader reader) throws IOException {
    String line;
    double[] masses = new double[1024];
    float[] intensities = new float[1024];
    TreeMap<Double, PeakInvestigatorMsSpectrum.Error> errors = new TreeMap<>();

    int size = 0;
    while ((line = reader.readLine()) != null) {
      if (line.startsWith("#")) {
        continue;
      }

      StringTokenizer tokenizer = new StringTokenizer(line);

      double mass = Double.parseDouble(tokenizer.nextToken());
      masses = ArrayUtil.addToArray(masses, mass, size);

      float intenisty = Float.parseFloat(tokenizer.nextToken());
      intensities = ArrayUtil.addToArray(intensities, intenisty, size);

      // assume we are either two columns or five columns
      if (tokenizer.hasMoreTokens()) {
        double mzError = Double.parseDouble(tokenizer.nextToken());
        float intensityError = Float.parseFloat(tokenizer.nextToken());
        double minError = Double.parseDouble(tokenizer.nextToken());

        errors.put(mass, new PeakInvestigatorMsSpectrum.Error(mzError, intensityError, minError));
      }

      size++;
    }

    if (size == 0) {
      return null;
    }

    MsSpectrum result =
        new SimpleMsSpectrum(masses, intensities, size, MsSpectrumType.CENTROIDED);
    if (errors.size() == size) {
      result = new PeakInvestigatorMsSpectrum(result, errors);
    }

    return result;
  }

  /**
   * This class is used to keep track of the number of bytes read from an InputStream.
   */
  private class BytesReadInputStream extends InputStream {

    private final InputStream stream;
    private long bytesRead = 0;

    public BytesReadInputStream(InputStream stream) {
      this.stream = stream;
    }

    @Override
    public int read() throws IOException {
      int value = stream.read();
      if (value >= 0) {
        bytesRead++;
      }
      return value;
    }

    @Override
    public int read(byte[] bytes) throws IOException {
      int count = stream.read(bytes);
      if (count > 0) {
        bytesRead += count;
      }
      return count;
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
      int count = stream.read(bytes, offset, length);
      if (count > 0) {
        bytesRead += count;
      }
      return count;
    }

    public long getBytesRead() {
      return bytesRead;
    }
  }
}
