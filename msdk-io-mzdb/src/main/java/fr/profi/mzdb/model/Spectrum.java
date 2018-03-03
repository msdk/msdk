package fr.profi.mzdb.model;

// TODO: Auto-generated Javadoc
/**
 * The Class Spectrum.
 * 
 * @author David Bouyssie
 */
public class Spectrum {

	/** The header. */
	protected final SpectrumHeader header;

	/** The spectrum data. */
	protected SpectrumData spectrumData;

	/**
	 * Instantiates a new spectrum.
	 * 
	 * @param header
	 *            the header
	 * @param spectrumData
	 *            the spectrum data
	 */
	public Spectrum(SpectrumHeader header, SpectrumData spectrumData) {
		super();
		this.header = header;
		this.spectrumData = spectrumData;
	}

	/**
	 * Gets the header.
	 * 
	 * @return the header
	 */
	public SpectrumHeader getHeader() {
		return this.header;
	}

	/**
	 * Gets the data.
	 * 
	 * @return the data
	 */
	public SpectrumData getData() {
		return this.spectrumData;
	}

	/**
	 * Gets the peaks.
	 * 
	 * @return the peaks
	 */
	public Peak[] toPeaks() {
		return spectrumData.toPeaks(this.header);
	}
	
	public Peak getNearestPeak(double mz, double mzTolPPM) {
		return this.spectrumData.getNearestPeak(mz, mzTolPPM, header);
	}

}
