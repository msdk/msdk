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

package io.github.msdk.io.txt;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.datamodel.SimpleMsSpectrum;

public class TxtImportAlgorithmTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  private static String spectrumText = "10.0 100.0\n20.0 200.0\n30.0 300.0\n40.0 400.0";
  private static String spectraText = spectrumText + "\n\n50.0 500.0\n60.0 600.0";

  @Test
  public void test4Peaks() throws MSDKException {

    MsSpectrum spectrum = TxtImportAlgorithm.parseMsSpectrum(spectrumText);

    assertThat(spectrum.getNumberOfDataPoints(), equalTo(4));

    assertThat(spectrum.getMzValues()[0], equalTo(10.0));
    assertThat(spectrum.getMzValues()[3], equalTo(40.0));
    assertThat(spectrum.getIntensityValues()[0], equalTo(100.0f));
    assertThat(spectrum.getIntensityValues()[2], equalTo(300.0f));

  }

  @Test
  public void test4PeaksFromFile() throws IOException {
    File file = folder.newFile();
    FileWriter writer = new FileWriter(file);
    writer.write(spectrumText);
    writer.close();

    MsSpectrum spectrum = TxtImportAlgorithm.parseMsSpectrum(new FileReader(file));

    assertThat(spectrum.getNumberOfDataPoints(), equalTo(4));

    assertThat(spectrum.getMzValues()[0], equalTo(10.0));
    assertThat(spectrum.getMzValues()[3], equalTo(40.0));
    assertThat(spectrum.getIntensityValues()[0], equalTo(100.0f));
    assertThat(spectrum.getIntensityValues()[2], equalTo(300.0f));
  }

  @Test
  public void testTwoSpetra() {

    Collection<SimpleMsSpectrum> spectra =
        TxtImportAlgorithm.parseMsSpectra(new StringReader(spectraText));
    assertThat(spectra.size(), equalTo(2));

    Iterator<SimpleMsSpectrum> iterator = spectra.iterator();

    MsSpectrum spectrum = iterator.next();
    assertThat(spectrum.getMzValues()[0], equalTo(10.0));
    assertThat(spectrum.getMzValues()[3], equalTo(40.0));
    assertThat(spectrum.getIntensityValues()[1], equalTo(200.0f));
    assertThat(spectrum.getIntensityValues()[2], equalTo(300.0f));

    spectrum = iterator.next();
    assertThat(spectrum.getMzValues()[0], equalTo(50.0));
    assertThat(spectrum.getMzValues()[1], equalTo(60.0));
    assertThat(spectrum.getIntensityValues()[0], equalTo(500.0f));
    assertThat(spectrum.getIntensityValues()[1], equalTo(600.0f));
  }

  @Test
  public void testSpectrumWithHeader() {
    StringBuilder builder = new StringBuilder();
    builder.append("# header line 1\n");
    builder.append("header line 2\n");
    builder.append(spectrumText);

    Collection<SimpleMsSpectrum> spectra =
        TxtImportAlgorithm.parseMsSpectra(new StringReader(builder.toString()));
    assertThat(spectra.size(), equalTo(1));

    Iterator<SimpleMsSpectrum> iterator = spectra.iterator();

    MsSpectrum spectrum = iterator.next();
    assertThat(spectrum.getMzValues()[0], equalTo(10.0));
    assertThat(spectrum.getMzValues()[3], equalTo(40.0));
    assertThat(spectrum.getIntensityValues()[1], equalTo(200.0f));
    assertThat(spectrum.getIntensityValues()[2], equalTo(300.0f));
  }

  @Test
  public void testSpectrumWithFooter() {
    StringBuilder builder = new StringBuilder();
    builder.append(spectrumText);
    builder.append("# footer line 1\n");
    builder.append("footer line 2\n");

    Collection<SimpleMsSpectrum> spectra =
        TxtImportAlgorithm.parseMsSpectra(new StringReader(builder.toString()));
    assertThat(spectra.size(), equalTo(1));

    Iterator<SimpleMsSpectrum> iterator = spectra.iterator();

    MsSpectrum spectrum = iterator.next();
    assertThat(spectrum.getMzValues()[0], equalTo(10.0));
    assertThat(spectrum.getMzValues()[3], equalTo(40.0));
    assertThat(spectrum.getIntensityValues()[1], equalTo(200.0f));
    assertThat(spectrum.getIntensityValues()[2], equalTo(300.0f));
  }

  @Test
  public void testNoSpectrumAsString() {
    MsSpectrum spectrum = TxtImportAlgorithm.parseMsSpectrum("not a spectrum");
    assertThat(spectrum, nullValue());
  }

  @Test
  public void testNoSpectrumAsReader() {
    MsSpectrum spectrum = TxtImportAlgorithm.parseMsSpectrum(new StringReader("not a spectrum"));
    assertThat(spectrum, nullValue());
  }
}
