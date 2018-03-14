package fr.profi.mzdb.io.reader;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.profi.mzdb.util.sqlite.SQLiteQuery;

/**
 * Allows to manipulates data contained in the mzDB file.
 *
 * @author David
 */
public class MzDbReaderQueries {

	final static Logger logger = LoggerFactory.getLogger(MzDbReaderQueries.class);

	/**
	 *
	 * @return
	 * @throws SQLiteException
	 */
	public static String getModelVersion(SQLiteConnection connection) throws SQLiteException {
		String sqlString = "SELECT version FROM mzdb LIMIT 1";
		return new SQLiteQuery(connection, sqlString).extractSingleString();
	}

	public static String getPwizMzDbVersion(SQLiteConnection connection) throws SQLiteException {
		String sqlString = "SELECT version FROM software WHERE name LIKE '%mzDB'";
		return new SQLiteQuery(connection, sqlString).extractSingleString();
	}

	/**
	 * Gets the last time.
	 *
	 * @return float the rt of the last spectrum
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public static float getLastTime(SQLiteConnection connection) throws SQLiteException {
		// Retrieve the number of spectra
		String sqlString = "SELECT time FROM spectrum ORDER BY id DESC LIMIT 1";
		return (float) new SQLiteQuery(connection, sqlString).extractSingleDouble();
	}

	/**
	 * Gets the max ms level.
	 *
	 * @return the max ms level
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public static int getMaxMsLevel(SQLiteConnection connection) throws SQLiteException {
		return new SQLiteQuery(connection, "SELECT max(ms_level) FROM run_slice").extractSingleInt();
	}

	/**
	 * Gets the mz range.
	 *
	 * @param msLevel
	 *            the ms level
	 * @return runSlice min mz and runSlice max mz
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public static int[] getMzRange(int msLevel, SQLiteConnection connection) throws SQLiteException {

		final SQLiteStatement stmt = connection.prepare("SELECT min(begin_mz), max(end_mz) FROM run_slice WHERE ms_level=?");
		stmt.bind(1, msLevel);
		stmt.step();

		final int minMz = stmt.columnInt(0);
		final int maxMz = stmt.columnInt(1);
		stmt.dispose();

		final int[] mzRange = { minMz, maxMz };
		return mzRange;
	}

	/**
	 * Gets the bounding box count.
	 *
	 * @return int, the number of bounding box
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public static int getBoundingBoxesCount(SQLiteConnection connection) throws SQLiteException {
		return getTableRecordsCount("bounding_box", connection);
	}

	/**
	 * Gets the bounding box count.
	 *
	 * @param runSliceId
	 *            the run slice id
	 * @return the number of bounding box contained in the specified runSliceId
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public static int getBoundingBoxesCount(int runSliceId, SQLiteConnection connection) throws SQLiteException {
		String queryStr = "SELECT count(*) FROM bounding_box WHERE bounding_box.run_slice_id = ?";
		return new SQLiteQuery(connection, queryStr).bind(1, runSliceId).extractSingleInt();
	}

	/**
	 * Gets the cycle count.
	 *
	 * @return the cycle count
	 * @throws SQLiteException
	 */
	public static int getCyclesCount(SQLiteConnection connection) throws SQLiteException {
		String queryStr = "SELECT max(cycle) FROM spectrum";
		return new SQLiteQuery(connection, queryStr).extractSingleInt();
	}

	/**
	 * Gets the data encoding count.
	 *
	 * @return the data encoding count
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public static int getDataEncodingsCount(SQLiteConnection connection) throws SQLiteException {
		return getTableRecordsCount("data_encoding", connection);
	}

	/**
	 * Gets the spectra count.
	 *
	 * @return int the number of spectra
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public static int getSpectraCount(SQLiteConnection connection) throws SQLiteException {
		return getTableRecordsCount("spectrum", connection);
	}

	/**
	 * Gets the spectra count for a given MS level.
	 *
	 * @return int the number of spectra
	 * @throws SQLiteException
	 *             the SQLite exception
	 */
	public static int getSpectraCount(int msLevel, SQLiteConnection connection) throws SQLiteException {
		String queryStr = "SELECT count(*) FROM spectrum WHERE ms_level = ?";
		return new SQLiteQuery(connection, queryStr).bind(1, msLevel).extractSingleInt();
	}

	/**
	 * Gets the run slice count.
	 *
	 * @return int the number of runSlice
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public static int getRunSlicesCount(SQLiteConnection connection) throws SQLiteException {
		return getTableRecordsCount("run_slice", connection);
	}

	/**
	 * Gets the table records count.
	 *
	 * @param tableName
	 *            the table name
	 * @return the int
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public static int getTableRecordsCount(String tableName, SQLiteConnection connection) throws SQLiteException {
		return new SQLiteQuery(connection, "SELECT seq FROM sqlite_sequence WHERE name = ?").bind(1, tableName).extractSingleInt();
	}

	/**
	 * Gets the bounding box data.
	 *
	 * @param bbId
	 *            the bb id
	 * @return the bounding box data
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public static byte[] getBoundingBoxData(int bbId, SQLiteConnection connection) throws SQLiteException {
		String sqlString = "SELECT data FROM bounding_box WHERE bounding_box.id = ?";
		return new SQLiteQuery(connection, sqlString).bind(1, bbId).extractSingleBlob();
	}

	/**
	 * Gets the bounding box first spectrum index.
	 *
	 * @param spectrumId
	 *            the spectrum id
	 * @return the bounding box first spectrum index
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public static long getBoundingBoxFirstSpectrumId(long spectrumId, SQLiteConnection connection) throws SQLiteException {
		String sqlString = "SELECT bb_first_spectrum_id FROM spectrum WHERE id = ?";
		return new SQLiteQuery(connection, sqlString).bind(1, spectrumId).extractSingleLong();
	}

	/**
	 * Gets the bounding box min mz.
	 *
	 * @param bbId
	 *            the bb id
	 * @return the bounding box min mz
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public static float getBoundingBoxMinMz(int bbId, SQLiteConnection connection) throws SQLiteException {
		String sqlString = "SELECT min_mz FROM bounding_box_rtree WHERE bounding_box_rtree.id = ?";
		return (float) new SQLiteQuery(connection, sqlString).bind(1, bbId).extractSingleDouble();
	}

	/**
	 * Gets the bounding box min time.
	 *
	 * @param bbId
	 *            the bb id
	 * @return the bounding box min time
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public static float getBoundingBoxMinTime(int bbId, SQLiteConnection connection) throws SQLiteException {
		String sqlString = "SELECT min_time FROM bounding_box_rtree WHERE bounding_box_rtree.id = ?";
		return (float) new SQLiteQuery(connection, sqlString).bind(1, bbId).extractSingleDouble();
	}

	/**
	 * Gets the bounding box ms level.
	 *
	 * @param bbId
	 *            the bb id
	 * @return the bounding box ms level
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public static int getBoundingBoxMsLevel(int bbId, SQLiteConnection connection) throws SQLiteException {

		// FIXME: check that the mzDB file has the bounding_box_msn_rtree table
		String sqlString1 = "SELECT run_slice_id FROM bounding_box WHERE id = ?";
		int runSliceId = new SQLiteQuery(connection, sqlString1).bind(1, bbId).extractSingleInt();

		String sqlString2 = "SELECT ms_level FROM run_slice WHERE run_slice.id = ?";
		return new SQLiteQuery(connection, sqlString2).bind(1, runSliceId).extractSingleInt();

		/*
		 * String sqlString =
		 * "SELECT min_ms_level FROM bounding_box_msn_rtree WHERE bounding_box_msn_rtree.id = ?"; return new
		 * SQLiteQuery(connection, sqlString).bind(1, bbId).extractSingleInt();
		 */
	}

}