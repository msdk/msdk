package io.github.msdk.io.mgf;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.msspectra.MsSpectrum;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsScan;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentMatcher;

public class MgfFileExportMethodTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private static final double[] mzValues1 = { 100.0, 200.0 };
	private static final double[] mzValues2 = { 300.0, 400.0 };

	private static final float[] intensityValues1 = { 10.0f, 20.0f };
	private static final float[] intensityValues2 = { 30.0f, 40.0f };

	private static final String[] expectedSimpleResults = { "BEGIN IONS",
		"100.0 10.0", "200.0 20.0", "END IONS", "" };
	@Test
	public void testSimple() throws IOException, MSDKException {
		MsSpectrum spectrum = mockMsSpectrum(mzValues1, intensityValues1);
		File file = folder.newFile();

		MgfFileExportMethod method = new MgfFileExportMethod(spectrum, file);
		method.execute();

		assertThat((double) method.getFinishedPercentage(), closeTo(1.0, 0.001));
		List<String> lines = Files.readAllLines(file.toPath(),
				Charset.defaultCharset());
		assertThat(lines.toArray(new String[lines.size()]),
				arrayContaining(expectedSimpleResults));
	}

	private static final String[] expectedTwoResults = { "BEGIN IONS",
			"100.0 10.0", "200.0 20.0", "END IONS", "", "BEGIN IONS",
			"300.0 30.0", "400.0 40.0", "END IONS", "" };

	@Test
	public void testTwoSimpleResults() throws IOException, MSDKException {
		ArrayList<MsSpectrum> spectra = new ArrayList<>();
		spectra.add(mockMsSpectrum(mzValues1, intensityValues1));
		spectra.add(mockMsSpectrum(mzValues2, intensityValues2));
		File file = folder.newFile();

		MgfFileExportMethod method = new MgfFileExportMethod(spectra, file);
		method.execute();

		assertThat((double) method.getFinishedPercentage(), closeTo(1.0, 0.001));
		List<String> lines = Files.readAllLines(file.toPath(),
				Charset.defaultCharset());
		assertThat(lines.toArray(new String[lines.size()]),
				arrayContaining(expectedTwoResults));
	}

	private static final String[] expectedNoChromatography = { "BEGIN IONS",
			"PEPMASS=500.0", "CHARGE=1", "Title=Scan #1", "100.0 10.0",
			"200.0 20.0", "END IONS", "" };

	@Test
	public void testMsScanWithNoChromatograpy() throws IOException, MSDKException {
		IsolationInfo ii = mockIsolationInfo(500.0, 1);
		MsScan scan = mockMsScan(mzValues1, intensityValues1, ii, null, 1);
		File file = folder.newFile();

		MgfFileExportMethod method = new MgfFileExportMethod(scan, file);
		method.execute();

		assertThat((double) method.getFinishedPercentage(), closeTo(1.0, 0.001));
		List<String> lines = Files.readAllLines(file.toPath(),
				Charset.defaultCharset());
		assertThat(lines.toArray(new String[lines.size()]),
				arrayContaining(expectedNoChromatography));
	}

	private static final String[] expectedChromatography = { "BEGIN IONS",
			"PEPMASS=500.0", "CHARGE=1", "RTINSECONDS=1.0", "Title=Scan #1",
			"100.0 10.0", "200.0 20.0", "END IONS", "" };

	@Test
	public void testMsScanWithChromatograpy() throws IOException,
			MSDKException {
		IsolationInfo ii = mockIsolationInfo(500.0, 1);
		ChromatographyInfo ci = mockChromatographyInfo(1.0f);
		MsScan scan = mockMsScan(mzValues1, intensityValues1, ii, ci, 1);
		File file = folder.newFile();

		MgfFileExportMethod method = new MgfFileExportMethod(scan, file);
		method.execute();

		assertThat((double) method.getFinishedPercentage(), closeTo(1.0, 0.001));
		List<String> lines = Files.readAllLines(file.toPath(),
				Charset.defaultCharset());
		assertThat(lines.toArray(new String[lines.size()]),
				arrayContaining(expectedChromatography));
	}

	private double[] anyDoubleArray() {
		return argThat(new ArgumentMatcher<double[]>() {
			@Override
			public boolean matches(double[] argument) {
				return true;
			}

		});
	}

	private float[] anyFloatArray() {
		return argThat(new ArgumentMatcher<float[]>() {
			@Override
			public boolean matches(float[] argument) {
				return true;
			}

		});
	}

	private MsSpectrum mockMsSpectrum(double[] mzValues, float[] intensityValues) {
		if (mzValues.length != intensityValues.length) {
			throw new IllegalArgumentException(
					"Number of mzValues and intensityValues do not agree");
		}

		MsSpectrum spectrum = mock(MsSpectrum.class);
		when(spectrum.getNumberOfDataPoints()).thenReturn(mzValues.length);
		when(spectrum.getMzValues(anyDoubleArray())).thenReturn(mzValues);
		when(spectrum.getIntensityValues(anyFloatArray())).thenReturn(intensityValues);

		return spectrum;
	}

	private MsScan mockMsScan(double[] mzValues, float[] intensityValues,
			IsolationInfo ii, ChromatographyInfo ci, Integer scanNumber) {
		if (mzValues.length != intensityValues.length) {
			throw new IllegalArgumentException(
					"Number of mzValues and intensityValues do not agree");
		}

		MsScan scan = mock(MsScan.class);
		when(scan.getNumberOfDataPoints()).thenReturn(mzValues.length);
		when(scan.getMzValues(null)).thenReturn(mzValues);
		when(scan.getIntensityValues(null)).thenReturn(intensityValues);
		when(scan.getIsolations()).thenReturn(Collections.singletonList(ii));
		when(scan.getChromatographyInfo()).thenReturn(ci);
		when(scan.getScanNumber()).thenReturn(scanNumber);

		return scan;
	}

	private IsolationInfo mockIsolationInfo(Double precursorMz,
			Integer precursorCharge) {
		IsolationInfo ii = mock(IsolationInfo.class);
		when(ii.getPrecursorMz()).thenReturn(precursorMz);
		when(ii.getPrecursorCharge()).thenReturn(precursorCharge);

		return ii;
	}

	private ChromatographyInfo mockChromatographyInfo(Float retentionTime) {
		ChromatographyInfo ci = mock(ChromatographyInfo.class);
		when(ci.getRetentionTime()).thenReturn(retentionTime);

		return ci;
	}
}
