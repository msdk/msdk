/*
 * Package fr.profi.mzdb.model
 * @author Marc Dubois
 */
package fr.profi.mzdb.model;

// TODO: Auto-generated Javadoc
/**
 * The Enum DataMode.
 * 
 * @author Marc Dubois
 */
public enum DataMode {

	/** The profile. */
	PROFILE(-1),

	/** The centroid. */
	CENTROID(12),

	/** The fitted. */
	FITTED(20);

	/** The value. */
	private final int value;

	/**
	 * Instantiates a new data mode.
	 * 
	 * @param val
	 *            the val
	 */
	private DataMode(int val) {
		value = val;
	}

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

}
