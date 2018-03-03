package fr.profi.mzdb.io.reader.cache;

import java.util.Map;

import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.MzDbAsyncReader;
import fr.profi.mzdb.model.SpectrumHeader;
import rx.Observable;

/**
 * @author David Bouyssie
 * 
 */
public class SpectrumHeaderAsyncReader extends AbstractSpectrumHeaderReader {
	
	/** The mzDB reader. */
	private MzDbAsyncReader mzDbReader = null;
	
	/**
	 * @param mzDbReader
	 * @throws SQLiteException 
	 */
	public SpectrumHeaderAsyncReader(MzDbAsyncReader mzDbReader, AbstractDataEncodingReader dataEncodingReader) throws SQLiteException {
		super(mzDbReader, dataEncodingReader);
		this.mzDbReader = mzDbReader;
	}

	/** specialized getter */
	public MzDbAsyncReader getMzDbReader() {
		return this.mzDbReader;
	}
	
	/**
	 * Gets the spectrum headers.
	 * 
	 * @return the spectrum headers
	 * @throws SQLiteException
	 */
	public Observable<SpectrumHeader[]> getSpectrumHeaders() {
		return getMzDbReader().observeJobExecution( connection -> {
			return this.getSpectrumHeaders(connection);
		});
	}
	
	/**
	 * Gets the spectrum headers by id.
	 * 
	 * @return the spectrum header by id
	 * @throws SQLiteException
	 */
	public Observable<Map<Long, SpectrumHeader>> getSpectrumHeaderById() {
		return getMzDbReader().observeJobExecution( connection -> {
			return this.getSpectrumHeaderById(connection);
		});
	}

	/**
	 * Gets the MS1 spectrum headers.
	 * 
	 * @return the spectrum headers
	 * @throws SQLiteException
	 */
	public Observable<SpectrumHeader[]> getMs1SpectrumHeaders() {		
		return getMzDbReader().observeJobExecution( connection -> {
			return getMs1SpectrumHeaders(connection);
		});
	}

	/**
	 * Gets the MS1 spectrum header by id.
	 * 
	 * @return the spectrum header by id
	 * @throws SQLiteException
	 */
	public Observable<Map<Long, SpectrumHeader>> getMs1SpectrumHeaderById() {
		return getMzDbReader().observeJobExecution( connection -> {
			return getMs1SpectrumHeaderById(connection);
		});
	}

	/**
	 * Gets the MS2 spectrum headers.
	 * 
	 * @return the spectrum headers
	 * @throws SQLiteException
	 */
	public Observable<SpectrumHeader[]> getMs2SpectrumHeaders() {
		return getMzDbReader().observeJobExecution( connection -> {
			return getMs2SpectrumHeaders(connection);
		});
	}

	/**
	 * Gets the MS2 spectrum header by id.
	 * 
	 * @return the spectrum header by id
	 * @throws SQLiteException
	 */
	public Observable<Map<Long, SpectrumHeader>> getMs2SpectrumHeaderById() {
		return getMzDbReader().observeJobExecution( connection -> {
			return getMs2SpectrumHeaderById(connection);
		});
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
	public Observable<SpectrumHeader> getSpectrumHeader(long id) {
		return getMzDbReader().observeJobExecution( connection -> {
			return getSpectrumHeader(id, connection);
		});
	}

	/**
	 * Gets the spectrum time by id.
	 * 
	 * @return the spectrum time mapped by the spectrum id
	 * @throws SQLiteException the SQLite exception
	 */
	public Observable<Map<Long, Float>> getSpectrumTimeById() {		
		return getMzDbReader().observeJobExecution( connection -> {
			return getSpectrumTimeById(connection);
		});
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
	public Observable<SpectrumHeader> getSpectrumHeaderForTime(float time, int msLevel) {
		return getMzDbReader().observeJobExecution( connection -> {
			return getSpectrumHeaderForTime(time, msLevel, connection);
		});
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
	public Observable<long[]> getSpectrumIdsForTimeRange(float minRT, float maxRT, int msLevel) {
		return getMzDbReader().observeJobExecution( connection -> {
			return getSpectrumIdsForTimeRange(minRT, maxRT, msLevel, connection);
		});		
	}

}
