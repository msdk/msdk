package fr.profi.mzdb.io.reader.bb;

import fr.profi.mzdb.model.SpectrumData;
import fr.profi.mzdb.model.SpectrumSlice;

//import fr.profi.mzdb.io.reader.bb.AbstractBlobReader.BlobData;
//import fr.profi.mzdb.model.DataEncoding;

/**
 * Interface for reading SQLite Blob
 * 
 * @author marco
 * @author David Bouyssie
 * 
 */
public interface IBlobReader {
	
	/**
	 * Cleanup the blob if necessary
	 */
	void disposeBlob();
	
	/**
	 * @return the spectra count in the blob.
	 */
	int getSpectraCount();

	/**
	 * 
	 * @param i index of spectrum starting at 1
	 * @return long, the ID of the spectrum at the specified index in the blob
	 */
	long getSpectrumIdAt(int i);
	
	long[] getAllSpectrumIds();

	/**
	 * 
	 * @param i index of the wanted spectrum
	 * @return int, the number of peaks of the spectrum specified by the index
	 */
	//int nbPeaksOfSpectrumAt(int i);

	/**
	 * 
	 * @param runSliceId needed to correctly annotate the SpectrumSlice
	 * @return array of spectrumSlice representing the bounding box
	 */
	SpectrumSlice[] readAllSpectrumSlices(int runSliceId);

	/**
	 * 
	 * @param idx
	 *            index of specified spectrum
	 * @return SpectrumSlice of the specified spectrum
	 */
	SpectrumSlice readSpectrumSliceAt(int idx);
	
	/**
	 * 
	 * @param idx
	 *            index of specified spectrum
	 * @return SpectrumData of the specified spectrum
	 */
	SpectrumData readSpectrumSliceDataAt(int idx);
	
	/**
	 * 
	 * @param idx
	 *            index of specified spectrum
	 * @return SpectrumData of the specified spectrum
	 */
	SpectrumData readFilteredSpectrumSliceDataAt(int idx, double minMz, double maxMz);



}
