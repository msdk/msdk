package fr.profi.mzdb.io.reader.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

import fr.profi.mzdb.AbstractMzDbReader;
import fr.profi.mzdb.io.reader.MzDbReaderQueries;
import fr.profi.mzdb.io.reader.table.ParamTreeParser;
import fr.profi.mzdb.model.DataEncoding;
import fr.profi.mzdb.model.PeakEncoding;
import fr.profi.mzdb.model.SpectrumHeader;
import fr.profi.mzdb.util.sqlite.ISQLiteRecordExtraction;
import fr.profi.mzdb.util.sqlite.SQLiteQuery;
import fr.profi.mzdb.util.sqlite.SQLiteRecord;

/**
 * @author David Bouyssie
 * 
 */
public abstract class AbstractSpectrumHeaderReader extends MzDbEntityCacheContainer {
	
	/** The time index width. */
	protected final static int TIME_INDEX_WIDTH = 15;
	
	private AbstractDataEncodingReader _dataEncodingReader;
	
	/** The spectrum header extractor. */
	private ISQLiteRecordExtraction<SpectrumHeader> _spectrumHeaderExtractor;
	
	/**
	 * @param mzDbReader
	 */
	public AbstractSpectrumHeaderReader(AbstractMzDbReader mzDbReader, AbstractDataEncodingReader dataEncodingReader) throws SQLiteException {
		super(mzDbReader);
		
		this._dataEncodingReader = dataEncodingReader;
	}

	// Define some variable for spectrum header extraction
	private static String _spectrumHeaderQueryStr = 
		"SELECT id, initial_id, cycle, time, ms_level, tic, "+
		"base_peak_mz, base_peak_intensity, main_precursor_mz, main_precursor_charge, " +
		"data_points_count, param_tree, scan_list, precursor_list, data_encoding_id, bb_first_spectrum_id FROM spectrum";
	
	private static String _ms1SpectrumHeaderQueryStr = _spectrumHeaderQueryStr + " WHERE ms_level = 1";
	private static String _ms2SpectrumHeaderQueryStr = _spectrumHeaderQueryStr + " WHERE ms_level = 2";
	private static String _ms3SpectrumHeaderQueryStr = _spectrumHeaderQueryStr + " WHERE ms_level = 3";
	
	private enum SpectrumHeaderCol {

		ID("id"),
		INITIAL_ID("initial_id"),
		CYCLE("cycle"),
		TIME("time"),
		MS_LEVEL("ms_level"),
		TIC("tic"),
		BASE_PEAK_MZ("base_peak_mz"),
		BASE_PEAK_INTENSITY("base_peak_intensity"),
		MAIN_PRECURSOR_MZ("main_precursor_mz"),
		MAIN_PRECURSOR_CHARGE("main_precursor_charge"),
		DATA_POINTS_COUNT("data_points_count"),
		PARAM_TREE("param_tree"),
		SCAN_LIST("spectrum_list"),
		PRECURSOR_LIST("precursor_list"),
		DATA_ENCODING_ID("data_encoding_id"),
		BB_FIRST_SPECTRUM_ID("bb_first_spectrum_id");

		@SuppressWarnings("unused")
		protected final String columnName;

		private SpectrumHeaderCol(String colName) {
			this.columnName = colName;
		}

	}
	
	private static class SpectrumHeaderColIdx {	
		static int id = SpectrumHeaderCol.ID.ordinal();
		static int initialId= SpectrumHeaderCol.INITIAL_ID.ordinal();
		static int cycleCol= SpectrumHeaderCol.CYCLE.ordinal();
		static int time = SpectrumHeaderCol.TIME.ordinal();
		static int msLevel = SpectrumHeaderCol.MS_LEVEL.ordinal();
		static int tic = SpectrumHeaderCol.TIC.ordinal();
		static int basePeakMz = SpectrumHeaderCol.BASE_PEAK_MZ.ordinal();
		static int basePeakIntensity = SpectrumHeaderCol.BASE_PEAK_INTENSITY.ordinal();
		static int mainPrecursorMz = SpectrumHeaderCol.MAIN_PRECURSOR_MZ.ordinal();
		static int mainPrecursorCharge = SpectrumHeaderCol.MAIN_PRECURSOR_CHARGE.ordinal();
		static int dataPointsCount = SpectrumHeaderCol.DATA_POINTS_COUNT.ordinal();
		static int paramTree = SpectrumHeaderCol.PARAM_TREE.ordinal();
		static int scanList = SpectrumHeaderCol.SCAN_LIST.ordinal();
		static int precursorList = SpectrumHeaderCol.PRECURSOR_LIST.ordinal();
		static int dataEncodingId = SpectrumHeaderCol.DATA_ENCODING_ID.ordinal();
		static int bbFirstSpectrumId = SpectrumHeaderCol.BB_FIRST_SPECTRUM_ID.ordinal();
	}
	
	private ISQLiteRecordExtraction<SpectrumHeader> _getSpectrumHeaderExtractor(SQLiteConnection connection) throws SQLiteException {
		if( _spectrumHeaderExtractor != null ) return _spectrumHeaderExtractor;
		
		AbstractMzDbReader mzDbReader = this.getMzDbReader();
		
		_spectrumHeaderExtractor = new ISQLiteRecordExtraction<SpectrumHeader>() {

			public SpectrumHeader extract(SQLiteRecord record) throws SQLiteException {
	
				SQLiteStatement stmt = record.getStatement();
	
				// long nano = System.nanoTime();
				int msLevel = stmt.columnInt(SpectrumHeaderColIdx.msLevel);
	
				double precursorMz = 0.0;
				int precursorCharge = 0;
				if (msLevel >= 2) {
					precursorMz = stmt.columnDouble(SpectrumHeaderColIdx.mainPrecursorMz);
					precursorCharge = stmt.columnInt(SpectrumHeaderColIdx.mainPrecursorCharge);
				}
	
				int bbFirstSpectrumId = stmt.columnInt(SpectrumHeaderColIdx.bbFirstSpectrumId);
	
				DataEncoding dataEnc = _dataEncodingReader.getDataEncoding(stmt.columnInt(SpectrumHeaderColIdx.dataEncodingId), connection);
	
				boolean isHighRes = dataEnc.getPeakEncoding() == PeakEncoding.LOW_RES_PEAK ? false : true;
	
				SpectrumHeader sh = new SpectrumHeader(
					stmt.columnLong(SpectrumHeaderColIdx.id),
					stmt.columnInt(SpectrumHeaderColIdx.initialId),
					stmt.columnInt(SpectrumHeaderColIdx.cycleCol),
					(float) stmt.columnDouble(SpectrumHeaderColIdx.time),
					msLevel,
					stmt.columnInt(SpectrumHeaderColIdx.dataPointsCount),
					isHighRes,
					(float) stmt.columnDouble(SpectrumHeaderColIdx.tic),
					stmt.columnDouble(SpectrumHeaderColIdx.basePeakMz),
					(float) stmt.columnDouble(SpectrumHeaderColIdx.basePeakIntensity),
					precursorMz,
					precursorCharge,
					bbFirstSpectrumId
				);
				
				if (mzDbReader.isParamTreeLoadingEnabled()) {
					sh.setParamTree( ParamTreeParser.parseParamTree(stmt.columnString(SpectrumHeaderColIdx.paramTree)) );
				}
				if (mzDbReader.isScanListLoadingEnabled()) {
					sh.setScanList(ParamTreeParser.parseScanList(stmt.columnString(SpectrumHeaderColIdx.scanList)));
				}
				if (mzDbReader.isPrecursorListLoadingEnabled() && msLevel >= 2) {
					sh.setPrecursor(ParamTreeParser.parsePrecursor(stmt.columnString(SpectrumHeaderColIdx.precursorList)));
				}
	
				// System.out.println( (double) (System.nanoTime() - nano) / 1e3 );
	
				// sh.setParamTree(paramTree);
	
				return sh;
			}
	
		};
	
		return _spectrumHeaderExtractor;
	}
	
	private SpectrumHeader[] _loadSpectrumHeaders(SQLiteConnection connection, int msLevel, String queryStr) throws SQLiteException {

		int spectraCount = MzDbReaderQueries.getSpectraCount(msLevel, connection);

		SpectrumHeader[] spectrumHeaders = new SpectrumHeader[spectraCount];

		new SQLiteQuery(connection, queryStr)
			.extractRecords(this._getSpectrumHeaderExtractor(connection), spectrumHeaders);

		return spectrumHeaders;
	}

	private Map<Long, SpectrumHeader> _buildSpectrumHeaderById(SpectrumHeader[] spectrumHeaders) throws SQLiteException {

		Map<Long, SpectrumHeader> spectrumHeaderById = new HashMap<Long, SpectrumHeader>(spectrumHeaders.length);

		for (SpectrumHeader spectrumHeader : spectrumHeaders)
			spectrumHeaderById.put(spectrumHeader.getId(), spectrumHeader);

		return spectrumHeaderById;
	}
	
	/**
	 * Gets the spectrum headers.
	 *
	 * @param connection
	 *            the connection
	 * @return the spectrum headers
	 * @throws SQLiteException
	 *             the SQ lite exception
	 */
	protected SpectrumHeader[] getSpectrumHeaders(SQLiteConnection connection) throws SQLiteException {
		if (this.getEntityCache() != null && this.getEntityCache().spectrumHeaders != null) {
			return this.getEntityCache().spectrumHeaders;
		} else {
			SpectrumHeader[] ms1SpectrumHeaders = this.getMs1SpectrumHeaders(connection);
			SpectrumHeader[] ms2SpectrumHeaders = this.getMs2SpectrumHeaders(connection);
			SpectrumHeader[] ms3SpectrumHeaders = this.getMs3SpectrumHeaders(connection);

			int spectraCount = ms1SpectrumHeaders.length + ms2SpectrumHeaders.length + ms3SpectrumHeaders.length;
			
			SpectrumHeader[] spectrumHeaders = new SpectrumHeader[spectraCount];

			System.arraycopy(ms1SpectrumHeaders, 0, spectrumHeaders, 0, ms1SpectrumHeaders.length);
			System.arraycopy(ms2SpectrumHeaders, 0, spectrumHeaders, ms1SpectrumHeaders.length, ms2SpectrumHeaders.length);
			System.arraycopy(ms3SpectrumHeaders, 0, spectrumHeaders, ms1SpectrumHeaders.length + ms2SpectrumHeaders.length, ms3SpectrumHeaders.length);

			if (this.getEntityCache() != null)
				this.getEntityCache().spectrumHeaders = spectrumHeaders;

			return spectrumHeaders;
		}
	}

	/**
	 * Gets the spectrum headers by id.
	 *
	 * @param connection
	 *            the connection
	 * @return the spectrum header by id
	 * @throws SQLiteException
	 *             the SQ lite exception
	 */
	public Map<Long, SpectrumHeader> getSpectrumHeaderById(SQLiteConnection connection) throws SQLiteException {

		if (this.getEntityCache() != null && this.getEntityCache().spectrumHeaderById != null) {
			return this.getEntityCache().spectrumHeaderById;
		} else {
			
			Map<Long, SpectrumHeader> spectrumHeaderById = _buildSpectrumHeaderById(this.getSpectrumHeaders(connection));

			if (this.getEntityCache() != null)
				this.getEntityCache().spectrumHeaderById = spectrumHeaderById;

			return spectrumHeaderById;
		}
	}

	/**
	 * Gets the MS1 spectrum headers.
	 *
	 * @param connection
	 *            the connection
	 * @return the spectrum headers
	 * @throws SQLiteException
	 *             the SQ lite exception
	 */
	protected SpectrumHeader[] getMs1SpectrumHeaders(SQLiteConnection connection) throws SQLiteException {

		if (this.getEntityCache() != null && this.getEntityCache().ms1SpectrumHeaders != null) {
			return this.getEntityCache().ms1SpectrumHeaders;
		} else {

			SpectrumHeader[] ms1SpectrumHeaders = _loadSpectrumHeaders(connection, 1, _ms1SpectrumHeaderQueryStr);

			if (this.getEntityCache() != null)
				this.getEntityCache().ms1SpectrumHeaders = ms1SpectrumHeaders;

			return ms1SpectrumHeaders;
		}

	}

	/**
	 * Gets the MS1 spectrum header by id.
	 *
	 * @param connection
	 *            the connection
	 * @return the spectrum header by id
	 * @throws SQLiteException
	 *             the SQ lite exception
	 */
	public Map<Long, SpectrumHeader> getMs1SpectrumHeaderById(SQLiteConnection connection) throws SQLiteException {
		
		if (this.getEntityCache() != null && this.getEntityCache().ms1SpectrumHeaderById != null) {
			return this.getEntityCache().ms1SpectrumHeaderById;
		} else {
			Map<Long, SpectrumHeader> ms1SpectrumHeaderById = _buildSpectrumHeaderById(this.getMs1SpectrumHeaders(connection));

			if (this.getEntityCache() != null)
				this.getEntityCache().ms1SpectrumHeaderById = ms1SpectrumHeaderById;

			return ms1SpectrumHeaderById;
		}
	}

	/**
	 * Gets the MS2 spectrum headers.
	 *
	 * @param connection
	 *            the connection
	 * @return the spectrum headers
	 * @throws SQLiteException
	 *             the SQ lite exception
	 */
	protected SpectrumHeader[] getMs2SpectrumHeaders(SQLiteConnection connection) throws SQLiteException {

		if (this.getEntityCache() != null && this.getEntityCache().ms2SpectrumHeaders != null) {
			return this.getEntityCache().ms2SpectrumHeaders;
		} else {

			SpectrumHeader[] ms2SpectrumHeaders = _loadSpectrumHeaders(connection, 2, _ms2SpectrumHeaderQueryStr);

			if (this.getEntityCache() != null)
				this.getEntityCache().ms2SpectrumHeaders = ms2SpectrumHeaders;

			return ms2SpectrumHeaders;
		}

	}

	/**
	 * Gets the MS2 spectrum header by id.
	 *
	 * @param connection
	 *            the connection
	 * @return the spectrum header by id
	 * @throws SQLiteException
	 *             the SQ lite exception
	 */
	public Map<Long, SpectrumHeader> getMs2SpectrumHeaderById(SQLiteConnection connection) throws SQLiteException {

		if (this.getEntityCache() != null && this.getEntityCache().ms2SpectrumHeaderById != null) {
			return this.getEntityCache().ms2SpectrumHeaderById;
		} else {
			Map<Long, SpectrumHeader> ms2SpectrumHeaderById = _buildSpectrumHeaderById(this.getMs2SpectrumHeaders(connection));

			if (this.getEntityCache() != null)
				this.getEntityCache().ms2SpectrumHeaderById = ms2SpectrumHeaderById;

			return ms2SpectrumHeaderById;
		}
	}

	/**
	 * Gets the MS2 spectrum headers.
	 *
	 * @param connection
	 *            the connection
	 * @return the spectrum headers
	 * @throws SQLiteException
	 *             the SQ lite exception
	 */
	protected SpectrumHeader[] getMs3SpectrumHeaders(SQLiteConnection connection) throws SQLiteException {

		if (this.getEntityCache() != null && this.getEntityCache().ms3SpectrumHeaders != null) {
			return this.getEntityCache().ms3SpectrumHeaders;
		} else {

			SpectrumHeader[] ms3SpectrumHeaders = _loadSpectrumHeaders(connection, 3, _ms3SpectrumHeaderQueryStr);

			if (this.getEntityCache() != null)
				this.getEntityCache().ms3SpectrumHeaders = ms3SpectrumHeaders;

			return ms3SpectrumHeaders;
		}

	}

	/**
	 * Gets the MS2 spectrum header by id.
	 *
	 * @param connection
	 *            the connection
	 * @return the spectrum header by id
	 * @throws SQLiteException
	 *             the SQ lite exception
	 */
	public Map<Long, SpectrumHeader> getMs3SpectrumHeaderById(SQLiteConnection connection) throws SQLiteException {

		if (this.getEntityCache() != null && this.getEntityCache().ms3SpectrumHeaderById != null) {
			return this.getEntityCache().ms3SpectrumHeaderById;
		} else {
			Map<Long, SpectrumHeader> ms3SpectrumHeaderById = _buildSpectrumHeaderById(this.getMs3SpectrumHeaders(connection));

			if (this.getEntityCache() != null)
				this.getEntityCache().ms3SpectrumHeaderById = ms3SpectrumHeaderById;

			return ms3SpectrumHeaderById;
		}
	}

	/**
	 * /** Gets the spectrum header.
	 *
	 * @param id
	 *            the id
	 * @param connection
	 *            the connection
	 * @return spectrum header
	 * @throws SQLiteException
	 *             the SQ lite exception
	 */
	public SpectrumHeader getSpectrumHeader(long id, SQLiteConnection connection) throws SQLiteException {
		if (this.getEntityCache() != null) {
			return this.getSpectrumHeaderById(connection).get(id);
		} else {
			String queryStr = _spectrumHeaderQueryStr + " WHERE id = ? ";
			return new SQLiteQuery(connection, queryStr).bind(1, id).extractRecord(this._spectrumHeaderExtractor);
		}
	}

	/**
	 * Gets the spectrum time by id.
	 *
	 * @param connection
	 *            the connection
	 * @return the spectrum time mapped by the spectrum id
	 * @throws SQLiteException
	 *             the SQLite exception
	 */
	protected Map<Long, Float> getSpectrumTimeById(SQLiteConnection connection) throws SQLiteException {

		if (this.getEntityCache() != null && this.getEntityCache().spectrumTimeById != null) {
			return this.getEntityCache().spectrumTimeById;
		} else {
			
			int spectraCount = MzDbReaderQueries.getSpectraCount(connection);
			
			float[] spectrumTimes = new SQLiteQuery(connection, "SELECT time FROM spectrum").extractFloats(spectraCount);
			if( spectraCount != spectrumTimes.length ){
				System.err.println("extractFloats error: spectraCount != spectrumTimes.length");
			}
			Map<Long, Float> spectrumTimeById = new HashMap<Long, Float>(spectraCount);

			// TODO: check this approach is not too dangerous
			// FIXME: load the both values in the SQL query
			long spectrumId = 0;
			for (float spectrumTime : spectrumTimes) {
				spectrumId++;
				spectrumTimeById.put(spectrumId, spectrumTime);
			}

			if (this.getEntityCache() != null)
				this.getEntityCache().spectrumTimeById = spectrumTimeById;

			return spectrumTimeById;
		}
	}

	/**
	 * Gets the spectrum header for time.
	 *
	 * @param time
	 *            the time
	 * @param msLevel
	 *            the ms level
	 * @param connection
	 *            the connection
	 * @return SpectrumHeader the closest to the time input parameter
	 * @throws Exception
	 *             the exception
	 */
	protected SpectrumHeader getSpectrumHeaderForTime(float time, int msLevel, SQLiteConnection connection) throws Exception {

		if (this.getEntityCache() != null) {
			Map<Integer, ArrayList<Long>> spectrumIdsByTimeIndex = this._getSpectrumIdsByTimeIndex(connection);

			int timeIndex = (int) (time / TIME_INDEX_WIDTH);
			SpectrumHeader nearestSpectrumHeader = null;

			for (int index = timeIndex - 1; index <= timeIndex + 1; index++) {

				if (spectrumIdsByTimeIndex.containsKey(index) == false) {
					continue;
				}

				ArrayList<Long> tmpSpectrumIds = spectrumIdsByTimeIndex.get(index);
				for (Long tmpSpectrumId : tmpSpectrumIds) {

					SpectrumHeader spectrumH = this.getSpectrumHeader(tmpSpectrumId, connection);
					if (spectrumH == null) {
						throw new Exception("can' t retrieve spectrum with id =" + tmpSpectrumId);
					}

					if (spectrumH.getMsLevel() != msLevel)
						continue;

					if ( nearestSpectrumHeader == null || 
						 Math.abs(spectrumH.getTime() - time) < Math.abs(nearestSpectrumHeader.getTime() - time) ) {
						nearestSpectrumHeader = spectrumH;
					}
				}
			}

			return nearestSpectrumHeader;
		} else {
			String queryStr = "SELECT id FROM spectrum WHERE ms_level = ? ORDER BY abs(spectrum.time - ?) ASC limit 1";
			int spectrumId = new SQLiteQuery(connection, queryStr)
				.bind(1,msLevel)
				.bind(2,time)
				.extractSingleInt();
			
			return this.getSpectrumHeader(spectrumId, connection);
		}

	}

	/**
	 * Gets the spectrum ids by time index.
	 *
	 * @param msLevel
	 *            the ms level
	 * @param connection
	 *            the connection
	 * @return hashmap of key time index value array of spectrumIds
	 * @throws SQLiteException
	 *             the SQ lite exception
	 */
	private Map<Integer, ArrayList<Long>> _getSpectrumIdsByTimeIndex(SQLiteConnection connection) throws SQLiteException {

		HashMap<Integer, ArrayList<Long>> spectrumIdsByTimeIndex = null;
		if (this.getEntityCache() != null) {
		  spectrumIdsByTimeIndex = (HashMap<Integer, ArrayList<Long>>) this.getEntityCache().spectrumIdsByTimeIndex;
		}

		if (spectrumIdsByTimeIndex != null)
			return spectrumIdsByTimeIndex;
		else {
			spectrumIdsByTimeIndex = new HashMap<Integer, ArrayList<Long>>();

			SpectrumHeader[] spectrumHeaders = this.getSpectrumHeaders(connection);

			for (SpectrumHeader spectrumH : spectrumHeaders) {
				int timeIndex = (int) (spectrumH.getTime() / TIME_INDEX_WIDTH);

				if (spectrumIdsByTimeIndex.get(timeIndex) == null)
					spectrumIdsByTimeIndex.put(timeIndex, new ArrayList<Long>());

				spectrumIdsByTimeIndex.get(timeIndex).add(spectrumH.getId());
			}

			if (this.getEntityCache() != null) {
			  this.getEntityCache().spectrumIdsByTimeIndex = spectrumIdsByTimeIndex;
			}

			return spectrumIdsByTimeIndex;
		}
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
	 * @param connection
	 *            the connection
	 * @return array of integers corresponding to the ids of matching spectrum
	 * @throws SQLiteException
	 *             the SQ lite exception
	 */
	protected long[] getSpectrumIdsForTimeRange(float minRT, float maxRT, int msLevel, SQLiteConnection connection) throws SQLiteException {

		// TODO: use entity cache ?
		SQLiteQuery query = new SQLiteQuery(connection, "SELECT id FROM spectrum WHERE ms_level = ? AND time >= ? AND time <= ?");
		return query.bind(1, msLevel).bind(2, minRT).bind(3, maxRT).extractLongs(1);
	}

}
