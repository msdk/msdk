/*
 * Package fr.profi.mzdb.model
 * @author David Bouyssie
 */
package fr.profi.mzdb.model;

import fr.profi.mzdb.util.misc.AbstractInMemoryIdGen;

// TODO: Auto-generated Javadoc
/**
 * The Class RunSliceHeader.
 * 
 * @author David Bouyssie
 */
public class RunSliceHeader extends AbstractInMemoryIdGen implements Comparable<RunSliceHeader> {

	/** The id. */
	protected final int id;

	/** The ms level. */
	protected final int msLevel;

	/** The number. */
	protected final int number;

	/** The begin mz. */
	protected final double beginMz;

	/** The end mz. */
	protected final double endMz;

	/** The run id. */
	protected final int runId;

	/**
	 * Instantiates a new run slice header.
	 * 
	 * @param id
	 *            the id
	 * @param msLevel
	 *            the ms level
	 * @param number
	 *            the number
	 * @param beginMz
	 *            the begin mz
	 * @param endMz
	 *            the end mz
	 * @param runId
	 *            the run id
	 */
	public RunSliceHeader(int id, int msLevel, int number, double beginMz, double endMz, int runId) {
		super();
		this.id = id;
		this.msLevel = msLevel;
		this.number = number;
		this.beginMz = beginMz;
		this.endMz = endMz;
		this.runId = runId;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
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
	 * Gets the number.
	 * 
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Gets the begin mz.
	 * 
	 * @return the begin mz
	 */
	public double getBeginMz() {
		return beginMz;
	}

	/**
	 * Gets the end mz.
	 * 
	 * @return the end mz
	 */
	public double getEndMz() {
		return endMz;
	}

	/**
	 * Gets the run id.
	 * 
	 * @return the run id
	 */
	public int getRunId() {
		return runId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	// @Override
	public int compareTo(RunSliceHeader o) {
		if (beginMz < o.beginMz)
			return -1;
		else if (Math.abs(beginMz - o.beginMz) < 1e-6)
			return 0;
		else
			return 1;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RunSliceHeader [id=" + id + ", msLevel=" + msLevel + ", number=" + number + ", beginMz=" + beginMz + ", endMz=" + endMz + ", runId=" + runId
				+ "]";
	}

}
