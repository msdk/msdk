/*
 * Package fr.profi.mzdb.model
 * @author David Bouyssie
 */
package fr.profi.mzdb.model;

import java.nio.ByteOrder;

// TODO: Auto-generated Javadoc
/**
 * The Class DataEncoding. 1 dataEncoding to 1 Spectrum
 * 
 * @author David Bouyssie
 */
public class DataEncoding implements Cloneable {

	/** The id. */
	protected final int id;

	/** The mode. */
	protected final DataMode mode;

	/** The peak encoding. */
	protected PeakEncoding peakEncoding;

	/** The compression. */
	protected final String compression;

	/** The byte order. */
	protected final ByteOrder byteOrder;
	
	/** The byte order. */
	protected final int peakStructSize;

	/**
	 * Instantiates a new data encoding.
	 * 
	 * @param id
	 *            the id
	 * @param mode
	 *            the mode
	 * @param peakEncoding
	 *            the peak encoding
	 * @param compression
	 *            the compression
	 * @param byteOrder
	 *            the byte order
	 */
	public DataEncoding(int id, DataMode mode, PeakEncoding peakEncoding, String compression, ByteOrder byteOrder) {
		super();
		this.id = id;
		this.mode = mode;
		this.peakEncoding = peakEncoding;
		this.compression = compression;
		this.byteOrder = byteOrder;
		
		int peakBytesSize = this.getPeakEncoding().getValue();
		if (this.getMode() == DataMode.FITTED)
			peakBytesSize += 8; // add 2 floats (left hwhm and right hwhm)
		
		this.peakStructSize = peakBytesSize;
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
	 * Gets the mode.
	 * 
	 * @return the mode
	 */
	public DataMode getMode() {
		return mode;
	}

	/**
	 * Gets the peak encoding.
	 * 
	 * @return the peak encoding
	 */
	public PeakEncoding getPeakEncoding() {
		return peakEncoding;
	}

	/**
	 * Sets the peak encoding.
	 * 
	 * @param p
	 *            the new peak encoding
	 */
	public void setPeakEncoding(PeakEncoding p) {
		this.peakEncoding = p;
	}

	/**
	 * Gets the compression.
	 * 
	 * @return the compression
	 */
	public String getCompression() {
		return compression;
	}

	/**
	 * Gets the byte order.
	 * 
	 * @return the byte order
	 */
	public ByteOrder getByteOrder() {
		return byteOrder;
	}
	
	public int getPeakStructSize() {
		return peakStructSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public DataEncoding clone() {
		return new DataEncoding(id, mode, peakEncoding, compression, byteOrder);
	}

}
