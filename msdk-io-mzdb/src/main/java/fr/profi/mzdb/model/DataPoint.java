/*
 * Package fr.profi.mzdb.model
 * @author David Bouyssie
 */
package fr.profi.mzdb.model;

// TODO: Auto-generated Javadoc
/**
 * The Class DataPoint.
 * 
 * @author David Bouyssie
 */
public class DataPoint {

	/** The x. */
	protected final double x;

	/** The y. */
	protected final int y;

	/**
	 * Instantiates a new data point.
	 * 
	 * @param xValue
	 *            the x value
	 * @param yValue
	 *            the y value
	 */
	public DataPoint(double xValue, int yValue) {
		super();
		x = xValue;
		y = yValue;
	}

	/**
	 * Gets the x.
	 * 
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * Gets the y.
	 * 
	 * @return the y
	 */
	public int getY() {
		return y;
	}
}
