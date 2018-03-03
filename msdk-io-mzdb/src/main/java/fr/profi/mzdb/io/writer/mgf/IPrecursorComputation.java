package fr.profi.mzdb.io.writer.mgf;

import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.model.SpectrumHeader;

public interface IPrecursorComputation {

	/**
	 * Returns the precursor m/z value of the specified SpectrumHeader.
	 * 
	 * @param header : the MS2 SpectrumHeader 
	 * @param reader : the mzdbReader considered
	 * @return the precursor m/z value of the specified SpectrumHeader
	 */
	
	public double getPrecursorMz(MzDbReader mzDbReader, SpectrumHeader spectrumHeader) throws SQLiteException;
	
	/**
	 * Returns the precursor m/z value of the specified SpectrumHeader.
	 * 
	 * @param header : the MS2 SpectrumHeader 
	 * @param reader : the mzdbReader considered
	 * @return the precursor m/z value of the specified SpectrumHeader
	 */
	
	public int getPrecursorCharge(MzDbReader mzDbReader, SpectrumHeader spectrumHeader) throws SQLiteException;
	
	public String getParamName();
	
}
