package fr.profi.mzdb.io.reader.bb;

import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.profi.mzdb.model.DataEncoding;
import fr.profi.mzdb.model.DataMode;
import fr.profi.mzdb.model.PeakEncoding;
import fr.profi.mzdb.model.SpectrumData;
import fr.profi.mzdb.model.SpectrumHeader;
import fr.profi.mzdb.model.SpectrumSlice;

/**
 * Abstract Class containing commons objects and attributes through the implementations
 * 
 * @author marco
 * @author David Bouyssie
 * @see IBlobReader
 */
public abstract class AbstractBlobReader implements IBlobReader {
	
	/*
	 * Size of structure depending on dataMode selected
	 */
	final static int FITTED = 20;
	final static int CENTROID = 12;
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected int _spectraCount; // number of spectrum slices in the blob
	protected int[] _spectrumSliceStartPositions; // list of spectrum slice starting positions in the blob
	protected int[] _peaksCounts; // number of peaks in each spectrum slice of the blob

	protected Map<Long, SpectrumHeader> _spectrumHeaderById;
	protected Map<Long, DataEncoding> _dataEncodingBySpectrumId; // DataEncoding (32-64 bit, centroid/profile)

	/**
	 * Abstract constructor
	 * 
	 * @param spectrumHeaderById SpectrumHeader by spectrum id
	 * @param dataEncById DataEncoding by spectrum id
	 * @see DataEncoding
	 */
	protected AbstractBlobReader(
		final long firstSpectrumId,
		final long lastSpectrumId,
		final Map<Long, SpectrumHeader> spectrumHeaderById,
		final Map<Long, DataEncoding> dataEncodingBySpectrumId
	) {

		if( firstSpectrumId > lastSpectrumId ) {
			throw new IllegalArgumentException("lastSpectrumId must be greater or the same than firstSpectrumId");
		}
		
		this._spectrumHeaderById = spectrumHeaderById;
		this._dataEncodingBySpectrumId = dataEncodingBySpectrumId;
	}
	
	public long[] getAllSpectrumIds() {
		final int spectraCount = this.getSpectraCount();
		final long[] spectrumIds = new long[spectraCount];
		
		for (int i = 0; i < spectraCount; i++) {
			spectrumIds[i] = this.getSpectrumIdAt(i);
		}
		
		return spectrumIds;
	}
	
	/**
	 * Read spectrum slice data by using a ByteBuffer as input
	 * 
	 * @param bbByteBuffer array of bytes containing the SpectrumSlices of interest
	 * @param spectrumSliceStartPos, the starting position
	 * @param peaksBytesLength, length of bytes used by peaks
	 * @param structSize, size of the struct for a given peak
	 * @param de, the corresponding DataEncoding
	 * @param minMz, the minimum m/z value
	 * @param maxMz, the maximum m/z value
	 * @return
	 */
	protected SpectrumData readSpectrumSliceData(
		final ByteBuffer bbByteBuffer,
		final int spectrumSliceStartPos,
		final int peaksBytesLength,
		final DataEncoding de,
		final double minMz,
		final double maxMz
	) {
		
		final DataMode dataMode = de.getMode();
		final PeakEncoding pe = de.getPeakEncoding();
		final int structSize = de.getPeakStructSize();

		int peaksCount = 0;
		int peaksStartIdx = 0;
		
		// If no m/z range is provided
		if( minMz < 0 && maxMz < 0) {
			// Compute the peaks count for the whole spectrum slice
			peaksCount = peaksBytesLength / structSize;
			// Set peaksStartIdx value to spectrumSliceStartPos
			peaksStartIdx = spectrumSliceStartPos;
		// Else determine the peaksStartIdx and peaksCount corresponding to provided m/z filters
		} else {
			
			// Determine the max m/z threshold to use
			double maxMzThreshold = maxMz;
			if( maxMz < 0 ) {
				maxMzThreshold = Double.MAX_VALUE;
			}
			
			for (int i = 0; i < peaksBytesLength; i += structSize) {
				final int peakStartPos = spectrumSliceStartPos + i;
				
				double mz = 0.0;
				switch (pe) {
				case HIGH_RES_PEAK:
					mz = bbByteBuffer.getDouble(peakStartPos);
					break;
				case LOW_RES_PEAK:
					mz = (double) bbByteBuffer.getFloat(peakStartPos);
					break;
				case NO_LOSS_PEAK:
					mz = bbByteBuffer.getDouble(peakStartPos);
					break;
				}
				
				// Check if we are in the desired m/z range
				if( mz >= minMz && mz <= maxMzThreshold) {
					
					// Increment the number of peaks to read
					peaksCount++;
					
					// Determine the peaksStartIdx
					if( mz >= minMz && peaksStartIdx == 0 ) {
						peaksStartIdx = peakStartPos;
					}
				}
			}
		}
		
		// Set the position of the byte buffer
		bbByteBuffer.position(peaksStartIdx);

		// Create new arrays of primitives
		final double[] mzArray = new double[peaksCount];
		final float[] intensityArray = new float[peaksCount];
		final float[] lwhmArray = new float[peaksCount];
		final float[] rwhmArray = new float[peaksCount];
		
		for (int peakIdx = 0; peakIdx < peaksCount; peakIdx++ ) {
			
			switch (pe) {
			case HIGH_RES_PEAK:
				mzArray[peakIdx] = bbByteBuffer.getDouble();
				intensityArray[peakIdx] = bbByteBuffer.getFloat();
				break;
			case LOW_RES_PEAK:
				mzArray[peakIdx] = (double) bbByteBuffer.getFloat();
				intensityArray[peakIdx] = bbByteBuffer.getFloat();
				break;
			case NO_LOSS_PEAK:
				mzArray[peakIdx] = bbByteBuffer.getDouble();
				intensityArray[peakIdx] = (float) bbByteBuffer.getDouble();
				break;
			}
			
			if (dataMode == DataMode.FITTED) {
				lwhmArray[peakIdx] = bbByteBuffer.getFloat();
				rwhmArray[peakIdx] = bbByteBuffer.getFloat();
			}
			
		}
		
		// return the newly formed SpectrumData
		return new SpectrumData(mzArray, intensityArray, lwhmArray, rwhmArray);
	}
	
	protected void checkSpectrumIndexRange(int idx) {
		if (idx < 0 || idx >= this.getSpectraCount() ) {
			throw new IndexOutOfBoundsException("spectrum index out of bounds (idx="+idx+"), index counting starts at 0");
		}
	}
	
	protected void checkDataEncodingIsNotNull(final DataEncoding de, final long spectrumId) throws StreamCorruptedException {
		if (de == null) {
			throw new StreamCorruptedException("Scared that the mzdb file is corrupted, spectrum id is: " + spectrumId);
			//logger.error("Scared that the mzdb file is corrupted, spectrum id is: " + spectrumId);
			//System.exit(0);
		}
	}
	
	/**
	 * @see IBlobReader#readAllSpectrumSlices(int)
	 */
	public SpectrumSlice[] readAllSpectrumSlices(final int runSliceId) {
		
		int spectraCount = this.getSpectraCount();
		SpectrumSlice[] sl = new SpectrumSlice[spectraCount];
		
		for (int i = 0; i < spectraCount; i++) {
			SpectrumSlice s = this.readSpectrumSliceAt(i);
			s.setRunSliceId(runSliceId);
			sl[i] = s;
		}
		
		return sl;
	}
	
	// TODO: temp workaround (remove me when each BB is annotated with the number of spectra it contains)
	/*protected int[] intListToInts(List<Integer> integers, int size) {
		int[] ret = new int[size];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = integers.get(i).intValue();
		}
		return ret;
	}*/

}
