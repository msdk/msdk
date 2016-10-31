package io.github.msdk.io.txt;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.arrayContaining;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.github.msdk.datamodel.msspectra.MsSpectrum;

public class TxtExportAlgorithmTest {

    private static final double[] mzValues1 = { 100.0, 200.0 };
    private static final float[] intensityValues1 = { 10.0f, 20.0f };

    private static final double[] mzValues2 = { 300.0, 400.0 };
    private static final float[] intensityValues2 = { 30.0f, 40.0f };

    private static final String[] expectedSimple = { "100.0 10.0",
            "200.0 20.0" };
    private static final String[] expectedSimpleWithTabs = { "100.0\t10.0",
            "200.0\t20.0" };

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testExportSpectrum() throws IOException {
        File file = folder.newFile();
        MsSpectrum spectrum = mockSpectrum(mzValues1, intensityValues1);

        TxtExportAlgorithm.exportSpectrum(file, spectrum);

        List<String> lines = Files.readAllLines(file.toPath(),
                Charset.defaultCharset());
        assertThat(lines.toArray(new String[lines.size()]),
                arrayContaining(expectedSimple));
    }

    private static final String[] expectedTwoSpectra = { "100.0 10.0",
            "200.0 20.0", "300.0 30.0", "400.0 40.0" };

    @Test
    public void testExportSpectra() throws IOException {
        File file = folder.newFile();
        Collection<MsSpectrum> spectra = new ArrayList<>();

        spectra.add(mockSpectrum(mzValues1, intensityValues1));
        spectra.add(mockSpectrum(mzValues2, intensityValues2));

        TxtExportAlgorithm.exportSpectra(file, spectra);

        List<String> lines = Files.readAllLines(file.toPath(),
                Charset.defaultCharset());
        assertThat(lines.toArray(new String[lines.size()]),
                arrayContaining(expectedTwoSpectra));
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

    private MsSpectrum mockSpectrum(double[] mzValues,
            float[] intensityValues) {

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
