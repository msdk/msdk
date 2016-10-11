package io.github.msdk.io.mgf;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.msspectra.MsSpectrum;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MgfExportAlgorithmTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private static final String[] expectedSimpleResults = { "BEGIN IONS",
			"100.0 10.0", "200.0 20.0", "END IONS", "" };
	private static final double[] mzValues1 = { 100.0, 200.0 };
	private static final float[] intensityValues1 = { 10.0f, 20.0f };

	@Test
	public void testSimple() throws IOException, MSDKException {
		MsSpectrum spectrum = mockMsSpectrum(mzValues1, intensityValues1);
		File file = folder.newFile();

		MgfExportAlgorithm.exportSpectrum(file, spectrum);

		List<String> lines = Files.readAllLines(file.toPath(),
				Charset.defaultCharset());
		assertThat("Lines are not correct",
				lines.toArray(new String[lines.size()]),
				arrayContaining(expectedSimpleResults));
	}

	private MsSpectrum mockMsSpectrum(double[] mzValues, float[] intensityValues) {
		if (mzValues.length != intensityValues.length) {
			throw new IllegalArgumentException(
					"Number of mzValues and intensityValues do not agree");
		}

		MsSpectrum spectrum = mock(MsSpectrum.class);
		when(spectrum.getNumberOfDataPoints()).thenReturn(mzValues.length);
		when(spectrum.getMzValues(null)).thenReturn(mzValues);
		when(spectrum.getIntensityValues(null)).thenReturn(intensityValues);

		return spectrum;
	}

}
