package fr.profi.mzdb.io.reader.cache;

import java.util.Map;

import com.almworks.sqlite4java.SQLiteException;

import rx.Observable;

import fr.profi.mzdb.MzDbAsyncReader;
import fr.profi.mzdb.model.DataEncoding;

// TODO: Auto-generated Javadoc
/**
 * The Class DataEncodingReader.
 * 
 * @author David Bouyssie
 */
public class DataEncodingAsyncReader extends AbstractDataEncodingReader {
	
	/** The mzDB reader. */
	private MzDbAsyncReader mzDbReader = null;

	/**
	 * Instantiates a new data encoding reader.
	 * 
	 * @param mzDbReader
	 *            the mz db reader
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public DataEncodingAsyncReader(MzDbAsyncReader mzDbReader) throws SQLiteException {
		super(mzDbReader);
		this.mzDbReader = mzDbReader;
	}

	/** specialized getter */
	public MzDbAsyncReader getMzDbReader() {
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
	public Observable<DataEncoding> getDataEncoding(int dataEncodingId) {
		return mzDbReader.observeJobExecution( connection -> {
			return this.getDataEncoding(dataEncodingId, connection);
		});
	}

	/**
	 * Gets the data encodings.
	 * 
	 * @return the data encodings
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public Observable<DataEncoding[]> getDataEncodings() {
		return mzDbReader.observeJobExecution( connection -> {
			return this.getDataEncodings(connection);
		});
	}

	/**
	 * Gets the data encoding by id.
	 * 
	 * @return the data encoding by id
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public Observable<Map<Integer, DataEncoding>> getDataEncodingById() {
		return mzDbReader.observeJobExecution( connection -> {
			return this.getDataEncodingById(connection);
		});
	}

	/**
	 * Gets the data encoding by spectrum id.
	 * 
	 * @return the data encoding by spectrum id
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public Observable<Map<Long, DataEncoding>> getDataEncodingBySpectrumId() {
		return mzDbReader.observeJobExecution( connection -> {
			return this.getDataEncodingBySpectrumId(connection);
		});
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
	public Observable<DataEncoding> getSpectrumDataEncoding(long spectrumId) {
		return mzDbReader.observeJobExecution( connection -> {
			return this.getSpectrumDataEncoding(spectrumId, connection);
		});
	}

}
