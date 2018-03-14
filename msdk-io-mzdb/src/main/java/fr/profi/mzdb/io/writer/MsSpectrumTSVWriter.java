package fr.profi.mzdb.io.writer;

//import fr.profi.mzdb.model.Spectrum;
import fr.profi.mzdb.model.SpectrumHeader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.FileOutputStream;

import org.apache.commons.lang3.StringUtils;

//import com.codahale.jerkson.Json.generate;

public class MsSpectrumTSVWriter {

	public static void writeRun(SpectrumHeader[] spectra, Integer runId, File outFile) {

		// implicit def string2File(filename: String) = new File(filename)

		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileOutputStream(outFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		String[] colnames = { "id", "initial_id", "cycle", "time", "ms_level", "tic", "base_peak_moz",
				"base_peak_intensity", "precursor_moz", "precursor_charge", "serialized_properties", "run_id" };
		out.print(StringUtils.join(colnames, "\t") + "\n");

		for (SpectrumHeader spectrum : spectra) {
			out.print(spectrum2String(spectrum, runId));
		}
		// out.flush()
		out.close();

	}

	private static String spectrum2String(SpectrumHeader spectrum, Integer runId) {
		// SpectrumHeader header = spectrum.getHeader();

		String[] spectrumValues = { String.valueOf(spectrum.getId()), String.valueOf(spectrum.getInitialId()),
				String.valueOf(spectrum.getCycle()), String.valueOf(spectrum.getTime()),
				String.valueOf(spectrum.getMsLevel()), String.valueOf(spectrum.getTIC()),
				String.valueOf(spectrum.getBasePeakMz()), String.valueOf(spectrum.getBasePeakIntensity()),
				String.valueOf(spectrum.getPrecursorMz()), String.valueOf(spectrum.getPrecursorCharge()), "",// generate(LcmsSpectrumProperties)
				String.valueOf(runId) };
		return StringUtils.join(spectrumValues, "\t") + "\n";
	}
}