/*
 * Package fr.profi.mzdb.model
 * @author David Bouyssie
 */
package fr.profi.mzdb.model;

// TODO: Auto-generated Javadoc
/**
 * The Class RunSlice.
 * 
 * @author David Bouyssie
 */
public class RunSlice {

	/** The header. */
	protected final RunSliceHeader header;

	/** The data. */
	protected final RunSliceData data;

	/**
	 * Instantiates a new run slice.
	 * 
	 * @param header
	 *            the header
	 * @param spectrumData
	 *            the spectrum data
	 */
	public RunSlice(RunSliceHeader header, RunSliceData spectrumData) {
		super();
		this.header = header;
		this.data = spectrumData;
	}

	/**
	 * Gets the header.
	 * 
	 * @return the header
	 */
	public RunSliceHeader getHeader() {
		return header;
	}

	/**
	 * Gets the data.
	 * 
	 * @return the data
	 */
	public RunSliceData getData() {
		return data;
	}

}
