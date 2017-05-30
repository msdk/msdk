/* 
 * (C) Copyright 2015-2016 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */

package io.github.msdk.io.mzml2;

import java.util.HashMap;

public class MzMLSpectrum {
	private HashMap<String, String> spectrumData;

	public MzMLSpectrum() {
		spectrumData = new HashMap<>();
	}

	public void add(String accession, String value) {
		spectrumData.put(accession, value);
	}

	public HashMap<String, String> getSpectrumData() {
		return spectrumData;
	}

	public int getSpectrumDataSize() {
		return spectrumData.size();
	}
}
