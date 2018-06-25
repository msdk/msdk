/*
 * (C) Copyright 2015-2018 by MSDK Development Team
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

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.equalTo;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.spectra.peakinvestigator.PeakInvestigatorScanBundlingMethod;

public class PeakInvestigatorScanBundlingMethodTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  private static int HEADER_SIZE = 512;

  private final static double[] mzValues1 = {1.0, 2.0};
  private final static float[] intensityValues1 = {10.0f, 20.0f};

  private final static double[] mzValues2 = {3.0, 4.0};
  private final static float[] intensityValues2 = {30.0f, 40.0f};

  @Test
  public void testExecuteOneScan() throws IOException, MSDKException {
    List<MsScan> scans = new LinkedList<>();
    scans.add(mockScan(0, mzValues1, intensityValues1));

    RawDataFile rawDataFile = mockRawDataFile("SingleScan.raw", scans);

    PeakInvestigatorScanBundlingMethod method =
        new PeakInvestigatorScanBundlingMethod(rawDataFile, folder.newFile());
    File file = method.execute();

    assertThat(method.getFinishedPercentage(), equalTo(1.0f));

    try (GzipCompressorInputStream stream =
        new GzipCompressorInputStream(new FileInputStream(file))) {
      TarArchiveEntry entry = getEntryFromStream(stream);
      assertThat(entry.getName(), equalTo("scan00000.txt"));

      int size = (int) entry.getSize();
      byte[] bytes = new byte[size];
      assertThat(stream.read(bytes), equalTo(size));

      BufferedReader reader =
          new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
      assertThat(reader.readLine(), equalTo("1.0\t10.0"));
      assertThat(reader.readLine(), equalTo("2.0\t20.0"));
    }
  }

  @Test
  public void testExecuteTwoScans() throws IOException, MSDKException {
    List<MsScan> scans = new LinkedList<>();
    scans.add(mockScan(0, mzValues1, intensityValues1));
    scans.add(mockScan(12, mzValues2, intensityValues2));

    RawDataFile rawDataFile = mockRawDataFile("TwoScans.raw", scans);

    PeakInvestigatorScanBundlingMethod method =
        new PeakInvestigatorScanBundlingMethod(rawDataFile, folder.newFile());
    File file = method.execute();

    assertThat(method.getFinishedPercentage(), equalTo(1.0f));

    try (GzipCompressorInputStream stream =
        new GzipCompressorInputStream(new FileInputStream(file))) {
      TarArchiveEntry entry = getEntryFromStream(stream);
      assertThat(entry.getName(), equalTo("scan00000.txt"));

      int size = (int) entry.getSize();
      byte[] bytes = new byte[size];
      assertThat(stream.read(bytes), equalTo(size));

      BufferedReader reader =
          new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
      assertThat(reader.readLine(), equalTo("1.0\t10.0"));
      assertThat(reader.readLine(), equalTo("2.0\t20.0"));

      stream.skip(HEADER_SIZE - size % HEADER_SIZE);
      entry = getEntryFromStream(stream);
      assertThat(entry.getName(), equalTo("scan00012.txt"));

      size = (int) entry.getSize();
      bytes = new byte[size];
      assertThat(stream.read(bytes), equalTo(size));

      reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
      assertThat(reader.readLine(), equalTo("3.0\t30.0"));
      assertThat(reader.readLine(), equalTo("4.0\t40.0"));
    }

  }

  private MsScan mockScan(int scanNumber, double[] mzValues, float[] intensityValues) {
    MsScan scan = mock(MsScan.class);
    when(scan.getNumberOfDataPoints()).thenReturn(mzValues.length);
    when(scan.getMzValues()).thenReturn(mzValues);
    when(scan.getIntensityValues()).thenReturn(intensityValues);
    when(scan.getScanNumber()).thenReturn(scanNumber);

    return scan;
  }

  private RawDataFile mockRawDataFile(String name, List<MsScan> scans) {
    RawDataFile rawDataFile = mock(RawDataFile.class);
    when(rawDataFile.getName()).thenReturn(name);
    when(rawDataFile.getScans()).thenReturn(scans);

    return rawDataFile;
  }

  private TarArchiveEntry getEntryFromStream(InputStream stream) throws IOException {
    byte[] header = new byte[HEADER_SIZE];
    stream.read(header);
    return new TarArchiveEntry(header);
  }
}
