package fr.profi.mzdb.io.reader.cache;

import java.util.HashMap;
import java.util.Map;

import com.almworks.sqlite4java.SQLiteException;

import rx.Observable;

import fr.profi.mzdb.MzDbAsyncReader;
import fr.profi.mzdb.model.RunSliceHeader;

// TODO: Auto-generated Javadoc
/**
 * The Class RunSliceHeaderReader.
 * 
 * @author David Bouyssie
 */
public class RunSliceHeaderAsyncReader extends AbstractRunSliceHeaderReader {
	
	/** The mzDB reader. */
	private MzDbAsyncReader mzDbReader = null;
	
	/**
	 * Instantiates a new run slice header reader.
	 * 
	 * @param mzDbReader
	 *            the mz db reader
	 */
	public RunSliceHeaderAsyncReader(MzDbAsyncReader mzDbReader) {
		super(mzDbReader);
		this.mzDbReader = mzDbReader;
	}
	
	/** specialized getter */
	public MzDbAsyncReader getMzDbReader() {
		return this.mzDbReader;
	}
	
	/**
	 * Gets the run slices.
	 * 
	 * @return array of runSlice instance without data associated
	 * @throws SQLiteException
	 *             the SQLite exception
	 */
	public Observable<RunSliceHeader[]> getRunSliceHeaders() {
		return mzDbReader.observeJobExecution( connection -> {
			return this.getRunSliceHeaders(connection);
		});
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
	public Observable<RunSliceHeader[]> getRunSliceHeaders(int msLevel) {
		return mzDbReader.observeJobExecution( connection -> {
			return this.getRunSliceHeaders(msLevel, connection);
		});
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
	public Observable<HashMap<Integer, RunSliceHeader>> getRunSliceHeaderById(int msLevel) {
		return mzDbReader.observeJobExecution( connection -> {
			return this.getRunSliceHeaderById(msLevel, connection);
		});
	}

	/**
	 * Gets the run slice header by id.
	 * 
	 * @return the run slice header by id
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public Observable<Map<Integer, RunSliceHeader>> getRunSliceHeaderById() {
		return mzDbReader.observeJobExecution( connection -> {
			return this.getRunSliceHeaderById(connection);
		});
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
	public Observable<RunSliceHeader> getRunSliceHeader(int id) {
		return mzDbReader.observeJobExecution( connection -> {
			return this.getRunSliceHeader(id, connection);
		});
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
	public Observable<RunSliceHeader> getRunSliceForMz(double mz, int msLevel) {
		return mzDbReader.observeJobExecution( connection -> {
			return this.getRunSliceForMz(mz, msLevel, connection);
		});
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
	public Observable<int[]> getRunSliceIdsForMzRange(double minMz, double maxMz, int msLevel) {
		return mzDbReader.observeJobExecution( connection -> {
			return this.getRunSliceIdsForMzRange(minMz, maxMz, msLevel, connection);
		});
	}

}
