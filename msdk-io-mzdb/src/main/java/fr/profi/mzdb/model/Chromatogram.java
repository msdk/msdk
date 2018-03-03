/*
 * Package fr.profi.mzdb.model
 * @author David Bouyssie
 */
package fr.profi.mzdb.model;

// TODO: Auto-generated Javadoc
/**
 * The Class Chromatogram.
 * 
 * @author David Bouyssie
 */
public class Chromatogram {

	/** The data points. */
	protected DataPoint[] dataPoints;

	/**
	 * Instantiates a new chromatogram.
	 * 
	 * @param dataPoints
	 *            the data points
	 */
	public Chromatogram(DataPoint[] dataPoints) {
		super();
		this.dataPoints = dataPoints;
	}

	/**
	 * Gets the data points.
	 * 
	 * @return the data points
	 */
	public DataPoint[] getDataPoints() {
		return dataPoints;
	}

}
