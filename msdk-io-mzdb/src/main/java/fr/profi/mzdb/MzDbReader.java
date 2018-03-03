package fr.profi.mzdb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

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

/**
 * Allows to manipulates data contained in the mzDB file.
 *
 * @author David
 */
public class MzDbReader extends AbstractMzDbReader {

	final Logger logger = LoggerFactory.getLogger(MzDbReader.class);
	
	private SQLiteConnection connection = null;
	
	/** Some readers with internal entity cache **/
	private DataEncodingReader _dataEncodingReader = null;
	private SpectrumHeaderReader _spectrumHeaderReader = null;
	private RunSliceHeaderReader _runSliceHeaderReader = null;
	
	/** Some readers without internal entity cache **/
	private MzDbHeaderReader _mzDbHeaderReader = null;
	private InstrumentConfigReader _instrumentConfigReader = null;
	private RunReader _runReader = null;
	private SampleReader _sampleReader = null;
	private SoftwareReader _softwareListReader = null;
	private SourceFileReader _sourceFileReader = null;

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
	public MzDbReader(File dbLocation, MzDbEntityCache entityCache, boolean logConnections) throws ClassNotFoundException, FileNotFoundException,
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

		this.connection = new SQLiteConnection(dbLocation);
		this.connection.openReadonly();

		// SQLite optimization
		this.connection.exec("PRAGMA synchronous=OFF;");
		this.connection.exec("PRAGMA journal_mode=OFF;");
		this.connection.exec("PRAGMA temp_store=2;");
		this.connection.exec("PRAGMA cache_size=-100000;"); // around 100 Mo
		this.connection.exec("PRAGMA mmap_size=2147418112;"); // around 2 GB of mapped-memory (it may help for batch processing)

		// Create a temporary table containing a copy of the sepctrum table
		// System.out.println("before CREATE TEMP TABLE");
		// connection.exec("CREATE TEMP TABLE tmp_spectrum AS SELECT * FROM spectrum");
		// System.out.println("after CREATE TEMP TABLE");

		// Instantiates some readers without internal cache
		this._mzDbHeaderReader = new MzDbHeaderReader(this.connection);
		this._instrumentConfigReader = new InstrumentConfigReader(this.connection);
		this._runReader = new RunReader(this.connection);
		this._sampleReader = new SampleReader(this.connection);
		this._softwareListReader = new SoftwareReader(this.connection);
		this._sourceFileReader = new SourceFileReader(this.connection);

		// Instantiates some readers with internal cache (entity cache object)
		this._dataEncodingReader = new DataEncodingReader(this);
		this._spectrumHeaderReader = new SpectrumHeaderReader(this, _dataEncodingReader);
		this._runSliceHeaderReader = new RunSliceHeaderReader(this);

		// Set the mzDvHeader
		this.mzDbHeader = this._mzDbHeaderReader.getMzDbHeader();

		// Set the paramNameGetter
		String pwizMzDbVersion = MzDbReaderQueries.getPwizMzDbVersion(this.connection);
		this._paramNameGetter = (pwizMzDbVersion.compareTo("0.9.1") > 0) ? new MzDBParamName_0_9() : new MzDBParamName_0_8();

		// Set BB sizes
		this._setBBSizes(this._paramNameGetter);
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
	public MzDbReader(File dbLocation, boolean cacheEntities) throws ClassNotFoundException, FileNotFoundException, SQLiteException {
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
	public MzDbReader(String dbPath, boolean cacheEntities) throws ClassNotFoundException, FileNotFoundException, SQLiteException {
		this(new File(dbPath), cacheEntities ? new MzDbEntityCache() : null, false);
	}

	/**
	 * Gets the connection.
	 *
	 * @return the connection
	 */
	public SQLiteConnection getConnection() {
		return this.connection;
	}

	/**
	 * close the connection to avoid memory leaks.
	 */
	public void close() {
		this.connection.dispose();
	}
	
	@Override
	public DataEncodingReader getDataEncodingReader() {
		return _dataEncodingReader;
	}
	@Override
	public SpectrumHeaderReader getSpectrumHeaderReader() {
		return _spectrumHeaderReader;
	}
	@Override
	public RunSliceHeaderReader getRunSliceHeaderReader() {
		return _runSliceHeaderReader;
	}
	
	/**
	 *
	 * @return
	 * @throws SQLiteException
	 */
	public String getModelVersion() throws SQLiteException {
		return MzDbReaderQueries.getModelVersion(connection);
	}

	public String getPwizMzDbVersion() throws SQLiteException {
		return MzDbReaderQueries.getPwizMzDbVersion(connection);
	}
	

	/**
	 * Gets the last time.
	 *
	 * @return float the rt of the last spectrum
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public float getLastTime() throws SQLiteException {
		return MzDbReaderQueries.getLastTime(connection);
	}

	/**
	 * Gets the max ms level.
	 *
	 * @return the max ms level
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public int getMaxMsLevel() throws SQLiteException {
		return MzDbReaderQueries.getMaxMsLevel(connection);
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
	public int[] getMzRange(int msLevel) throws SQLiteException {
		return MzDbReaderQueries.getMzRange(msLevel, connection);
	}

	/**
	 * Gets the bounding box count.
	 *
	 * @return int, the number of bounding box
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public int getBoundingBoxesCount() throws SQLiteException {
		return MzDbReaderQueries.getBoundingBoxesCount(connection);
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
	public int getBoundingBoxesCount(int runSliceId) throws SQLiteException {
		return MzDbReaderQueries.getBoundingBoxesCount(runSliceId, connection);
	}

	/**
	 * Gets the cycle count.
	 *
	 * @return the cycle count
	 * @throws SQLiteException
	 */
	public int getCyclesCount() throws SQLiteException {
		return MzDbReaderQueries.getCyclesCount(connection);
	}

	/**
	 * Gets the data encoding count.
	 *
	 * @return the data encoding count
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public int getDataEncodingsCount() throws SQLiteException {
		return MzDbReaderQueries.getDataEncodingsCount(connection);
	}

	/**
	 * Gets the spectrum count.
	 *
	 * @return int the number of spectra
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public int getSpectraCount() throws SQLiteException {
		return MzDbReaderQueries.getSpectraCount(connection);
	}

	/**
	 * Gets the spectrum count.
	 *
	 * @return int the number of spectra
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public int getSpectraCount(int msLevel) throws SQLiteException {
		return MzDbReaderQueries.getSpectraCount(msLevel, connection);
	}

	/**
	 * Gets the run slice count.
	 *
	 * @return int the number of runSlice
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public int getRunSlicesCount() throws SQLiteException {
		return MzDbReaderQueries.getRunSlicesCount(connection);
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
	public int getTableRecordsCount(String tableName) throws SQLiteException {
		return MzDbReaderQueries.getTableRecordsCount(tableName, connection);
	}
	
	/**
	 * Gets the data encoding.
	 *
	 * @param id
	 *            the id
	 * @return the data encoding
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public DataEncoding getDataEncoding(int id) throws SQLiteException {
		return this._dataEncodingReader.getDataEncoding(id);
	}

	/**
	 * Gets the data encoding by spectrum id.
	 *
	 * @return the data encoding by spectrum id
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public Map<Long, DataEncoding> getDataEncodingBySpectrumId() throws SQLiteException {
		return this._dataEncodingReader.getDataEncodingBySpectrumId();
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
		return this._dataEncodingReader.getSpectrumDataEncoding(spectrumId);
	}

	/**
	 * Gets the run slices.
	 *
	 * @return array of runSlice instance without data associated
	 * @throws SQLiteException
	 *             the SQLite exception
	 */
	public RunSliceHeader[] getRunSliceHeaders(int msLevel) throws SQLiteException {
		return this._runSliceHeaderReader.getRunSliceHeaders(msLevel);
	}

	/**
	 * Gets the run slice header by id.
	 *
	 * @param msLevel
	 *            the ms level
	 * @return the run slice header by id
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public HashMap<Integer, RunSliceHeader> getRunSliceHeaderById(int msLevel) throws SQLiteException {
		return this._runSliceHeaderReader.getRunSliceHeaderById(msLevel);
	}

	/**
	 * Gets the run slice data.
	 *
	 * @param runSliceId
	 *            the run slice id
	 * @return the run slice data
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public RunSliceData getRunSliceData(int runSliceId) throws SQLiteException, StreamCorruptedException {
		return this.getRunSliceData(runSliceId, connection);
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
	public byte[] getBoundingBoxData(int bbId) throws SQLiteException {
		return MzDbReaderQueries.getBoundingBoxData(bbId, connection);
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
	public long getBoundingBoxFirstSpectrumId(long spectrumId) throws SQLiteException {
		return MzDbReaderQueries.getBoundingBoxFirstSpectrumId(spectrumId, connection);
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
	public float getBoundingBoxMinMz(int bbId) throws SQLiteException {
		return MzDbReaderQueries.getBoundingBoxMinMz(bbId, connection);
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
	public float getBoundingBoxMinTime(int bbId) throws SQLiteException {
		return MzDbReaderQueries.getBoundingBoxMinTime(bbId, connection);
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
	public int getBoundingBoxMsLevel(int bbId) throws SQLiteException {
		return MzDbReaderQueries.getBoundingBoxMsLevel(bbId, connection);
	}
	

	/**
	 * Gets the MS1 spectrum headers.
	 *
	 * @return the spectrum headers
	 * @throws SQLiteException
	 *             the SQLiteException
	 */
	public SpectrumHeader[] getMs1SpectrumHeaders() throws SQLiteException {
		return this._spectrumHeaderReader.getMs1SpectrumHeaders();
	}

	/**
	 * Gets the MS1 spectrum header by id.
	 *
	 * @return the spectrum header by id
	 * @throws SQLiteException
	 *             the SQLiteException
	 */
	public Map<Long, SpectrumHeader> getMs1SpectrumHeaderById() throws SQLiteException {
		return this._spectrumHeaderReader.getMs1SpectrumHeaderById();
	}

	/**
	 * Gets the MS2 spectrum headers.
	 *
	 * @return the spectrum headers
	 * @throws SQLiteException
	 *             the SQLiteException
	 */
	public SpectrumHeader[] getMs2SpectrumHeaders() throws SQLiteException {
		return this._spectrumHeaderReader.getMs2SpectrumHeaders();
	}

	/**
	 * Gets the MS2 spectrum header by id.
	 *
	 * @return the spectrum header by id
	 * @throws SQLiteException
	 *             the SQLiteException
	 */
	public Map<Long, SpectrumHeader> getMs2SpectrumHeaderById() throws SQLiteException {
		return this._spectrumHeaderReader.getMs2SpectrumHeaderById();
	}

	/**
	 * Gets all spectrum headers.
	 *
	 * @return the spectrum headers
	 * @throws SQLiteException
	 *             the SQLiteException
	 */
	public SpectrumHeader[] getSpectrumHeaders() throws SQLiteException {
		return this._spectrumHeaderReader.getSpectrumHeaders();
	}

	/**
	 * Gets each spectrum header mapped by its id.
	 *
	 * @return the spectrum header by id
	 * @throws SQLiteException
	 *             the SQLiteException
	 */
	public Map<Long, SpectrumHeader> getSpectrumHeaderById() throws SQLiteException {
		return this._spectrumHeaderReader.getSpectrumHeaderById();
	}

	/**
	 * Gets the spectrum header.
	 *
	 * @param id
	 *            the id
	 * @return the spectrum header
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public SpectrumHeader getSpectrumHeader(long id) throws SQLiteException {
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
	 * @throws Exception
	 */
	public SpectrumHeader getSpectrumHeaderForTime(float time, int msLevel) throws Exception {
		return this._spectrumHeaderReader.getSpectrumHeaderForTime(time, msLevel);
	}
	
	/**
	 * Gets the spectrum data.
	 *
	 * @param spectrumId
	 *            the spectrum id
	 * @return the spectrum data
	 * @throws SQLiteException
	 *             the sQ lite exception
	 * @throws StreamCorruptedException
	 */
	public SpectrumData getSpectrumData(long spectrumId) throws SQLiteException, StreamCorruptedException {
		return this.getSpectrumData(spectrumId, connection);
	}

	/**
	 * Gets the spectrum.
	 *
	 * @param spectrumId
	 *            the spectrum id
	 * @return the spectrum
	 * @throws SQLiteException
	 *             the SQlite exception
	 * @throws StreamCorruptedException
	 */
	public Spectrum getSpectrum(long spectrumId) throws SQLiteException, StreamCorruptedException {
		return this.getSpectrum(spectrumId, connection);
	}

	/**
	 * Gets the spectrum peaks.
	 *
	 * @param spectrumId
	 *            the spectrum id
	 * @return the spectrum peaks
	 * @throws SQLiteException
	 *             the sQ lite exception
	 * @throws StreamCorruptedException
	 */
	public Peak[] getSpectrumPeaks(int spectrumId) throws SQLiteException, StreamCorruptedException {
		return this.getSpectrumPeaks(spectrumId, connection);
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
	 * @throws SQLiteException
	 *             the sQ lite exception
	 * @throws StreamCorruptedException
	 */
	public SpectrumSlice[] getMsSpectrumSlices(double minMz, double maxMz, float minRt, float maxRt) throws SQLiteException, StreamCorruptedException {
		return this.getMsSpectrumSlices(minMz, maxMz, minRt, maxRt, connection);
	}

	// TODO: think about msLevel > 2
	public SpectrumSlice[] getMsnSpectrumSlices(double parentMz, double minFragMz, double maxFragMz, float minRt, float maxRt) throws SQLiteException, StreamCorruptedException {
		return getMsnSpectrumSlices(parentMz, minFragMz, maxFragMz, minRt, maxRt, connection);
	}

	public Iterator<BoundingBox> getBoundingBoxIterator(int msLevel) throws SQLiteException, StreamCorruptedException {
		// TODO: try to use msn_rtree join instead (may be faster)
		SQLiteStatement stmt = connection.prepare(
			"SELECT bounding_box.* FROM bounding_box, spectrum WHERE spectrum.id = bounding_box.first_spectrum_id AND spectrum.ms_level= ?", false);
		stmt.bind(1, msLevel);

		return new BoundingBoxIterator(
			this._spectrumHeaderReader,
			this._dataEncodingReader,
			connection,
			stmt,
			msLevel
		);
	}
	
	public Iterator<Spectrum> getSpectrumIterator() throws SQLiteException, StreamCorruptedException {
		return new SpectrumIterator(this, connection);
	}

	/**
	 * Gets the ms spectrum iterator.
	 *
	 * @param msLevel
	 *            the ms level
	 * @return the ms spectrum iterator
	 * @throws SQLiteException
	 *             the sQ lite exception
	 * @throws StreamCorruptedException
	 */
	public Iterator<Spectrum> getSpectrumIterator(int msLevel) throws SQLiteException, StreamCorruptedException {
		return new SpectrumIterator(this, connection, msLevel);
	}

	/**
	 * Gets a RunSlice iterator.
	 *
	 * @return the RunSlice iterator
	 * @throws SQLiteException
	 * @throws StreamCorruptedException
	 */
	public Iterator<RunSlice> getLcMsRunSliceIterator() throws SQLiteException, StreamCorruptedException {

		// First pass to load the index
		/*final SQLiteStatement fakeStmt = this.connection.prepare("SELECT data FROM bounding_box", false);
		while (fakeStmt.step()) {
		}
		fakeStmt.dispose();*/
		readWholeFile(this.dbLocation);

		return new LcMsRunSliceIterator(this, connection);
	}
	
	private static void readWholeFile(File file) {

		try {
			java.io.FileInputStream fis = new java.io.FileInputStream(file);
			byte[] b = new byte[1024 * 1024];

			while (fis.available() != 0) {
				fis.read(b);
			}

			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Gets a RunSlice iterator for a given m/z range
	 *
	 * @param minRunSliceMz
	 * @param minRunSliceMz
	 * @return the RunSlice iterator
	 * @throws SQLiteException
	 * @throws StreamCorruptedException
	 */
	public Iterator<RunSlice> getLcMsRunSliceIterator(double minRunSliceMz, double maxRunSliceMz) throws SQLiteException, StreamCorruptedException {
		return new LcMsRunSliceIterator(this, connection, minRunSliceMz, maxRunSliceMz);
	}

	/**
	 * Gets a DIA data RunSlice iterator
	 *
	 * @param minParentMz
	 * @param maxParentMz
	 * @return the RunSlice iterator
	 * @throws SQLiteException
	 * @throws StreamCorruptedException
	 */
	public Iterator<RunSlice> getLcMsnRunSliceIterator(double minParentMz, double maxParentMz) throws SQLiteException, StreamCorruptedException {

		// First pass to load the index
		final SQLiteStatement fakeStmt = this.connection.prepare("SELECT data FROM bounding_box", false);
		while (fakeStmt.step()) {
		}
		fakeStmt.dispose();

		return new LcMsnRunSliceIterator(this, connection, minParentMz, maxParentMz);
	}

	/**
	 * Gets a DIA data RunSlice iterator for a given m/z range
	 *
	 * @param msLevel
	 * @param minParentMz
	 * @param maxParentMz
	 * @return the RunSlice iterator
	 * @throws SQLiteException
	 * @throws StreamCorruptedException
	 */
	public Iterator<RunSlice> getLcMsnRunSliceIterator(double minParentMz, double maxParentMz, double minRunSliceMz, double maxRunSliceMz)
			throws SQLiteException, StreamCorruptedException {
		return new LcMsnRunSliceIterator(this, connection, minParentMz, maxParentMz, minRunSliceMz, maxRunSliceMz);
	}

	/**
	 * Lazy loading of the acquisition mode, parameter
	 *
	 * @return
	 * @throws SQLiteException
	 */
	public AcquisitionMode getAcquisitionMode() throws SQLiteException {
		return this.getAcquisitionMode(connection);
	}

	/**
	 * Get the DIA IsolationWindows
	 * 
	 * @return
	 * @throws SQLiteException
	 */
	public IsolationWindow[] getDIAIsolationWindows() throws SQLiteException {
		return this.getDIAIsolationWindows(connection);
	}
	
	public List<InstrumentConfiguration> getInstrumentConfigurations() throws SQLiteException {
		if (this.instrumentConfigs == null) {
			this.instrumentConfigs = this._instrumentConfigReader.getInstrumentConfigList();
		}
		return this.instrumentConfigs;
	}

	public List<Run> getRuns() throws SQLiteException {
		if (this.runs == null) {
			this.runs = this._runReader.getRunList();
		}
		return this.runs;
	}

	public List<Sample> getSamples() throws SQLiteException {
		if (this.samples == null) {
			this.samples = this._sampleReader.getSampleList();
		}
		return this.samples;
	}

	public List<Software> getSoftwareList() throws SQLiteException {
		if (this.softwareList == null) {
			this.softwareList = this._softwareListReader.getSoftwareList();
		}
		return this.softwareList;
	}

	public List<SourceFile> getSourceFiles() throws SQLiteException {
		if (this.sourceFiles == null) {
			this.sourceFiles = this._sourceFileReader.getSourceFileList();
		}
		return this.sourceFiles;
	}

	/**
	 * Gets the xic.
	 *
	 * @param minMz
	 *            the min mz
	 * @param maxMz
	 *            the max mz
	 * @param msLevel
	 *            the ms level
	 * @return the xic
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public Peak[] getMsXicInMzRange(double minMz, double maxMz, XicMethod method) throws SQLiteException, StreamCorruptedException {
		return this.getMsXicInMzRange(minMz, maxMz, method, connection);
	}

	public Peak[] getMsXicInMzRtRanges(double minMz, double maxMz, float minRt, float maxRt, XicMethod method) throws SQLiteException, StreamCorruptedException {
		return this.getMsXicInMzRtRanges(minMz, maxMz, minRt, maxRt, method, connection);
	}

	public Peak[] getMsXic(double mz, double mzTolInDa, float minRt, float maxRt, int msLevel, XicMethod method) throws SQLiteException, StreamCorruptedException {
		return this.getMsXic(mz, mzTolInDa, minRt, maxRt, method, connection);
	}

	public Peak[] getMsnXIC(double parentMz, double fragmentMz, double fragmentMzTolInDa, float minRt, float maxRt, XicMethod method) throws SQLiteException, StreamCorruptedException {
		return this.getMsnXic(parentMz, fragmentMz, fragmentMzTolInDa, minRt, maxRt, method, connection);
	}
	
	public Peak[] getMsPeaksInMzRtRanges(double minMz, double maxMz, float minRt, float maxRt) throws SQLiteException, StreamCorruptedException {
		return this.getMsPeaksInMzRtRanges(minMz, maxMz, minRt, maxRt, connection);
	}
	
	public Peak[] getMsnPeaksInMzRtRanges(double parentMz, double minFragMz, double maxFragMz, float minRt, float maxRt) throws SQLiteException, StreamCorruptedException {
		return this.getMsnPeaksInMzRtRanges(parentMz, minFragMz, maxFragMz, minRt, maxRt, connection);
	}

}