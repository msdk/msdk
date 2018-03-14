package fr.profi.mzdb.io.reader.cache;

import java.util.HashMap;
import java.util.Map;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.model.RunSliceHeader;

// TODO: Auto-generated Javadoc
/**
 * The Class RunSliceHeaderReader.
 * 
 * @author David Bouyssie
 */
public class RunSliceHeaderReader extends AbstractRunSliceHeaderReader {
	
	private SQLiteConnection connection;
	private MzDbReader mzDbReader = null;

	/**
	 * Instantiates a new run slice header reader.
	 * 
	 * @param mzDbReader
	 *            the mz db reader
	 */
	public RunSliceHeaderReader(MzDbReader mzDbReader) {
		super(mzDbReader);
		this.mzDbReader = mzDbReader;
		this.connection = mzDbReader.getConnection();
	}
	
	/** specialized getter */
	public MzDbReader getMzDbReader() {
		return this.mzDbReader;
	}
	

	/**
	 * Gets the run slices.
	 * 
	 * @return array of runSlice instance without data associated
	 * @throws SQLiteException
	 *             the SQLite exception
	 */
	public RunSliceHeader[] getRunSliceHeaders() throws SQLiteException {
		return this.getRunSliceHeaders(connection);
	}

	/**
	 * Gets the run slices.
	 * 
	 * @param msLevel
	 *            the ms level
	 * @return array of runSlice instance without data associated
	 * @throws SQLiteException
	 *             the SQLite exception
	 */
	public RunSliceHeader[] getRunSliceHeaders(int msLevel) throws SQLiteException {
		return this.getRunSliceHeaders(msLevel, connection);
	}

	/**
	 * Gets the run slice by id.
	 * 
	 * @param msLevel
	 *            the ms level
	 * @return the run slice by id
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public HashMap<Integer, RunSliceHeader> getRunSliceHeaderById(int msLevel) throws SQLiteException {
		return this.getRunSliceHeaderById(msLevel, connection);
	}

	/**
	 * Gets the run slice header by id.
	 * 
	 * @return the run slice header by id
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public Map<Integer, RunSliceHeader> getRunSliceHeaderById() throws SQLiteException {
		return this.getRunSliceHeaderById(connection);
	}

	/**
	 * Gets the run slice header.
	 * 
	 * @param id
	 *            the id
	 * @return the run slice header
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public RunSliceHeader getRunSliceHeader(int id) throws SQLiteException {
		return this.getRunSliceHeader(id, connection);
	}

	/**
	 * Gets the run slice for mz.
	 * 
	 * @param mz
	 *            the mz
	 * @param msLevel
	 *            the ms level
	 * @return the run slice for mz
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public RunSliceHeader getRunSliceForMz(double mz, int msLevel) throws SQLiteException {
		return this.getRunSliceForMz(mz, msLevel, connection);
	}

	/**
	 * Gets the run slice ids for mz range.
	 * 
	 * @param minMz
	 *            the min mz
	 * @param maxMz
	 *            the max mz
	 * @param msLevel
	 *            the ms level
	 * @return the run slice ids for mz range
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public int[] getRunSliceIdsForMzRange(double minMz, double maxMz, int msLevel) throws SQLiteException {
		return this.getRunSliceIdsForMzRange(minMz, maxMz, msLevel, connection);
	}

}
