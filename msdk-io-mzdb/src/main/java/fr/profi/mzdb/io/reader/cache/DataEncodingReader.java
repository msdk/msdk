package fr.profi.mzdb.io.reader.cache;

import java.util.Map;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.model.DataEncoding;

// TODO: Auto-generated Javadoc
/**
 * The Class DataEncodingReader.
 * 
 * @author David Bouyssie
 */
public class DataEncodingReader extends AbstractDataEncodingReader {
	
	private SQLiteConnection connection;
	private MzDbReader mzDbReader = null;

	/**
	 * Instantiates a new data encoding reader.
	 * 
	 * @param mzDbReader
	 *            the mz db reader
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public DataEncodingReader(MzDbReader mzDbReader) throws SQLiteException {
		super(mzDbReader);
		this.mzDbReader = mzDbReader;
		this.connection = mzDbReader.getConnection();
	}

	/** specialized getter */
	public MzDbReader getMzDbReader() {
		return this.mzDbReader;
	}
	

	/**
	 * Gets the data encoding.
	 * 
	 * @param dataEncodingId
	 *            the data encoding id
	 * @return the data encoding
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public DataEncoding getDataEncoding(int dataEncodingId) throws SQLiteException {
		return this.getDataEncoding(dataEncodingId, connection);
	}

	/**
	 * Gets the data encodings.
	 * 
	 * @return the data encodings
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public DataEncoding[] getDataEncodings() throws SQLiteException {
		return this.getDataEncodings(connection);
	}

	/**
	 * Gets the data encoding by id.
	 * 
	 * @return the data encoding by id
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public Map<Integer, DataEncoding> getDataEncodingById() throws SQLiteException {
		return this.getDataEncodingById(connection);
	}

	/**
	 * Gets the data encoding by spectrum id.
	 * 
	 * @return the data encoding by spectrum id
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public Map<Long, DataEncoding> getDataEncodingBySpectrumId() throws SQLiteException {
		return this.getDataEncodingBySpectrumId(connection);
	}

	/**
	 * Gets the spectrum data encoding.
	 * 
	 * @param spectrumId
	 *            the spectrum id
	 * @return the spectrum data encoding
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public DataEncoding getSpectrumDataEncoding(long spectrumId) throws SQLiteException {
		return this.getSpectrumDataEncoding(spectrumId, connection);
	}

}
