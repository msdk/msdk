package fr.profi.mzdb.io.reader.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.AbstractMzDbReader;
import fr.profi.mzdb.db.table.RunSliceTable;
import fr.profi.mzdb.model.RunSliceHeader;
import fr.profi.mzdb.util.sqlite.ISQLiteRecordExtraction;
import fr.profi.mzdb.util.sqlite.SQLiteQuery;
import fr.profi.mzdb.util.sqlite.SQLiteRecord;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractRunSliceHeaderReader.
 *
 * @author bouyssie
 */
public abstract class AbstractRunSliceHeaderReader extends MzDbEntityCacheContainer {

	/**
	 * Instantiates a new abstract run slice header reader.
	 *
	 * @param mzDbReader
	 *            the mz db reader
	 */
	public AbstractRunSliceHeaderReader(AbstractMzDbReader mzDbReader) {
		super(mzDbReader);
	}

	/**
	 * The Class RunSliceHeaderExtractor.
	 *
	 * @author bouyssie
	 */
	private class RunSliceHeaderExtractor implements ISQLiteRecordExtraction<RunSliceHeader> {

		/*
		 * public RunSliceHeader extract(SQLiteRecord record, int runSliceNumber ) throws SQLiteException {
		 * 
		 * return new RunSliceHeader( record.columnInt(RunSliceTable.ID),
		 * record.columnInt(RunSliceTable.MS_LEVEL), runSliceNumber,
		 * record.columnDouble(RunSliceTable.BEGIN_MZ), record.columnDouble(RunSliceTable.END_MZ),
		 * record.columnInt(RunSliceTable.RUN_ID) );
		 * 
		 * }
		 */
		
		/* (non-Javadoc)
		 * @see fr.profi.mzdb.utils.sqlite.ISQLiteRecordExtraction#extract(fr.profi.mzdb.utils.sqlite.SQLiteRecord)
		 */
		public RunSliceHeader extract(SQLiteRecord record) throws SQLiteException {
			// return this.extract( record, record.columnInt(RunSliceTable.NUMBER) );

			return new RunSliceHeader(
					record.columnInt(RunSliceTable.ID),
					record.columnInt(RunSliceTable.MS_LEVEL),
					record.columnInt(RunSliceTable.NUMBER),
					record.columnDouble(RunSliceTable.BEGIN_MZ),
					record.columnDouble(RunSliceTable.END_MZ),
					record.columnInt(RunSliceTable.RUN_ID));
		}

	}

	/** The _run slice header extractor. */
	private RunSliceHeaderExtractor _runSliceHeaderExtractor = new RunSliceHeaderExtractor();

	/**
	 * Gets the run slices.
	 *
	 * @param connection
	 *            the connection
	 * @return array of runSlice instance without data associated
	 * @throws SQLiteException
	 *             the SQLite exception
	 */
	protected RunSliceHeader[] getRunSliceHeaders(SQLiteConnection connection) throws SQLiteException {

		if (this.getEntityCache() != null && this.getEntityCache().runSliceHeaders != null) {
			return this.getEntityCache().runSliceHeaders;
		} else {

			// Retrieve the corresponding run slices
			String queryStr = "SELECT * FROM run_slice";
			List<RunSliceHeader> rshList = new SQLiteQuery(connection, queryStr).extractRecordList(_runSliceHeaderExtractor);
			
			RunSliceHeader[] runSliceHeaders = rshList.toArray(new RunSliceHeader[rshList.size()]);

			if (this.getEntityCache() != null)
				this.getEntityCache().runSliceHeaders = runSliceHeaders;

			return runSliceHeaders;
		}

	}

	/**
	 * Gets the run slices.
	 *
	 * @param msLevel
	 *            the ms level
	 * @param connection
	 *            the connection
	 * @return array of runSlice instance without data associated
	 * @throws SQLiteException
	 *             the SQLite exception
	 */
	protected RunSliceHeader[] getRunSliceHeaders(int msLevel, SQLiteConnection connection) throws SQLiteException {

		ArrayList<RunSliceHeader> rshList = new ArrayList<RunSliceHeader>();

		if (this.getEntityCache() != null && this.getEntityCache().runSliceHeaders != null) {
			RunSliceHeader[] runSliceHeaders = this.getEntityCache().runSliceHeaders;
			for (RunSliceHeader rsh : runSliceHeaders) {
				if (rsh.getMsLevel() == msLevel)
					rshList.add(rsh);
			}
		} else {

			// Retrieve the corresponding run slices
			String queryStr = "SELECT * FROM run_slice WHERE ms_level=? ORDER BY begin_mz "; // number
			
			SQLiteQuery query = new SQLiteQuery(connection, queryStr).bind(1, msLevel);
			rshList = (ArrayList<RunSliceHeader>) query.extractRecordList(_runSliceHeaderExtractor);
		}

		return rshList.toArray(new RunSliceHeader[rshList.size()]);

	}

	/**
	 * _get run slice header by id.
	 * 
	 * @param runSliceHeaders
	 *            the run slice headers
	 * @return the hash map
	 */
	private HashMap<Integer, RunSliceHeader> _getRunSliceHeaderById(RunSliceHeader[] runSliceHeaders) {

		HashMap<Integer, RunSliceHeader> runSliceHeaderById = new HashMap<Integer, RunSliceHeader>(
				runSliceHeaders.length);
		for (RunSliceHeader runSlice : runSliceHeaders) {
			runSliceHeaderById.put(runSlice.getId(), runSlice);
		}

		return runSliceHeaderById;
	}

	/**
	 * Gets the run slice by id.
	 *
	 * @param msLevel
	 *            the ms level
	 * @param connection
	 *            the connection
	 * @return the run slice by id
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public HashMap<Integer, RunSliceHeader> getRunSliceHeaderById(int msLevel, SQLiteConnection connection) throws SQLiteException {
		RunSliceHeader[] runSliceHeaders = this.getRunSliceHeaders(msLevel, connection);
		return this._getRunSliceHeaderById(runSliceHeaders);
	}

	/**
	 * Gets the run slice header by id.
	 *
	 * @param connection
	 *            the connection
	 * @return the run slice header by id
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	protected Map<Integer, RunSliceHeader> getRunSliceHeaderById(SQLiteConnection connection) throws SQLiteException {

		if (this.getEntityCache() != null && this.getEntityCache().runSliceHeaderById != null) {
			return this.getEntityCache().runSliceHeaderById;
		} else {

			HashMap<Integer, RunSliceHeader> runSliceHeaderById = this._getRunSliceHeaderById(this.getRunSliceHeaders(connection));

			if (this.getEntityCache() != null)
				this.getEntityCache().runSliceHeaderById = runSliceHeaderById;

			return runSliceHeaderById;
		}
	}

	/**
	 * Gets the run slice header.
	 *
	 * @param id
	 *            the id
	 * @param connection
	 *            the connection
	 * @return the run slice header
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	protected RunSliceHeader getRunSliceHeader(int id, SQLiteConnection connection) throws SQLiteException {
		if (this.getEntityCache() != null) {
			return this.getRunSliceHeaderById(connection).get(id);
		} else {
			String queryStr = "SELECT * FROM run_slice WHERE id = ?";
			return new SQLiteQuery(connection, queryStr).bind(1, id).extractRecord(this._runSliceHeaderExtractor);
		}
	}

	/**
	 * Gets the run slice for mz.
	 *
	 * @param mz
	 *            the mz
	 * @param msLevel
	 *            the ms level
	 * @param connection
	 *            the connection
	 * @return the run slice for mz
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	protected RunSliceHeader getRunSliceForMz(double mz, int msLevel, SQLiteConnection connection) throws SQLiteException {

		// Retrieve the corresponding run slices
		String queryStr = "SELECT * FROM run_slice WHERE ms_level = ? AND begin_mz <= ? AND end_mz > ?";
		return new SQLiteQuery(connection, queryStr)
				.bind(1, msLevel)
				.bind(2, mz)
				.bind(3, mz)
				.extractRecord(_runSliceHeaderExtractor);
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
	 * @param connection
	 *            the connection
	 * @return the run slice ids for mz range
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	protected int[] getRunSliceIdsForMzRange(double minMz, double maxMz, int msLevel, SQLiteConnection connection) throws SQLiteException {

		RunSliceHeader firstRunSlice = this.getRunSliceForMz(minMz, msLevel, connection);
		RunSliceHeader lastRunSlice = this.getRunSliceForMz(maxMz, msLevel, connection);
		double mzHeight = (msLevel == 1) ? this.getMzDbReader().getBBSizes().BB_MZ_HEIGHT_MS1 : this.getMzDbReader().getBBSizes().BB_MZ_HEIGHT_MSn;

		int bufferLength = 1 + (int) ((maxMz - minMz) / mzHeight);

		String queryStr = "SELECT id FROM run_slice WHERE ms_level = ? AND begin_mz >= ? AND end_mz <= ?";

		return new SQLiteQuery(connection, queryStr)
				.bind(1, msLevel)
				.bind(2, firstRunSlice.getBeginMz())
				.bind(3, lastRunSlice.getEndMz())
				.extractInts(bufferLength);
	}

}
