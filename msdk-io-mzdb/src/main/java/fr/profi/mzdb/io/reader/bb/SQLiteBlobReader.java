package fr.profi.mzdb.io.reader.bb;

import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

import com.almworks.sqlite4java.SQLiteBlob;
import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.model.DataEncoding;
import fr.profi.mzdb.model.SpectrumData;
import fr.profi.mzdb.model.SpectrumHeader;
import fr.profi.mzdb.model.SpectrumSlice;
import fr.profi.mzdb.util.primitives.BytesUtils;

/**
 * Class for manipulating Blob in SQLite datafile using sqlite4java.SQLiteBlob
 * 
 * @author Marc Dubois
 * @see AbstractBlobReader
 * 
 */
public class SQLiteBlobReader extends AbstractBlobReader {

	/** SQLiteBlob Object */
	protected SQLiteBlob _blob;

	/**
	 * Constructor
	 * @throws StreamCorruptedException 
	 * 
	 * @see SQLiteBlob
	 * @see DataEncoding
	 */
	public SQLiteBlobReader(
		final SQLiteBlob blob,
		final long firstSpectrumId,
		final long lastSpectrumId,
		final Map<Long, SpectrumHeader> spectrumHeaderById,
		final Map<Long, DataEncoding> dataEncodingBySpectrumId
	) throws StreamCorruptedException {
		super(firstSpectrumId, lastSpectrumId, spectrumHeaderById, dataEncodingBySpectrumId);
		
		this._blob = blob;
		this._indexSpectrumSlices((int) (1 + lastSpectrumId - firstSpectrumId) );
	}

	/**
	 * @see IBlobReader#disposeBlob()
	 */
	public void disposeBlob() {
		_blob.dispose();
	}

	/**
	 * @see IBlobReader#getBlobSize()
	 */
	public int getBlobSize() {
		try {
			return _blob.getSize();
		} catch (SQLiteException e) {
			logger.error("can't get SQLiteBlob size",e);
			return 0;
		}
	}

	/**
	 * @see IBlobReader#getSpectraCount()
	 */
	public int getSpectraCount() {
		return _spectraCount;
	}

	/**
	 * @throws StreamCorruptedException 
	 * @see AbstractBlobReader
	 * @see AbstractBlobReader#_buildMapPositions()
	 */
	// TODO: factorize this code with the one from BytesReader
	protected void _indexSpectrumSlices(final int estimatedSpectraCount) throws StreamCorruptedException {

		final int[] spectrumSliceStartPositions = new int[estimatedSpectraCount];
		final int[] peaksCounts = new int[estimatedSpectraCount];
		
		final int size = getBlobSize();
		int spectrumSliceIdx = 0;
		int byteIdx = 0;

		while (byteIdx < size) {

			// Retrieve the spectrum id
			final long spectrumId = (long) _getIntFromBlob(_blob, byteIdx);
			_spectrumSliceStartPositions[spectrumSliceIdx] = byteIdx;
			// spectrumSliceStartPositions.add(byteIdx);

			// Skip the spectrum id bytes
			byteIdx += 4;

			// Retrieve the number of peaks
			final int peaksCount = _getIntFromBlob(_blob, byteIdx);
			_peaksCounts[spectrumSliceIdx] = peaksCount;
			// peaksCounts.add(byteIdx);

			// Skip the peaksCount bytes
			byteIdx += 4;

			// Retrieve the DataEncoding corresponding to this spectrum
			final DataEncoding de = this._dataEncodingBySpectrumId.get(spectrumId);
			this.checkDataEncodingIsNotNull(de, spectrumId);

			byteIdx += peaksCount * de.getPeakStructSize(); // skip nbPeaks * size of one peak

			spectrumSliceIdx++;
		} // statement inside a while loop

		this._spectraCount = spectrumSliceIdx;
		this._spectrumSliceStartPositions = Arrays.copyOf(spectrumSliceStartPositions, _spectraCount);
		this._peaksCounts = Arrays.copyOf(peaksCounts, _spectraCount);

		// this._spectraCount = spectrumSliceStartPositions.size();
		// this._spectrumSliceStartPositions = intListToInts(spectrumSliceStartPositions, _spectraCount);
		// this._peaksCounts = intListToInts(peaksCounts, _spectraCount);
	}

	/**
	 * @see IBlobReader#idOfSpectrumAt(int)
	 */
	public long getSpectrumIdAt(final int idx) {
		this.checkSpectrumIndexRange(idx);
		
		return _getSpectrumIdAt(idx);
	}
	
	private long _getSpectrumIdAt(final int idx) {
		return (long) _getIntFromBlob(_blob, idx);
	}
	
	private int _getIntFromBlob( final SQLiteBlob blob, final int idx ) {
		
		final byte[] byteBuffer = new byte[4];

		try {
			blob.read(idx, byteBuffer, 0, 4);
		} catch (SQLiteException e) {
			logger.error("can't read bytes from the SQLiteBlob",e);
		} // read 4 bytes

		return BytesUtils.bytesToInt(byteBuffer, 0);
	}

	/**
	 * @see IBlobReader#nbPeaksOfSpectrumAt(int)
	 */
	/*public int nbPeaksOfSpectrumAt(int i) {
		if (i > _nbSpectra || i < 1) {
			throw new IndexOutOfBoundsException("nbPeaksOfSpectrumAt: Index out of bound, starting counting at 1");
		}
		return _nbPeaks.get(i);
	}*/
	
	/**
	 * @see IBlobReader#readSpectrumSliceAt(int)
	 */
	// TODO: factorize this code with the one from BytesReader
	public SpectrumSlice readSpectrumSliceAt(final int idx) {
		final long spectrumId = _getSpectrumIdAt(idx);
		final SpectrumData spectrumSliceData = this._readFilteredSpectrumSliceDataAt(idx, spectrumId, -1.0, -1.0);
		final SpectrumHeader sh = _spectrumHeaderById.get( spectrumId );
		
		// Instantiate a new SpectrumSlice
		return new SpectrumSlice(sh, spectrumSliceData);
	}
	
	/**
	 * @see IBlobReader#readSpectrumSliceAt(int)
	 */
	// TODO: factorize this code with the one from BytesReader
	public SpectrumData readSpectrumSliceDataAt(final int idx) {
		return this._readFilteredSpectrumSliceDataAt(idx, _getSpectrumIdAt(idx), -1.0, -1.0 );
	}
	
	public SpectrumData readFilteredSpectrumSliceDataAt(final int idx, final double minMz, final double maxMz) {
		return this._readFilteredSpectrumSliceDataAt(idx, _getSpectrumIdAt(idx), minMz, maxMz );		
	}

	/**
	 * @see IBlobReader#spectrumSliceOfSpectrumAt(int)
	 */
	// TODO: factorize this code with the one from BytesReader
	private SpectrumData _readFilteredSpectrumSliceDataAt(final int idx, final long spectrumId, final double minMz, final double maxMz) {
		
		// Determine peak size in bytes
		final DataEncoding de = this._dataEncodingBySpectrumId.get(spectrumId);

		// Determine peaks bytes length
		final int peaksBytesSize = _peaksCounts[idx] * de.getPeakStructSize();
		
		// Skip spectrum id and peaks count (two integers)
		final int spectrumSliceStartPos = _spectrumSliceStartPositions[idx] + 8;

		final byte[] peaksBytes = new byte[peaksBytesSize];

		try {
			_blob.read(spectrumSliceStartPos, peaksBytes, 0, peaksBytesSize);
		} catch (SQLiteException e) {
			logger.error("can't read bytes from the SQLiteBlob",e);
		}

		// Instantiate a new SpectrumData for the corresponding spectrum slice
		return this.readSpectrumSliceData(
			ByteBuffer.wrap(peaksBytes), spectrumSliceStartPos, peaksBytesSize, de, minMz, maxMz
		);
	}

}
