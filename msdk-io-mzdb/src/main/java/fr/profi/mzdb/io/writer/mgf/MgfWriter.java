package fr.profi.mzdb.io.writer.mgf;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.db.table.SpectrumTable;
import fr.profi.mzdb.io.reader.iterator.SpectrumIterator;
import fr.profi.mzdb.model.DataEncoding;
import fr.profi.mzdb.model.PeakEncoding;
import fr.profi.mzdb.model.Spectrum;
import fr.profi.mzdb.model.SpectrumData;
import fr.profi.mzdb.model.SpectrumHeader;
import fr.profi.mzdb.util.sqlite.ISQLiteRecordOperation;
import fr.profi.mzdb.util.sqlite.SQLiteQuery;
import fr.profi.mzdb.util.sqlite.SQLiteRecord;

/**
 * @author MDB
 */
public class MgfWriter {

	public static String LINE_SPERATOR = System.getProperty("line.separator");
	private static Integer precNotFound = 0;
	final Logger logger = LoggerFactory.getLogger(MgfWriter.class);

	private static String titleQuery = "SELECT id, title FROM spectrum WHERE ms_level=?";
	private final String mzDBFilePath;
	private final int msLevel;
	private MzDbReader mzDbReader;
	private Map<Integer, String> titleBySpectrumId = new HashMap<Integer, String>();

	/**
	 * 
	 * @param mzDBFilePath
	 * @param msLevel
	 * @throws SQLiteException
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException 
	 */
	public MgfWriter(String mzDBFilePath, int msLevel) throws SQLiteException, FileNotFoundException, ClassNotFoundException {
		if (msLevel < 2 || msLevel > 3) {
			throw new IllegalArgumentException("msLevel must be 2 or 3");
		}
		
		this.mzDBFilePath = mzDBFilePath;
		this.msLevel = msLevel;

		// Create reader
		this.mzDbReader = new MzDbReader(this.mzDBFilePath, true);

		this._fillTitleBySpectrumId();
		this.logger.info("Number of loaded spectra titles: " + this.titleBySpectrumId.size());
	}
	
	public MgfWriter(String mzDBFilePath) throws SQLiteException, FileNotFoundException, ClassNotFoundException {
		this(mzDBFilePath, 2);
	}

	public MzDbReader getMzDbReader() {
		return mzDbReader;
	}

	private void _fillTitleBySpectrumId() throws SQLiteException {

		/** inner class for treating sql resulting records */
		final class TitleByIdFiller implements ISQLiteRecordOperation {
			private Map<Integer, String> titleById;

			TitleByIdFiller(Map<Integer, String> t) {
				this.titleById = t;
			}

			@Override
			public void execute(SQLiteRecord elem, int idx) throws SQLiteException {
				int id = elem.columnInt(SpectrumTable.ID);
				String title = elem.columnString(SpectrumTable.TITLE);
				titleById.put(id, title);
			}
		} // end inner class

		TitleByIdFiller f = new TitleByIdFiller(this.titleBySpectrumId);

		new SQLiteQuery(this.mzDbReader.getConnection(), titleQuery).bind(1, this.msLevel).forEachRecord(f);
	}

	public void write(String mgfFile, PrecursorMzComputationEnum precComp, float mzTolPPM, float intensityCutoff, boolean exportProlineTitle)
		throws SQLiteException, IOException {
		write(mgfFile, new DefaultPrecursorComputer(precComp, mzTolPPM), intensityCutoff, exportProlineTitle);
	}

	public void write(String mgfFile, IPrecursorComputation precComp, float intensityCutoff, boolean exportProlineTitle) throws SQLiteException, IOException {
		
		// treat path mgfFile ?
		if (mgfFile.isEmpty())
			mgfFile = this.mzDBFilePath + ".mgf";
		
		// Reset precNotFound static var
		MgfWriter.precNotFound = 0;
		
		// Configure the mzDbReader in order to load all precursor lists and all scan list when reading spectra headers
		mzDbReader.enablePrecursorListLoading();
		mzDbReader.enableScanListLoading();

		// Iterate MSn spectra
		final Iterator<Spectrum> spectrumIterator = new SpectrumIterator(mzDbReader, mzDbReader.getConnection(), msLevel);
		final PrintWriter mgfWriter = new PrintWriter(new BufferedWriter(new FileWriter(mgfFile)));
		final Map<Long, DataEncoding> dataEncodingBySpectrumId = this.mzDbReader.getDataEncodingBySpectrumId();

		int spectraCount = 0;
		while (spectrumIterator.hasNext()) {
			
			Spectrum s = spectrumIterator.next();
			long spectrumId = s.getHeader().getId();
			
			DataEncoding dataEnc = dataEncodingBySpectrumId.get(spectrumId);
			String spectrumAsStr = this.stringifySpectrum(s, dataEnc, precComp, intensityCutoff, exportProlineTitle);
			
			//this.logger.debug("Writing spectrum with ID="+spectrumId);

			// Write the spectrum
			mgfWriter.println(spectrumAsStr);
			
			// Write a blank line between two spectra
			mgfWriter.println();
			
			spectraCount++;
		}

		this.logger.info(String.format("MGF file successfully created: %d spectra exported.", spectraCount));
		this.logger.info(String.format("#Precursor not found: %d", MgfWriter.precNotFound));
		mgfWriter.flush();
		mgfWriter.close();
	}

	/**
	 * 
	 * @param spectrum
	 * @param dataEnc
	 * @param precComp
	 * @param intensityCutoff
	 * @return
	 * @throws SQLiteException
	 * @throws StreamCorruptedException 
	 */
	protected String stringifySpectrum(
		Spectrum spectrum,
		DataEncoding dataEnc,
		IPrecursorComputation precComp,
		float intensityCutoff,
		boolean exportProlineTitle
	) throws SQLiteException, StreamCorruptedException {

		String mzFragFormat = null;
		// FIXME: check if is_high_res parameter is used and is correct
		if (dataEnc.getPeakEncoding() == PeakEncoding.LOW_RES_PEAK) {
			mzFragFormat = "%.1f";
		} else { // We assume high resolution m/z for fragments
			mzFragFormat = "%.3f";
		}

		// Unpack data
		final SpectrumHeader spectrumHeader = spectrum.getHeader();
		String title;
		if (exportProlineTitle == false)
			title = this.titleBySpectrumId.get(spectrumHeader.getSpectrumId());
		else {
			float timeInMinutes = spectrumHeader.getTime() / 60;
			title = String.format("first_cycle:%d;last_cycle:%d;first_scan:%d;last_scan:%d;first_time:%.3f;last_time:%.3f;raw_file_identifier:%s;",
				spectrumHeader.getCycle(),
				spectrumHeader.getCycle(),
				spectrumHeader.getInitialId(),
				spectrumHeader.getInitialId(),
				timeInMinutes,
				timeInMinutes,
				mzDbReader.getFirstSourceFileName().split("\\.")[0]);
		}

		final float time = spectrumHeader.getElutionTime();

		final double precMz = precComp.getPrecursorMz(mzDbReader, spectrumHeader);
		final int charge = precComp.getPrecursorCharge(mzDbReader, spectrumHeader);

		final MgfHeader mgfSpectrumHeader = charge != 0 ? new MgfHeader(title, precMz, charge, time) : new MgfHeader(title, precMz, time);

		StringBuilder spectrumStringBuilder = new StringBuilder();
		mgfSpectrumHeader.appendToStringBuilder(spectrumStringBuilder);

		// Spectrum Data
		final SpectrumData data = spectrum.getData();
		final double[] mzs = data.getMzList();
		final float[] ints = data.getIntensityList();
		//final float[] leftHwhms = data.getLeftHwhmList();
		//final float[] rightHwhms = data.getRightHwhmList();

		final int intsLength = ints.length;

		//final double[] intsAsDouble = new double[intsLength];
		//for (int i = 0; i < intsLength; ++i) {
		//	intsAsDouble[i] = (double) ints[i];
		//}
		//final double intensityCutOff = 0.0; // new Percentile().evaluate(intsAsDouble, 5.0);

		for (int i = 0; i < intsLength; ++i) {

			float intensity = ints[i];

			// DBO: here we tried boost intensities (but this should be optional)
			// The benefit is not the same for all instruments
			// TODO: take into account the width of the peaks
			//float intensity = (float) Math.pow(ints[i], 1.5 ); // ^ 3/2

			/*if( dataEnc.getMode().equals(DataMode.FITTED) ) {
			  float peakIntensity = ints[i];
			  float leftHwhm = leftHwhms[i];
			  float rightHwhm = rightHwhms[i];
			  float fwhm = leftHwhm + rightHwhm;
			  logger.debug("leftHwhm:"+leftHwhm);
			  if( fwhm != 0 ) {
			    logger.trace("fwhm:" +fwhm);
			  }
			  // Approximate the area using a triangle area computation
			  // TODO: use a more sophisticated mathematical function
			  intensity = peakIntensity * fwhm * 1e6f;
			} else {
			  intensity = ints[i];
			}*/

			if (intensity >= intensityCutoff) {
				double mz = mzs[i];

				spectrumStringBuilder
					.append(String.format(mzFragFormat, mz))
					.append(" ")
					.append(String.format("%.0f", intensity))
					.append(LINE_SPERATOR);
			}
		}

		spectrumStringBuilder.append(MgfField.END_IONS);

		return spectrumStringBuilder.toString();
	}

}
