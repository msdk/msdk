package fr.profi.mzdb.model;

import java.util.HashSet;

import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.io.reader.bb.IBlobReader;
import fr.profi.mzdb.model.DataMode;
import fr.profi.mzdb.model.SpectrumSlice;

/**
 * The Class BoundingBox.
 * 
 * @author Marc Dubois
 */
public class BoundingBox implements Comparable<BoundingBox> {

	/** The _id. */
	private int _id;

	/** The _first spectrum id. */
	protected long _firstSpectrumId;

	protected long _lastSpectrumId;

	/** The _run slice id. */
	protected int _runSliceId;

	/** The _ms level. */
	protected int _msLevel;

	/** The _data mode. */
	protected DataMode _dataMode;

	/** The _reader. */
	protected IBlobReader _reader;

	/**
	 * Instantiates a new bounding box.
	 * 
	 * @param id the BoundingBox id
	 * @param _reader a IBlobReader instance
	 */
	public BoundingBox(int id, IBlobReader _reader) {
		_id = id;
		this._reader = _reader;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return _id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param _id the new id
	 */
	public void setId(int _id) {
		this._id = _id;
	}

	/**
	 * Gets the reader.
	 * 
	 * @return the reader
	 */
	public IBlobReader getReader() {
		return _reader;
	}

	/**
	 * Gets the first spectrum id.
	 * 
	 * @return the first spectrum id
	 */
	public long getFirstSpectrumId() {
		return _firstSpectrumId;
	}

	/**
	 * Sets the first spectrum id.
	 * 
	 * @param spectrumid the new first spectrum id
	 */
	public void setFirstSpectrumId(long spectrumId) {
		_firstSpectrumId = spectrumId;
	}

	public long getLastSpectrumId() {
		return _lastSpectrumId;
	}

	public void setLastSpectrumId(long spectrumId) {
		_lastSpectrumId = spectrumId;
	}

	/**
	 * Gets the run slice id.
	 * 
	 * @return the run slice id
	 */
	public int getRunSliceId() {
		return _runSliceId;
	}

	/**
	 * Sets the run slice id.
	 * 
	 * @param _runSliceId the new run slice id
	 */
	public void setRunSliceId(int _runSliceId) {
		this._runSliceId = _runSliceId;
	}

	/**
	 * Spectra count.
	 * 
	 * @return the int
	 */
	public int getSpectraCount() {
		return _reader.getSpectraCount();
	}

	/**
	 * Min id.
	 * 
	 * @return the float
	 */
	public float getMinSpectrumId() throws SQLiteException {
		return this._reader.getSpectrumIdAt(0);
	}

	/**
	 * Max id.
	 * 
	 * @return the float
	 */
	public float getMaxSpectrumId() {
		return this._reader.getSpectrumIdAt(this.getSpectraCount() - 1);
	}

	/**
	 * As spectrum slices array.
	 * 
	 * @param firstSpectrumID the first spectrum id
	 * @param runSliceID the run slice id
	 * @return the spectrum slice[]
	 */
	public SpectrumSlice[] toSpectrumSlices() {
		
		SpectrumSlice[] spectrumSliceArray = _reader.readAllSpectrumSlices(this._runSliceId);
		
		// FIXME: remove this workaround when raw2mzDB has been fixed
		// raw2mzDB is inserting multiple empty spectrum slices pointing to the same spectrum id
		// Workaround added the 22/01/2015 by DBO
		HashSet<Long> spectrumIdSet = new HashSet<Long>();
		
		for (SpectrumSlice spectrumSlice : spectrumSliceArray) {

			long spectrumId = spectrumSlice.getHeader().getId();
			
			if (spectrumIdSet.contains(spectrumId) == true) {
				throw new IllegalArgumentException("duplicated spectrum id is: "+spectrumId);
			}
			
			spectrumIdSet.add(spectrumId);
		}

		return spectrumSliceArray;
	}
	
	/**
	 * Spectrum slice of spectrum at.
	 * 
	 * @param idx the idx
	 * @return the spectrum slice
	 */
	/*
	 * public SpectrumSlice spectrumSliceOfSpectrumAt(int idx) { return _reader.spectrumSliceOfSpectrumAt(idx); }
	 */

	/*
	 * public ByteBuffer getByteBuffer() { return this._reader.getByteBuffer(); }
	 */

	/*
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(BoundingBox bb) {

		try {
			if (this.getMinSpectrumId() < bb.getMinSpectrumId()) {
				return -1;
			} else if (Math.abs(this.getMinSpectrumId() - bb.getMinSpectrumId()) == 0) {
				return 0;
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		}

		return 1;
	}

}
