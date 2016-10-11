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

	@Rule public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testSimple() throws IOException, MSDKException {
		MsSpectrum spectrum = mock(MsSpectrum.class);
		when(spectrum.getNumberOfDataPoints()).thenReturn(2);
		when(spectrum.getMzValues(null)).thenReturn(new double[] { 100.0, 200.0 });
		when(spectrum.getIntensityValues(null)).thenReturn(new float[]{ 10.0f, 20.0f});
		
		File file = folder.newFile();
		MgfExportAlgorithm.exportSpectrum(file, spectrum);

		List<String> lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
		assertThat("Number of lines is not correct.", lines.size(), equalTo(5));
		assertThat(lines.get(0), equalTo("BEGIN IONS"));
		assertThat(lines.get(1), equalTo("100.0 10.0"));
		assertThat(lines.get(2), equalTo("200.0 20.0"));
		assertThat(lines.get(3), equalTo("END IONS"));
		assertThat(lines.get(4), equalTo(""));
	}

}
