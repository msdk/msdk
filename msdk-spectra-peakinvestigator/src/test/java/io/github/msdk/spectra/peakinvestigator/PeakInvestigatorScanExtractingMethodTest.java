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

import static org.junit.Assert.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.spectra.peakinvestigator.PeakInvestigatorMsSpectrum;
import io.github.msdk.spectra.peakinvestigator.PeakInvestigatorScanExtractingMethod;

public class PeakInvestigatorScanExtractingMethodTest {

  private final static String BASE_TEST_PATH = "src/test/resources/";

  @Test
  public void testSingleScan() throws MSDKException {
    PeakInvestigatorScanExtractingMethod method =
        new PeakInvestigatorScanExtractingMethod(BASE_TEST_PATH + "one_scan.tar");
    List<MsSpectrum> spectra = method.execute();
    assertThat(method.getFinishedPercentage(), equalTo(1.0f));

    assertThat(spectra.size(), equalTo(1));

    MsSpectrum spectrum = spectra.get(0);
    assertThat(spectrum, not(instanceOf(PeakInvestigatorMsSpectrum.class)));
    assertThat(spectrum.getNumberOfDataPoints(), equalTo(2));

    double[] mzValues = spectrum.getMzValues();
    assertThat(mzValues[0], equalTo(1.0));
    assertThat(mzValues[1], equalTo(2.0));

    float[] intensityValues = spectrum.getIntensityValues();
    assertThat(intensityValues[0], equalTo(10.0f));
    assertThat(intensityValues[1], equalTo(20.0f));
  }

  @Test
  public void testTwoScans() throws MSDKException {
    PeakInvestigatorScanExtractingMethod method =
        new PeakInvestigatorScanExtractingMethod(BASE_TEST_PATH + "two_scans.tar");
    List<MsSpectrum> spectra = method.execute();
    assertThat(method.getFinishedPercentage(), equalTo(1.0f));

    assertThat(spectra.size(), equalTo(2));

    // first spectrum
    MsSpectrum spectrum = spectra.get(0);
    assertThat(spectrum, not(instanceOf(PeakInvestigatorMsSpectrum.class)));
    assertThat(spectrum.getNumberOfDataPoints(), equalTo(2));

    double[] mzValues = spectrum.getMzValues();
    assertThat(mzValues[0], equalTo(1.0));
    assertThat(mzValues[1], equalTo(2.0));

    float[] intensityValues = spectrum.getIntensityValues();
    assertThat(intensityValues[0], equalTo(10.0f));
    assertThat(intensityValues[1], equalTo(20.0f));

    // second spectrum
    spectrum = spectra.get(1);
    assertThat(spectrum, not(instanceOf(PeakInvestigatorMsSpectrum.class)));
    assertThat(spectrum.getNumberOfDataPoints(), equalTo(2));

    mzValues = spectrum.getMzValues();
    assertThat(mzValues[0], equalTo(3.0));
    assertThat(mzValues[1], equalTo(4.0));

    intensityValues = spectrum.getIntensityValues();
    assertThat(intensityValues[0], equalTo(30.0f));
    assertThat(intensityValues[1], equalTo(40.0f));
  }

  @Test
  public void testSingleScanWithErrors() throws MSDKException {
    PeakInvestigatorScanExtractingMethod method =
        new PeakInvestigatorScanExtractingMethod(BASE_TEST_PATH + "one_scan.error.tar");
    List<MsSpectrum> spectra = method.execute();
    assertThat(method.getFinishedPercentage(), equalTo(1.0f));

    assertThat(spectra.size(), equalTo(1));
    assertThat(spectra.get(0), instanceOf(PeakInvestigatorMsSpectrum.class));

    PeakInvestigatorMsSpectrum spectrum = (PeakInvestigatorMsSpectrum) spectra.get(0);
    assertThat(spectrum.getNumberOfDataPoints(), equalTo(2));

    double[] mzValues = spectrum.getMzValues();
    assertThat(mzValues[0], equalTo(1.0));
    assertThat(mzValues[1], equalTo(2.0));

    float[] intensityValues = spectrum.getIntensityValues();
    assertThat(intensityValues[0], equalTo(10.0f));
    assertThat(intensityValues[1], equalTo(20.0f));

    // test default of 1 sigma
    assertThat(spectrum.getToleranceRange(1.0), equalTo(Range.closed(0.999, 1.001)));
    assertThat(spectrum.getToleranceRange(2.0), equalTo(Range.closed(1.998, 2.002)));

    // test sigma where minError > mzError
    spectrum.setMultiplier(0.5);
    assertThat(spectrum.getToleranceRange(1.0), equalTo(Range.closed(0.999, 1.001)));
    assertThat(spectrum.getToleranceRange(2.0), equalTo(Range.closed(1.998, 2.002)));

    // test sigma where muliplier * mzError > minError
    spectrum.setMultiplier(2.0);
    assertThat(spectrum.getToleranceRange(1.0), equalTo(Range.closed(0.998, 1.002)));
    assertThat(spectrum.getToleranceRange(2.0), equalTo(Range.closed(1.996, 2.004)));
  }

  @Test
  public void testTwoScansWithErrors() throws MSDKException {
    PeakInvestigatorScanExtractingMethod method =
        new PeakInvestigatorScanExtractingMethod(BASE_TEST_PATH + "two_scans.error.tar");
    List<MsSpectrum> spectra = method.execute();
    assertThat(method.getFinishedPercentage(), equalTo(1.0f));

    assertThat(spectra.size(), equalTo(2));
    assertThat(spectra.get(0), instanceOf(PeakInvestigatorMsSpectrum.class));
    assertThat(spectra.get(1), instanceOf(PeakInvestigatorMsSpectrum.class));

    // first spectrum
    PeakInvestigatorMsSpectrum spectrum = (PeakInvestigatorMsSpectrum) spectra.get(0);
    assertThat(spectrum.getNumberOfDataPoints(), equalTo(2));

    double[] mzValues = spectrum.getMzValues();
    assertThat(mzValues[0], equalTo(1.0));
    assertThat(mzValues[1], equalTo(2.0));

    float[] intensityValues = spectrum.getIntensityValues();
    assertThat(intensityValues[0], equalTo(10.0f));
    assertThat(intensityValues[1], equalTo(20.0f));

    // test default of 1 sigma
    assertThat(spectrum.getToleranceRange(1.0), equalTo(Range.closed(0.999, 1.001)));
    assertThat(spectrum.getToleranceRange(2.0), equalTo(Range.closed(1.998, 2.002)));

    // second spectrum
    spectrum = (PeakInvestigatorMsSpectrum) spectra.get(1);
    assertThat(spectrum.getNumberOfDataPoints(), equalTo(2));

    mzValues = spectrum.getMzValues();
    assertThat(mzValues[0], equalTo(3.0));
    assertThat(mzValues[1], equalTo(4.0));

    intensityValues = spectrum.getIntensityValues();
    assertThat(intensityValues[0], equalTo(30.0f));
    assertThat(intensityValues[1], equalTo(40.0f));

    // test default of 1 sigma
    assertThat(spectrum.getToleranceRange(3.0), equalTo(Range.closed(2.997, 3.003)));
    assertThat(spectrum.getToleranceRange(4.0), equalTo(Range.closed(3.996, 4.004)));

    // test sigma where minError > mzError
    spectrum.setMultiplier(0.5);
    assertThat(spectrum.getToleranceRange(3.0), equalTo(Range.closed(2.997, 3.003)));
    assertThat(spectrum.getToleranceRange(4.0), equalTo(Range.closed(3.996, 4.004)));

    // test sigma where muliplier * mzError > minError
    spectrum.setMultiplier(2.0);
    assertThat(spectrum.getToleranceRange(3.0), equalTo(Range.closed(2.994, 3.006)));
    assertThat(spectrum.getToleranceRange(4.0), equalTo(Range.closed(3.992, 4.008)));
  }
}
