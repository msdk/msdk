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

package io.github.msdk.io.mgf;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.IsolationInfo;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.MsSpectrum;

public class MgfFileExportMethodTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  private static final double[] mzValues1 = {100.0, 200.0};
  private static final double[] mzValues2 = {300.0, 400.0};

  private static final float[] intensityValues1 = {10.0f, 20.0f};
  private static final float[] intensityValues2 = {30.0f, 40.0f};

  private static final String[] expectedSimpleResults =
      {"BEGIN IONS", "100.0 10.0", "200.0 20.0", "END IONS", ""};

  @Test
  public void testSimple() throws IOException, MSDKException {
    MsSpectrum spectrum = mockMsSpectrum(mzValues1, intensityValues1);
    File file = folder.newFile();

    MgfFileExportMethod method = new MgfFileExportMethod(spectrum, file);
    method.execute();

    assertThat((double) method.getFinishedPercentage(), closeTo(1.0, 0.001));
    List<String> lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
    assertThat(lines.toArray(new String[lines.size()]), arrayContaining(expectedSimpleResults));
  }

  private static final String[] expectedTwoResults = {"BEGIN IONS", "100.0 10.0", "200.0 20.0",
      "END IONS", "", "BEGIN IONS", "300.0 30.0", "400.0 40.0", "END IONS", ""};

  @Test
  public void testTwoSimpleResults() throws IOException, MSDKException {
    ArrayList<MsSpectrum> spectra = new ArrayList<>();
    spectra.add(mockMsSpectrum(mzValues1, intensityValues1));
    spectra.add(mockMsSpectrum(mzValues2, intensityValues2));
    File file = folder.newFile();

    MgfFileExportMethod method = new MgfFileExportMethod(spectra, file);
    method.execute();

    assertThat((double) method.getFinishedPercentage(), closeTo(1.0, 0.001));
    List<String> lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
    assertThat(lines.toArray(new String[lines.size()]), arrayContaining(expectedTwoResults));
  }

  private static final String[] expectedNoChromatography = {"BEGIN IONS", "PEPMASS=500.0",
      "CHARGE=1", "Title=Scan #1", "100.0 10.0", "200.0 20.0", "END IONS", ""};

  @Test
  public void testMsScanWithNoChromatograpy() throws IOException, MSDKException {
    IsolationInfo ii = mockIsolationInfo(500.0, 1);
    MsScan scan = mockMsScan(mzValues1, intensityValues1, ii, null, 1);
    File file = folder.newFile();

    MgfFileExportMethod method = new MgfFileExportMethod(scan, file);
    method.execute();

    assertThat((double) method.getFinishedPercentage(), closeTo(1.0, 0.001));
    List<String> lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
    assertThat(lines.toArray(new String[lines.size()]), arrayContaining(expectedNoChromatography));
  }

  private static final String[] expectedChromatography = {"BEGIN IONS", "PEPMASS=500.0", "CHARGE=1",
      "RTINSECONDS=1.0", "Title=Scan #1", "100.0 10.0", "200.0 20.0", "END IONS", ""};

  @Test
  public void testMsScanWithChromatograpy() throws IOException, MSDKException {
    IsolationInfo ii = mockIsolationInfo(500.0, 1);
    MsScan scan = mockMsScan(mzValues1, intensityValues1, ii, 1.0f, 1);
    File file = folder.newFile();

    MgfFileExportMethod method = new MgfFileExportMethod(scan, file);
    method.execute();

    assertThat((double) method.getFinishedPercentage(), closeTo(1.0, 0.001));
    List<String> lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
    assertThat(lines.toArray(new String[lines.size()]), arrayContaining(expectedChromatography));
  }

  private MsSpectrum mockMsSpectrum(double[] mzValues, float[] intensityValues) {
    if (mzValues.length != intensityValues.length) {
      throw new IllegalArgumentException("Number of mzValues and intensityValues do not agree");
    }

    MsSpectrum spectrum = mock(MsSpectrum.class);
    when(spectrum.getNumberOfDataPoints()).thenReturn(mzValues.length);
    when(spectrum.getMzValues()).thenReturn(mzValues);
    when(spectrum.getIntensityValues()).thenReturn(intensityValues);

    return spectrum;
  }

  private MsScan mockMsScan(double[] mzValues, float[] intensityValues, IsolationInfo ii,
      Float rt, Integer scanNumber) {
    if (mzValues.length != intensityValues.length) {
      throw new IllegalArgumentException("Number of mzValues and intensityValues do not agree");
    }

    MsScan scan = mock(MsScan.class);
    when(scan.getNumberOfDataPoints()).thenReturn(mzValues.length);
    when(scan.getMzValues()).thenReturn(mzValues);
    when(scan.getIntensityValues()).thenReturn(intensityValues);
    when(scan.getIsolations()).thenReturn(Collections.singletonList(ii));
    when(scan.getRetentionTime()).thenReturn(rt);
    when(scan.getScanNumber()).thenReturn(scanNumber);

    return scan;
  }

  private IsolationInfo mockIsolationInfo(Double precursorMz, Integer precursorCharge) {
    IsolationInfo ii = mock(IsolationInfo.class);
    when(ii.getPrecursorMz()).thenReturn(precursorMz);
    when(ii.getPrecursorCharge()).thenReturn(precursorCharge);

    return ii;
  }

}
