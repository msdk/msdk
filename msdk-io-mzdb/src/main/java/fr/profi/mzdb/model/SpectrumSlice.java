/*
 * Package fr.profi.mzdb.model
 * @author David Bouyssie
 */
package fr.profi.mzdb.model;

// TODO: Auto-generated Javadoc
/**
 * The Class SpectrumSlice.
 * 
 * @author David Bouyssie
 */
public class SpectrumSlice extends Spectrum {

	// SpectrumData {

	/** The run slice id. */
	protected int runSliceId;

	/**
	 * Instantiates a new spectrum slice.
	 * 
	 * @param spectrumId
	 *            the spectrum id
	 * @param runSliceId
	 *            the run slice id
	 * @param mzList
	 *            the mz list
	 * @param intensityList
	 *            the intensity list
	 */
	public SpectrumSlice(SpectrumHeader header, SpectrumData spectrumData) {
		super(header, spectrumData);
		
    if( header == null ) {
      throw new IllegalArgumentException("a SpectrumHeader must be provided");
    }
    
    if( spectrumData == null ) {
      throw new IllegalArgumentException("a SpectrumData must be provided");
    }
	}

	/**
	 * Gets the spectrum id.
	 * 
	 * @return the spectrum id
	 */
	public long getSpectrumId() {
		return header.getId();
	}

	/**
	 * Gets the run slice id.
	 * 
	 * @return the run slice id
	 */
	public int getRunSliceId() {
		return runSliceId;
	}

	/**
	 * Sets the run slice id.
	 * 
	 * @param runSliceId
	 *            the new run slice id
	 */
	public void setRunSliceId(int runSliceId) {
		this.runSliceId = runSliceId;
	}
}
