/*
 * Package fr.profi.mzdb.model
 * @author David Bouyssie
 */
package fr.profi.mzdb.model;

import java.util.Comparator;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.db.model.AbstractTableModel;
import fr.profi.mzdb.db.model.params.Precursor;
import fr.profi.mzdb.db.model.params.ScanList;
import fr.profi.mzdb.io.reader.table.ParamTreeParser;
import fr.profi.mzdb.util.sqlite.SQLiteQuery;

// TODO: Auto-generated Javadoc
/**
 * The Class SpectrumHeader.
 * 
 * @author David Bouyssie
 */
public class SpectrumHeader extends AbstractTableModel implements ILcContext {

	/** The id. */
	protected final long id;

	/** The initial id. */
	protected final int initialId;

	/** The cycle. */
	protected final int cycle;

	/** The time. */
	protected final float time;

	/** The ms level. */
	protected final int msLevel;

	/** The peaks count. */
	protected final int peaksCount;

	/** Is high resolution boolean. */
	protected final boolean isHighResolution;

	/** total ion chromatogram of the spectrum */
	protected final float tic;

	/** The base peak mz. */
	protected final double basePeakMz;

	/** The base peak intensity. */
	protected final float basePeakIntensity;

	/** The precursor mz. */
	protected final double precursorMz; // TODO: set as Double

	/** The precursor charge. */
	protected final int precursorCharge; // TODO: set as Integer

	/** The bounding box first spectrum id. */
	protected final int bbFirstSpectrumId;

	/** The spectrum list. */
	protected ScanList scanList = null;

	/** The precursor: contains selected ions list */
	protected Precursor precursor = null;

	/**
	 * Instantiates a new spectrum header.
	 * 
	 * @param id
	 *            the id
	 * @param initialId
	 *            the initial id
	 * @param cycle
	 *            the cycle
	 * @param time
	 *            the time
	 * @param msLevel
	 *            the ms level
	 * @param peaksCount
	 *            the peaks count
	 * @param dataMode
	 *            the data mode
	 * @param basePeakMz
	 *            the base peak mz
	 * @param basePeakIntensity
	 *            the base peak intensity
	 * @param precursorMz
	 *            the precursor mz
	 * @param precursorCharge
	 *            the precursor charge
	 */
	public SpectrumHeader(
		long id,
		int initialId,
		int cycle,
		float time,
		int msLevel,
		int peaksCount,
		boolean isHighResolution,
		float tic,
		double basePeakMz,
		float basePeakIntensity,
		double precursorMz,
		int precursorCharge,
		int firstBBSpectrumId
	) {
		super(id, null);
		this.id = id;
		this.initialId = initialId;
		this.cycle = cycle;
		this.time = time;
		this.msLevel = msLevel;
		this.peaksCount = peaksCount;
		this.isHighResolution = isHighResolution;
		this.tic = tic;
		this.basePeakMz = basePeakMz;
		this.basePeakIntensity = basePeakIntensity;
		this.precursorMz = precursorMz;
		this.precursorCharge = precursorCharge;
		this.bbFirstSpectrumId = firstBBSpectrumId;
	}

	/**
	 * Instantiates a new spectrum header.
	 * 
	 * @param id
	 *            the id
	 * @param initialId
	 *            the initial id
	 * @param cycle
	 *            the cycle
	 * @param time
	 *            the time
	 * @param msLevel
	 *            the ms level
	 * @param peaksCount
	 *            the peaks count
	 */
	/*
	 * public SpectrumHeader(int id, int initialId, int cycle, float time, int msLevel, int peaksCount) { this(
	 * id, initialId, cycle, time, msLevel, peaksCount, false, 0, 0, 0, 0); }
	 */

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Gets the initial id.
	 * 
	 * @return the initial id
	 */
	public int getInitialId() {
		return initialId;
	}

	/**
	 * Gets the cycle.
	 * 
	 * @return the cycle
	 */
	public int getCycle() {
		return cycle;
	}

	/**
	 * Gets the time.
	 * 
	 * @return the time
	 */
	public float getTime() {
		return time;
	}

	/**
	 * Gets the ms level.
	 * 
	 * @return the ms level
	 */
	public int getMsLevel() {
		return msLevel;
	}

	/**
	 * Gets the peaks count.
	 * 
	 * @return the peaks count
	 */
	public int getPeaksCount() {
		return peaksCount;
	}

	/**
	 * Checks if is high resolution.
	 * 
	 * @return true, if is high resolution
	 */
	public boolean isHighResolution() {
		return isHighResolution;
	}

	/**
	 * Gets the base peak mz.
	 * 
	 * @return the base peak mz
	 */
	public double getBasePeakMz() {
		return basePeakMz;
	}

	/**
	 * Gets the base peak intensity.
	 * 
	 * @return the base peak intensity
	 */
	public float getBasePeakIntensity() {
		return basePeakIntensity;
	}

	/**
	 * Gets the precursor mz.
	 * 
	 * @return the precursor mz
	 */
	public double getPrecursorMz() {
		return precursorMz;
	}

	/**
	 * Gets the precursor charge.
	 * 
	 * @return the precursor charge
	 */
	public int getPrecursorCharge() {
		return precursorCharge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.profi.mzdb.model.ILcContext#getSpectrumId()
	 */
	public long getSpectrumId() {
		return this.id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.profi.mzdb.model.ILcContext#getElutionTime()
	 */
	public float getElutionTime() {
		return this.time;
	}

	public int getBBFirstSpectrumId() {
		return this.bbFirstSpectrumId;
	}

	public ScanList getScanList() {
		return this.scanList;
	}
	
	public void setScanList( ScanList scanList ) {
		this.scanList = scanList;
	}

	public Precursor getPrecursor() {
		return this.precursor;
	}

	public void setPrecursor(Precursor precursor) {
		this.precursor = precursor;
	}

	/** The rt comp. */
	public static Comparator<SpectrumHeader> rtComp = new Comparator<SpectrumHeader>() {
		// @Override
		public int compare(SpectrumHeader o1, SpectrumHeader o2) {
			if (o1.time < o2.time)
				return -1;
			else if (Math.abs(o1.time - o2.time) < 1e-6)
				return 0;
			else
				return 1;
		}
	};

	public float getTIC() {
		return tic;
	}

	public static void loadParamTrees(SpectrumHeader[] spectrumHeaders, SQLiteConnection mzDbConnection) {
		// TODO: load all param_trees in a single SQL query
		for (SpectrumHeader header : spectrumHeaders) {
			if (!header.hasParamTree())
				try {
					header.loadParamTree(mzDbConnection);
				} catch (SQLiteException e) {
					e.printStackTrace();
				}
		}
	}

	@Override
	public void loadParamTree(SQLiteConnection mzDbConnection) throws SQLiteException {
		if (!this.hasParamTree()) {
			String sqlString = "SELECT param_tree FROM spectrum WHERE id = ?";
			String paramTreeAsStr = new SQLiteQuery(mzDbConnection, sqlString).bind(1,
				this.getId()).extractSingleString();
			this.paramTree = ParamTreeParser.parseParamTree(paramTreeAsStr);
		}
	}

	public void loadScanList(SQLiteConnection mzDbConnection) throws SQLiteException {
		if (scanList == null) {
			String sqlString = "SELECT scan_list FROM spectrum WHERE id = ?";
			String scanListAsStr = new SQLiteQuery(mzDbConnection, sqlString).bind(1,
				this.getId()).extractSingleString();
			this.scanList = ParamTreeParser.parseScanList(scanListAsStr);
		}
	}

	public void loadPrecursorList(SQLiteConnection mzDbConnection) throws SQLiteException {
		if (precursor == null) {
			String sqlString = "SELECT precursor_list FROM spectrum WHERE id = ?";
			String precursorListAsStr = new SQLiteQuery(mzDbConnection, sqlString).bind(1,
				this.getId()).extractSingleString();
			this.precursor = ParamTreeParser.parsePrecursor(precursorListAsStr);
		}
	}

}
