package fr.profi.mzdb.io.reader.bb;

import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.model.DataEncoding;
import fr.profi.mzdb.model.SpectrumData;
import fr.profi.mzdb.model.SpectrumHeader;
import fr.profi.mzdb.model.SpectrumSlice;

/**
 * @author marco This implementation is mainly used is mzDbReader
 * <p>
 *   Use a ByteBuffer to store the blob's bytes This class extends AbstractBlobReader.
 * </p>
 */
public class BytesReader extends AbstractBlobReader {

	/** the data */
	protected ByteBuffer _bbByteBuffer;

	/** size of the Blob */
	protected int _blobSize;
	protected DataEncoding _firstDataEncondig;

	/**
	 * Constructor
	 * 
	 * @param dataEncodings, DataEncoding object for each spectrum, usually given by a mzDbReaderInstance
	 * @param data, array of byte of the blob
	 * @throws StreamCorruptedException 
	 * @see MzDbReader
	 * @see DataEncoding
	 */
	public BytesReader(
		final byte[] bytes,
		final long firstSpectrumId,
		final long lastSpectrumId,
		final Map<Long, SpectrumHeader> spectrumHeaderById,
		final Map<Long, DataEncoding> dataEncodingBySpectrumId
	) throws StreamCorruptedException {
		super(firstSpectrumId, lastSpectrumId, spectrumHeaderById, dataEncodingBySpectrumId);
		
		this._bbByteBuffer = ByteBuffer.wrap(bytes);
		this._firstDataEncondig = dataEncodingBySpectrumId.values().iterator().next();
		this._bbByteBuffer.order(_firstDataEncondig.getByteOrder());
		this._blobSize = bytes.length;
		
		//logger.debug("BytesReader: blobSize="+ _blobSize);
		
		this._indexSpectrumSlices((int) (1 + lastSpectrumId - firstSpectrumId) );
	}

	/**
	 * Do a first parse of the blob to determine beginning index of each spectrum slice
	 * @throws StreamCorruptedException 
	 * 
	 * @see AbstractBlobReader
	 * @see AbstractBlobReader._buildMpaPositions()
	 */
	protected void _indexSpectrumSlices(final int estimatedSpectraCount) throws StreamCorruptedException {
		
		final int[] spectrumSliceStartPositions = new int[estimatedSpectraCount];
		final int[] peaksCounts = new int[estimatedSpectraCount];
		
		int spectrumSliceIdx = 0;
		int byteIdx = 0;
		
		while (byteIdx < _blobSize) {
			
			// Set the new position to access the byte buffer
			_bbByteBuffer.position(byteIdx);
			
			// Retrieve the spectrum id
			final long spectrumId = (long) _bbByteBuffer.getInt();
			spectrumSliceStartPositions[spectrumSliceIdx] = byteIdx;
			//System.out.println("spectrum id is: "+spectrumId);
			
			// Retrieve the number of peaks
			final int peaksCount = _bbByteBuffer.getInt(); 
			peaksCounts[spectrumSliceIdx] = peaksCount;

			// Retrieve the DataEncoding corresponding to this spectrum
			final DataEncoding de = this._dataEncodingBySpectrumId.get(spectrumId);
			this.checkDataEncodingIsNotNull(de, spectrumId);
			
			// Skip the spectrum id, peaksCount and peaks (peaksCount * size of one peak)
			byteIdx += 8 + (peaksCount * de.getPeakStructSize());
			
			spectrumSliceIdx++;
			
		} // statement inside a while loop
		
		this._spectraCount = spectrumSliceIdx;
		this._spectrumSliceStartPositions = Arrays.copyOf(spectrumSliceStartPositions, _spectraCount);
		this._peaksCounts = Arrays.copyOf(peaksCounts, _spectraCount);
	}

	/**
	 * @see IBlobReader#disposeBlob()
	 */
	public void disposeBlob() {}

	/**
	 * @see IBlobReader#getBlobSize()
	 */
	public int getBlobSize() {
		return _blobSize;
	}

	/**
	 * @see IBlobReader#getSpectraCount()
	 */
	public int getSpectraCount() {
		return _spectraCount;
	}

	/**
	 * @see IBlobReader#idOfSpectrumAt(int)
	 */
	public long getSpectrumIdAt(final int idx) {
		this.checkSpectrumIndexRange(idx);
		return _getSpectrumIdAt(idx);
	}
	
	private long _getSpectrumIdAt(final int idx) {
		return (long) _bbByteBuffer.getInt(_spectrumSliceStartPositions[idx]);
	}

	/**
	 * @see IBlobReader#nbPeaksOfSpectrumAt(int)
	 */
	/*public int nbPeaksOfSpectrumAt(int idx) {
		if (idx < 0 || idx >= _spectraCount) {
			throw new IndexOutOfBoundsException("nbPeaksOfSpectrumAt: index out of bounds (i="+idx+"), index counting starts at 0");
		}
		
		return _peaksCounts[idx];
	}*/

	/**
	 * @see IBlobReader#readSpectrumSliceAt(int)
	 */
	public SpectrumSlice readSpectrumSliceAt(final int idx) {
		long spectrumId = _getSpectrumIdAt(idx);
		SpectrumData spectrumSliceData = this._readFilteredSpectrumSliceDataAt(idx, spectrumId, -1.0, -1.0 );
		SpectrumHeader sh = _spectrumHeaderById.get( spectrumId );
		
		// Instantiate a new SpectrumSlice
		return new SpectrumSlice(sh, spectrumSliceData);
	}
	
	/**
	 * @see IBlobReader#readSpectrumSliceAt(int)
	 */
	public SpectrumData readSpectrumSliceDataAt(final int idx) {
		return this._readFilteredSpectrumSliceDataAt(idx, _getSpectrumIdAt(idx), -1.0, -1.0 );		
	}
	
	public SpectrumData readFilteredSpectrumSliceDataAt(final int idx, final double minMz, final double maxMz) {
		return this._readFilteredSpectrumSliceDataAt(idx, _getSpectrumIdAt(idx), minMz, maxMz );		
	}
	
	private SpectrumData _readFilteredSpectrumSliceDataAt(final int idx, final long spectrumId, final double minMz, final double maxMz) {
		
		// Determine peak size in bytes
		final DataEncoding de = this._dataEncodingBySpectrumId.get(spectrumId);

		// Determine peaks bytes length
		final int peaksBytesSize = _peaksCounts[idx] * de.getPeakStructSize();
		
		// Skip spectrum id and peaks count (two integers)
		final int spectrumSliceStartPos = _spectrumSliceStartPositions[idx] + 8;

		// Instantiate a new SpectrumData for the corresponding spectrum slice
		return this.readSpectrumSliceData(_bbByteBuffer, spectrumSliceStartPos, peaksBytesSize, de, minMz, maxMz);	
	}

}
