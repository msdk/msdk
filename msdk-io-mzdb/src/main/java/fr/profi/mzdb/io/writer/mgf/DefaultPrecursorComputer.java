package fr.profi.mzdb.io.writer.mgf;

import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.db.model.params.IsolationWindowParamTree;
import fr.profi.mzdb.db.model.params.Precursor;
import fr.profi.mzdb.db.model.params.param.CVEntry;
import fr.profi.mzdb.db.model.params.param.CVParam;
import fr.profi.mzdb.db.model.params.param.UserParam;
import fr.profi.mzdb.model.Peak;
import fr.profi.mzdb.model.SpectrumHeader;
import fr.profi.mzdb.model.SpectrumSlice;
import fr.profi.mzdb.util.ms.MsUtils;

public class DefaultPrecursorComputer implements IPrecursorComputation {

	final Logger logger = LoggerFactory.getLogger(DefaultPrecursorComputer.class);

	private PrecursorMzComputationEnum precComp;
	private float mzTolPPM;
	
	protected DefaultPrecursorComputer(float mzTolPPM) {
		this.mzTolPPM = mzTolPPM;
	}
	
	public DefaultPrecursorComputer(PrecursorMzComputationEnum precComp, float mzTolPPM) {
		this(mzTolPPM);
		this.precComp = precComp;
	}

	@Override
	public String getParamName() {
		return precComp.getUserParamName();
	}

	@Override
	public int getPrecursorCharge(MzDbReader mzDbReader, SpectrumHeader spectrumHeader) throws SQLiteException {
		return spectrumHeader.getPrecursorCharge();
	}
	
	@Override
	public double getPrecursorMz(MzDbReader mzDbReader, SpectrumHeader spectrumHeader) throws SQLiteException {

		final float time = spectrumHeader.getElutionTime();
		double precMz = spectrumHeader.getPrecursorMz();

		if (precComp == PrecursorMzComputationEnum.SELECTED_ION_MZ) {
			try {
				Precursor precursor = spectrumHeader.getPrecursor();
				precMz = precursor.parseFirstSelectedIonMz();
			} catch (Exception e) {
				this.logger.error("Selected ion m/z value not found: fall back to default", e);
			}
		} else if (precComp == PrecursorMzComputationEnum.REFINED) {

			try {
				Precursor precursor = spectrumHeader.getPrecursor();
				precMz = precursor.parseFirstSelectedIonMz();
				precMz = this.refinePrecMz(mzDbReader, precursor, precMz, mzTolPPM, time, 5);
			} catch (Exception e) {
				this.logger.error("Refined precursor m/z computation failed: fall back to default", e);
			}

			/*if (Math.abs(refinedPrecMz - precMz) > 0.5) {
				System.out.println("" + precMz + ", " + refinedPrecMz + ", " + thermoTrailer);
			}
			
			if (Math.abs(refinedPrecMz - thermoTrailer) > 0.5) {
				System.out.println("" + precMz + ", " + refinedPrecMz + ", " + thermoTrailer);
			}*/

		} else if (precComp == PrecursorMzComputationEnum.REFINED_THERMO) {
			try {
				if( spectrumHeader.getScanList() == null ) {
					spectrumHeader.loadScanList(mzDbReader.getConnection());
				}
				
				UserParam precMzParam = spectrumHeader.getScanList().getScans().get(0).getUserParam("[Thermo Trailer Extra]Monoisotopic M/Z:");

				precMz = Double.parseDouble(precMzParam.getValue());
			} catch (NullPointerException e) {
				this.logger.error("Refined thermo value not found: fall back to default");
			}
		} 
		
		return precMz;

	}

	/**
	 * Detects isotopic pattern in the survey and return the most probable mono-isotopic m/z value
	 * 
	 * @param centerMz
	 *            the m/z value at the center of the isolation window
	 * @return
	 * @throws SQLiteException
	 * @throws StreamCorruptedException
	 */
	// TODO: it should be nice to perform this operation in mzdb-processing
	// This requires that the MgfWriter is be moved to this package
	protected double extractPrecMz(MzDbReader mzDbReader, Precursor precursor, double precMz, SpectrumHeader spectrumHeader, float timeTol)
			throws StreamCorruptedException, SQLiteException {

		long sid = spectrumHeader.getId();
		float time = spectrumHeader.getTime();

		// Do a XIC in the isolation window and around the provided time
		// FIXME: isolation window is not available for AbSciex files yet
		// final SpectrumSlice[] spectrumSlices = this._getSpectrumSlicesInIsolationWindow(precursor, time, timeTol);
		final SpectrumSlice[] spectrumSlices = mzDbReader.getMsSpectrumSlices(precMz - 1, precMz + 1, time - timeTol, time + timeTol);

		// TODO: perform the operation on all loaded spectrum slices ???
		SpectrumSlice nearestSpectrumSlice = null;
		for (SpectrumSlice sl : spectrumSlices) {
			if (nearestSpectrumSlice == null)
				nearestSpectrumSlice = sl;
			else if (Math.abs(sl.getHeader().getElutionTime() - time) < Math.abs(nearestSpectrumSlice.getHeader()
					.getElutionTime() - time))
				nearestSpectrumSlice = sl;
		}

		Peak curPeak = nearestSpectrumSlice.getNearestPeak(precMz, mzTolPPM);
		if (curPeak == null)
			return precMz;

		final ArrayList<Peak> previousPeaks = new ArrayList<Peak>();

		for (int putativeZ = 2; putativeZ <= 4; putativeZ++) {

			// avgIsoMassDiff = 1.0027
			double prevPeakMz = precMz + (1.0027 * -1 / putativeZ);
			Peak prevPeak = nearestSpectrumSlice.getNearestPeak(prevPeakMz, mzTolPPM);

			if (prevPeak != null) {
				prevPeak.setLcContext(nearestSpectrumSlice.getHeader());

				double prevPeakExpMz = prevPeak.getMz();
				double approxZ = 1 / Math.abs(precMz - prevPeakExpMz);
				double approxMass = precMz * approxZ - approxZ * MsUtils.protonMass;

				if (approxMass > 2000 && approxMass < 7000) {

					// TODO: find a solution for high mass values
					float minIntRatio = (float) (1400.0 / approxMass);// inferred from lookup table
					float maxIntRatio = Math.min((float) (2800.0 / approxMass), 1);// inferred from lookup table

					// Mass Min Max
					// 2000 0.7 1.4
					// 2500 0.56 1.12
					// 3000 0.47 0.93
					// 3500 0.4 0.8
					// 4000 0.35 0.7
					// 4500 0.31 0.62
					// 5000 0.28 0.56
					// 6000 0.23 0.47
					// 7000 0.2 0.4

					// Check if intensity ratio is valid (in the expected theoretical range)
					// TODO: analyze the full isotope pattern
					float intRatio = prevPeak.getIntensity() / curPeak.getIntensity();

					if (intRatio > minIntRatio && intRatio < maxIntRatio) {

						// Check if there is no next peak with a different charge state that could explain
						// this previous peak
						boolean foundInterferencePeak = false;
						double interferencePeakMz = 0.0;
						for (int interferenceZ = 1; interferenceZ <= 6; interferenceZ++) {
							if (interferenceZ != putativeZ) {
								interferencePeakMz = prevPeakExpMz + (1.0027 * +1 / interferenceZ);
								Peak interferencePeak = nearestSpectrumSlice.getNearestPeak(interferencePeakMz, mzTolPPM);

								// If there is no defined peak with higher intensity
								if (interferencePeak != null && interferencePeak.getIntensity() > prevPeak.getIntensity()) {
									foundInterferencePeak = true;
									break;
								}
							}
						}

						if (foundInterferencePeak == false) {
							logger.debug("Found better m/z value for precMz=" + precMz + " at spectrum id=" + sid
									+ " with int ratio=" + intRatio + " and z=" + putativeZ + " : " + prevPeakExpMz);
							previousPeaks.add(prevPeak);
						} else {
							logger.debug("Found interference m/z value for precMz=" + precMz + " at spectrum id=" + sid + " : " + interferencePeakMz);
						}
					}
				}
			}
		}

		int nbPrevPeaks = previousPeaks.size();
		if (nbPrevPeaks == 0)
			return precMz;

		Collections.sort(previousPeaks, Peak.getIntensityComp());
		Peak mostIntensePrevPeak = previousPeaks.get(previousPeaks.size() - 1);

		return mostIntensePrevPeak.getMz();
	}

	/**
	 * Refines the provided target m/z value by looking at the nearest value in the survey.
	 * 
	 * @param precMz
	 *            the precursor m/z value to refine
	 * @return the refined precursor m/z value
	 * @throws SQLiteException
	 * @throws StreamCorruptedException
	 */
	protected Double refinePrecMz(MzDbReader mzDbReader, Precursor precursor, double precMz, double mzTolPPM, float time, float timeTol)
			throws StreamCorruptedException, SQLiteException {

		// Do a XIC in the isolation window and around the provided time
		final SpectrumSlice[] spectrumSlices = this._getSpectrumSlicesInIsolationWindow(mzDbReader, precursor, time, timeTol);
		if (spectrumSlices == null) {
			return null;
		}

		final ArrayList<Peak> peaks = new ArrayList<Peak>();
		for (SpectrumSlice sl : spectrumSlices) {
			Peak p = sl.getNearestPeak(precMz, mzTolPPM);
			if (p != null) {
				p.setLcContext(sl.getHeader());
				peaks.add(p);
			}
		}

		// Take the median value of mz
		if (peaks.isEmpty()) {
			return null;
		}

		if (peaks.size() == 1)
			return peaks.get(0).getMz();

		Collections.sort(peaks);// will use compareTo
		double medMz = 0.0;
		final int l = peaks.size();
		if (l % 2 != 0) {
			medMz = peaks.get(l / 2).getMz();
		} else {
			medMz = (peaks.get(l / 2 - 1).getMz() + peaks.get(l / 2).getMz()) / 2.0;
		}

		return medMz;
	}

	private SpectrumSlice[] _getSpectrumSlicesInIsolationWindow(MzDbReader mzDbReader, Precursor precursor, float time, float timeTol)
			throws StreamCorruptedException, SQLiteException {

		// do a XIC over isolation window
		final IsolationWindowParamTree iw = precursor.getIsolationWindow();
		if (iw == null) {
			return null;
		}

		CVEntry[] cvEntries = new CVEntry[] {
				CVEntry.ISOLATION_WINDOW_LOWER_OFFSET,
				CVEntry.ISOLATION_WINDOW_TARGET_MZ,
				CVEntry.ISOLATION_WINDOW_UPPER_OFFSET
		};
		final CVParam[] cvParams = iw.getCVParams(cvEntries);

		final float lowerMzOffset = Float.parseFloat(cvParams[0].getValue());
		final float targetMz = Float.parseFloat(cvParams[1].getValue());
		final float upperMzOffset = Float.parseFloat(cvParams[2].getValue());
		final double minmz = targetMz - lowerMzOffset;
		final double maxmz = targetMz + upperMzOffset;
		final float minrt = time - timeTol;
		final float maxrt = time + timeTol;

		return mzDbReader.getMsSpectrumSlices(minmz, maxmz, minrt, maxrt);
	}


}
