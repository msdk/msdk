package fr.profi.mzdb;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.almworks.sqlite4java.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.profi.mzdb.db.model.*;
import fr.profi.mzdb.io.reader.MzDbReaderQueries;
import fr.profi.mzdb.io.reader.cache.*;
import fr.profi.mzdb.io.reader.iterator.BoundingBoxIterator;
import fr.profi.mzdb.io.reader.iterator.LcMsRunSliceIterator;
import fr.profi.mzdb.io.reader.iterator.LcMsnRunSliceIterator;
import fr.profi.mzdb.io.reader.iterator.SpectrumIterator;
import fr.profi.mzdb.io.reader.table.*;
import fr.profi.mzdb.model.*;
import fr.profi.mzdb.util.sqlite.ISQLiteConnectionFunction;
import fr.profi.mzdb.util.sqlite.SQLiteJobWrapper;
import fr.profi.mzdb.util.sqlite.SQLiteObservableJob;

import rx.Observable;

/**
 * Allows to manipulates data contained in the mzDB file.
 *
 * @author David
 */
public class MzDbAsyncReader extends AbstractMzDbReader {

	final Logger logger = LoggerFactory.getLogger(MzDbAsyncReader.class);
	
	private SQLiteQueue queue = null;
	
	/** Some readers with internal entity cache **/
	private DataEncodingAsyncReader _dataEncodingReader = null;
	private SpectrumHeaderAsyncReader _spectrumHeaderReader = null;
	private RunSliceHeaderAsyncReader _runSliceHeaderReader = null;
	//private SQLiteConnection _blockingSQLiteConnection = null;
	
	// Acquire a lock for some methods that could be modify the reader in parallel
	private final Object _blockingConnectionLock = new Object();

	/**
	 * Instantiates a new mzDB reader (primary constructor). Builds a SQLite connection.
	 *
	 * @param dbLocation
	 *            the db location
	 * @param cacheEntities
	 *            the cache entities
	 * @param logConnections
	 *            the log connections
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws SQLiteException
	 *             the SQLite exception
	 */
	public MzDbAsyncReader(File dbLocation, MzDbEntityCache entityCache, boolean logConnections) throws ClassNotFoundException, FileNotFoundException,
			SQLiteException {

		this.entityCache = entityCache;

		if (logConnections == false) {
			java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(java.util.logging.Level.OFF);
		}

		// Check if database exists
		if (!dbLocation.exists()) {
			throw (new FileNotFoundException("can't find the mzDB file at the given path: "+dbLocation));
		}

		this.dbLocation = dbLocation;

		this.queue = new SQLiteQueue(dbLocation);
		
		// Starts the queue
		this.queue.start();
		
		queue.execute(new SQLiteJob<Void>() {
			// this method is called from database thread and passed the connection
			protected Void job(SQLiteConnection connection) throws SQLiteException {
				
				// TODO: put a protected method of AbstractMzDbReader
				connection.openReadonly();

				// SQLite optimization
				connection.exec("PRAGMA synchronous=OFF;");
				connection.exec("PRAGMA journal_mode=OFF;");
				connection.exec("PRAGMA temp_store=2;");
				connection.exec("PRAGMA cache_size=-100000;"); // around 100 Mo
				
				// connection.exec("PRAGMA mmap_size=3000000000"); // note: it may help for batch processing
				
				// Set the mzDbHeader
				MzDbAsyncReader.this.mzDbHeader = new MzDbHeaderReader(connection).getMzDbHeader();
				
				// Set the paramNameGetter
				String pwizMzDbVersion = MzDbReaderQueries.getPwizMzDbVersion(connection);
				MzDbAsyncReader.this._paramNameGetter = (pwizMzDbVersion.compareTo("0.9.1") > 0) ? new MzDBParamName_0_9() : new MzDBParamName_0_8();

				// Set BB sizes
				MzDbAsyncReader.this._setBBSizes(MzDbAsyncReader.this._paramNameGetter);
				
				return null;
			}
		});

		// Instantiates some readers with internal cache (entity cache object)
		this._dataEncodingReader = new DataEncodingAsyncReader(this);
		this._spectrumHeaderReader = new SpectrumHeaderAsyncReader(this, _dataEncodingReader);
		this._runSliceHeaderReader = new RunSliceHeaderAsyncReader(this);
	}

	/**
	 * Instantiates a new mzDB reader (secondary constructor).
	 *
	 * @param dbLocation
	 *            the db location
	 * @param cacheEntities
	 *            the cache entities
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public MzDbAsyncReader(File dbLocation, boolean cacheEntities) throws ClassNotFoundException, FileNotFoundException, SQLiteException {
		this(dbLocation, cacheEntities ? new MzDbEntityCache() : null, false);
	}

	/**
	 * Instantiates a new mzDB reader (secondary constructor).
	 *
	 * @param dbPath
	 *            the db path
	 * @param cacheEntities
	 *            the cache entities
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public MzDbAsyncReader(String dbPath, boolean cacheEntities) throws ClassNotFoundException, FileNotFoundException, SQLiteException {
		this(new File(dbPath), cacheEntities ? new MzDbEntityCache() : null, false);
	}

	/**
	 * close the connection to avoid memory leaks.
	 * @throws InterruptedException 
	 */
	public void close() {
		this.queue.stop(true);
		logger.debug("Waiting for Job queue to finish");
		try {
			this.queue.join();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public DataEncodingAsyncReader getDataEncodingReader() {
		return _dataEncodingReader;
	}
	@Override
	public SpectrumHeaderAsyncReader getSpectrumHeaderReader() {
		return _spectrumHeaderReader;
	}
	@Override
	public RunSliceHeaderAsyncReader getRunSliceHeaderReader() {
		return _runSliceHeaderReader;
	}
	
	/**
	 * Waits for the reader to stop execution of queries.
	 * @throws InterruptedException 
	 */
	public void waitForQueries() throws InterruptedException {
		this.queue.flush();
	}
	
	/**
	 * @return the _blockingMzDbReader
	 * @throws SQLiteException 
	 * @throws FileNotFoundException 
	 * @throws ClassNotFoundException 
	 */
	protected SQLiteConnection createBlockingSQLiteConnection() throws SQLiteException {
		
		synchronized(_blockingConnectionLock) {
			try {
				return new MzDbReader(this.dbLocation, this.entityCache, false).getConnection();
			} catch (ClassNotFoundException | FileNotFoundException | SQLiteException e) {
				logger.error("Can't open blocking MzDbReader at location: "+ this.dbLocation.getAbsolutePath(), e);
				throw new SQLiteException(0, "Error acquiring blocking SQLiteConnection");
			}
		}
	
	}
	
	/*public <T> SQLiteObservableJob<T> observeJobExecution( ISQLiteConnectionFunction<T> sqliteJobWrapper ) {
		return new SQLiteObservableJob<T>( queue, (SQLiteJobWrapper<T>) sqliteJobWrapper);
	}*/
	
	public <T> SQLiteObservableJob<T> observeJobExecution( ISQLiteConnectionFunction<T> sqliteConnFunction ) {
	    SQLiteJobWrapper<T> jobWrapper = new SQLiteJobWrapper<T>() {
			public T job(SQLiteConnection connection) throws Exception {
				return sqliteConnFunction.apply(connection);
			}
		};
		return new SQLiteObservableJob<T>( queue, jobWrapper);
	}

	public Observable<String> getModelVersion() {
		return this.observeJobExecution( connection -> {
			return MzDbReaderQueries.getModelVersion(connection);
		});
	}

	public Observable<String> getPwizMzDbVersion() {
		return this.observeJobExecution( connection -> {
			return MzDbReaderQueries.getPwizMzDbVersion(connection);
		});
	}

	/**
	 * Gets the last time.
	 *
	 * @return float the rt of the last spectrum
	 */
	public Observable<Float> getLastTime() {
		return this.observeJobExecution( connection -> {
			return MzDbReaderQueries.getLastTime(connection);
		});
	}

	/**
	 * Gets the max ms level.
	 *
	 * @return the max ms level
	 */
	public Observable<Integer> getMaxMsLevel() {
		return this.observeJobExecution( connection -> {
			return MzDbReaderQueries.getMaxMsLevel(connection);
		});
	}

	/**
	 * Gets the mz range.
	 *
	 * @param msLevel
	 *            the ms level
	 * @return runSlice min mz and runSlice max mz
	 */
	public Observable<int[]> getMzRange(int msLevel) {
		return this.observeJobExecution( connection -> {
			return MzDbReaderQueries.getMzRange(msLevel, connection);
		});
	}

	/**
	 * Gets the bounding box count.
	 *
	 * @return int, the number of bounding box
	 */
	public Observable<Integer> getBoundingBoxesCount() {
		return this.observeJobExecution( connection -> {
			return MzDbReaderQueries.getBoundingBoxesCount(connection);
		});
	}

	/**
	 * Gets the bounding box count.
	 *
	 * @param runSliceId
	 *            the run slice id
	 * @return the number of bounding box contained in the specified runSliceId
	 */
	public Observable<Integer> getBoundingBoxesCount(int runSliceId) {
		return this.observeJobExecution( connection -> {
			return MzDbReaderQueries.getBoundingBoxesCount(runSliceId, connection);
		});
	}

	/**
	 * Gets the cycle count.
	 *
	 * @return the cycle count
	 */
	public Observable<Integer> getCyclesCount() {
		return this.observeJobExecution( connection -> {
			return MzDbReaderQueries.getCyclesCount(connection);
		});
	}

	/**
	 * Gets the data encoding count.
	 *
	 * @return the data encoding count
	 */
	public Observable<Integer> getDataEncodingsCount() {
		return this.observeJobExecution( connection -> {
			return MzDbReaderQueries.getDataEncodingsCount(connection);
		});
	}

	/**
	 * Gets the spectrum count.
	 *
	 * @return int the number of spectra
	 */
	public Observable<Integer> getSpectraCount() {
		return this.observeJobExecution( connection -> {
			return MzDbReaderQueries.getSpectraCount(connection);
		});
	}

	/**
	 * Gets the spectrum count.
	 *
	 * @return int the number of spectra
	 */
	public Observable<Integer> getSpectraCount(int msLevel) {
		return this.observeJobExecution( connection -> {
			return MzDbReaderQueries.getSpectraCount(msLevel, connection);
		});
	}

	/**
	 * Gets the run slice count.
	 *
	 * @return int the number of runSlice
	 */
	public Observable<Integer> getRunSlicesCount() {
		return this.observeJobExecution( connection -> {
			return MzDbReaderQueries.getRunSlicesCount(connection);
		});
	}

	/**
	 * Gets the table records count.
	 *
	 * @param tableName
	 *            the table name
	 * @return the int
	 */
	public Observable<Integer> getTableRecordsCount(String tableName) {
		return this.observeJobExecution( connection -> {
			return MzDbReaderQueries.getTableRecordsCount(tableName, connection);
		});
	}
	
	
	/**
	 * Gets the data encoding.
	 *
	 * @param id
	 *            the id
	 * @return the data encoding
	 */
	public Observable<DataEncoding> getDataEncoding(int id) {
		return this._dataEncodingReader.getDataEncoding(id);
	}

	/**
	 * Gets the data encoding by spectrum id.
	 *
	 * @return the data encoding by spectrum id
	 */
	public Observable<Map<Long, DataEncoding>> getDataEncodingBySpectrumId() {
		return this._dataEncodingReader.getDataEncodingBySpectrumId();
	}

	/**
	 * Gets the spectrum data encoding.
	 *
	 * @param spectrumId
	 *            the spectrum id
	 * @return the spectrum data encoding
	 */
	public Observable<DataEncoding> getSpectrumDataEncoding(long spectrumId) {
		return this._dataEncodingReader.getSpectrumDataEncoding(spectrumId);
	}

	/**
	 * Gets the run slices.
	 *
	 * @return array of runSlice instance without data associated
	 */
	public Observable<RunSliceHeader[]> getRunSliceHeaders(int msLevel) {
		return this._runSliceHeaderReader.getRunSliceHeaders(msLevel);
	}

	/**
	 * Gets the run slice header by id.
	 *
	 * @param msLevel
	 *            the ms level
	 * @return the run slice header by id
	 */
	public Observable<HashMap<Integer, RunSliceHeader>> getRunSliceHeaderById(int msLevel) {
		return this._runSliceHeaderReader.getRunSliceHeaderById(msLevel);
	}

	/**
	 * Gets the run slice data.
	 *
	 * @param runSliceId
	 *            the run slice id
	 * @return the run slice data
	 */
	public Observable<RunSliceData> getRunSliceData(int runSliceId) {
		return this.observeJobExecution( connection -> {
			return this.getRunSliceData(runSliceId, connection);
		});
	}

	/**
	 * Gets the bounding box data.
	 *
	 * @param bbId
	 *            the bb id
	 * @return the bounding box data
	 */
	public Observable<byte[]> getBoundingBoxData(int bbId) {
		return this.observeJobExecution( connection -> {
			return MzDbReaderQueries.getBoundingBoxData(bbId, connection);
		});
	}

	/**
	 * Gets the bounding box first spectrum index.
	 *
	 * @param spectrumId
	 *            the spectrum id
	 * @return the bounding box first spectrum index
	 */
	public Observable<Long> getBoundingBoxFirstSpectrumId(long spectrumId) {
		return this.observeJobExecution( connection -> {
			return MzDbReaderQueries.getBoundingBoxFirstSpectrumId(spectrumId, connection);
		});
	}

	/**
	 * Gets the bounding box min mz.
	 *
	 * @param bbId
	 *            the bb id
	 * @return the bounding box min mz
	 */
	public Observable<Float> getBoundingBoxMinMz(int bbId) {
		return this.observeJobExecution( connection -> {
			return MzDbReaderQueries.getBoundingBoxMinMz(bbId, connection);
		});
	}

	/**
	 * Gets the bounding box min time.
	 *
	 * @param bbId
	 *            the bb id
	 * @return the bounding box min time
	 */
	public Observable<Float> getBoundingBoxMinTime(int bbId) {
		return this.observeJobExecution( connection -> {
			return MzDbReaderQueries.getBoundingBoxMinTime(bbId, connection);
		});
	}

	/**
	 * Gets the bounding box ms level.
	 *
	 * @param bbId
	 *            the bb id
	 * @return the bounding box ms level
	 */
	public Observable<Integer> getBoundingBoxMsLevel(int bbId) {
		return this.observeJobExecution( connection -> {
			return MzDbReaderQueries.getBoundingBoxMsLevel(bbId, connection);
		});
	}
	

	/**
	 * Gets the MS1 spectrum headers.
	 *
	 * @return the spectrum headers
	 */
	public Observable<SpectrumHeader[]> getMs1SpectrumHeaders() {
		return this._spectrumHeaderReader.getMs1SpectrumHeaders();
	}

	/**
	 * Gets the MS1 spectrum header by id.
	 *
	 * @return the spectrum header by id
	 */
	public Observable<Map<Long, SpectrumHeader>> getMs1SpectrumHeaderById() {
		return this._spectrumHeaderReader.getMs1SpectrumHeaderById();
	}

	/**
	 * Gets the MS2 spectrum headers.
	 *
	 * @return the spectrum headers
	 */
	public Observable<SpectrumHeader[]> getMs2SpectrumHeaders() {
		return this._spectrumHeaderReader.getMs2SpectrumHeaders();
	}

	/**
	 * Gets the MS2 spectrum header by id.
	 *
	 * @return the spectrum header by id
	 */
	public Observable<Map<Long, SpectrumHeader>> getMs2SpectrumHeaderById() {
		return this._spectrumHeaderReader.getMs2SpectrumHeaderById();
	}

	/**
	 * Gets all spectrum headers.
	 *
	 * @return the spectrum headers
	 */
	public Observable<SpectrumHeader[]> getSpectrumHeaders() {
		return this._spectrumHeaderReader.getSpectrumHeaders();
	}

	/**
	 * Gets each spectrum header mapped by its id.
	 *
	 * @return the spectrum header by id
	 */
	public Observable<Map<Long, SpectrumHeader>> getSpectrumHeaderById() {
		return this._spectrumHeaderReader.getSpectrumHeaderById();
	}

	/**
	 * Gets the spectrum header.
	 *
	 * @param id
	 *            the id
	 * @return the spectrum header
	 */
	public Observable<SpectrumHeader> getSpectrumHeader(long id) {
		return this._spectrumHeaderReader.getSpectrumHeader(id);
	}

	/**
	 * Gets the spectrum header for time.
	 *
	 * @param time
	 *            the time
	 * @param msLevel
	 *            the ms level
	 * @return spectrumheader the closest to the time input parameter
	 */
	public Observable<SpectrumHeader> getSpectrumHeaderForTime(float time, int msLevel) {
		return this._spectrumHeaderReader.getSpectrumHeaderForTime(time, msLevel);
	}
	
	/**
	 * Gets the spectrum data.
	 *
	 * @param spectrumId
	 *            the spectrum id
	 * @return the spectrum data
	 */
	public Observable<SpectrumData> getSpectrumData(long spectrumId) {
		return this.observeJobExecution( connection -> {
			return this.getSpectrumData(spectrumId, connection);
		});
	}

	/**
	 * Gets the spectrum.
	 *
	 * @param spectrumId
	 *            the spectrum id
	 * @return the spectrum
	 */
	public Observable<Spectrum> getSpectrum(long spectrumId) {
		return this.observeJobExecution( connection -> {
			return this.getSpectrum(spectrumId, connection);
		});
	}

	/**
	 * Gets the spectrum peaks.
	 *
	 * @param spectrumId
	 *            the spectrum id
	 * @return the spectrum peaks
	 */
	public Observable<Peak[]> getSpectrumPeaks(int spectrumId) {
		return this.observeJobExecution( connection -> {
			return this.getSpectrumPeaks(spectrumId, connection);
		});
	}

	/**
	 * Gets the spectrum slices. Each returned spectrum slice corresponds to a single spectrum.
	 *
	 * @param minmz
	 *            the minMz
	 * @param maxmz
	 *            the maxMz
	 * @param minrt
	 *            the minRt
	 * @param maxrt
	 *            the maxRt
	 * @param msLevel
	 *            the ms level
	 * @return the spectrum slices
	 */
	public Observable<SpectrumSlice[]> getMsSpectrumSlices(double minMz, double maxMz, float minRt, float maxRt) {
		return this.observeJobExecution( connection -> {
			return this.getMsSpectrumSlices(minMz, maxMz, minRt, maxRt, connection);
		});
	}

	// TODO: think about msLevel > 2
	public Observable<SpectrumSlice[]> getMsnSpectrumSlices(double parentMz, double minFragMz, double maxFragMz, float minRt, float maxRt) {
		return this.observeJobExecution( connection -> {
			return getMsnSpectrumSlices(parentMz, minFragMz, maxFragMz, minRt, maxRt, connection);
		});
	}

	/**
	 * Gets the bounding box iterator.
	 *
	 * @param msLevel
	 *            the ms level
	 * @return the bounding box iterator
	 */
	public Observable<BoundingBox> getBoundingBoxStream(int msLevel) {
		
		return Observable.create(subscriber -> {
			
			SQLiteJob<Void> sqliteJob = SQLiteObservableJob.buildSQLiteJob( subscriber, connection -> {
    			
    			// TODO: try to use msn_rtree join instead (may be faster)
    			SQLiteStatement stmt = connection.prepare(
    				"SELECT bounding_box.* FROM bounding_box, spectrum WHERE spectrum.id = bounding_box.first_spectrum_id AND spectrum.ms_level= ?", false
    			);
    			stmt.bind(1, msLevel);
    			
    			BoundingBoxIterator bbIter = new BoundingBoxIterator(
    				this._spectrumHeaderReader,
    				this._dataEncodingReader,
    				connection,
    				stmt,
    				msLevel
    			);
    			
    			SQLiteObservableJob.observeIterator( subscriber, bbIter );
				
				return null;
    		});
    		
    		queue.execute(sqliteJob);
		});
		
	}
	
	public Observable<Spectrum> getSpectrumStream() {
		return Observable.create(subscriber -> {			
			SQLiteJob<Void> sqliteJob = SQLiteObservableJob.buildSQLiteJob( subscriber, connection -> {    			
				
				SQLiteObservableJob.observeIterator( subscriber, new SpectrumIterator(this, connection) );
				
				return null;
    		});
    		
    		queue.execute(sqliteJob);
		});

	}

	/**
	 * Gets the ms spectrum iterator.
	 *
	 * @param msLevel
	 *            the ms level
	 * @return the ms spectrum iterator
	 */
	public Observable<Spectrum> getSpectrumStream(int msLevel) {
		return Observable.create(subscriber -> {			
			SQLiteJob<Void> sqliteJob = SQLiteObservableJob.buildSQLiteJob( subscriber, connection -> {
				
				SQLiteObservableJob.observeIterator( subscriber, new SpectrumIterator(this, connection, msLevel) );
				
				return null;
    		});
    		
    		queue.execute(sqliteJob);
		});

	}

	/**
	 * Gets a RunSlice iterator.
	 *
	 * @return the RunSlice iterator
	 */
	public Observable<RunSlice> getLcMsRunSliceStream() {
		return Observable.create(subscriber -> {
			
			SQLiteJob<Void> sqliteJob = SQLiteObservableJob.buildSQLiteJob( subscriber, connection -> {
    			
				// First pass to load the index
				final SQLiteStatement fakeStmt = connection.prepare("SELECT * FROM bounding_box", false);
				while (fakeStmt.step()) {}
				fakeStmt.dispose();
				
				SQLiteObservableJob.observeIterator( subscriber, new LcMsRunSliceIterator(this, connection) );
				
				return null;
    		});
    		
    		queue.execute(sqliteJob);
		});
	}

	/**
	 * Gets a RunSlice iterator for a given m/z range
	 *
	 * @param minRunSliceMz
	 * @param minRunSliceMz
	 * @return the RunSlice iterator
	 */
	public Observable<RunSlice> getLcMsRunSliceStream(double minRunSliceMz, double maxRunSliceMz) {
		return Observable.create(subscriber -> {
			
			SQLiteJob<Void> sqliteJob = SQLiteObservableJob.buildSQLiteJob( subscriber, connection -> {

				LcMsRunSliceIterator runSliceIter = new LcMsRunSliceIterator(
					this,
					connection,
					minRunSliceMz,
					maxRunSliceMz
				);
    			
				SQLiteObservableJob.observeIterator( subscriber, runSliceIter );
				
				return null;
    		});
    		
    		queue.execute(sqliteJob);
		});
	}

	/**
	 * Gets a DIA data RunSlice iterator
	 *
	 * @param minParentMz
	 * @param maxParentMz
	 * @return the RunSlice iterator
	 */
	public Observable<RunSlice> getLcMsnRunSliceStream(double minParentMz, double maxParentMz) {		
		return Observable.create(subscriber -> {
			
			SQLiteJob<Void> sqliteJob = SQLiteObservableJob.buildSQLiteJob( subscriber, connection -> {

				// First pass to load the index
				final SQLiteStatement fakeStmt = connection.prepare("SELECT * FROM bounding_box", false);
				while (fakeStmt.step()) {
				}
				fakeStmt.dispose();

				LcMsnRunSliceIterator runSliceIter = new LcMsnRunSliceIterator(
					this,
					connection,
					minParentMz,
					maxParentMz
				);
				
				SQLiteObservableJob.observeIterator( subscriber, runSliceIter );
				
				return null;
    		});
    		
    		queue.execute(sqliteJob);
		});
	}

	/**
	 * Gets a DIA data RunSlice iterator for a given m/z range
	 *
	 * @param msLevel
	 * @param minParentMz
	 * @param maxParentMz
	 * @return the RunSlice iterator
	 */
	public Observable<RunSlice> getLcMsnRunSliceStream(double minParentMz, double maxParentMz, double minRunSliceMz, double maxRunSliceMz) {
		return Observable.create(subscriber -> {
			
			SQLiteJob<Void> sqliteJob = SQLiteObservableJob.buildSQLiteJob( subscriber, connection -> {

				// First pass to load the index
				final SQLiteStatement fakeStmt = connection.prepare("SELECT * FROM bounding_box", false);
				while (fakeStmt.step()) {
				}
				fakeStmt.dispose();

				LcMsnRunSliceIterator runSliceIter = new LcMsnRunSliceIterator(
					this,
					connection,
					minParentMz,
					maxParentMz,
					minRunSliceMz,
					maxRunSliceMz
				);
				
				SQLiteObservableJob.observeIterator( subscriber, runSliceIter );
				
				return null;
    		});
    		
    		queue.execute(sqliteJob);
		});
	}

	/**
	 * Lazy loading of the acquisition mode, parameter
	 *
	 * @return
	 */
	public Observable<AcquisitionMode> getAcquisitionMode() {
		return this.observeJobExecution( connection -> {
			return this.getAcquisitionMode(connection);
		});
	}
	
	/**
	 * ImmutablePair can not be wrapped into an array
	 *
	 * @return
	 */
	public Observable<IsolationWindow[]> getDIAIsolationWindows() {
		return this.observeJobExecution( connection -> {
			return this.getDIAIsolationWindows(connection);
		});
	}
	
	/*this._instrumentConfigReader = new InstrumentConfigReader(this.connection);
	this._runReader = new RunReader(this.connection);
	this._sampleReader = new SampleReader(this.connection);
	this._softwareListReader = new SoftwareReader(this.connection);
	this._sourceFileReader = new SourceFileReader(this.connection);*/
	
	public List<InstrumentConfiguration> getInstrumentConfigurations() throws SQLiteException {
		synchronized (_blockingConnectionLock) {
			if (this.instrumentConfigs == null) {
				this.instrumentConfigs = new InstrumentConfigReader(this.createBlockingSQLiteConnection()).getInstrumentConfigList();
				/*// TODO: try to understand why a blocking observable is problematic
				this.instrumentConfigs = this.observeJobExecution( connection -> {				
					return new InstrumentConfigReader(connection).getInstrumentConfigList();
				}).toBlocking().first();*/
			}
		}
		
		return this.instrumentConfigs;
	}

	public List<Run> getRuns() throws SQLiteException {
		synchronized (_blockingConnectionLock) {
			if (this.runs == null) {
				this.runs = new RunReader(this.createBlockingSQLiteConnection()).getRunList();
				/*// TODO: try to understand why a blocking observable is problematic
				this.runs = this.observeJobExecution( connection -> {				
					return new RunReader(connection).getRunList();
				}).toBlocking().first();*/
			}
		}
		
		/*if (this.runs == null) {
			// TODO: synchronize me
			ISQLiteConnectionFunction<List<Run> > runListReaderFunction = new ISQLiteConnectionFunction<List<Run>>() {
				
				@Override
				public List<Run> apply(SQLiteConnection connection) throws Exception {
					System.out.println("apply from SQLLiteConnection => doing RunReader().getRunList()");
					try {
						return new RunReader(connection).getRunList();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}
				}
			};
			SQLiteObservableJob<List<Run>> observeJobExecution = this.observeJobExecution( runListReaderFunction );
			BlockingObservable<List<Run>> blocking = observeJobExecution.toBlocking();
			try {
				this.runs = blocking.getIterator().next();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}*/
		
		return this.runs;
	}

	public List<Sample> getSamples() throws SQLiteException {
		synchronized (_blockingConnectionLock) {
			if (this.samples == null) {
				this.samples = new SampleReader(this.createBlockingSQLiteConnection()).getSampleList();
			}
		}
		
		return this.samples;
		
		// TODO: try to understand why a blocking observable is problematic
		/*if (this.samples == null) {
			// TODO: synchronize me
			this.samples = this.observeJobExecution( connection -> {				
				return new SampleReader(connection).getSampleList();
			}).toBlocking().first();
		}
		return this.samples;*/
	}

	public List<Software> getSoftwareList() throws SQLiteException {
		synchronized (_blockingConnectionLock) {
			if (this.softwareList == null) {
				this.softwareList = new SoftwareReader(this.createBlockingSQLiteConnection()).getSoftwareList();
			}
		}
		
		return this.softwareList;
		
		// TODO: try to understand why a blocking observable is problematic
		/*if (this.softwareList == null) {
			// TODO: synchronize me
			this.softwareList = this.observeJobExecution( connection -> {				
				return new SoftwareReader(connection).getSoftwareList();
			}).toBlocking().first();
		}
		return this.softwareList;*/
	}

	public List<SourceFile> getSourceFiles() throws SQLiteException {
		synchronized (_blockingConnectionLock) {
			if (this.sourceFiles == null) {
				this.sourceFiles = new SourceFileReader(this.createBlockingSQLiteConnection()).getSourceFileList();
			}
		}
		
		return this.sourceFiles;
		
		// TODO: try to understand why a blocking observable is problematic
		/*if (this.sourceFiles == null) {
			// TODO: synchronize me
			this.sourceFiles = this.observeJobExecution( connection -> {				
				return new SourceFileReader(connection).getSourceFileList();
			}).toBlocking().first();
		}
		return this.sourceFiles;*/
	}

	public Observable<Peak[]> getMsXicInMzRange(double minMz, double maxMz, XicMethod method) {
		return this.observeJobExecution( connection -> {
			return this.getMsXicInMzRange(minMz, maxMz, method, connection);
		});
	}

	public Observable<Peak[]> getMsXicInMzRtRanges(double minMz, double maxMz, float minRt, float maxRt, XicMethod method) {
		return this.observeJobExecution( connection -> {
			return this.getMsXicInMzRtRanges(minMz, maxMz, minRt, maxRt, method, connection);
		});		
	}

	public Observable<Peak[]> getMsXic(double mz, double mzTolInDa, float minRt, float maxRt, XicMethod method) {
		return this.observeJobExecution( connection -> {
			return this.getMsXic(mz, mzTolInDa, minRt, maxRt, method, connection);
		});
	}

	public Observable<Peak[]> getMsnXic(double parentMz, double fragmentMz, double fragmentMzTolInDa, float minRt, float maxRt, XicMethod method) {
		return this.observeJobExecution( connection -> {
			return this.getMsnXic(parentMz, fragmentMz, fragmentMzTolInDa, minRt, maxRt, method, connection);
		});		
	}
	
	public Observable<Peak[]> getMsPeaksInMzRtRanges(double minMz, double maxMz, float minRt, float maxRt) {
		return this.observeJobExecution( connection -> {
			return this.getMsPeaksInMzRtRanges(minMz, maxMz, minRt, maxRt, connection);
		});
	}
	
	public Observable<Peak[]> getMsnPeaksInMzRtRanges(double parentMz, double minFragMz, double maxFragMz, float minRt, float maxRt) {
		return this.observeJobExecution( connection -> {
			return this.getMsnPeaksInMzRtRanges(parentMz, minFragMz, maxFragMz, minRt, maxRt, connection);
		});		
	}

}