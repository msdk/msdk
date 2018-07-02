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

package io.github.msdk.io.txt;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.datamodel.SimpleMsSpectrum;

public class TxtExportAlgorithmTest {

  private static final double[] mzValues1 = {100.0, 200.0};
  private static final float[] intensityValues1 = {10.0f, 20.0f};

  private static final double[] mzValues2 = {300.0, 400.0};
  private static final float[] intensityValues2 = {30.0f, 40.0f};

  private static final String[] expectedSimple = {"100.0 10.0", "200.0 20.0"};
  private static final String[] expectedSimpleWithTabs = {"100.0\t10.0", "200.0\t20.0"};

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void testExportSpectrum() throws IOException {
    File file = folder.newFile();
    MsSpectrum spectrum = mockSpectrum(mzValues1, intensityValues1);

    TxtExportAlgorithm.exportSpectrum(file, spectrum);

    List<String> lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
    String empty = lines.remove(lines.size() - 1); // hack to remove empty
                                                   // last line
    assertThat(empty, isEmptyString());
    assertThat(lines.toArray(new String[lines.size()]), arrayContaining(expectedSimple));
  }

  private static final String[] expectedTwoSpectra =
      {"100.0 10.0", "200.0 20.0", "", "300.0 30.0", "400.0 40.0", ""};

  @Test
  public void testExportSpectra() throws IOException {
    File file = folder.newFile();
    Collection<MsSpectrum> spectra = new ArrayList<>();

    spectra.add(mockSpectrum(mzValues1, intensityValues1));
    spectra.add(mockSpectrum(mzValues2, intensityValues2));

    TxtExportAlgorithm.exportSpectra(file, spectra);

    List<String> lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
    assertThat(lines.toArray(new String[lines.size()]), arrayContaining(expectedTwoSpectra));

    Collection<SimpleMsSpectrum> importedSpectra =
        TxtImportAlgorithm.parseMsSpectra(new FileReader(file));
    assertThat(importedSpectra.size(), equalTo(2));

    Iterator<SimpleMsSpectrum> iterator = importedSpectra.iterator();

    MsSpectrum spectrum = iterator.next();
    assertThat(spectrum.getMzValues()[0], equalTo(mzValues1[0]));
    assertThat(spectrum.getMzValues()[1], equalTo(mzValues1[1]));
    assertThat(spectrum.getIntensityValues()[0], equalTo(intensityValues1[0]));
    assertThat(spectrum.getIntensityValues()[1], equalTo(intensityValues1[1]));

    spectrum = iterator.next();
    assertThat(spectrum.getMzValues()[0], equalTo(mzValues2[0]));
    assertThat(spectrum.getMzValues()[1], equalTo(mzValues2[1]));
    assertThat(spectrum.getIntensityValues()[0], equalTo(intensityValues2[0]));
    assertThat(spectrum.getIntensityValues()[1], equalTo(intensityValues2[1]));
  }

  @Test
  public void testSpectrumToString() {
    MsSpectrum spectrum = mockSpectrum(mzValues1, intensityValues1);

    String result = TxtExportAlgorithm.spectrumToString(spectrum);
    String lines[] = result.split("\\r?\\n");
    assertThat(lines, arrayContaining(expectedSimple));
  }

  @Test
  public void testSpectrumToStringWithTabs() {
    MsSpectrum spectrum = mockSpectrum(mzValues1, intensityValues1);

    String result = TxtExportAlgorithm.spectrumToString(spectrum, "\t");
    String lines[] = result.split("\\r?\\n");
    assertThat(lines, arrayContaining(expectedSimpleWithTabs));
  }

  private MsSpectrum mockSpectrum(double[] mzValues, float[] intensityValues) {

    if (mzValues.length != intensityValues.length) {
      fail("Inconsistent sizes when mocking spectrum.");
    }

    MsSpectrum spectrum = mock(MsSpectrum.class);
    when(spectrum.getMzValues()).thenReturn(mzValues);
    when(spectrum.getIntensityValues()).thenReturn(intensityValues);
    when(spectrum.getNumberOfDataPoints()).thenReturn(mzValues.length);

    return spectrum;
  }
}
