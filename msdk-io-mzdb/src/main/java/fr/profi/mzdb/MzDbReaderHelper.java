package fr.profi.mzdb;

import java.io.File;
import java.util.concurrent.Callable;

import fr.profi.mzdb.io.reader.cache.MzDbEntityCache;
import fr.profi.mzdb.model.SpectrumSlice;
import fr.profi.mzdb.util.concurrent.Callback;

/**
 * @author JeT
 *
 */
public class MzDbReaderHelper {

	private MzDbReaderHelper() {
		// helper class
	}

	/**
	 * get an async
	 *
	 * @param minMz
	 * @param maxMz
	 * @param minRt
	 * @param maxRt
	 * @param file
	 * @param cache
	 * @param callback
	 * @return
	 */
	public static Callable<SpectrumSlice[]> getSpectrumSlicesInRanges(
		final double minMz,
		final double maxMz,
		final float minRt,
		final float maxRt,
		final File file,
		final MzDbEntityCache cache,
		final Callback<SpectrumSlice[]> callback) {
		
		return new Callable<SpectrumSlice[]>() {

			@Override
			public SpectrumSlice[] call() throws Exception {
				MzDbReader reader = new MzDbReader(file, cache, false);
				SpectrumSlice[] msSpectrumSlices = reader.getMsSpectrumSlices(minMz, maxMz, minRt, maxRt);
				if (callback != null) {
					callback.onCompletion(msSpectrumSlices);
				}
				return msSpectrumSlices;
			}
		};

	}
}
