package fr.profi.mzdb.io.reader.cache;

import java.util.Map;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.model.SpectrumHeader;

/**
 * @author David Bouyssie
 * 
 */
public class SpectrumHeaderReader extends AbstractSpectrumHeaderReader {
	
	private SQLiteConnection connection;
	private MzDbReader mzDbReader = null;
	
	/**
	 * @param mzDbReader
	 * @throws SQLiteException 
	 */
	public SpectrumHeaderReader(MzDbReader mzDbReader, AbstractDataEncodingReader dataEncodingReader) throws SQLiteException {
		super(mzDbReader, dataEncodingReader);
		this.mzDbReader = mzDbReader;
		this.connection = mzDbReader.getConnection();
	}

	/** specialized getter */
	public MzDbReader getMzDbReader() {
		return this.mzDbReader;
	}
	

	/**
	 * Gets the spectrum headers.
	 * 
	 * @return the spectrum headers
	 * @throws SQLiteException
	 */
	public SpectrumHeader[] getSpectrumHeaders() throws SQLiteException {
		return this.getSpectrumHeaders(connection);
	}
	
	/**
	 * Gets the spectrum headers by id.
	 * 
	 * @return the spectrum header by id
	 * @throws SQLiteException
	 */
	public Map<Long, SpectrumHeader> getSpectrumHeaderById() throws SQLiteException {
		return this.getSpectrumHeaderById(connection);
	}

	/**
	 * Gets the MS1 spectrum headers.
	 * 
	 * @return the spectrum headers
	 * @throws SQLiteException
	 */
	public SpectrumHeader[] getMs1SpectrumHeaders() throws SQLiteException {
		return getMs1SpectrumHeaders(connection);
	}

	/**
	 * Gets the MS1 spectrum header by id.
	 * 
	 * @return the spectrum header by id
	 * @throws SQLiteException
	 */
	public Map<Long, SpectrumHeader> getMs1SpectrumHeaderById() throws SQLiteException {
		return getMs1SpectrumHeaderById(connection);
	}

	/**
	 * Gets the MS2 spectrum headers.
	 * 
	 * @return the spectrum headers
	 * @throws SQLiteException
	 */
	public SpectrumHeader[] getMs2SpectrumHeaders() throws SQLiteException {
		return getMs2SpectrumHeaders(connection);
	}

	/**
	 * Gets the MS2 spectrum header by id.
	 * 
	 * @return the spectrum header by id
	 * @throws SQLiteException
	 */
	public Map<Long, SpectrumHeader> getMs2SpectrumHeaderById() throws SQLiteException {
		return getMs2SpectrumHeaderById(connection);
	}

	/**
	/**
	 * Gets the spectrum header.
	 * 
	 * @param id
	 *            the id
	 * @return spectrum header
	 * @throws SQLiteException
	 */
	public SpectrumHeader getSpectrumHeader(long id) throws SQLiteException {
		return getSpectrumHeader(id, connection);
	}

	/**
	 * Gets the spectrum time by id.
	 * 
	 * @return the spectrum time mapped by the spectrum id
	 * @throws SQLiteException the SQLite exception
	 */
	public Map<Long, Float> getSpectrumTimeById() throws SQLiteException {
		return getSpectrumTimeById(connection);
	}

	/**
	 * Gets the spectrum header for time.
	 * 
	 * @param time
	 *            the time
	 * @param msLevel
	 *            the ms level
	 * @return SpectrumHeader the closest to the time input parameter
	 * @throws Exception
	 */
	public SpectrumHeader getSpectrumHeaderForTime(float time, int msLevel) throws Exception {
		return getSpectrumHeaderForTime(time, msLevel, connection);
	}

	/**
	 * Gets the spectrum ids for time range.
	 * 
	 * @param minRT
	 *            the min rt
	 * @param maxRT
	 *            the max rt
	 * @param msLevel
	 *            the ms level
	 * @return array of integers corresponding to the ids of matching spectrum
	 * @throws SQLiteException
	 */
	public long[] getSpectrumIdsForTimeRange(float minRT, float maxRT, int msLevel) throws SQLiteException {
		return getSpectrumIdsForTimeRange(minRT, maxRT, msLevel, connection);
	}

}
