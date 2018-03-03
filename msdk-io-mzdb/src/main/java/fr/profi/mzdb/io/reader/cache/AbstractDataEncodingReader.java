package fr.profi.mzdb.io.reader.cache;

import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.AbstractMzDbReader;
import fr.profi.mzdb.db.model.params.ParamTree;
import fr.profi.mzdb.db.model.params.param.CVParam;
import fr.profi.mzdb.db.table.DataEncodingTable;
import fr.profi.mzdb.db.table.SpectrumTable;
import fr.profi.mzdb.io.reader.MzDbReaderQueries;
import fr.profi.mzdb.io.reader.table.ParamTreeParser;
import fr.profi.mzdb.model.DataEncoding;
import fr.profi.mzdb.model.DataMode;
import fr.profi.mzdb.model.PeakEncoding;
import fr.profi.mzdb.util.sqlite.ISQLiteRecordExtraction;
import fr.profi.mzdb.util.sqlite.SQLiteQuery;
import fr.profi.mzdb.util.sqlite.SQLiteRecord;
import fr.profi.mzdb.util.sqlite.SQLiteRecordIterator;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractDataEncodingReader.
 *
 * @author bouyssie
 */
public abstract class AbstractDataEncodingReader extends MzDbEntityCacheContainer {
	
	// Define some variable for spectrum header extraction
	/** The _data encoding query str. */
	private static String _dataEncodingQueryStr = "SELECT * FROM data_encoding";
	
	/** The _data encoding extractor. */
	private ISQLiteRecordExtraction<DataEncoding> _dataEncodingExtractor;

	/**
	 * Instantiates a new abstract data encoding reader.
	 *
	 * @param mzDbReader
	 *            the mz db reader
	 * @param modelVersion
	 *            the model version
	 * @throws SQLiteException
	 *             the SQ lite exception
	 */
	public AbstractDataEncodingReader(AbstractMzDbReader mzDbReader) throws SQLiteException {
		super(mzDbReader);
	}
	
	/**
	 * _get data encoding extractor.
	 *
	 * @return the ISQ lite record extraction
	 * @throws SQLiteException
	 *             the SQ lite exception
	 */
	private ISQLiteRecordExtraction<DataEncoding> _getDataEncodingExtractor(SQLiteConnection connection) throws SQLiteException {
		if( _dataEncodingExtractor != null ) return _dataEncodingExtractor;
		
		String modelVersion = MzDbReaderQueries.getModelVersion(connection);
		
		// Check if model version is newer than 0.6
		if ( modelVersion.compareTo("0.6") > 0 ) {
			_dataEncodingExtractor = new ISQLiteRecordExtraction<DataEncoding>() {

				public DataEncoding extract(SQLiteRecord record) throws SQLiteException {

					// Extract record values
					int id = record.columnInt(DataEncodingTable.ID);
					String dmAsStr = record.columnString(DataEncodingTable.MODE);
					String compression = record.columnString(DataEncodingTable.COMPRESSION);
					String byteOrderAsStr = record.columnString(DataEncodingTable.BYTE_ORDER);

					// Parse record values
					DataMode dm;
					if (dmAsStr.equalsIgnoreCase("FITTED"))
						dm = DataMode.FITTED;
					else
						dm = DataMode.CENTROID;

					ByteOrder bo;
					if (byteOrderAsStr.equalsIgnoreCase("big_endian"))
						bo = ByteOrder.BIG_ENDIAN;
					else
						bo = ByteOrder.LITTLE_ENDIAN;
					
					int mzPrecision = record.columnInt(DataEncodingTable.MZ_PRECISION);
					int intPrecision = record.columnInt(DataEncodingTable.INTENSITY_PRECISION);

					PeakEncoding peakEnc = null;
					if( mzPrecision == 32 ) {
						peakEnc = PeakEncoding.LOW_RES_PEAK;
					} else {
						if( intPrecision == 32 ) {
							peakEnc = PeakEncoding.HIGH_RES_PEAK;
						} else {
							peakEnc = PeakEncoding.NO_LOSS_PEAK;
						}
					}

					// Return data encoding object
					return new DataEncoding(id, dm, peakEnc, compression, bo);
				}
			};
		} else {
			_dataEncodingExtractor = new ISQLiteRecordExtraction<DataEncoding>() {

				public DataEncoding extract(SQLiteRecord record) throws SQLiteException {

					// Extract record values
					int id = record.columnInt(DataEncodingTable.ID);
					String dmAsStr = record.columnString(DataEncodingTable.MODE);
					String compression = record.columnString(DataEncodingTable.COMPRESSION);
					String byteOrderAsStr = record.columnString(DataEncodingTable.BYTE_ORDER);

					// Parse record values
					DataMode dm;
					if (dmAsStr.equalsIgnoreCase("FITTED"))
						dm = DataMode.FITTED;
					else
						dm = DataMode.CENTROID;

					ByteOrder bo;
					if (byteOrderAsStr.equalsIgnoreCase("big_endian"))
						bo = ByteOrder.BIG_ENDIAN;
					else
						bo = ByteOrder.LITTLE_ENDIAN;
					
					// Parse param tree
					String paramTreeAsStr = record.columnString(SpectrumTable.PARAM_TREE);
					ParamTree paramTree = ParamTreeParser.parseParamTree(paramTreeAsStr);		
					
					// NOTE: the two CV params may have the same AC => it could be conflicting...
					// It has been in fixed in version 0.9.8 of pwiz-mzdb
					List<CVParam> cvParams = paramTree.getCVParams();
					CVParam mzEncoding = cvParams.get(0);
					CVParam intEncoding = cvParams.get(1);
					
					PeakEncoding peakEnc = null;
					if( mzEncoding.getValue().equals("32") ) {
						peakEnc = PeakEncoding.LOW_RES_PEAK;
					} else {
						if( intEncoding.getValue().equals("32") ) {
							peakEnc = PeakEncoding.HIGH_RES_PEAK;
						} else {
							peakEnc = PeakEncoding.NO_LOSS_PEAK;
						}
					}

					// Return data encoding object
					return new DataEncoding(id, dm, peakEnc, compression, bo);
				}
			};
		}
		
		return _dataEncodingExtractor;
	}

	/**
	 * Gets the data encoding.
	 *
	 * @param dataEncodingId
	 *            the data encoding id
	 * @param connection
	 *            the connection
	 * @return the data encoding
	 * @throws SQLiteException
	 *             the SQ lite exception
	 */
	protected DataEncoding getDataEncoding(int dataEncodingId, SQLiteConnection connection) throws SQLiteException {

		if (this.getEntityCache() != null) {
			return this.getDataEncodingById(connection).get(dataEncodingId);
		} else {
			
			// Retrieve data encoding record
			String queryStr = _dataEncodingQueryStr + " WHERE id = ?";
			return new SQLiteQuery(connection, queryStr)
				.bind(1, dataEncodingId)
				.extractRecord(this._getDataEncodingExtractor(connection) );
		}

	}

	/**
	 * Gets the data encodings.
	 *
	 * @param connection
	 *            the connection
	 * @return the data encodings
	 * @throws SQLiteException
	 *             the SQ lite exception
	 */
	protected DataEncoding[] getDataEncodings(SQLiteConnection connection) throws SQLiteException {
		
		List<DataEncoding> dataEncodings = new SQLiteQuery(connection, _dataEncodingQueryStr)
				.extractRecordList(this._getDataEncodingExtractor(connection));
		
		return dataEncodings.toArray(new DataEncoding[dataEncodings.size()]);
	}

	/**
	 * Gets the data encoding by id.
	 *
	 * @param connection
	 *            the connection
	 * @return the data encoding by id
	 * @throws SQLiteException
	 *             the SQ lite exception
	 */
	protected Map<Integer, DataEncoding> getDataEncodingById(SQLiteConnection connection) throws SQLiteException {

		if (this.getEntityCache() != null && this.getEntityCache().dataEncodingById != null) {
			return this.getEntityCache().dataEncodingById;
		} else {
			DataEncoding[] dataEncodings = this.getDataEncodings(connection);
			HashMap<Integer, DataEncoding> dataEncodingById = new HashMap<Integer, DataEncoding>(
					dataEncodings.length);

			for (DataEncoding dataEncoding : dataEncodings)
				dataEncodingById.put(dataEncoding.getId(), dataEncoding);

			if (this.getEntityCache() != null)
				this.getEntityCache().dataEncodingById = dataEncodingById;

			return dataEncodingById;
		}
	}

	/**
	 * Gets the data encoding by spectrum id.
	 *
	 * @param connection
	 *            the connection
	 * @return the data encoding by spectrum id
	 * @throws SQLiteException
	 *             the SQ lite exception
	 */
	public Map<Long, DataEncoding> getDataEncodingBySpectrumId(SQLiteConnection connection) throws SQLiteException {

		if (this.getEntityCache() != null && this.getEntityCache().dataEncodingBySpectrumId != null) {
			return this.getEntityCache().dataEncodingBySpectrumId;
		} else {

			Map<Integer, DataEncoding> dataEncodingById = this.getDataEncodingById(connection);

			// Retrieve encoding PK for the given spectrum id
			String queryStr = "SELECT id, data_encoding_id FROM spectrum";
			SQLiteRecordIterator records = new SQLiteQuery(connection, queryStr).getRecordIterator();

			HashMap<Long, DataEncoding> dataEncodingBySpectrumId = new HashMap<Long, DataEncoding>();
			while (records.hasNext()) {
				SQLiteRecord record = records.next();

				long spectrumId = record.columnLong(SpectrumTable.ID);
				int spectrumDataEncodingId = record.columnInt(SpectrumTable.DATA_ENCODING_ID);
				
				DataEncoding dataEnc = dataEncodingById.get(spectrumDataEncodingId);

				/*
				// Looking for the appropriate peak encoding
				// FIXME: retrieve the resolution from the data encoding param tree
				PeakEncoding pe = (h.isHighResolution()) ? PeakEncoding.HIGH_RES_PEAK
						: PeakEncoding.LOW_RES_PEAK;
				if (mzDbReader.isNoLossMode())
					pe = PeakEncoding.NO_LOSS_PEAK;

				// Setting new peak encoding was set to null before
				dataEnc.setPeakEncoding(pe);*/

				dataEncodingBySpectrumId.put(spectrumId, dataEnc);
			}

			if (this.getEntityCache() != null) {
				this.getEntityCache().dataEncodingBySpectrumId = dataEncodingBySpectrumId;
			}

			return dataEncodingBySpectrumId;
		}

	}

	/**
	 * Gets the spectrum data encoding.
	 *
	 * @param spectrumId
	 *            the spectrum id
	 * @param connection
	 *            the connection
	 * @return the spectrum data encoding
	 * @throws SQLiteException
	 *             the SQ lite exception
	 */
	protected DataEncoding getSpectrumDataEncoding(long spectrumId, SQLiteConnection connection) throws SQLiteException {

		if (this.getEntityCache() != null) {
			return this.getDataEncodingBySpectrumId(connection).get(spectrumId);
		} else {
			// Retrieve encoding PK for the given spectrum id
			String queryStr = "SELECT data_encoding_id FROM spectrum WHERE id = " + spectrumId;
			return this.getDataEncoding(new SQLiteQuery(connection, queryStr).extractSingleInt(), connection);
		}

	}

}
