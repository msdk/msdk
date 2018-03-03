/*
 * Package fr.profi.mzdb.model
 * @author David Bouyssie
 */
package fr.profi.mzdb.model;

import fr.profi.mzdb.util.misc.AbstractInMemoryIdGen;

// TODO: Auto-generated Javadoc
/**
 * The Class RunSliceData.
 * 
 * @author David Bouyssie
 */
public class RunSliceData extends AbstractInMemoryIdGen {

	/** The id. */
	protected final int id;

	/** The spectrum slice list. */
	protected final SpectrumSlice[] spectrumSliceList;

	/**
	 * Instantiates a new run slice data.
	 * 
	 * @param runSliceId
	 *            the run slice id
	 * @param spectrumSliceList
	 *            the spectrum slice list
	 */
	public RunSliceData(int runSliceId, SpectrumSlice[] spectrumSliceList) {
		super();
		this.id = runSliceId;
		this.spectrumSliceList = spectrumSliceList;
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
	 * Gets the spectrum slice list.
	 * 
	 * @return the spectrum slice list
	 */
	public SpectrumSlice[] getSpectrumSliceList() {
		return spectrumSliceList;
	}

}
