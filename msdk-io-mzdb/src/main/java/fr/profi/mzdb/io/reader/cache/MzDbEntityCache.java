package fr.profi.mzdb.io.reader.cache;

import java.util.ArrayList;
import java.util.Map;

import fr.profi.mzdb.model.DataEncoding;
import fr.profi.mzdb.model.RunSliceHeader;
import fr.profi.mzdb.model.SpectrumHeader;

/**
 * @author David Bouyssie
 * 
 */
public class MzDbEntityCache {

	protected SpectrumHeader[] ms1SpectrumHeaders = null;

	protected Map<Long, SpectrumHeader> ms1SpectrumHeaderById = null;
	
	protected SpectrumHeader[] ms2SpectrumHeaders = null;

	protected Map<Long, SpectrumHeader> ms2SpectrumHeaderById = null;

	protected SpectrumHeader[] ms3SpectrumHeaders = null;

	protected Map<Long, SpectrumHeader> ms3SpectrumHeaderById = null;

	protected SpectrumHeader[] spectrumHeaders = null;

	protected Map<Long, SpectrumHeader> spectrumHeaderById = null;

	protected Map<Long, Float> spectrumTimeById = null;
	
	protected Map<Integer, ArrayList<Long>> spectrumIdsByTimeIndex = null;

	protected Map<Integer, DataEncoding> dataEncodingById = null;

	protected Map<Long, DataEncoding> dataEncodingBySpectrumId = null;

	protected RunSliceHeader[] runSliceHeaders = null;

	protected Map<Integer, RunSliceHeader> runSliceHeaderById = null;

	public SpectrumHeader[] getMs1SpectrumHeaders() {
		return ms1SpectrumHeaders;
	}

	public Map<Long, SpectrumHeader> getMs1SpectrumHeaderById() {
		return ms1SpectrumHeaderById;
	}
	
	public SpectrumHeader[] getMs2SpectrumHeaders() {
		return ms2SpectrumHeaders;
	}

	public Map<Long, SpectrumHeader> getMs2SpectrumHeaderById() {
		return ms2SpectrumHeaderById;
	}

	public Map<Long, Float> getSpectrumTimeById() {
		return spectrumTimeById;
	}

	public Map<Long, DataEncoding> getDataEncodingBySpectrumId() {
		return dataEncodingBySpectrumId;
	}

	public RunSliceHeader[] getRunSliceHeaders() {
		return runSliceHeaders;
	}

	public Map<Integer, RunSliceHeader> getRunSliceHeaderById() {
		return runSliceHeaderById;
	}

}
